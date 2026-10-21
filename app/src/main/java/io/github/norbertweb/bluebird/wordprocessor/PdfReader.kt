package io.github.norbertweb.bluebird.wordprocessor

import androidx.compose.ui.res.stringResource
import io.github.norbertweb.bluebird.R
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/** Lightweight native PDF reader. It only reads/renders PDFs; it does not edit or annotate them. */
private class PdfSession(uri: Uri, resolver: android.content.ContentResolver) : AutoCloseable {
    private val descriptor: ParcelFileDescriptor = requireNotNull(resolver.openFileDescriptor(uri, "r"))
    private val renderer = PdfRenderer(descriptor)

    val pageCount: Int get() = renderer.pageCount

    fun render(pageIndex: Int, targetWidth: Int): Bitmap = synchronized(renderer) {
        val page = renderer.openPage(pageIndex)
        try {
            val width = max(320, targetWidth)
            val height = max(1, (width.toFloat() * page.height / page.width).toInt())
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } finally {
            page.close()
        }
    }

    override fun close() {
        renderer.close()
        descriptor.close()
    }
}

@Composable
fun PdfReaderScreen(
    uri: Uri,
    isDark: Boolean,
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val palette = rememberWordFluentPalette()
    val session = remember(uri) {
        runCatching { PdfSession(uri, context.contentResolver) }.getOrNull()
    }
    val listState = rememberLazyListState()
    var zoomState = remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    val zoom = zoomState.floatValue

    DisposableEffect(session) {
        onDispose { session?.close() }
    }

    if (session == null) {
        Column(
            Modifier.fillMaxSize().background(palette.appBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.could_not_open_this_pdf), color = palette.text)
            androidx.compose.material3.TextButton(onClick = onBack) { Text(stringResource(R.string.setup_back)) }
        }
        return
    }

    Column(Modifier.fillMaxSize().background(palette.appBackground)) {
        Row(
            Modifier.fillMaxWidth().height(48.dp).background(palette.titleBar).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                FluentIcon("arrow_undo", stringResource(R.string.setup_back), Modifier.size(20.dp), Color.White)
            }
            Text(
                text = pdfDisplayName(uri, context.contentResolver),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text("${listState.firstVisibleItemIndex + 1}/${session.pageCount}", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium)
        }

        Row(
            Modifier.fillMaxWidth().height(38.dp).background(palette.ribbonSurface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { zoomState.floatValue = (zoom - .1f).coerceIn(.75f, 2.5f) }, modifier = Modifier.size(36.dp)) {
                FluentIcon("zoom_out", stringResource(R.string.zoom_out), Modifier.size(18.dp), palette.text)
            }
            Text("${(zoom * 100).toInt()}%", color = palette.text, style = MaterialTheme.typography.labelSmall)
            IconButton(onClick = { zoomState.floatValue = (zoom + .1f).coerceIn(.75f, 2.5f) }, modifier = Modifier.size(36.dp)) {
                FluentIcon("zoom_in", stringResource(R.string.zoom_in), Modifier.size(18.dp), palette.text)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
        ) {
            items((0 until session.pageCount).toList(), key = { it }) { pageIndex ->
                PdfPage(session, pageIndex, zoom)
            }
        }
    }
}

@Composable
private fun PdfPage(session: PdfSession, pageIndex: Int, zoom: Float) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = (maxWidth.value * androidx.compose.ui.platform.LocalDensity.current.density * zoom).toInt().coerceIn(320, 1800)
        val bitmap by produceState<Bitmap?>(initialValue = null, pageIndex, width) {
            value = withContext(Dispatchers.Default) { runCatching { session.render(pageIndex, width) }.getOrNull() }
        }
        val horizontal = rememberScrollState()
        Box(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontal)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White),
            contentAlignment = Alignment.TopCenter
        ) {
            if (bitmap == null) {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(28.dp)) }
            } else {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.pdf_page, pageIndex + 1),
                    modifier = Modifier.width((width / androidx.compose.ui.platform.LocalDensity.current.density).dp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

private fun pdfDisplayName(uri: Uri, resolver: android.content.ContentResolver): String = runCatching {
    resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0) else null
    }
}.getOrNull()?.takeIf { it.isNotBlank() } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "PDF"
