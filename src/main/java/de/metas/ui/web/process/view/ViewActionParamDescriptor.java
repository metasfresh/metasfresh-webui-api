package de.metas.ui.web.process.view;

import java.util.Set;

import com.google.common.base.Preconditions;

import de.metas.ui.web.process.view.ViewActionDescriptor.ViewActionMethodArgumentExtractor;
import de.metas.ui.web.view.IDocumentViewSelection;
import de.metas.ui.web.window.datatypes.DocumentId;
import de.metas.ui.web.window.descriptor.DocumentFieldDescriptor;
import de.metas.ui.web.window.descriptor.DocumentFieldDescriptor.Characteristic;
import de.metas.ui.web.window.model.Document;
import lombok.Builder;
import lombok.NonNull;

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


@Builder
public final class ViewActionParamDescriptor
{
	private final @NonNull String parameterName;
	private final @NonNull Class<?> parameterValueClass;
	private final ViewActionParam parameterAnnotation;

	private final @NonNull ViewActionMethodArgumentExtractor methodArgumentExtractor;

	public boolean isUserParameter()
	{
		return parameterAnnotation != null;
	}

	public DocumentFieldDescriptor.Builder createParameterFieldDescriptor()
	{
		Preconditions.checkState(isUserParameter(), "parameter is internal");

		return DocumentFieldDescriptor.builder(parameterName)
				.setCaption(parameterAnnotation.caption())
				// .setDescription(attribute.getDescription())
				//
				.setValueClass(parameterValueClass)
				.setWidgetType(parameterAnnotation.widgetType())
				// .setLookupDescriptorProvider(lookupDescriptor)
				//
				// .setDefaultValueExpression(defaultValueExpr)
				.setReadonlyLogic(false)
				.setDisplayLogic(true)
				.setMandatoryLogic(parameterAnnotation.mandatory())
				//
				.addCharacteristic(Characteristic.PublicField)
		//
		// .setDataBinding(new ASIAttributeFieldBinding(attributeId, fieldName, attribute.isMandatory(), readMethod, writeMethod))
		;

	}

	public Object extractArgument(final IDocumentViewSelection view, final Document processParameters, final Set<DocumentId> selectedDocumentIds)
	{
		return methodArgumentExtractor.extractArgument(view, processParameters, selectedDocumentIds);
	}

}