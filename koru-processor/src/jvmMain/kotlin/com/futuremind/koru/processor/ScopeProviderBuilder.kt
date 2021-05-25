package com.futuremind.koru.processor

import com.futuremind.koru.ScopeProvider
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.Locale

/**
 * Generates a top level property [ScopeProvider] exposing a CoroutineScope to be injected into
 * generated native classes via @ToNativeClass(launchOnScope = ...).
 */
class ScopeProviderBuilder(
    packageName: String,
    poetMetadataSpec: TypeSpec
) {

    private val scopeProviderClassName = ClassName(packageName, poetMetadataSpec.name.toString())
    private val statelyClassName = ClassName("co.touchlab.stately.isolate", "IsolateState")
            .parameterizedBy(scopeProviderClassName)
    private val scopePropertyName = "exportedScopeProvider_" + poetMetadataSpec.name!!.decapitalize(
            Locale.ROOT)

    fun build(): PropertySpec = PropertySpec
        .builder(scopePropertyName, statelyClassName, KModifier.PUBLIC)
        .initializer("%T{ %T() }", statelyClassName, scopeProviderClassName)
        .build()
}
