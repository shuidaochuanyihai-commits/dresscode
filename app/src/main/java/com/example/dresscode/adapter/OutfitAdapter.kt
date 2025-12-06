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

    interface OnItemClickListener {
        fun onFavoriteClick(outfit: Outfit, position: Int)
        fun onOutfitSelect(outfit: Outfit) {}
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

        // 设置爱心状态
        val heartIconRes = if (outfit.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        holder.favBtn.setImageResource(heartIconRes)

        // 加载图片
        Glide.with(holder.itemView.context)
            .load(outfit.imageResId)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.imageView)

        // --- 🔴 关键修复区 ---

        // 1. 整个卡片的点击事件
        holder.itemView.setOnClickListener {
            listener.onOutfitSelect(outfit)
        }

        // 2. 为了保险，给图片也单独加一个点击事件 (双保险)
        holder.imageView.setOnClickListener {
            listener.onOutfitSelect(outfit)
        }

        // 3. 爱心的点击事件 (独立处理)
        holder.favBtn.setOnClickListener {
            listener.onFavoriteClick(outfit, position)
        }
    }

    override fun getItemCount() = outfitList.size

    fun updateItem(outfit: Outfit, position: Int) {
        if (position in 0 until outfitList.size) {
            outfitList[position] = outfit
            notifyItemChanged(position)
        }
    }
}