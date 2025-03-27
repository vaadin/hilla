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
        } else if (node is EndpointExposedNode && node.source is ClassInfoModel) {
            return KEndpointExposedNode(node.source, (node.source.get() as Class<*>).kotlin)
        } else if (node is MethodNode && node.source is MethodInfoModel) {
            return createKMethodNode(node, parentPath)
        } else if (node is MethodParameterNode && node.source is MethodParameterInfoModel) {
            return KMethodParameterNode(node.source, node.target,
                    (parentPath.node as KMethodNode).kFunction.parameters
                            .filter { it.kind == KParameter.Kind.VALUE }
                              .first { it.name == node.source.name })
        } else if (node is TypedNode) {
            if (node.type is TypeArgumentModel) { // it depends on the parent node
                val typeSignatureNode = node as TypeSignatureNode
                if (parentPath.node is KTypeSignatureNode) {
                    val parentType = (parentPath.node as KTypeSignatureNode).kType
                    // if parent is a map, then the key is always ignored and the index to read the generic type is 1
                    val position = if (parentType.toString().startsWith("kotlin.collections.Map<")) 1
                    else typeSignatureNode.position
                    return KTypeSignatureNode(
                        node.type, node.target, node.annotations, typeSignatureNode.position,
                        parentType.arguments[position].type!!
                    )
                } else if (parentPath.node is KMethodParameterNode) {
                    return KTypeSignatureNode(
                        node.type, node.target, node.annotations, typeSignatureNode.position,
                        (parentPath.node as KMethodParameterNode).kParameter.type.arguments[typeSignatureNode.position].type!!
                    )
                }
            } else if (node.type is ClassRefSignatureModel || node.type is BaseSignatureModel || node.type is TypeVariableModel) {
                if (parentPath.node is KMethodNode) { // method return type node
                    return KTypeSignatureNode(node.type, node.target, node.annotations, position = null,
                        (parentPath.node as KMethodNode).kFunction.returnType)
                } else if (parentPath.node is KMethodParameterNode) { // method parameter node
                    return KTypeSignatureNode(node.type, node.target, node.annotations, position = null,
                        (parentPath.node as KMethodParameterNode).kParameter.type)
                } else if (parentPath.node is KTypeSignatureNode) { // type argument node
                    return KTypeSignatureNode(node.type, node.target, node.annotations, position = null,
                        (parentPath.node as KTypeSignatureNode).kType)
                } else if (parentPath.node is TypeSignatureNode &&
                            (parentPath.node as TypeSignatureNode).type is TypeVariableModel &&
                            parentPath.parentPath.node is KMethodParameterNode) {
                    // method parameter type variable node
                    return KTypeSignatureNode(node.type, node.target, node.annotations, position = null,
                        (parentPath.parentPath.node as KMethodParameterNode).kParameter.type)
                } else if (parentPath.node is KPropertyNode) { // property node
                    return KTypeSignatureNode(node.type, node.target, node.annotations, position = null,
                        (parentPath.node as KPropertyNode).kProperty.returnType)
                }
            }
        } else if (node is EntityNode && node.source is ClassInfoModel) {
            return KEntityNode(node.source, node.target as ObjectSchema, (node.source.get() as Class<*>).kotlin)
        } else if (node is PropertyNode && node.source is JacksonPropertyModel) {
            return KPropertyNode(node.source, node.target,
                (parentPath.node as KEntityNode).kClass.memberProperties.first { it.name == node.source.name })
        }

        return super.resolve(node, parentPath)
    }

    override fun exit(nodePath: NodePath<*>?) {
        val node = nodePath!!.node
        if (node is KTypeSignatureNode && node.target is Schema<*>) {
            val schema = node.target as Schema<*>
            schema.nullable = if (node.kType.isMarkedNullable) true else null
        } else if (node is KPropertyNode) {
            val entityNode = nodePath.parentPath.node as KEntityNode
            val propertySchema = entityNode.target.properties[node.target]
            propertySchema?.nullable = if (node.kProperty.returnType.isMarkedNullable) true else null
        }
    }

    private fun createKMethodNode(node: MethodNode, parentPath: NodePath<*>): KMethodNode {
        if (parentPath.node is KEndpointNode) {
            return KMethodNode(node.source, node.target,
                (parentPath.node as KEndpointNode).kClass.memberFunctions.first {
                    it.name == node.source.name
                })
        } else if (parentPath.node is EndpointExposedNode) {
            if ((parentPath.node as KEndpointExposedNode).kClass.memberFunctions.none { it.name == node.source.name }) {
                throw IllegalArgumentException(
                    "Defining public class properties in BrowserCallable class body is not supported. " +
                    "Consider marking '${(parentPath.node as KEndpointExposedNode).kClass.qualifiedName} -> " +
                    "${node.source.name.substring(3).lowercase()}' as either private or protected")
            }
            return KMethodNode(node.source, node.target,
                (parentPath.node as KEndpointExposedNode).kClass.memberFunctions.first {
                    it.name == node.source.name
                })
        }
        throw IllegalStateException(("Cannot create KMethodNode as 'parentPath.node' is neither KEndpointNode nor " +
            "KEndpointExposedNode. Parent Node: ${parentPath.node}").trim())
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
