package com.vaadin.hilla

import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
class HillaGradleKotlinApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<HillaGradleKotlinApplication>(*args)
}
