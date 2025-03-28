package com.vaadin.hilla.parser.plugins.nonnull.kotlin

import com.vaadin.hilla.parser.core.*
import com.vaadin.hilla.parser.models.*
import com.vaadin.hilla.parser.models.jackson.JacksonPropertyModel
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.backbone.knodes.*
import com.vaadin.hilla.parser.plugins.backbone.nodes.*
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import java.lang.reflect.Method
import java.util.*
import kotlin.Comparator
import kotlin.reflect.KParameter
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod

class KotlinNullabilityPlugin : AbstractPlugin<PluginConfiguration>() {

    override fun getRequiredPlugins(): Collection<Class<out Plugin?>> =
        listOf<Class<out Plugin?>>(BackbonePlugin::class.java)

    /*
     * This method is overridden to sort the child nodes of the EntityNode,
     * first by the order of the properties in the class constructor, and then for the
     * computed properties, the order is determined by the order of the getter/setter methods.
     * The computed properties are listed after other properties and fields, and the order
     * of the setter/getter methods is determined by what Java reflection 'declaredMethods'
     * is returning, which is not guaranteed be in the same order of appearance in the class.
     */
    override fun scan(nodeDependencies: NodeDependencies): NodeDependencies {
        if (nodeDependencies.node !is EntityNode) {
            return nodeDependencies
        }
        val entityNode = nodeDependencies.node as EntityNode
        val clazz = entityNode.source.get() as Class<*>
        if (!isKotlinClass(clazz)) {
            return nodeDependencies
        }
        val fields = clazz.declaredFields.map { it.name.substringBefore("\$delegate") }
        val getters = clazz.declaredMethods.filter { it.name.startsWith("get") && it.parameterCount == 0 }
            .map { it.name.substring(3)
            .replaceFirstChar { it.lowercase(Locale.getDefault()) } }
        val setters = clazz.declaredMethods.filter { it.name.startsWith("set") && it.parameterCount == 1 }
            .map { it.name.substring(3)
            .replaceFirstChar { it.lowercase(Locale.getDefault()) } }

        val propertyNames = (fields + getters + setters).distinct()
        val namesComparator = Comparator.comparing<String, Int>({ propertyNames.indexOf(it) })
        val propertyComparator = Comparator.nullsLast(Comparator.comparing<PropertyNode, String>({ it.source.name }, namesComparator))
        val childNodesComparator = Comparator.comparing<Node<*, *>, PropertyNode>({ if (it is PropertyNode) it else null }, propertyComparator)
        return nodeDependencies.processChildNodes { it.sorted(childNodesComparator) }
    }

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
            // it depends on the parent node
            if (node.type is TypeArgumentModel) { // generic types
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
                if (parentPath.node is KMethodNode) { // method return type
                    return KTypeSignatureNode(node.type, node.target, node.annotations, position = null,
                        (parentPath.node as KMethodNode).kFunction.returnType)
                } else if (parentPath.node is KMethodParameterNode) { // method parameter
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
            // a property node in jackson model can be a field or an accessor method
            val kProperty = (parentPath.node as KEntityNode).kClass.memberProperties
                .firstOrNull { it.name == node.source.name }
            if (kProperty != null) {
                return KPropertyNode(node.source, node.target, kProperty)
            }
            return node // if the property is not found, return the original node
                        // and then handle it in the exit method
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
        } else if (node is PropertyNode && nodePath.parentPath.node is KEntityNode) {
            val entityNode = nodePath.parentPath.node as KEntityNode
            val propertySchema = entityNode.target.properties[node.target]
            val member = node.source.primaryMember.get()
            if (member is Method) {
                val kMember = entityNode.kClass.functions.firstOrNull { it.javaMethod == member }
                if (kMember != null) {
                    if (member == node.source.getter) {
                        propertySchema?.nullable = if (kMember.returnType.isMarkedNullable) true else null
                    } else if (member == node.source.setter) {
                        propertySchema?.nullable = if (kMember.parameters.first().type.isMarkedNullable) true else null
                    }
                }
            }
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
