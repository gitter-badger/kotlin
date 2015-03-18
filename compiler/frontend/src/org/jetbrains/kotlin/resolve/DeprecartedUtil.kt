package org.jetbrains.kotlin.resolve

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall

private val JAVA_DEPRECATED = FqName(javaClass<Deprecated>().getName())
private val KOTLIN_DEPRECATED = DescriptorUtils.getFqNameSafe(KotlinBuiltIns.getInstance().getDeprecatedAnnotation())

public fun DeclarationDescriptor.getDeprecatedAnnotation(): AnnotationDescriptor? = when (this) {
    is ConstructorDescriptor -> {
        getDeclaredDeprecatedAnnotation()
        ?: getContainingDeclaration().getDeclaredDeprecatedAnnotation()
    }
    is PropertyAccessorDescriptor -> {
        getDeclaredDeprecatedAnnotation()
        ?: getContainingDeclaration().getDeclaredDeprecatedAnnotation()
    }
    else -> getDeclaredDeprecatedAnnotation()
}

public fun DeclarationDescriptor.getDeclaredDeprecatedAnnotation(): AnnotationDescriptor? {
    return getAnnotations().findAnnotation(KOTLIN_DEPRECATED) ?: getAnnotations().findAnnotation(JAVA_DEPRECATED)
}

public fun createDeprecationDiagnostic(element: PsiElement, descriptor: DeclarationDescriptor, deprecated: AnnotationDescriptor): Diagnostic {
    val message = getMessageFromAnnotationDescriptor(deprecated)
    return if (message == null)
        Errors.DEPRECATED_SYMBOL.on(element, descriptor)
    else
        Errors.DEPRECATED_SYMBOL_WITH_MESSAGE.on(element, descriptor, message)
}

private fun getMessageFromAnnotationDescriptor(descriptor: AnnotationDescriptor): String? {
    val parameterName = Name.identifier("value")
    for ((parameterDescriptor, argument) in descriptor.getAllValueArguments()) {
        if (parameterDescriptor.getName() == parameterName) {
            val parameterValue = argument.getValue()
            if (parameterValue is String) {
                return parameterValue
            }
            else
                return null
        }
    }
    return null
}

public fun <F : CallableDescriptor> checkDeprecatedCall(resolvedCall: ResolvedCall<F>, trace: BindingTrace, element: PsiElement? = null) {
    val targetDescriptor = resolvedCall.getResultingDescriptor()
    val deprecated = targetDescriptor.getDeprecatedAnnotation()
    if (deprecated != null) {
        val call = resolvedCall.getCall()
        val reportElement = element ?: call.getCalleeExpression()
        if (reportElement != null) {
            trace.report(createDeprecationDiagnostic(reportElement, targetDescriptor, deprecated))
        }
    }
}

public fun checkDeprecatedDescriptor(targetDescriptor: DeclarationDescriptor, trace: BindingTrace, element: PsiElement) {
    val deprecated = targetDescriptor.getDeprecatedAnnotation()
    if (deprecated != null) {
        trace.report(createDeprecationDiagnostic(element, targetDescriptor, deprecated))
    }

}

