/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.changeset.internal.upgrade.v2_2_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBInspector;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Roselaine Marques
 */
@RunWith(Arquillian.class)
public class ChangesetEntryClassExternalReferenceCodeUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() {
		_db = DBManagerUtil.getDB();
	}

	@Test
	public void testUpgrade() throws Exception {
		_db.runSQLTemplate(
			"alter_column_type ChangesetEntry classExternalReferenceCode " +
				"VARCHAR(1000) null",
			true);

		long changesetEntryId1 = RandomTestUtil.nextLong();

		_db.runSQL(
			StringBundler.concat(
				"insert into ChangesetEntry (changesetEntryId, ",
				"classExternalReferenceCode) values (", changesetEntryId1,
				", '", _OVERSIZED_EXTERNAL_REFERENCE_CODE, "')"));

		long changesetEntryId2 = RandomTestUtil.nextLong();

		_db.runSQL(
			StringBundler.concat(
				"insert into ChangesetEntry (changesetEntryId, ",
				"classExternalReferenceCode) values (", changesetEntryId2,
				", '", _MAX_LENGTH_EXTERNAL_REFERENCE_CODE, "')"));

		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		try {
			upgradeProcess.upgrade();

			try (Connection connection = DataAccess.getConnection()) {
				DBInspector dbInspector = new DBInspector(connection);

				Assert.assertTrue(
					dbInspector.hasColumnType(
						"ChangesetEntry", "classExternalReferenceCode",
						"VARCHAR(500) null"));
				Assert.assertTrue(
					dbInspector.hasIndex("ChangesetEntry", "IX_71B99FC2"));
			}

			try (Connection connection = DataAccess.getConnection();

				PreparedStatement preparedStatement =
					connection.prepareStatement(
						"select classExternalReferenceCode from " +
							"ChangesetEntry where changesetEntryId = ?")) {

				preparedStatement.setLong(1, changesetEntryId1);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					Assert.assertTrue(resultSet.next());
					Assert.assertEquals(
						_MAX_LENGTH_EXTERNAL_REFERENCE_CODE,
						resultSet.getString("classExternalReferenceCode"));
				}

				preparedStatement.setLong(1, changesetEntryId2);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					Assert.assertTrue(resultSet.next());
					Assert.assertEquals(
						_MAX_LENGTH_EXTERNAL_REFERENCE_CODE,
						resultSet.getString("classExternalReferenceCode"));
				}
			}
		}
		finally {
			_db.runSQL(
				StringBundler.concat(
					"delete from ChangesetEntry where changesetEntryId in (",
					changesetEntryId1, ", ", changesetEntryId2, ")"));
		}
	}

	private static final String _CLASS_NAME =
		"com.liferay.changeset.internal.upgrade.v2_2_0." +
			"ChangesetEntryClassExternalReferenceCodeUpgradeProcess";

	private static final String _MAX_LENGTH_EXTERNAL_REFERENCE_CODE =
		"a".repeat(500);

	private static final String _OVERSIZED_EXTERNAL_REFERENCE_CODE = "a".repeat(
		501);

	private static DB _db;

	@Inject(
		filter = "component.name=com.liferay.changeset.internal.upgrade.registry.ChangesetServiceUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}