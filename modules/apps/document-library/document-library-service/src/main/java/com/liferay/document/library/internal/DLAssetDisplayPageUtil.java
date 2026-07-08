/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.internal;

import com.liferay.asset.display.page.constants.AssetDisplayPageConstants;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryTypeConstants;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryServiceUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;

/**
 * @author Roberto Díaz
 */
public class DLAssetDisplayPageUtil {

	public static boolean hasAssetDisplayPage(ServiceContext serviceContext) {
		return hasAssetDisplayPage(serviceContext, 0);
	}

	public static boolean hasAssetDisplayPage(
		ServiceContext serviceContext, long fileEntryId) {

		int displayPageType = ParamUtil.getInteger(
			serviceContext, "displayPageType",
			AssetDisplayPageConstants.TYPE_DEFAULT);

		if (displayPageType == AssetDisplayPageConstants.TYPE_DEFAULT) {
			long fileEntryTypeId = ParamUtil.getLong(
				serviceContext, "fileEntryTypeId",
				_getFileEntryTypeId(fileEntryId));

			LayoutPageTemplateEntry layoutPageTemplateEntry =
				LayoutPageTemplateEntryServiceUtil.
					fetchDefaultLayoutPageTemplateEntry(
						serviceContext.getScopeGroupId(),
						PortalUtil.getClassNameId(FileEntry.class),
						fileEntryTypeId);

			if (layoutPageTemplateEntry == null) {
				return false;
			}
		}
		else if (displayPageType == AssetDisplayPageConstants.TYPE_NONE) {
			return false;
		}
		else if ((displayPageType == AssetDisplayPageConstants.TYPE_SPECIFIC) &&
				 (ParamUtil.getLong(serviceContext, "assetDisplayPageId") ==
					 0)) {

			return false;
		}

		return true;
	}

	private static long _getFileEntryTypeId(long fileEntryId) {
		if (fileEntryId > 0) {
			DLFileEntry dlFileEntry =
				DLFileEntryLocalServiceUtil.fetchDLFileEntry(fileEntryId);

			if (dlFileEntry != null) {
				return dlFileEntry.getFileEntryTypeId();
			}
		}

		return DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT;
	}

}