package com.zinc.berrybucket.presentation.feed.view.recyclerView.viewHolder

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.tabs.TabLayoutMediator
import com.zinc.berrybucket.R
import com.zinc.berrybucket.databinding.WidgetFeedContentBinding
import com.zinc.berrybucket.model.FeedInfo
import com.zinc.berrybucket.presentation.feed.view.recyclerView.adapter.FeedImageViewPagerAdapter
import com.zinc.berrybucket.util.dp2px


class FeedContentItemViewHolder(private val binding: WidgetFeedContentBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(info: FeedInfo) {
        binding.apply {
            this.feedInfo = info

            val context = binding.root.context
            Glide.with(context).load(R.drawable.kakao)
                .transform(CenterCrop(), RoundedCorners(context.dp2px(12))).into(profileImageView)

            info.imageList?.let {
                imageViewPager.adapter = FeedImageViewPagerAdapter(it)
                TabLayoutMediator(
                    tabLayout, imageViewPager
                ) { tab, position ->
                    Toast.makeText(context, "Tab $position", Toast.LENGTH_SHORT).show()
                }.attach()
            }
        }
    }
}

