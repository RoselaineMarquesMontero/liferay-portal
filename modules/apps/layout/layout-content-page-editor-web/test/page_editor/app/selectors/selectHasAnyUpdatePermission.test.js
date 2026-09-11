/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import selectHasAnyUpdatePermission from '../../../../src/main/resources/META-INF/resources/page_editor/app/selectors/selectHasAnyUpdatePermission';

describe('selectHasAnyUpdatePermission', () => {
	it('returns true when the user has update permission', () => {
		const hasAnyUpdatePermission = selectHasAnyUpdatePermission({
			permissions: {UPDATE: true},
		});

		expect(hasAnyUpdatePermission).toBe(true);
	});

	it('returns true when the user has one of the layout update permissions', () => {
		const hasAnyUpdatePermission = selectHasAnyUpdatePermission({
			permissions: {UPDATE_LAYOUT_LIMITED: true},
		});

		expect(hasAnyUpdatePermission).toBe(true);
	});

	it('returns false when the page template is locked, even with update permission', () => {
		const hasAnyUpdatePermission = selectHasAnyUpdatePermission({
			permissions: {LOCKED_PAGE_TEMPLATE: true, UPDATE: true},
		});

		expect(hasAnyUpdatePermission).toBe(false);
	});

	it('returns false when the user has no update permission', () => {
		const hasAnyUpdatePermission = selectHasAnyUpdatePermission({
			permissions: {},
		});

		expect(hasAnyUpdatePermission).toBeFalsy();
	});
});
