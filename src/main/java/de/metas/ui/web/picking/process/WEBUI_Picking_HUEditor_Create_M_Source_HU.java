package de.metas.ui.web.picking.process;

import org.springframework.beans.factory.annotation.Autowired;

import de.metas.handlingunits.picking.SourceHUsRepository;
import de.metas.process.IProcessPrecondition;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 * This process is available from the HU editor window opened by {@link WEBUI_Picking_HUEditor_Open}.<br>
 * Its job is to flag the currently selected HUs so they are available as source-HUs for either {@link WEBUI_Picking_PickQtyToNewHU} or {@link WEBUI_Picking_PickQtyToExistingHU}.
 * 
 * @task https://github.com/metasfresh/metasfresh/issues/2298
 * 
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public class WEBUI_Picking_HUEditor_Create_M_Source_HU
		extends WEBUI_Picking_Select_M_HU_Base
		implements IProcessPrecondition
{

	@Autowired
	private SourceHUsRepository sourceHUsRepository;

	@Override
	protected String doIt() throws Exception
	{
		retrieveEligibleHUEditorRows().forEach(
				huEditorRow -> {

					sourceHUsRepository.addSourceHu(huEditorRow.getM_HU_ID());
				});

		invalidateViewsAndPrepareReturn();
		return MSG_OK;
	}
}