/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.upgrade.v4_3_0.test;

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
public class OAuth2ApplicationExternalReferenceCodeUpgradeProcessTest {

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
			"alter_column_type OAuth2Application externalReferenceCode " +
				"VARCHAR(1000) null",
			true);

		long oAuth2ApplicationId1 = RandomTestUtil.nextLong();

		_db.runSQL(
			StringBundler.concat(
				"insert into OAuth2Application (oAuth2ApplicationId, ",
				"externalReferenceCode) values (", oAuth2ApplicationId1,
				", '", _OVERSIZED_EXTERNAL_REFERENCE_CODE, "')"));

		long oAuth2ApplicationId2 = RandomTestUtil.nextLong();

		_db.runSQL(
			StringBundler.concat(
				"insert into OAuth2Application (oAuth2ApplicationId, ",
				"externalReferenceCode) values (", oAuth2ApplicationId2,
				", '", _MAX_LENGTH_EXTERNAL_REFERENCE_CODE, "')"));

		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		try {
			upgradeProcess.upgrade();

			try (Connection connection = DataAccess.getConnection()) {
				DBInspector dbInspector = new DBInspector(connection);

				Assert.assertTrue(
					dbInspector.hasColumnType(
						"OAuth2Application", "externalReferenceCode",
						"VARCHAR(500) null"));
				Assert.assertTrue(
					dbInspector.hasIndex("OAuth2Application", "IX_67BC29B0"));
			}

			try (Connection connection = DataAccess.getConnection();
				 PreparedStatement preparedStatement =
					 connection.prepareStatement(
						 "select externalReferenceCode from OAuth2Application " +
							 "where oAuth2ApplicationId = ?")) {

				preparedStatement.setLong(1, oAuth2ApplicationId1);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					Assert.assertTrue(resultSet.next());
					Assert.assertEquals(
						_MAX_LENGTH_EXTERNAL_REFERENCE_CODE,
						resultSet.getString("externalReferenceCode"));
				}

				preparedStatement.setLong(1, oAuth2ApplicationId2);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					Assert.assertTrue(resultSet.next());
					Assert.assertEquals(
						_MAX_LENGTH_EXTERNAL_REFERENCE_CODE,
						resultSet.getString("externalReferenceCode"));
				}
			}
		}
		finally {
			_db.runSQL(
				StringBundler.concat(
					"delete from OAuth2Application where oAuth2ApplicationId ",
					"in (", oAuth2ApplicationId1, ", ",
					oAuth2ApplicationId2, ")"));
		}
	}

	private static final String _CLASS_NAME =
		"com.liferay.oauth2.provider.internal.upgrade.v4_3_0." +
			"OAuth2ApplicationExternalReferenceCodeUpgradeProcess";

	private static final String _MAX_LENGTH_EXTERNAL_REFERENCE_CODE =
		"a".repeat(500);

	private static final String _OVERSIZED_EXTERNAL_REFERENCE_CODE =
		"a".repeat(501);

	private static DB _db;

	@Inject(
		filter = "component.name=com.liferay.oauth2.provider.internal.upgrade.registry.OAuth2ServiceUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

}
