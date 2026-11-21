/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.plugins.nonnull.kotlin

import com.vaadin.hilla.parser.core.AbstractPlugin
import com.vaadin.hilla.parser.core.Node
import com.vaadin.hilla.parser.core.NodeDependencies
import com.vaadin.hilla.parser.core.NodePath
import com.vaadin.hilla.parser.core.Plugin
import com.vaadin.hilla.parser.core.PluginConfiguration
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.backbone.knodes.KEndpointExposedNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KEndpointNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KEntityNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KMethodNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KMethodParameterNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KPropertyNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KTypeSignatureNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointExposedNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.EndpointNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.EntityNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.MethodParameterNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.PropertyNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypeSignatureNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.TypedNode
import com.vaadin.hilla.parser.utils.Signatures
import java.lang.reflect.Method
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod

// The parser core is deprecated only to warn code outside Hilla.
@Suppress("DEPRECATION")
class KotlinNullabilityPlugin : AbstractPlugin<PluginConfiguration>() {

    override fun getRequiredPlugins(): Collection<Class<out Plugin?>> =
        listOf<Class<out Plugin?>>(BackbonePlugin::class.java)

    /*
     * This method is overridden to sort the child nodes of the EntityNode,
     * first by the order of the properties in the class constructor, and then for the
     * computed properties, which are listed after other properties and fields, by their
     * names: Java reflection 'declaredMethods' returns the getter/setter methods in no
     * particular order, which can change from one run to another, so it cannot be used
     * if the same classes are to give the same output every time.
     */
    override fun scan(nodeDependencies: NodeDependencies): NodeDependencies {
        if (nodeDependencies.node !is EntityNode) return nodeDependencies

        val entityNode = nodeDependencies.node as EntityNode
        val clazz = entityNode.source
        if (!isKotlinClass(clazz)) return nodeDependencies

        val propertyNames = getKotlinPropertyNames(clazz)
        val namesComparator = Comparator.comparing<String, Int> { propertyNames.indexOf(it) }
        val propertyComparator = Comparator.nullsLast(Comparator.comparing<PropertyNode, String>(
            { it.source.name }, namesComparator))
        val childNodesComparator = Comparator.comparing<Node<*, *>, PropertyNode>(
            { it as? PropertyNode }, propertyComparator)
        return nodeDependencies.processChildNodes { it.sorted(childNodesComparator) }
    }

    private fun getKotlinPropertyNames(clazz: Class<*>): List<String> {
        val fields = clazz.declaredFields.map { it.name.substringBefore("\$delegate") }

        val getters = clazz.declaredMethods
            .filter { it.name.startsWith("get") && it.parameterCount == 0 }
            .map { it.name.removePrefix("get").replaceFirstChar(Char::lowercase) }

        val setters = clazz.declaredMethods
            .filter { it.name.startsWith("set") && it.parameterCount == 1 }
            .map { it.name.removePrefix("set").replaceFirstChar(Char::lowercase) }

        return (fields + (getters + setters).sorted()).distinct()
    }

    override fun resolve(node: Node<*, *>, parentPath: NodePath<*>): Node<*, *> {
        // If node is already a Kotlin node, nothing to do.
        if (node is KNode) return node

        // Check that the parent class is Kotlin. If not, return unchanged.
        val parentClass = when (node) {
            is EndpointNode, is EntityNode -> node.source as? Class<*>
            is MethodNode, is PropertyNode -> parentPath.node.source as? Class<*>
            else -> null
        }
        if (parentClass != null && !isKotlinClass(parentClass)) return node
        if (parentPath != parentPath.rootPath && !isKotlinClass(findClosestClass(parentPath))) return node

        return when (node) {
            is EndpointNode -> KEndpointNode(node.source, node.target, node.source.kotlin)
            is EndpointExposedNode -> KEndpointExposedNode(node.source, node.source.kotlin)
            is MethodNode -> createKMethodNode(node, parentPath)
            is MethodParameterNode -> KMethodParameterNode(node.source, node.target,
                    (parentPath.node as KMethodNode).kFunction.parameters
                        .first { it.kind == KParameter.Kind.VALUE && it.name == node.source.name })
            is TypedNode -> resolveTypedNode(node, parentPath)
            is EntityNode ->
                KEntityNode(node.source, node.target, node.source.kotlin)
            is PropertyNode -> {
                val kProperty = (parentPath.node as? KEntityNode)?.kClass?.memberProperties
                    ?.firstOrNull { it.name == node.source.name }
                // If the property is not found, it possibly can be a property defined using getter/setter methods
                // then leave it unchanged (will be handled in exit)
                kProperty?.let { KPropertyNode(node.source, node.target, it) } ?: node
            }
            else -> super.resolve(node, parentPath)
        }
    }

    private fun resolveTypedNode(node: TypedNode, parentPath: NodePath<*>): Node<*, *> {
        val type = node.type
        return when {
            isTypeArgument(node, parentPath) -> resolveTypeArgument(node as TypeSignatureNode, parentPath)
            Signatures.isClassRef(type) || Signatures.isBase(type) || Signatures.isTypeVariable(type) ->
                resolveTypeSignature(node, parentPath)
            else -> node as Node<*, *>
        }
    }

    /**
     * Whether a typed node stands for a type argument of the type above it, which is what
     * picks the Kotlin type of the argument out of the Kotlin type of its parent.
     */
    private fun isTypeArgument(node: TypedNode, parentPath: NodePath<*>): Boolean {
        val parent = parentPath.node as? TypedNode ?: return false
        return node is TypeSignatureNode && node.position != null &&
            Signatures.getTypeArguments(parent.type).isNotEmpty()
    }

    /**
     * This handles these cases:
     * - generic argument of method return type (and their nested generic arguments)
     * - generic argument of method parameter type (and their nested generic arguments)
     *
     * This does not handle cases:
     * - generic arguments in class definition, e.g. as 'Long' in:
     *      class Person: AbstractEntity<Long>()
     */
    private fun resolveTypeArgument(node: TypeSignatureNode, parentPath: NodePath<*>): Node<*, *> =
        when (val parentNode = parentPath.node) {
            is KTypeSignatureNode -> {
                val parentType = parentNode.kType
                // For maps, always use index 1 (ignore key type); otherwise, use the node's position.
                val position = if (parentType.toString().startsWith("kotlin.collections.Map<")) 1
                else node.position
                KTypeSignatureNode(
                    node.type, node.target, node.annotations, node.position,
                    parentType.arguments[position].type!!
                )
            }
            else -> node
        }

    private fun resolveTypeSignature(node: TypedNode, parentPath: NodePath<*>): Node<*, *> =
        when (val parentNode = parentPath.node) {
            is KMethodNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                parentNode.kFunction.returnType
            )
            is KMethodParameterNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                parentNode.kParameter.type
            )
            is KTypeSignatureNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                parentNode.kType
            )
            is KPropertyNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                parentNode.kProperty.returnType
            )
            // Other cases: return unchanged as it doesn't make any change in the nullability
            // TypeSignature of a class reference, e.g. an Entity parent class, or
            // CompositeTypeSignature for properties defined using getter/setter methods
            else -> node as Node<*, *>
        }

    override fun exit(nodePath: NodePath<*>?) {
        when (val node = nodePath!!.node) {
            is KTypeSignatureNode -> {
                node.target?.isOptional = node.kType.isMarkedNullable
            }
            is KPropertyNode -> {
                node.valueType?.isOptional = node.kProperty.returnType.isMarkedNullable
            }
            is PropertyNode -> {
                if (nodePath.parentPath.node is KEntityNode) {
                    val entityNode = nodePath.parentPath.node as KEntityNode
                    val member = node.primaryMember
                    if (member is Method) {
                        val kMember = entityNode.kClass.memberFunctions.firstOrNull { it.javaMethod == member }
                        if (kMember != null) {
                            // Check if the member is a getter or setter
                            when (member) {
                                node.source.getter.member ->
                                    node.valueType?.isOptional = kMember.returnType.isMarkedNullable
                                node.source.setter.member ->
                                    node.valueType?.isOptional =
                                        kMember.parameters.first().type.isMarkedNullable
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createKMethodNode(node: MethodNode, parentPath: NodePath<*>): KMethodNode =
        when (val parentNode = parentPath.node) {
            is KEndpointNode -> {
                KMethodNode(
                    node.source,
                    parentNode.kClass.memberFunctions.first { it.name == node.source.name }
                )
            }

            is KEndpointExposedNode -> {
                val methodName = node.source.name
                val kClass = parentNode.kClass

                kClass.memberFunctions
                    .find { it.name == methodName }
                    ?.let { kFunction ->
                        KMethodNode(node.source, kFunction)
                    } ?: error("Defining public class properties in BrowserCallable class body is not supported. " +
                        "Consider marking '${kClass.qualifiedName} -> " +
                        "${methodName.substring(3).lowercase()}' as either private or protected")
            }

            else -> error("Cannot create KMethodNode as parent node is neither KEndpointNode nor KEndpointExposedNode. " +
                    "Parent node: $parentNode")
        }

    private fun isKotlinClass(clazz: Class<*>): Boolean =
        clazz.isAnnotationPresent(Metadata::class.java)

    private fun findClosestClass(nodePath: NodePath<*>): Class<*> {
        return nodePath.stream().map{ it.node }
            .filter{ it.source is Class<*> }
            .map{ it.source as Class<*> }
            .findFirst().get()
    }
}
