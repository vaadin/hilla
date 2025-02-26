package com.vaadin.hilla.parser.plugin.nonnull.kotlin

import com.vaadin.hilla.parser.core.AbstractPlugin
import com.vaadin.hilla.parser.core.NodeDependencies
import com.vaadin.hilla.parser.core.NodePath
import com.vaadin.hilla.parser.core.Plugin
import com.vaadin.hilla.parser.core.PluginConfiguration
import com.vaadin.hilla.parser.models.ClassInfoModel
import com.vaadin.hilla.parser.models.ClassRefSignatureModel
import com.vaadin.hilla.parser.models.MethodInfoModel
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberFunctions

class KotlinNullabilityPlugin : AbstractPlugin<PluginConfiguration>() {

    override fun getRequiredPlugins(): Collection<Class<out Plugin?>> =
        listOf<Class<out Plugin?>>(BackbonePlugin::class.java)

    override fun enter(nodePath: NodePath<*>?) {}

    override fun exit(nodePath: NodePath<*>?) {
        val node = nodePath!!.node
        if (node.target is Schema<*>) {
            if (isKotlinClass(findClosestClass(nodePath)) && isMethodParameterNode(nodePath)) {
                val schema = node.target as Schema<*>
                schema.nullable = if (isMethodParameterNullable(nodePath)) true else null
            }
        }
    }

    override fun scan(nodeDependencies: NodeDependencies): NodeDependencies = nodeDependencies

    private fun isKotlinClass(clazz: Class<*>): Boolean {
        return clazz.declaredAnnotations.any {
            it.annotationClass.qualifiedName == "kotlin.Metadata"
        }
    }

    private fun findClosestClass(nodePath: NodePath<*>): Class<*> {
        return nodePath.stream().map{ it.node }
            .filter{ node-> node.getSource() is ClassInfoModel }
            .map{ node-> node.getSource() as ClassInfoModel }.findFirst().get().get() as Class<*>
    }

    private fun isMethodParameterNode(nodePath: NodePath<*>): Boolean {
        return nodePath.node.source is ClassRefSignatureModel && nodePath.parentPath.node is MethodParameterNode
    }

    private fun isMethodParameterNullable(nodePath: NodePath<*>): Boolean {
        val classInfo = ((nodePath.parentPath.parentPath.parentPath.node.source as ClassInfoModel).get() as Class<*>)
        return classInfo.kotlin
            .memberFunctions.first {
                it.name ==  (nodePath.parentPath.parentPath.node.source as MethodInfoModel).name
            }.parameters.first {
                it.name == nodePath.parentPath.node.target && it.kind == KParameter.Kind.VALUE
            }.type.isMarkedNullable
    }
}
