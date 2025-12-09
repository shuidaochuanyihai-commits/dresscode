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

    // 当前展示模式 (默认为标题)
    private var currentMode: String = "title"

    // 外部调用此方法修改模式
    fun setDisplayMode(mode: String) {
        this.currentMode = mode
        notifyDataSetChanged() // 刷新列表
    }

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

        // 1. 设置文字内容
        holder.titleView.text = when (currentMode) {
            "style" -> "风格：${outfit.style}"
            "season" -> "季节：${outfit.season}"
            "scene" -> "场景：${outfit.scene}"
            else -> outfit.title
        }

        // 2. 设置爱心状态
        val heartIconRes = if (outfit.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        holder.favBtn.setImageResource(heartIconRes)

        // 🔴 3. 关键修复：智能加载图片
        // 先检查是不是用户上传的 (imagePath 有值)
        if (!outfit.imagePath.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(outfit.imagePath) // 加载文件路径 (String/Uri)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imageView)
        } else {
            // 没有路径，说明是系统预设的 (加载 imageResId)
            Glide.with(holder.itemView.context)
                .load(outfit.imageResId) // 加载资源 ID (Int)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imageView)
        }

        // 4. 点击事件
        holder.itemView.setOnClickListener { listener.onOutfitSelect(outfit) }
        holder.imageView.setOnClickListener { listener.onOutfitSelect(outfit) }
        holder.favBtn.setOnClickListener { listener.onFavoriteClick(outfit, position) }
    }

    override fun getItemCount() = outfitList.size

    fun updateItem(outfit: Outfit, position: Int) {
        if (position in 0 until outfitList.size) {
            outfitList[position] = outfit
            notifyItemChanged(position)
        }
    }
}