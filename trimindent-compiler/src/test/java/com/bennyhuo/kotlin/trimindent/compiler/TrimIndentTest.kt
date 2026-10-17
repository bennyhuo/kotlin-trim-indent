package com.bennyhuo.kotlin.trimindent.compiler

import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_KOTLIN_LIKE_JC
import com.bennyhuo.kotlin.compiletesting.extensions.module.KotlinModule
import com.bennyhuo.kotlin.compiletesting.extensions.module.checkResult
import com.bennyhuo.kotlin.compiletesting.extensions.source.FileBasedModuleInfoLoader
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test

/**
 * Created by benny at 2022/1/15 8:55 PM.
 */
@OptIn(ExperimentalCompilerApi::class)
class TrimIndentTest {

    @Test
    fun basic() {
        testBase("basic.kt")
    }

    @Test
    fun reIndent() {
        testBase("reIndent.kt")
    }

    @Test
    fun blankLine() {
        testBase("blankLine.kt")
    }

    @Test
    fun embedded() {
        testBase("embedded.kt")
    }

    private fun testBase(fileName: String) {
        val loader = FileBasedModuleInfoLoader("testData/$fileName")
        val sourceModuleInfos = loader.loadSourceModuleInfos()

        val modules = sourceModuleInfos.map {
            KotlinModule(it, compilerPluginRegistrars = listOf(TrimIndentCompilerPluginRegistrar()))
        }

        modules.checkResult(
            loader.loadExpectModuleInfos(),
            executeEntries = true,
            checkGeneratedIr = true,
            irOutputType = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC
        )
    }

}
