package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.translation.kmp.painterDrawableRes
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.ui.AutoSizeImage

@Composable
fun AsyncImage(
    model: Any,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    placeholder: Painter = painterDrawableRes("ic_loading"),
    onError: (Throwable) -> Unit = {
        it.printStackTrace()
    }
) {
    AutoSizeImage(
        request = ImageRequest {
            data(model)

        },
        contentDescription = contentDescription,
        modifier = modifier,
        placeholderPainter = { placeholder },
        errorPainter = { rememberVectorPainter(Icons.Default.Error) }
    )
}

@Composable
fun ShadowedAsyncRoundImage(
    modifier: Modifier = Modifier,
    model: Any,
    contentDescription: String? = null,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier
            .shadow(8.dp, CircleShape, ambientColor = Color(0xffbdbdbd)),
    )
}

@Composable
fun ShadowedRoundImage(
    modifier: Modifier = Modifier,
    funnyIcon: FunnyIcon,
    contentDescription: String? = null,
) {
    FunnyImage(
        modifier = modifier.shadow(4.dp, CircleShape, ambientColor = Color(0xffbdbdbd)),
        funnyIcon = funnyIcon,
        contentDescription = contentDescription,
        contentScale = ContentScale.FillBounds
    )
}

@Composable
fun FunnyImage(
    modifier: Modifier = Modifier,
    funnyIcon : FunnyIcon,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
) {
    val icon = funnyIcon.get()
    if (icon is ImageVector){
        Image(imageVector = icon, contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
    }else if(icon is Int){
        error("image id is not support in CMP")
//        Image(painter = painterResource(id = icon), contentDescription = contentDescription, modifier = modifier, contentScale = contentScale)
    }
}