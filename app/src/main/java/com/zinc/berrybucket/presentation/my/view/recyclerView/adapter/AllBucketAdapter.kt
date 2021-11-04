package com.zinc.berrybucket.presentation.my.view.recyclerView.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zinc.berrybucket.databinding.WidgetCardTextBinding
import com.zinc.berrybucket.presentation.my.view.recyclerView.viewHolder.CardTextViewHolder
import com.zinc.data.models.BucketInfoSimple

class AllBucketAdapter(
    private val list: List<BucketInfoSimple>,
    private val loadingFinished: () -> Unit
) :
    RecyclerView.Adapter<CardTextViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTextViewHolder {
        return CardTextViewHolder(
            WidgetCardTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holderText: CardTextViewHolder, position: Int) {
        holderText.bind(list[position], loadingFinished)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}