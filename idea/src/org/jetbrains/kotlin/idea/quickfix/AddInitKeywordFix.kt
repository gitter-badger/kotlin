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

import org.jetbrains.kotlin.psi.*
import kotlin.platform.*
import org.jetbrains.kotlin.diagnostics.*
import com.intellij.codeInsight.intention.*
import org.jetbrains.kotlin.idea.*
import com.intellij.openapi.project.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import org.jetbrains.kotlin.idea.project.PluginJetFilesProvider
import org.jetbrains.kotlin.idea.quickfix.quickfixUtil.createIntentionFactory
import org.jetbrains.kotlin.idea.quickfix.quickfixUtil.createIntentionForFirstParentOfType
import org.jetbrains.kotlin.lexer.JetTokens
import org.jetbrains.kotlin.psi.psiUtil.*
import java.util.ArrayList


public class AddInitKeywordFix(element: JetClassInitializer) : JetIntentionAction<JetClassInitializer>(element) {
    override fun getText() = JetBundle.message("add.init.keyword")

    override fun getFamilyName() = JetBundle.message("add.init.keyword.family")

    override fun invoke(project: Project, editor: Editor?, file: JetFile) {
        addInitKeyword(element)
    }

    companion object Factory : JetSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic) = diagnostic.createIntentionForFirstParentOfType(::AddInitKeywordFix)

        public fun createWholeProjectFixFactory(): JetSingleIntentionActionFactory = createIntentionFactory {
            JetWholeProjectForEachElementOfTypeFix.createByPredicate<JetClassInitializer>(
                predicate = { !it.hasInitKeyword() },
                taskProcessor = { addInitKeyword(it) },
                modalTitle = JetBundle.message("add.init.keyword.in.whole.project.modal.title"),
                name = JetBundle.message("add.init.keyword.in.whole.project"),
                familyName = JetBundle.message("add.init.keyword.in.whole.project.family")
            )
        }

        private fun addInitKeyword(element: JetClassInitializer) {
            if (element.hasInitKeyword()) return

            val psiFactory = JetPsiFactory(element)
            val initKeyword = psiFactory.createInitKeyword()
            val anchor = element.getBody() ?: return
            element.addBefore(initKeyword, anchor)
            element.addBefore(psiFactory.createWhiteSpace(), anchor)

            val prevLeaf: PsiElement? = element.prevLeafSkipWhitespaces()
            if (prevLeaf?.getNode()?.getElementType() == JetTokens.SEMICOLON) {
                prevLeaf!!.delete()
            }
        }
    }
}
