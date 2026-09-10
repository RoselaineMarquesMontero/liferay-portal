/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.page.template.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureService;
import com.liferay.layout.page.template.service.persistence.impl.constants.LayoutPersistenceConstants;
import com.liferay.layout.page.template.test.util.LayoutPageTemplateTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Liferay
 */
@RunWith(Arquillian.class)
public class LayoutPageTemplateStructureServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				LayoutPersistenceConstants.BUNDLE_SYMBOLIC_NAME));

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_layoutPageTemplateCollection =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateCollection(
				_group.getGroupId());
	}

	@Test(expected = PrincipalException.class)
	public void testUpdateLayoutPageTemplateStructureDataWhenLocked()
		throws Exception {

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			LayoutPageTemplateTestUtil.addLayoutPageTemplateEntry(
				_layoutPageTemplateCollection.
					getLayoutPageTemplateCollectionId());

		_layoutPageTemplateEntryLocalService.updateLock(
			layoutPageTemplateEntry.getLayoutPageTemplateEntryId(), true);

		_layoutPageTemplateStructureService.
			updateLayoutPageTemplateStructureData(
				_group.getGroupId(), layoutPageTemplateEntry.getPlid(), 0,
				StringPool.BLANK);
	}

	@DeleteAfterTestRun
	private Group _group;

	private LayoutPageTemplateCollection _layoutPageTemplateCollection;

	@Inject
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Inject
	private LayoutPageTemplateStructureService
		_layoutPageTemplateStructureService;

}