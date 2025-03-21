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
        if (node is KNode) {
            return node
        }
        if (node is EndpointNode || node is EntityNode) {
            if (!isKotlinClass((node.source as ClassInfoModel).get() as Class<*>)) {
                return node
            }
        }
        if (node is MethodNode || node is PropertyNode) {
            if (!isKotlinClass((parentPath.node.source as ClassInfoModel).get() as Class<*>)) {
                return node
            }
        }
        if (parentPath != parentPath.rootPath && !isKotlinClass(findClosestClass(parentPath))) {
            return node
        }

        if (node is EndpointNode && node.source is ClassInfoModel) {
            return KEndpointNode(node.source, node.target, (node.source.get() as Class<*>).kotlin)
        } else if (node is MethodNode && node.source is MethodInfoModel) {
            return KMethodNode(node.source, node.target, (parentPath.node as KEndpointNode).kClass.memberFunctions.first {
                       it.name == node.source.name
                   })
        } else if (node is MethodParameterNode && node.source is MethodParameterInfoModel) {
            return KMethodParameterNode(node.source, node.target, (parentPath.node as KMethodNode).kFunction
                   .parameters.first {
                       it.name == node.source.name && it.kind == KParameter.Kind.VALUE
                   })
        } else if (node is TypeSignatureNode) {
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

        } else if (node is EntityNode && node.source is ClassInfoModel) {
            return KEntityNode(node.source, node.target as ObjectSchema, (node.source.get() as Class<*>).kotlin)
        } else if (node is PropertyNode && node.source is JacksonPropertyModel) {
            return KPropertyNode(node.source, node.target, (parentPath.node as KEntityNode).kClass.memberProperties.first {
                       it.name == node.source.name
                   })
        }

        return super.resolve(node, parentPath)
    }

    override fun exit(nodePath: NodePath<*>?) {
        val node = nodePath!!.node
        if (node.target !is Schema<*> && node !is KPropertyNode) {
            return
        }
        if (node is KTypeSignatureNode) {
            val schema = node.target as Schema<*>
            schema.nullable = if (node.kType.isMarkedNullable) true else null
        } else if (node is KMethodParameterNode) {
            val schema = node.target as Schema<*>
            schema.nullable = if (node.kParameter.type.isMarkedNullable) true else null
        } else if (node is KPropertyNode) {
            val entityNode = nodePath.parentPath.node as KEntityNode
            val propertySchema = entityNode.target.properties[node.target]
            propertySchema?.nullable = if (node.kProperty.returnType.isMarkedNullable) true else null
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
