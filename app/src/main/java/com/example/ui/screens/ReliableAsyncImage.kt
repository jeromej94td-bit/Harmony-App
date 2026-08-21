package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.example.ui.components.buildReliableTotImageRequest

/**
 * More specific overload used by the game runner whenever it passes an ImageRequest.
 * It keeps the existing call sites intact while enforcing a local placeholder/error image
 * for every image-backed game card and result card.
 */
@Composable
fun AsyncImage(
    model: ImageRequest,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reliableModel = remember(model, context) {
        buildReliableTotImageRequest(context, model.data)
    }
    coil.compose.AsyncImage(
        model = reliableModel,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
