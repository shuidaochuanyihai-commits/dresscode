package com.example.dresscode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dresscode.R
import com.example.dresscode.database.Outfit

class OutfitAdapter(private val outfitList: MutableList<Outfit>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder>() {

    // 1. 定义接口：用于把点击事件传给 Fragment
    interface OnItemClickListener {
        fun onFavoriteClick(outfit: Outfit, position: Int)
    }

    class OutfitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_outfit)
        val titleView: TextView = view.findViewById(R.id.tv_title)
        val favBtn: ImageView = view.findViewById(R.id.btn_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutfitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outfit, parent, false)
        return OutfitViewHolder(view)
    }

    override fun onBindViewHolder(holder: OutfitViewHolder, position: Int) {
        val outfit = outfitList[position]

        holder.titleView.text = outfit.title

        // 2. 根据收藏状态显示不同的爱心图标
        val heartIconRes = if (outfit.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        holder.favBtn.setImageResource(heartIconRes)

        // 使用 Glide 加载本地资源
        Glide.with(holder.itemView.context)
            .load(outfit.imageResId)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.imageView)

        // 3. 设置收藏按钮的点击事件
        holder.favBtn.setOnClickListener {
            // 将点击事件传给 Fragment 处理，同时传入当前的数据和位置
            listener.onFavoriteClick(outfit, position)
        }
    }

    override fun getItemCount() = outfitList.size

    // 4. 用于外部调用更新列表项的函数
    fun updateItem(outfit: Outfit, position: Int) {
        // 找到列表中的旧数据，替换为新数据
        outfitList[position] = outfit
        // 刷新列表项，只刷新这一个，效率高
        notifyItemChanged(position)
    }
}