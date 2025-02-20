package com.vaadin.hilla.gradle.test

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@Theme(value = "hilla-gradle-test")
class HillaGradleKotlinApplication : AppShellConfigurator

fun main(args: Array<String>) {
	runApplication<HillaGradleKotlinApplication>(*args)
}
