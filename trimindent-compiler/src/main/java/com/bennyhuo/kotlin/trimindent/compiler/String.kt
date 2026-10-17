package com.bennyhuo.kotlin.trimindent.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

/**
 * Created by benny.
 */
fun List<String>.minCommonIndent(): Int {
    return filter(String::isNotBlank).minOfOrNull { string ->
        string.indexOfFirst {
            !it.isWhitespace()
        }.let {
            if (it == -1) string.length else it
        }
    } ?: 0
}

sealed interface IrStringElement

fun IrExpression.toStringElement(): IrStringElement {
    return if (this is IrConst && this.kind == IrConstKind.String) {
        IrConstStringElement(this)
    } else {
        IrExpressionElement(this)
    }
}

class IrExpressionElement(val irExpression: IrExpression) : IrStringElement {
    fun reIndent(pluginContext: IrPluginContext, indent: String): IrExpressionElement {
        if (indent.isEmpty()) return this

        val preIndentFunction = pluginContext.prependIndent()
        val irCall = IrCallImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            preIndentFunction.returnType,
            preIndentFunction.symbol as IrSimpleFunctionSymbol,
            0
        ).apply {
            arguments[0] = irExpression
            arguments[1] = IrConstImpl.string(
                irExpression.startOffset,
                irExpression.endOffset,
                pluginContext.irBuiltIns.stringType,
                indent
            )
        }

        return IrExpressionElement(irCall)
    }
}

class IrConstStringElement(val irConst: IrConst) : IrStringElement {

    val values = (irConst.value as String).split("\n").toMutableList()

    fun trimFirstEmptyLine() {
        if (values.firstOrNull()?.isBlank() == true) values.removeFirst()
    }

    fun trimLastEmptyLine() {
        if (values.lastOrNull()?.isBlank() == true) values.removeLast()
    }
}