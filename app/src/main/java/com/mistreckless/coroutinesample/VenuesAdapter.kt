package com.mistreckless.coroutinesample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mistreckless.coroutinesample.data.VenuesItem
import kotlinx.android.synthetic.main.venue_item.view.*

class VenuesAdapter(private val venues: List<VenuesItem>) : RecyclerView.Adapter<VenueViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): VenueViewHolder = VenueViewHolder(parent)

    override fun getItemCount(): Int = venues.size

    override fun onBindViewHolder(holder: VenueViewHolder, position: Int) {
        holder.bind(venues[position])
    }
}

class VenueViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(LayoutInflater.from(parent.context)
    .inflate(R.layout.venue_item, parent, false)){

    fun bind(venue: VenuesItem){
        with(itemView){
            tvName.text = venue.name
            tvAddress.text = venue.location?.address
        }
    }
}