package com.zinc.berrybucket.presentation.detail.listview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.zinc.berrybucket.compose.theme.BaseTheme
import com.zinc.berrybucket.compose.ui.component.ImageViewPagerInsideIndicator
import com.zinc.berrybucket.databinding.WidgetComposeViewBinding
import com.zinc.berrybucket.model.DetailImageInfo

class DetailImageViewHolder(private val binding: WidgetComposeViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(detailImageInfo: DetailImageInfo) {
        binding.composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                BaseTheme {
                    ImageViewPagerInsideIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        imageList = detailImageInfo.imageList
                    )
                }
            }
        }
    }
}