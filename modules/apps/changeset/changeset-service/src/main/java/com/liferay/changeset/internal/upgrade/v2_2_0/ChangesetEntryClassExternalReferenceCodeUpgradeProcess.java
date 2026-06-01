/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.changeset.internal.upgrade.v2_2_0;

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
public class ChangesetEntryClassExternalReferenceCodeUpgradeProcess
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

		String collisionCheckSQL = StringBundler.concat(
			"select 1 from ChangesetEntry where ", lengthFunction,
			"(classExternalReferenceCode) > 500 group by ",
			"changesetCollectionId, classNameId, ", substringFunction,
			"(classExternalReferenceCode, 1, 500) having count(*) > 1");

		try (PreparedStatement preparedStatement = connection.prepareStatement(
				collisionCheckSQL);

			ResultSet resultSet = preparedStatement.executeQuery()) {

			if (resultSet.next()) {
				throw new UpgradeException(
					"Unable to truncate classExternalReferenceCode in " +
						"ChangesetEntry: truncation would produce duplicate " +
							"unique index entries.");
			}
		}

		runSQL(
			StringBundler.concat(
				"update ChangesetEntry set classExternalReferenceCode = ",
				substringFunction,
				"(classExternalReferenceCode, 1, 500) where ", lengthFunction,
				"(classExternalReferenceCode) > 500"));

		List<IndexMetadata> indexMetadatas = dropIndexes(
			"ChangesetEntry", "classExternalReferenceCode");

		alterColumnType(
			"ChangesetEntry", "classExternalReferenceCode",
			"VARCHAR(500) null");

		addIndexes(connection, indexMetadatas);
	}

}