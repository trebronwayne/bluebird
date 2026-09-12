package io.github.norbertweb.bluebird.wordprocessor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Mutable bridge deliberately not observed by the parent on every keystroke. */
class SimpleEditorController {
    var value by mutableStateOf(TextFieldValue(""))
        internal set
    var revision by mutableStateOf(0)
        internal set
}

@Composable
fun SimpleDocumentEditor(
    doc: WordDocument,
    textColor: Color,
    controller: SimpleEditorController,
    modifier: Modifier = Modifier,
    onChanged: () -> Unit = {},
) {
    var pending by remember(doc.id) { mutableStateOf<String?>(null) }
    @Suppress("UNUSED_VARIABLE") val renderRevision = controller.revision

    LaunchedEffect(doc.id) {
        controller.value = TextFieldValue(flattenParagraphsForEditor(doc))
        controller.revision++
    }

    LaunchedEffect(pending) {
        val text = pending ?: return@LaunchedEffect
        delay(250)
        syncEditorTextToDocument(doc, text)
        doc.isDirty = true
        doc.lastModified = System.currentTimeMillis()
        onChanged()
        pending = null
    }

    BasicTextField(
        value = controller.value,
        onValueChange = {
            controller.value = it
            pending = it.text
        },
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        textStyle = TextStyle(color = textColor, fontSize = 16.sp, lineHeight = 24.sp),
        visualTransformation = remember(doc.id) { DocumentVisualTransformation(doc) },
        cursorBrush = SolidColor(textColor),
    )
}

internal fun flattenParagraphsForEditor(doc: WordDocument): String =
    doc.blocks.filterIsInstance<ParagraphBlock>().joinToString("\n\n") { it.field.text }

internal fun syncEditorTextToDocument(doc: WordDocument, text: String) {
    val existing = doc.blocks.filterIsInstance<ParagraphBlock>()
    val parts = text.split("\n\n")
    val paragraphs = parts.ifEmpty { listOf("") }

    paragraphs.forEachIndexed { index, content ->
        val p = existing.getOrNull(index) ?: ParagraphBlock().also { doc.blocks.add(it) }
        if (p.field.text != content) {
            val oldText = p.field.text
            val (changeStart, oldEnd, newEnd) = diffRegion(oldText, content)
            val base = BuiltInStyles.byId(p.styleId).baseAttrs()
            p.spans = adjustSpansForEdit(
                p.spans, content.length, changeStart, oldEnd, newEnd, p.typingStyle, base
            )
            p.field = TextFieldValue(content, androidx.compose.ui.text.TextRange(content.length))
        }
    }
    while (doc.blocks.filterIsInstance<ParagraphBlock>().size > paragraphs.size) {
        val last = doc.blocks.indexOfLast { it is ParagraphBlock }
        if (last >= 0) doc.blocks.removeAt(last) else break
    }
}

private class DocumentVisualTransformation(
    private val doc: WordDocument
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // During active typing the model intentionally lags by 250 ms. Returning identity here
        // avoids rebuilding an AnnotatedString for every keystroke and keeps input responsive.
        if (text.text != flattenParagraphsForEditor(doc)) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val builder = AnnotatedString.Builder()
        doc.blocks.filterIsInstance<ParagraphBlock>().forEachIndexed { index, p ->
            if (index > 0) builder.append("\n\n")
            val paragraphText = p.field.text
            if (paragraphText.isEmpty()) return@forEachIndexed
            val base = BuiltInStyles.byId(p.styleId).baseAttrs()
            normalizeAndMerge(p.spans, paragraphText.length, base).forEach { range ->
                val s = range.style
                val start = builder.length
                builder.append(paragraphText.substring(range.start, range.end))
                val end = builder.length
                builder.addStyle(
                    SpanStyle(
                        color = s.color,
                        fontSize = s.fontSize.sp,
                        fontWeight = if (s.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (s.italic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = when {
                            s.underline && s.strikethrough -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                            s.underline -> TextDecoration.Underline
                            s.strikethrough -> TextDecoration.LineThrough
                            else -> TextDecoration.None
                        }
                    ), start, end
                )
            }
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
