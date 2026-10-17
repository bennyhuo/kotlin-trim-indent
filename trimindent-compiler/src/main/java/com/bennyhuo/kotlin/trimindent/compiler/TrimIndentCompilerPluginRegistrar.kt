package com.bennyhuo.kotlin.trimindent.compiler

import com.bennyhuo.kotlin.trimindent.BuildConfig
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration


lateinit var logger: Logger

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class TrimIndentCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        logger = Logger(configuration.get(CLIConfigurationKeys.ORIGINAL_MESSAGE_COLLECTOR_KEY))
        IrGenerationExtension.registerExtension(TrimIndentIrGenerator())
    }

    override val supportsK2: Boolean
        get() = true
}


