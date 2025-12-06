package com.example.dresscode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dresscode.adapter.OutfitAdapter
import com.example.dresscode.database.AppDatabase
import com.example.dresscode.database.Outfit
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteSelectionDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_favorite_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_favorites)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerView.layoutManager = layoutManager

        // 1. 异步加载收藏数据
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            // 调用 DAO 查询收藏的衣服
            val favorites = db.outfitDao().getFavoriteOutfits()

            if (favorites.isEmpty()) {
                Toast.makeText(requireContext(), "你还没有收藏任何衣服哦", Toast.LENGTH_SHORT).show()
                dismiss() // 没数据就关闭弹窗
                return@launch
            }

            // 2. 设置适配器
            // 这里我们创建一个 Adapter，并实现点击事件
            // 注意：OutfitAdapter 需要 MutableList，所以转换一下
            val adapter = OutfitAdapter(favorites.toMutableList(), object : OutfitAdapter.OnItemClickListener {

                // 处理收藏心形点击 (这里不需要做操作，或者禁止操作)
                override fun onFavoriteClick(outfit: Outfit, position: Int) {
                    // 弹窗里暂时不支持取消收藏，防止列表跳动
                }

                // 🔴 处理选中点击
                override fun onOutfitSelect(outfit: Outfit) {
                    val parent = parentFragment

                    if (parent is AiTryOnFragment) {
                        // 🔴 关键点：必须调用父 Fragment 的这个方法！
                        // 只有这个方法里才写了 isOutfitSet = true
                        parent.onOutfitSelectedFromDialog(outfit)

                        Toast.makeText(requireContext(), "已选择: ${outfit.title}", Toast.LENGTH_SHORT).show()
                        dismiss() // 关闭弹窗
                    }
                    else {
                        // 这是一个“查看模式” (在我的收藏里打开时)
                        Toast.makeText(requireContext(), "这是你收藏的: ${outfit.title}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            recyclerView.adapter = adapter
        }
    }
}