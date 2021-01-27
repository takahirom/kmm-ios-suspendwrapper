package com.futuremind.iossuspendwrapper.processor

import com.futuremind.iossuspendwrapper.SuspendWrapper
import com.futuremind.iossuspendwrapper.FlowWrapper
import com.squareup.kotlinpoet.*


/**
 * Generates a wrapper to a @WrapForIos annotated class and wraps its methods:
 * 1) Regular methods will be called directly.
 * 2) suspend methods will return SuspendWrapper<T> instead of being suspended.
 * 3) Methods returning Flow<T> will instead return FlowWrapper<T>.
 */
class WrapperClassBuilder(
    wrappedClassName: ClassName,
    poetMetadataSpec: TypeSpec,
    private val newTypeName: String,
    private val interfaceGeneratedFromClass: GeneratedSuperInterface?,
    private val scopeProviderSpec: PropertySpec?
) {

    companion object {
        private const val WRAPPED_PROPERTY_NAME = "wrapped"
    }

    private val constructorSpec = FunSpec.constructorBuilder()
        .addParameter(WRAPPED_PROPERTY_NAME, wrappedClassName)
        .build()

    private val wrappedClassPropertySpec =
        PropertySpec.builder(WRAPPED_PROPERTY_NAME, wrappedClassName)
            .initializer(WRAPPED_PROPERTY_NAME)
            .addModifiers(KModifier.PRIVATE)
            .build()

    private val functions = poetMetadataSpec.funSpecs
        .map { originalFuncSpec ->
            originalFuncSpec.toBuilder(name = originalFuncSpec.name)
                .clearBody()
                .addFunctionBody(originalFuncSpec)
                .addReturnStatement(originalFuncSpec.returnType)
                .apply {
                    modifiers.remove(KModifier.SUSPEND)
                    modifiers.remove(KModifier.ABSTRACT)
                    addMissingOverrideModifier(originalFuncSpec)
                }
                .build()
        }

    //if we have an interface generated based on class signature, we need to add the override modifier to its methods explicitly
    private fun FunSpec.Builder.addMissingOverrideModifier(originalFuncSpec: FunSpec) {

        fun FunSpec.hasSameSignature(other: FunSpec) =
            this.name == other.name && this.parameters == other.parameters

        fun TypeSpec.containsFunctionSignature() =
            this.funSpecs.any { it.hasSameSignature(originalFuncSpec) }

        if (interfaceGeneratedFromClass?.typeSpec?.containsFunctionSignature() == true) {
            this.modifiers.add(KModifier.OVERRIDE)
        }
    }

    private fun FunSpec.Builder.addFunctionBody(originalFunSpec: FunSpec) =
        when {
            this.isSuspend -> this.addStatement(
                "return %T(${scopeProviderSpec?.name}) { ${originalFunSpec.asInvocation()} }",
                SuspendWrapper::class
            )
            originalFunSpec.returnType.isFlow -> this.addStatement(
                "return %T(${scopeProviderSpec?.name}, ${originalFunSpec.asInvocation()})",
                FlowWrapper::class
            )
            else -> this.addStatement("return ${originalFunSpec.asInvocation()}")
        }

    private fun FunSpec.asInvocation(): String {
        val paramsDeclaration = parameters.joinToString(", ") { it.name }
        return "${WRAPPED_PROPERTY_NAME}.${this.name}($paramsDeclaration)"
    }

    fun build(): TypeSpec = TypeSpec
        .classBuilder(newTypeName)
        .primaryConstructor(constructorSpec)
        .addProperty(wrappedClassPropertySpec)
        .addFunctions(functions)
        .build()

}
