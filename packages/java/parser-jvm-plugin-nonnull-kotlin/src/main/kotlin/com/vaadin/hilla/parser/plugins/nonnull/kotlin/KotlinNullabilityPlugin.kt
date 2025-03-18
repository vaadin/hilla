package com.vaadin.hilla.parser.plugins.nonnull.kotlin

import com.vaadin.hilla.parser.core.*
import com.vaadin.hilla.parser.models.*
import com.vaadin.hilla.parser.plugins.backbone.BackbonePlugin
import com.vaadin.hilla.parser.plugins.backbone.knodes.KEndpointNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KMethodNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KMethodParameterNode
import com.vaadin.hilla.parser.plugins.backbone.knodes.KTypeSignatureNode
import com.vaadin.hilla.parser.plugins.backbone.nodes.*
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberFunctions

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
                        return KTypeSignatureNode(
                            node.source,
                            node.target,
                            node.annotations,
                            node.position,
                            (parentPath.node as KTypeSignatureNode).kType.arguments[node.position].type!!
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
                } else if (node.source is ClassRefSignatureModel) {
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
        }
        /*val clazz = findClosestClass(nodePath)
        if (!isKotlinClass(clazz)){
            return
        }
        val schema = node.target as Schema<*>

        if (isMethodParameterNode(nodePath)) {
            val methodInfo = nodePath.parentPath.parentPath.node.source as MethodInfoModel
            schema.nullable = if (isMethodParameterNullable(nodePath, clazz, methodInfo.name)) true else null
        } else if (isMethodReturnTypeNode(nodePath)) {
            val methodInfo = nodePath.parentPath.node.source as MethodInfoModel
            schema.nullable = if (isMethodReturnTypeNullable(clazz, methodInfo.name)) true else null
        } else if (isMethodReturnTypeTypeVariableNode(nodePath)) {
            val methodInfo = nodePath.parentPath.parentPath.node.source as MethodInfoModel
            // val typeVariable = nodePath.node.source as TypeVariableModel
            // val typeParameter = clazz.kotlin.typeParameters.first {
            //    it.name == typeVariable.name
            // }
            // schema.nullable = if (typeParameter.upperBounds.first().isMarkedNullable) true else null
        }*/
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

    private fun isMethodParameterNode(nodePath: NodePath<*>): Boolean {
        return nodePath.node.source is ClassRefSignatureModel && nodePath.parentPath.node is MethodParameterNode
    }

    private fun isMethodReturnTypeNode(nodePath: NodePath<*>): Boolean {
        return nodePath.node is TypeSignatureNode && nodePath.parentPath.node is MethodNode
    }

    private fun isMethodReturnTypeTypeVariableNode(nodePath: NodePath<*>): Boolean {
        return nodePath.node.source is TypeArgumentModel
            && nodePath.parentPath.node is TypeSignatureNode
            && nodePath.parentPath.parentPath.node is MethodNode
    }

    private fun isMethodParameterNullable(nodePath: NodePath<*>, clazz: Class<*>, methodName: String): Boolean {
        return clazz.kotlin
            .memberFunctions.first {
                it.name == methodName
            }.parameters.first {
                it.name == nodePath.parentPath.node.target &&
                    it.kind == KParameter.Kind.VALUE
            }.type.isMarkedNullable
    }

    private fun isMethodReturnTypeNullable(clazz: Class<*>, methodName: String): Boolean {
        return clazz.kotlin
            .memberFunctions.first {
                it.name == methodName
            }.returnType.isMarkedNullable
    }
}
