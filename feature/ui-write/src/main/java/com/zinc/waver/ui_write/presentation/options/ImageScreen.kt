package com.zinc.waver.ui_write.presentation.options

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.zinc.waver.model.LoadedImageInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray2
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.IconButton
import com.zinc.waver.ui_write.R
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun ImageItem(
    imageInfo: LoadedImageInfo,
    deleteImage: ((LoadedImageInfo) -> Unit)? = null
) {
    ConstraintLayout(
        modifier = Modifier
            .size(80.dp)
            .border(1.dp, color = Gray3, shape = RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))

    ) {
        val (icon, image) = createRefs()

        Image(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .clip(RoundedCornerShape(10.dp))
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            painter = rememberAsyncImagePainter(
                model = imageInfo.uri,
                contentScale = ContentScale.Crop
            ),
            contentDescription = "Captured image"
        )


        if (deleteImage != null) {
            IconButton(
                onClick = { deleteImage(imageInfo) },
                image = CommonR.drawable.btn_12_close,
                modifier = Modifier
                    .padding(5.dp)
                    .background(
                        color = Gray1.copy(alpha = 0.7f), shape = CircleShape
                    )
                    .then(Modifier.size(20.dp))
                    .constrainAs(icon) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    },
                contentDescription = "닫기"
            )
        }
    }

}

@Composable
fun AddImageItem(
    canUseImage: Boolean,
    addButtonClicked: () -> Unit
) {
    val borderColor = if (canUseImage) Gray3 else Main4

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                addButtonClicked()
            }
            .border(1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .background(Gray2, RoundedCornerShape(10.dp))


    ) {
        Image(
            painter = painterResource(id = CommonR.drawable.btn_20_add),
            contentDescription = stringResource(id = R.string.addImageDesc),
            modifier = Modifier.align(Alignment.Center)
        )

        if (!canUseImage) {
            Image(
                modifier = Modifier
                    .size(width = 67.dp, height = 32.dp)
                    .align(Alignment.TopEnd),
                painter = painterResource(id = R.drawable.chip),
                contentDescription = stringResource(id = R.string.addImageWaverPlusDesc)
            )
        }
    }
}

@Preview
@Composable
private fun AddImageItemPreview() {
    AddImageItem(canUseImage = false) {

    }
}