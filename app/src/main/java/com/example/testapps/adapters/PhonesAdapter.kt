package com.example.testapps.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testapps.AppInfo
import com.example.testapps.PhoneInfo
import com.example.testapps.databinding.ItemAppinfoBinding
import com.example.testapps.databinding.ItemPhoneinfoBinding
import com.squareup.picasso.Picasso

class PhonesAdapter(val onClick: ((phoneInfo: PhoneInfo) -> Unit)): ListAdapter<PhoneInfo, PhonesAdapter.ViewHolder>(AppInfoDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemPhoneinfoBinding = ItemPhoneinfoBinding.inflate(inflater, parent, false)
        return ViewHolder(itemPhoneinfoBinding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder(val itemPhoneinfoBinding: ItemPhoneinfoBinding, val onClick: ((phoneInfo: PhoneInfo) -> Unit)): RecyclerView.ViewHolder(itemPhoneinfoBinding.root){

        fun bind(item: PhoneInfo) {
            itemPhoneinfoBinding.apply {
                contentName.text = item.name
                phone.text = item.mobileNumber

                Picasso.get().load(item.photoURI).into(icon)

                root.setOnClickListener {
                    onClick(item)
                }
            }
        }

    }

    class AppInfoDiffUtil: DiffUtil.ItemCallback<PhoneInfo>() {
        override fun areItemsTheSame(oldItem: PhoneInfo, newItem: PhoneInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PhoneInfo, newItem: PhoneInfo): Boolean {
            return oldItem == newItem
        }
    }

}