package com.example.dresscode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dresscode.adapter.OutfitAdapter
import com.example.dresscode.database.Outfit
import androidx.lifecycle.Observer
import androidx.room.Update

class OutfitDisplayFragment : Fragment(R.layout.fragment_outfit_display), OutfitAdapter.OnItemClickListener { // 🔴 继承 Adapter 的监听接口

    private lateinit var viewModel: OutfitViewModel
    private lateinit var adapter: OutfitAdapter

    // 我们需要一个可变的列表，因为 adapter 里的数据列表是可变的
    private val outfitList = mutableListOf<Outfit>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化 ViewModel
        viewModel = ViewModelProvider(this)[OutfitViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        // 2. 设置布局和适配器
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerView.layoutManager = layoutManager

        // 传入可变的列表和自己作为监听器
        adapter = OutfitAdapter(outfitList, this)
        recyclerView.adapter = adapter

        // 3. 观察数据变化 (LiveData)
        viewModel.outfitList.observe(viewLifecycleOwner, Observer { newList ->
            // 当 ViewModel 从数据库拿到新数据时，更新列表
            outfitList.clear()
            outfitList.addAll(newList)
            adapter.notifyDataSetChanged()
        })

        // 4. 首次加载数据
        viewModel.loadOutfits()
    }

    // 5. 实现 Adapter 接口的方法：处理收藏按钮点击
    override fun onFavoriteClick(outfit: Outfit, position: Int) {
        // 切换数据库中的收藏状态
        viewModel.toggleFavorite(outfit)

        // 🔴 关键步骤：本地修改数据状态并立即刷新 UI
        // 注意：toggleFavorite 里会重新 loadOutfits，最终会触发 LiveData 刷新整个列表
        // 但为了更快响应，我们也可以只刷新单个 item:
        val newOutfitState = outfit.copy(isFavorite = !outfit.isFavorite, id = outfit.id)
        adapter.updateItem(newOutfitState, position)
    }
}