package com.vaadin.hilla.route.records;


/**
 * Implementation of TypeScript's Hilla ConfigView.Menu.
 * Represents a view's menu configuration
 * from Hilla file-system-routing.
 * Used for configuring MainLayout navigation items
 *
 */
public record ClientViewMenuConfig(String title, Long order, String icon, Boolean exclude) {
}
