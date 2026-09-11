/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import switchSidebarPanel from '../../../../src/main/resources/META-INF/resources/page_editor/app/thunks/switchSidebarPanel';

describe('switchSidebarPanel', () => {
	it('dispatches the action when the page template is not locked', () => {
		const dispatch = jest.fn();

		switchSidebarPanel({hidden: false})(dispatch, () => ({
			permissions: {LOCKED_PAGE_TEMPLATE: false},
		}));

		expect(dispatch).toBeCalledWith(
			expect.objectContaining({hidden: false})
		);
	});

	it('does not dispatch the action when the page template is locked and the sidebar would be shown', () => {
		const dispatch = jest.fn();

		switchSidebarPanel({hidden: false})(dispatch, () => ({
			permissions: {LOCKED_PAGE_TEMPLATE: true},
		}));

		expect(dispatch).not.toBeCalled();
	});

	it('still dispatches the action when the page template is locked but the sidebar is being hidden', () => {
		const dispatch = jest.fn();

		switchSidebarPanel({hidden: true})(dispatch, () => ({
			permissions: {LOCKED_PAGE_TEMPLATE: true},
		}));

		expect(dispatch).toBeCalledWith(
			expect.objectContaining({hidden: true})
		);
	});

	it('still dispatches the action when the page template is locked and hidden is not part of the action', () => {
		const dispatch = jest.fn();

		switchSidebarPanel({itemConfigurationOpen: true})(dispatch, () => ({
			permissions: {LOCKED_PAGE_TEMPLATE: true},
		}));

		expect(dispatch).toBeCalledWith(
			expect.objectContaining({itemConfigurationOpen: true})
		);
	});
});
