package com.futuremind.koru.processor

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec


class WrapperInterfaceBuilder(
    private val newTypeName: String,
    originalTypeSpec: TypeSpec
) {

    private val functions = originalTypeSpec.funSpecs
        .filter { !it.modifiers.contains(KModifier.PRIVATE) }
        .map { originalFuncSpec ->
            originalFuncSpec.toBuilder(name = originalFuncSpec.name)
                .clearBody()
                .setReturnType(originalFuncSpec)
                .apply {
                    modifiers.remove(KModifier.SUSPEND)
                    modifiers.add(KModifier.ABSTRACT)
                }
                .build()
        }

    private val properties = originalTypeSpec.propertySpecs
        .filter { !it.modifiers.contains(KModifier.PRIVATE) }
        .map { originalPropertySpec ->
            PropertySpec
                .builder(
                    name = originalPropertySpec.name,
                    type = originalPropertySpec.wrappedType
                )
                .mutable(false)
                .apply {
                    modifiers.add(KModifier.ABSTRACT)
                }
                .build()
        }

    fun build(): TypeSpec = TypeSpec
        .interfaceBuilder(newTypeName)
        .addFunctions(functions)
        .addProperties(properties)
        .build()

}
