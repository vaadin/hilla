package com.vaadin.hilla.parser.plugins.nonnull.kotlin

import com.vaadin.hilla.parser.core.AbstractPlugin
import com.vaadin.hilla.parser.core.Node
import com.vaadin.hilla.parser.core.NodeDependencies
import com.vaadin.hilla.parser.core.NodePath
import com.vaadin.hilla.parser.core.Plugin
import com.vaadin.hilla.parser.core.PluginConfiguration
import com.vaadin.hilla.parser.models.BaseSignatureModel
import com.vaadin.hilla.parser.models.ClassInfoModel
import com.vaadin.hilla.parser.models.ClassRefSignatureModel
import com.vaadin.hilla.parser.models.TypeArgumentModel
import com.vaadin.hilla.parser.models.TypeVariableModel
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
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import java.lang.reflect.Method
import kotlin.reflect.KParameter
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
        if (nodeDependencies.node !is EntityNode) return nodeDependencies

        val entityNode = nodeDependencies.node as EntityNode
        val clazz = entityNode.source.get() as Class<*>
        if (!isKotlinClass(clazz)) return nodeDependencies

        val propertyNames = getKotlinPropertyNames(clazz)
        val namesComparator = Comparator.comparing<String, Int> { propertyNames.indexOf(it) }
        val propertyComparator = Comparator.nullsLast(Comparator.comparing<PropertyNode, String>(
            { it.source.name }, namesComparator))
        val childNodesComparator = Comparator.comparing<Node<*, *>, PropertyNode>(
            { if (it is PropertyNode) it else null }, propertyComparator)
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

        return (fields + getters + setters).distinct()
    }

    override fun enter(nodePath: NodePath<*>?) {
        // No action needed on enter
    }

    override fun resolve(node: Node<*, *>, parentPath: NodePath<*>): Node<*, *> {
        // If node is already a Kotlin node, nothing to do.
        if (node is KNode) return node

        // Check that the parent class is Kotlin. If not, return unchanged.
        val parentClass: Class<*>? = when (node) {
            is EndpointNode, is EntityNode -> (node.source as? ClassInfoModel)?.get() as? Class<*>
            is MethodNode, is PropertyNode -> (parentPath.node.source as? ClassInfoModel)?.get() as? Class<*>
            else -> null
        }
        if (parentClass != null && !isKotlinClass(parentClass)) return node
        if (parentPath != parentPath.rootPath && !isKotlinClass(findClosestClass(parentPath))) return node

        return when (node) {
            is EndpointNode -> KEndpointNode(node.source, node.target, (node.source.get() as Class<*>).kotlin)
            is EndpointExposedNode -> KEndpointExposedNode(node.source, (node.source.get() as Class<*>).kotlin)
            is MethodNode -> createKMethodNode(node, parentPath)
            is MethodParameterNode -> KMethodParameterNode(node.source, node.target,
                    (parentPath.node as KMethodNode).kFunction.parameters
                        .first { it.kind == KParameter.Kind.VALUE && it.name == node.source.name })
            is TypedNode -> resolveTypedNode(node, parentPath)
            is EntityNode ->
                KEntityNode(node.source, node.target as ObjectSchema, (node.source.get() as Class<*>).kotlin)
            is PropertyNode -> {
                val kProperty = (parentPath.node as? KEntityNode)?.kClass?.memberProperties
                    ?.firstOrNull { it.name == node.source.name }
                if (kProperty != null) {
                    KPropertyNode(node.source, node.target, kProperty)
                } else {
                    // If the property is not found, it possibly can be a property defined using getter/setter methods
                    // then leave it unchanged (will be handled in exit)
                    node
                }
            }
            else -> super.resolve(node, parentPath)
        }
    }

    private fun resolveTypedNode(node: TypedNode, parentPath: NodePath<*>): Node<*, *> {
        return when (node.type) {
            is TypeArgumentModel -> resolveTypeArgumentModel(node as TypeSignatureNode, parentPath)
            is ClassRefSignatureModel, is BaseSignatureModel, is TypeVariableModel -> resolveTypeSignature(node, parentPath)
            else -> node as Node<*, *>
        }
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
    private fun resolveTypeArgumentModel(node: TypeSignatureNode, parentPath: NodePath<*>): Node<*, *> {
        return when (parentPath.node) {
            is KTypeSignatureNode -> {
                val parentType = (parentPath.node as KTypeSignatureNode).kType
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
    }

    private fun resolveTypeSignature(node: TypedNode, parentPath: NodePath<*>): Node<*, *> {
        return when (parentPath.node) {
            is KMethodNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                (parentPath.node as KMethodNode).kFunction.returnType
            )
            is KMethodParameterNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                (parentPath.node as KMethodParameterNode).kParameter.type
            )
            is KTypeSignatureNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                (parentPath.node as KTypeSignatureNode).kType
            )
            is KPropertyNode -> KTypeSignatureNode(
                node.type, node.target, node.annotations, position = null,
                (parentPath.node as KPropertyNode).kProperty.returnType
            )
            // Other cases: return unchanged as it doesn't make any change in the nullability
            // TypeSignature with ClassRefSignatureModel, e.g. an Entity parent class, or
            // CompositeTypeSignature for properties defined using getter/setter methods
            else -> node as Node<*, *>
        }
    }

    override fun exit(nodePath: NodePath<*>?) {
        when (val node = nodePath!!.node) {
            is KTypeSignatureNode -> {
                if (node.target is Schema<*>) {
                    val schema = node.target as Schema<*>
                    schema.nullable = if (node.kType.isMarkedNullable) true else null
                }
            }
            is KPropertyNode -> {
                val entityNode = nodePath.parentPath.node as KEntityNode
                val propertySchema = entityNode.target.properties[node.target]
                propertySchema?.nullable = if (node.kProperty.returnType.isMarkedNullable) true else null
            }
            is PropertyNode -> {
                if (nodePath.parentPath.node is KEntityNode) {
                    val entityNode = nodePath.parentPath.node as KEntityNode
                    val propertySchema = entityNode.target.properties[node.target]
                    val member = node.source.primaryMember.get()
                    if (member is Method) {
                        val kMember = entityNode.kClass.memberFunctions.firstOrNull { it.javaMethod == member }
                        if (kMember != null) {
                            // Check if the member is a getter or setter
                            when (member) {
                                node.source.get().getter.member ->
                                    propertySchema?.nullable = if (kMember.returnType.isMarkedNullable) true else null
                                node.source.get().setter.member ->
                                    propertySchema?.nullable =
                                        if (kMember.parameters.first().type.isMarkedNullable) true else null
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createKMethodNode(node: MethodNode, parentPath: NodePath<*>): KMethodNode {
        return when (val parentNode = parentPath.node) {
            is KEndpointNode -> {
                KMethodNode(
                    node.source,
                    node.target,
                    parentNode.kClass.memberFunctions.first { it.name == node.source.name }
                )
            }

            is KEndpointExposedNode -> {
                val methodName = node.source.name
                val kClass = parentNode.kClass

                kClass.memberFunctions
                    .find { it.name == methodName }
                    ?.let { kFunction ->
                        KMethodNode(node.source, node.target, kFunction)
                    } ?: error("Defining public class properties in BrowserCallable class body is not supported. " +
                        "Consider marking '${kClass.qualifiedName} -> " +
                        "${methodName.substring(3).lowercase()}' as either private or protected")
            }

            else -> error("Cannot create KMethodNode as parent node is neither KEndpointNode nor KEndpointExposedNode. " +
                    "Parent node: $parentNode")
        }
    }

    private fun isKotlinClass(clazz: Class<*>): Boolean {
        return clazz.declaredAnnotations.any {
            it.annotationClass.qualifiedName == "kotlin.Metadata"
        }
    }

    private fun findClosestClass(nodePath: NodePath<*>): Class<*> {
        return nodePath.stream().map{ it.node }
            .filter{ it.source is ClassInfoModel }
            .map{ it.source as ClassInfoModel }
            .findFirst().get().get() as Class<*>
    }
}
