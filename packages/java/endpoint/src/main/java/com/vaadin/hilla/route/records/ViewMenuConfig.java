package com.vaadin.hilla.route.records;


/**
 * Implementation of TypeScript's Hilla ConfigView.Menu.
 * Represents a view's menu configuration
 * from Hilla file-system-routing.
 * Used for configuring MainLayout navigation items
 * @see <a href="https://github.com/vaadin/hilla/tree/main/packages/ts/hilla-file-router/src/utils.ts#L38">ConfigView menu</a>
 *
 */
public record ViewMenuConfig(String title, Long priority, Boolean exclude) {
}
