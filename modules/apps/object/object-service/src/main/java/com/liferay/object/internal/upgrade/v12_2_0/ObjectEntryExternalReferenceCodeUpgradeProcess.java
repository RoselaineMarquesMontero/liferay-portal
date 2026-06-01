/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.upgrade.v12_2_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.db.IndexMetadata;
import com.liferay.portal.kernel.upgrade.UpgradeException;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;

/**
 * @author Roselaine Marques
 */
public class ObjectEntryExternalReferenceCodeUpgradeProcess
	extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		DBType dbType = DBManagerUtil.getDB(
		).getDBType();

		String lengthFunction = "CHAR_LENGTH";
		String substringFunction = "SUBSTRING";

		if (dbType == DBType.ORACLE) {
			lengthFunction = "LENGTH";
			substringFunction = "SUBSTR";
		}
		else if (dbType == DBType.SQLSERVER) {
			lengthFunction = "LEN";
		}

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				StringBundler.concat(
					"select 1 from ObjectEntry where ", lengthFunction,
					"(externalReferenceCode) > 500 group by ",
					"objectDefinitionId, groupId, companyId, ",
					substringFunction,
					"(externalReferenceCode, 1, 500) having count(*) > 1"));

			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (resultSet.next()) {
				throw new UpgradeException(
					"Unable to truncate externalReferenceCode in " +
						"ObjectEntry: truncation would produce duplicate " +
							"unique index entries.");
			}
		}

		runSQL(
			StringBundler.concat(
				"update ObjectEntry set externalReferenceCode = ",
				substringFunction, "(externalReferenceCode, 1, 500) where ",
				lengthFunction, "(externalReferenceCode) > 500"));

		List<IndexMetadata> indexMetadatas = dropIndexes(
			"ObjectEntry", "externalReferenceCode");

		alterColumnType(
			"ObjectEntry", "externalReferenceCode", "VARCHAR(500) null");

		addIndexes(connection, indexMetadatas);
	}

}