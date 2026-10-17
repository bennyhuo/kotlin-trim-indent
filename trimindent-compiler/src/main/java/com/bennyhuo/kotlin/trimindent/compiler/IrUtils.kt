package com.bennyhuo.kotlin.trimindent.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrStringConcatenationImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Created by benny.
 */
fun IrConst.copyWithNewValue(newValue: String) =
    IrConstImpl.string(startOffset, endOffset, type, newValue)

fun IrStringConcatenation.copyWithNewValues(
    arguments: Collection<IrExpression>
): IrStringConcatenationImpl {
    return IrStringConcatenationImpl(startOffset, endOffset, type, arguments)
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrCall.isTrimIndent(): Boolean {
    return symbol.owner.let {
        it.name == Name.identifier("trimIndent")
                && it.parameters.firstOrNull()?.type?.classFqName?.asString() == "kotlin.String"
                && it.getPackageFragment().packageFqName.asString() == "kotlin.text"
    }

}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrPluginContext.prependIndent(): IrFunction {
    return finderForBuiltins().findFunctions(CallableId(FqName("kotlin.text"), Name.identifier("prependIndent")))
        .singleOrNull {
            it.owner.parameters.firstOrNull()?.type?.classFqName?.asString() == "kotlin.String"
        }?.owner!!
}
