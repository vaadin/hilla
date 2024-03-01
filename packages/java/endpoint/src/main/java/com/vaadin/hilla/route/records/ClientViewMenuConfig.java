package com.vaadin.hilla.route.records;

/**
 * Implementation of TypeScript's Hilla ViewConfig.Menu. Represents a view's
 * menu configuration from Hilla file-system-routing. Used for configuring
 * navigation items
 *
 * @see <a href=
 *      "https://github.com/vaadin/hilla/blob/main/packages/ts/hilla-file-router/src/types.d.ts#L43">ViewConfig
 *      menu</a>
 */
public record ClientViewMenuConfig(String title, Long order, String icon,
        Boolean exclude) {
}
