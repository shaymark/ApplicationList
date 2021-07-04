package com.example.testapps.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.testapps.AppInfo
import com.example.testapps.databinding.ItemAppinfoBinding

class ApplicationsAdapter(val onClick: ((appInfo: AppInfo) -> Unit)): ListAdapter<AppInfo, ApplicationsAdapter.ViewHolder>(AppInfoDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemAppinfoBinding = ItemAppinfoBinding.inflate(inflater, parent, false)
        return ViewHolder(itemAppinfoBinding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder(val itemAppinfoBinding: ItemAppinfoBinding, val onClick: ((appInfo: AppInfo) -> Unit)): RecyclerView.ViewHolder(itemAppinfoBinding.root){

        fun bind(item: AppInfo) {
            itemAppinfoBinding.apply {
                appName.text = item.appName
                packageName.text = item.packageName
                icon.setImageDrawable(item.appIcon)

                root.setOnClickListener {
                    onClick(item)
                }
            }
        }

    }

    class AppInfoDiffUtil: DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }

}