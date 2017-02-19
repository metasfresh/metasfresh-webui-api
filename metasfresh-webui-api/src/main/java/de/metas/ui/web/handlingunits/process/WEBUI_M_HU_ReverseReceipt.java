package de.metas.ui.web.handlingunits.process;

import java.util.List;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.util.Services;
import org.adempiere.util.lang.MutableInt;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.Adempiere;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import de.metas.handlingunits.inout.ReceiptCorrectHUsProcessor;
import de.metas.handlingunits.model.I_M_HU;
import de.metas.handlingunits.model.I_M_ReceiptSchedule;
import de.metas.process.IProcessPrecondition;
import de.metas.process.IProcessPreconditionsContext;
import de.metas.process.JavaProcess;
import de.metas.process.Param;
import de.metas.process.ProcessPreconditionsResolution;
import de.metas.process.RunOutOfTrx;
import de.metas.ui.web.WebRestApiApplication;
import de.metas.ui.web.handlingunits.HUDocumentView;
import de.metas.ui.web.handlingunits.HUDocumentViewSelection;
import de.metas.ui.web.process.DocumentViewAsPreconditionsContext;
import de.metas.ui.web.process.ProcessInstance;
import de.metas.ui.web.view.IDocumentViewsRepository;
import de.metas.ui.web.window.datatypes.DocumentPath;
import de.metas.ui.web.window.model.DocumentCollection;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 * Reverse the receipts which contain the selected HUs.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
@Profile(value = WebRestApiApplication.PROFILE_Webui)
public class WEBUI_M_HU_ReverseReceipt extends JavaProcess implements IProcessPrecondition
{
	@Override
	public ProcessPreconditionsResolution checkPreconditionsApplicable(final IProcessPreconditionsContext context)
	{
		final DocumentViewAsPreconditionsContext viewContext = DocumentViewAsPreconditionsContext.castOrNull(context);
		if (viewContext == null)
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("webui view not available");
		}
		
		if(viewContext.isNoSelection())
		{
			return ProcessPreconditionsResolution.rejectBecauseNoSelection();
		}
		
		final MutableInt checkedDocumentsCount = new MutableInt(0);
		final ProcessPreconditionsResolution firstRejection = viewContext.getView(HUDocumentViewSelection.class)
				.streamByIds(viewContext.getSelectedDocumentIds())
				.filter(document -> document.isPureHU())
				//
				.peek(document -> checkedDocumentsCount.incrementAndGet()) // count checked documents
				.map(document -> rejectResolutionOrNull(document)) // create reject resolution if any
				.filter(resolution -> resolution != null) // filter out those which are not errors
				.findFirst()
				.orElse(null);
		if (firstRejection != null)
		{
			// found a record which is not eligible => don't run the process
			return firstRejection;
		}
		if (checkedDocumentsCount.getValue() <= 0)
		{
			return ProcessPreconditionsResolution.rejectWithInternalReason("no eligible rows");
		}

		return ProcessPreconditionsResolution.accept();
	}
	
	private static final ProcessPreconditionsResolution rejectResolutionOrNull(final HUDocumentView document)
	{
		if (!document.isHUStatusActive())
		{
			return ProcessPreconditionsResolution.reject("Only active HUs can be reversed"); // TODO: trl
		}

		return null;
	}


	@Autowired
	private IDocumentViewsRepository documentViewsRepo;
	@Autowired
	private DocumentCollection documentsCollection;

	@Param(parameterName = ProcessInstance.PARAM_ViewId, mandatory = true)
	private String p_WebuiViewId;

	public WEBUI_M_HU_ReverseReceipt()
	{
		super();
		Adempiere.autowire(this);
	}

	@Override
	@RunOutOfTrx
	protected String doIt() throws Exception
	{
		final I_M_ReceiptSchedule receiptSchedule = getM_ReceiptSchedule();
		
		ReceiptCorrectHUsProcessor.builder()
				.setM_ReceiptSchedule(receiptSchedule)
				.build()
				.reverseReceiptsForHUs(retrieveHUsToReverse());

		//
		// Reset the view's affected HUs
		getView().invalidateAll();

		documentViewsRepo.notifyRecordChanged(TableRecordReference.of(receiptSchedule));

		return MSG_OK;
	}

	private HUDocumentViewSelection getView()
	{
		return documentViewsRepo.getView(p_WebuiViewId, HUDocumentViewSelection.class);
	}

	private I_M_ReceiptSchedule getM_ReceiptSchedule()
	{
		final HUDocumentViewSelection view = getView();
		final DocumentPath referencingDocumentPath = view.getReferencingDocumentPath();

		return documentsCollection.getTableRecordReference(referencingDocumentPath)
				.getModel(this, I_M_ReceiptSchedule.class);
	}

	private List<I_M_HU> retrieveHUsToReverse()
	{
		return Services.get(IQueryBL.class).createQueryBuilder(I_M_HU.class, this)
				.filter(getProcessInfo().getQueryFilter())
				.addOnlyActiveRecordsFilter()
				.create()
				.list(I_M_HU.class);
	}
}
