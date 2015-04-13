/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.JetBundle
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.analyzeFully
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers
import org.jetbrains.kotlin.idea.util.ShortenReferences
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm
import org.jetbrains.kotlin.types.typeUtil.isJavaLangClass
import java.util.ArrayList

public class ReplaceJavaClassAsAnnotationArgumentWithKClassFix(
        annotationEntry: JetAnnotationEntry
) : JetIntentionAction<JetAnnotationEntry>(annotationEntry) {
    private val psiFactory: JetPsiFactory = JetPsiFactory(annotationEntry)
    override fun getText() = JetBundle.message("replace.java.class.with.kclass")
    override fun getFamilyName() = JetBundle.message("replace.java.class.with.kclass.family")

    override fun invoke(project: Project, editor: Editor?, file: JetFile?) {
        processTasks(psiFactory, createReplacementTasks(element))
    }

    companion object : JetSingleIntentionActionFactory() {
        fun createReplacementTasks(element: JetAnnotationEntry): List<ReplacementTask> {
            val replacementTasks = arrayListOf<ReplacementTask>()

            element.accept(object : JetTreeVisitorVoid() {
                override fun visitCallExpression(expression: JetCallExpression) {
                    expression.acceptChildren(this)

                    val context = expression.analyze()
                    val resolvedCall = expression.getResolvedCall(context) ?: return

                    if (context.getDiagnostics().any { it.isJavaLangClassInAnnotation(expression) } &&
                        resolvedCall.getResultingDescriptor().getReturnType()?.isJavaLangClass() ?: false) {

                        val explicitType = expression.getTypeArguments().firstOrNull()?.getTypeReference()

                        if (explicitType != null && explicitType.getText() != null) {
                            replacementTasks.add(ReplacementTask(expression, explicitType.getText(), false))
                        }
                        else {
                            val inferredType = resolvedCall.getResultingDescriptor().getReturnType()?.getArguments()?.first()?.getType() ?:
                                               return
                            val renderedType = IdeDescriptorRenderers.SOURCE_CODE.renderType(inferredType)
                            replacementTasks.add(ReplacementTask(expression, renderedType, true))
                        }
                    }
                }
            })

            return replacementTasks
        }

        fun processTasks(psiFactory: JetPsiFactory, replacementTasks: Collection<ReplacementTask>) {
            val elementsToShorten = arrayListOf<JetElement>()
            replacementTasks.forEach {
                task ->
                val newElement = task.javaClassElement.replace(psiFactory.createClassLiteral(task.className)) as JetElement
                if (task.needShortening) {
                    elementsToShorten.add(newElement)
                }
            }

            ShortenReferences.DEFAULT.process(elementsToShorten)
        }

        private fun Diagnostic.isJavaLangClassInAnnotation(expression: JetCallExpression) =
                getFactory() == ErrorsJvm.JAVA_LANG_CLASS_ARGUMENT_IN_ANNOTATION &&
                getPsiElement().isAncestor(expression)

        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val entry = diagnostic.getPsiElement().getNonStrictParentOfType<JetAnnotationEntry>() ?: return null
            return ReplaceJavaClassAsAnnotationArgumentWithKClassFix(entry)
        }
    }
}

private data class ReplacementTask(val javaClassElement: JetExpression, val className: String, val needShortening: Boolean)

public class ReplaceJavaClassAsAnnotationArgumentWithKClassInWholeProjectFix(
        annotationEntry: JetAnnotationEntry
) : JetWholeProjectModalAction<JetAnnotationEntry, Collection<ReplacementTask>>(
        annotationEntry, JetBundle.message("replace.java.class.with.kclass.in.whole.project.modal.title")
) {
    private val psiFactory: JetPsiFactory = JetPsiFactory(annotationEntry)
    override fun getText() = JetBundle.message("replace.java.class.with.kclass.in.whole.project")
    override fun getFamilyName() = JetBundle.message("replace.java.class.with.kclass.in.whole.project.family")

    override fun collectDataForFile(project: Project, file: JetFile): Collection<ReplacementTask>? {
        val result = arrayListOf<ReplacementTask>()

        file.accept(object : JetTreeVisitorVoid() {
            override fun visitAnnotationEntry(annotationEntry: JetAnnotationEntry) {
                result.addAll(ReplaceJavaClassAsAnnotationArgumentWithKClassFix.createReplacementTasks(annotationEntry))
            }
        })

        return result
    }

    override fun applyChangesForFile(project: Project, file: JetFile, data: Collection<ReplacementTask>) {
        ReplaceJavaClassAsAnnotationArgumentWithKClassFix.processTasks(psiFactory, data)
    }

    companion object : JetSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): IntentionAction? {
            val entry = diagnostic.getPsiElement().getNonStrictParentOfType<JetAnnotationEntry>() ?: return null
            return ReplaceJavaClassAsAnnotationArgumentWithKClassInWholeProjectFix(entry)
        }
    }
}
