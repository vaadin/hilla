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
        val getters = clazz.declaredMethods.filter { it.name.startsWith("get") && it.parameterCount == 0 }
            .map { it.name.substring(3).replaceFirstChar { char -> char.lowercase(Locale.getDefault()) } }
        val setters = clazz.declaredMethods.filter { it.name.startsWith("set") && it.parameterCount == 1 }
            .map { it.name.substring(3).replaceFirstChar { char -> char.lowercase(Locale.getDefault()) } }
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
            is EndpointNode -> {
                if (node.source is ClassInfoModel)
                    KEndpointNode(node.source, node.target, (node.source.get() as Class<*>).kotlin)
                else node
            }
            is EndpointExposedNode -> {
                if (node.source is ClassInfoModel)
                    KEndpointExposedNode(node.source, (node.source.get() as Class<*>).kotlin)
                else node
            }
            is MethodNode -> {
                if (node.source is MethodInfoModel) createKMethodNode(node, parentPath) else node
            }
            is MethodParameterNode -> {
                if (node.source is MethodParameterInfoModel) {
                    val kMethodNode = parentPath.node as? KMethodNode
                        ?: throw IllegalStateException("Expected parent node to be KMethodNode")
                    KMethodParameterNode(
                        node.source,
                        node.target,
                        kMethodNode.kFunction.parameters
                            .filter { it.kind == KParameter.Kind.VALUE }
                            .first { it.name == node.source.name }
                    )
                } else node
            }
            is TypedNode -> {
                when (node.type) {
                    is TypeArgumentModel -> {
                        val typeSignatureNode = node as TypeSignatureNode
                        when (parentPath.node) {
                            is KTypeSignatureNode -> {
                                val parentType = (parentPath.node as KTypeSignatureNode).kType
                                // For maps, always use index 1 (ignore key type); otherwise, use the node’s position.
                                val position = if (parentType.toString().startsWith("kotlin.collections.Map<")) 1
                                else typeSignatureNode.position
                                KTypeSignatureNode(
                                    node.type, node.target, node.annotations, typeSignatureNode.position,
                                    parentType.arguments[position].type!!
                                )
                            }

                            is KMethodParameterNode -> {
                                KTypeSignatureNode(
                                    node.type, node.target, node.annotations, typeSignatureNode.position,
                                    (parentPath.node as KMethodParameterNode).kParameter.type
                                        .arguments[typeSignatureNode.position].type!!
                                )
                            }

                            else -> node
                        }
                    }

                    is ClassRefSignatureModel, is BaseSignatureModel, is TypeVariableModel -> {
                        when (parentPath.node) {
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

                            is TypeSignatureNode -> {
                                if ((parentPath.node as TypeSignatureNode).type is TypeVariableModel &&
                                    parentPath.parentPath.node is KMethodParameterNode
                                ) {
                                    KTypeSignatureNode(
                                        node.type, node.target, node.annotations, position = null,
                                        (parentPath.parentPath.node as KMethodParameterNode).kParameter.type
                                    )
                                } else node
                            }

                            is KPropertyNode -> KTypeSignatureNode(
                                node.type, node.target, node.annotations, position = null,
                                (parentPath.node as KPropertyNode).kProperty.returnType
                            )

                            else -> node
                        }
                    }

                    else -> node
                }
            }
            is EntityNode -> {
                if (node.source is ClassInfoModel)
                    KEntityNode(node.source, node.target as ObjectSchema, (node.source.get() as Class<*>).kotlin)
                else node
            }
            is PropertyNode -> {
                if (node.source is JacksonPropertyModel) {
                    val kProperty = (parentPath.node as? KEntityNode)?.kClass?.memberProperties
                        ?.firstOrNull { it.name == node.source.name }
                    if (kProperty != null) {
                        KPropertyNode(node.source, node.target, kProperty)
                    } else {
                        node // if not found, leave it unchanged (will be handled in exit)
                    }
                } else node
            }
            else -> super.resolve(node, parentPath)
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
            .filter{ node -> node.getSource() is ClassInfoModel }
            .map{ node -> node.getSource() as ClassInfoModel }
            .findFirst().get().get() as Class<*>
    }
}
