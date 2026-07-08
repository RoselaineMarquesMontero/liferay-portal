/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.internal;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryTypeConstants;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryServiceUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Roselaine Marques
 */
public class DLAssetDisplayPageUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_dlFileEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
			DLFileEntryLocalServiceUtil.class);
		_layoutPageTemplateEntryServiceUtilMockedStatic = Mockito.mockStatic(
			LayoutPageTemplateEntryServiceUtil.class);

		_portalUtilMockedStatic = Mockito.mockStatic(PortalUtil.class);

		_portalUtilMockedStatic.when(
			() -> PortalUtil.getClassNameId(FileEntry.class)
		).thenReturn(
			_CLASS_NAME_ID
		);

		_layoutPageTemplateEntryServiceUtilMockedStatic.when(
			() ->
				LayoutPageTemplateEntryServiceUtil.
					fetchDefaultLayoutPageTemplateEntry(
						ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(),
						ArgumentMatchers.anyLong())
		).thenReturn(
			Mockito.mock(LayoutPageTemplateEntry.class)
		);
	}

	@After
	public void tearDown() {
		_dlFileEntryLocalServiceUtilMockedStatic.close();
		_layoutPageTemplateEntryServiceUtilMockedStatic.close();
		_portalUtilMockedStatic.close();
	}

	@Test
	public void testHasAssetDisplayPageResolvesPersistedFileEntryTypeWhenRequestFileEntryTypeIdIsAbsent() {
		long fileEntryId = RandomTestUtil.randomLong();
		long fileEntryTypeId = RandomTestUtil.randomLong();

		_whenFetchDLFileEntryReturn(fileEntryId, fileEntryTypeId);

		ServiceContext serviceContext = new ServiceContext();

		DLAssetDisplayPageUtil.hasAssetDisplayPage(serviceContext, fileEntryId);

		_layoutPageTemplateEntryServiceUtilMockedStatic.verify(
			() ->
				LayoutPageTemplateEntryServiceUtil.
					fetchDefaultLayoutPageTemplateEntry(
						serviceContext.getScopeGroupId(), _CLASS_NAME_ID,
						fileEntryTypeId));
	}

	@Test
	public void testHasAssetDisplayPageUsesBasicDocumentWhenFileEntryDoesNotExist() {
		long fileEntryId = RandomTestUtil.randomLong();

		_dlFileEntryLocalServiceUtilMockedStatic.when(
			() -> DLFileEntryLocalServiceUtil.fetchDLFileEntry(fileEntryId)
		).thenReturn(
			null
		);

		ServiceContext serviceContext = new ServiceContext();

		DLAssetDisplayPageUtil.hasAssetDisplayPage(serviceContext, fileEntryId);

		_layoutPageTemplateEntryServiceUtilMockedStatic.verify(
			() ->
				LayoutPageTemplateEntryServiceUtil.
					fetchDefaultLayoutPageTemplateEntry(
						serviceContext.getScopeGroupId(), _CLASS_NAME_ID,
						DLFileEntryTypeConstants.
							FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT));
	}

	@Test
	public void testHasAssetDisplayPageUsesBasicDocumentWhenFileEntryIdIsZero() {
		ServiceContext serviceContext = new ServiceContext();

		DLAssetDisplayPageUtil.hasAssetDisplayPage(serviceContext);

		_layoutPageTemplateEntryServiceUtilMockedStatic.verify(
			() ->
				LayoutPageTemplateEntryServiceUtil.
					fetchDefaultLayoutPageTemplateEntry(
						serviceContext.getScopeGroupId(), _CLASS_NAME_ID,
						DLFileEntryTypeConstants.
							FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT));

		_dlFileEntryLocalServiceUtilMockedStatic.verifyNoInteractions();
	}

	@Test
	public void testHasAssetDisplayPageUsesRequestFileEntryTypeIdWhenPresent() {
		long fileEntryId = RandomTestUtil.randomLong();
		long persistedFileEntryTypeId = RandomTestUtil.randomLong();
		long requestFileEntryTypeId = RandomTestUtil.randomLong();

		_whenFetchDLFileEntryReturn(fileEntryId, persistedFileEntryTypeId);

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAttribute("fileEntryTypeId", requestFileEntryTypeId);

		DLAssetDisplayPageUtil.hasAssetDisplayPage(serviceContext, fileEntryId);

		_layoutPageTemplateEntryServiceUtilMockedStatic.verify(
			() ->
				LayoutPageTemplateEntryServiceUtil.
					fetchDefaultLayoutPageTemplateEntry(
						serviceContext.getScopeGroupId(), _CLASS_NAME_ID,
						requestFileEntryTypeId));

		_dlFileEntryLocalServiceUtilMockedStatic.verifyNoInteractions();
	}

	private void _whenFetchDLFileEntryReturn(
		long fileEntryId, long fileEntryTypeId) {

		DLFileEntry dlFileEntry = Mockito.mock(DLFileEntry.class);

		Mockito.when(
			dlFileEntry.getFileEntryTypeId()
		).thenReturn(
			fileEntryTypeId
		);

		_dlFileEntryLocalServiceUtilMockedStatic.when(
			() -> DLFileEntryLocalServiceUtil.fetchDLFileEntry(fileEntryId)
		).thenReturn(
			dlFileEntry
		);
	}

	private static final long _CLASS_NAME_ID = RandomTestUtil.randomLong();

	private MockedStatic<DLFileEntryLocalServiceUtil>
		_dlFileEntryLocalServiceUtilMockedStatic;
	private MockedStatic<LayoutPageTemplateEntryServiceUtil>
		_layoutPageTemplateEntryServiceUtilMockedStatic;
	private MockedStatic<PortalUtil> _portalUtilMockedStatic;

}