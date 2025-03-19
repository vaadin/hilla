package com.vaadin.hilla.parser.plugins.nonnull.kotlin

import com.vaadin.hilla.parser.core.*
import com.vaadin.hilla.parser.models.*
import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.backbone.knodes.*
import com.vaadin.hilla.parser.plugins.backbone.nodes.*
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

class KotlinNullabilityPlugin : AbstractPlugin<PluginConfiguration>() {

    override fun getRequiredPlugins(): Collection<Class<out Plugin?>> =
        listOf<Class<out Plugin?>>(BackbonePlugin::class.java)

    override fun scan(nodeDependencies: NodeDependencies): NodeDependencies = nodeDependencies

    override fun enter(nodePath: NodePath<*>?) {}

    override fun resolve(node: Node<*, *>, parentPath: NodePath<*>): Node<*, *> {
        if (node is EndpointNode && node.source is ClassInfoModel && isKotlinClass(node.source.get() as Class<*>)) {
            return if (node is KEndpointNode) node
                else KEndpointNode(node.source, node.target, (node.source.get() as Class<*>).kotlin)
        } else if (node is MethodNode && node.source is MethodInfoModel && isKotlinClass((parentPath.node.source as ClassInfoModel).get() as Class<*>)) {
            return if (node is KMethodNode) node
                else KMethodNode(node.source, node.target, (parentPath.node as KEndpointNode).kClass.memberFunctions.first {
                    it.name == node.source.name
                })
        } else if (node is MethodParameterNode && node.source is MethodParameterInfoModel && isKotlinClass(findClosestClass(parentPath))) {
            return if (node is KMethodParameterNode) node
                else KMethodParameterNode(node.source, node.target, (parentPath.node as KMethodNode).kFunction
                .parameters.first {
                    it.name == node.source.name && it.kind == KParameter.Kind.VALUE
                })
        } else if (node is TypeSignatureNode && isKotlinClass(findClosestClass(parentPath))) {
            if (node is KTypeSignatureNode)
                return node
            else {
                if (node.source is TypeArgumentModel) { // it depends on the parent node
                    if (parentPath.node is KTypeSignatureNode) {
                        val parentType = (parentPath.node as KTypeSignatureNode).kType
                        // if parent is a map, then the key is always ignored and the index to read the generic type is 1
                        val position = if (parentType.toString().startsWith("kotlin.collections.Map<")) 1
                                       else node.position
                        return KTypeSignatureNode(
                            node.source,
                            node.target,
                            node.annotations,
                            node.position,
                            parentType.arguments[position].type!!
                        )
                    } else if (parentPath.node is KMethodParameterNode) {
                        return KTypeSignatureNode(
                            node.source,
                            node.target,
                            node.annotations,
                            node.position,
                            (parentPath.node as KMethodParameterNode).kParameter.type.arguments[node.position].type!!
                        )
                    }
                } else if (node.source is ClassRefSignatureModel || node.source is BaseSignatureModel) {
                    if (parentPath.node is KMethodNode) { // method return type node
                        return KTypeSignatureNode(
                            node.source,
                            node.target,
                            node.annotations,
                            node.position,
                            (parentPath.node as KMethodNode).kFunction.returnType
                        )
                    } else if (parentPath.node is KMethodParameterNode) { // method parameter node
                        return KTypeSignatureNode(
                            node.source,
                            node.target,
                            node.annotations,
                            node.position,
                            (parentPath.node as KMethodParameterNode).kParameter.type
                        )
                    } else if (parentPath.node is KTypeSignatureNode) { // type argument node
                        return KTypeSignatureNode(
                            node.source,
                            node.target,
                            node.annotations,
                            node.position,
                            (parentPath.node as KTypeSignatureNode).kType
                        )
                    }
                }
            }
        } else if (node is EntityNode && node.source is ClassInfoModel && isKotlinClass(node.source.get() as Class<*>)) {
            return if (node is KEntityNode) node
                else KEntityNode(node.source, node.target as ObjectSchema, (node.source.get() as Class<*>).kotlin)
        } else if (node is PropertyNode && node.source is JacksonPropertyModel && isKotlinClass((parentPath.node.source as ClassInfoModel).get() as Class<*>)) {
            return if (node is KPropertyNode) node
                else KPropertyNode(node.source, node.target, (parentPath.node as KEntityNode).kClass.memberProperties.first {
                    it.name == node.source.name
                })
        }

        return super.resolve(node, parentPath)
    }

    override fun exit(nodePath: NodePath<*>?) {
        val node = nodePath!!.node
        if (node.target !is Schema<*>) {
            return
        }
        val schema = node.target as Schema<*>
        if (node is KTypeSignatureNode) {
            schema.nullable = if (node.kType.isMarkedNullable) true else null
        } else if (node is KMethodParameterNode) {
            schema.nullable = if (node.kParameter.type.isMarkedNullable) true else null
        } else if (node is KEntityNode) {

            node.kClass.declaredMemberProperties
        }
    }

    private fun isKotlinClass(clazz: Class<*>): Boolean {
        return clazz.declaredAnnotations.any {
            it.annotationClass.qualifiedName == "kotlin.Metadata"
        }
    }

    private fun findClosestClass(nodePath: NodePath<*>): Class<*> {
        return nodePath.stream().map{ it.node }
            .filter{ node -> node.getSource() is ClassInfoModel }
            .map{ node -> node.getSource() as ClassInfoModel }
            .findFirst().get().get() as Class<*>
    }
}
