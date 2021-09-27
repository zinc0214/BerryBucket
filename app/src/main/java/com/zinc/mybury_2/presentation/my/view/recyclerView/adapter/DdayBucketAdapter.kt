package com.zinc.mybury_2.presentation.my.view.recyclerView.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zinc.data.models.BucketInfoSimple
import com.zinc.mybury_2.databinding.WidgetCardTextDdayBinding
import com.zinc.mybury_2.presentation.my.view.recyclerView.CardTextDdayViewHolder

class DdayBucketAdapter(
    private val list: List<BucketInfoSimple>,
    private val loadingFinished: () -> Unit
) :
    RecyclerView.Adapter<CardTextDdayViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTextDdayViewHolder {
        return CardTextDdayViewHolder(
            WidgetCardTextDdayBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holderText: CardTextDdayViewHolder, position: Int) {
        holderText.bind(list[position], loadingFinished)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}