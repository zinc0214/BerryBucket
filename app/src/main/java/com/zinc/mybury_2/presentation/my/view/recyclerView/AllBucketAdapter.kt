package com.zinc.mybury_2.presentation.my.view.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zinc.data.models.BucketInfoSimple
import com.zinc.mybury_2.databinding.WidgetCardTextBinding

class AllBucketAdapter(
    private val list: List<BucketInfoSimple>,
    private val loadingFinished: () -> Unit
) :
    RecyclerView.Adapter<CardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(
            WidgetCardTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(list[position], loadingFinished)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}