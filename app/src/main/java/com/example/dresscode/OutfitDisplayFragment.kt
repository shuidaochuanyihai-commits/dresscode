package com.example.dresscode

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dresscode.adapter.OutfitAdapter
import com.example.dresscode.database.Outfit

class OutfitDisplayFragment : Fragment(R.layout.fragment_outfit_display), OutfitAdapter.OnItemClickListener {

    private lateinit var viewModel: OutfitViewModel
    private lateinit var adapter: OutfitAdapter
    private val outfitList = mutableListOf<Outfit>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[OutfitViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)

        // 筛选用的 Spinner
        val spStyle = view.findViewById<Spinner>(R.id.sp_style)
        val spSeason = view.findViewById<Spinner>(R.id.sp_season)
        val spScene = view.findViewById<Spinner>(R.id.sp_scene)

        // 🔴 绑定悬浮按钮
        val fabAdd = view.findViewById<View>(R.id.fab_add_outfit)
        fabAdd.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), UploadActivity::class.java))
        }

        // 初始化列表
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerView.layoutManager = layoutManager


        adapter = OutfitAdapter(outfitList, this)
        recyclerView.adapter = adapter

        // 观察数据
        viewModel.outfitList.observe(viewLifecycleOwner, Observer { newList ->
            outfitList.clear()
            outfitList.addAll(newList)
            adapter.notifyDataSetChanged()
        })

        // 搜索框监听
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.applyFilters(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.applyFilters(newText ?: "")
                return true
            }
        })

        // 初始化手动筛选下拉菜单
        setupSpinners(spStyle, spSeason, spScene)
    }

    // 🔴 关键：每次页面可见时，读取设置
    override fun onResume() {
        super.onResume()

        // 1. 刷新数据
        viewModel.applyFilters()

        // 2. 读取“展示模式” (显示标题还是标签)
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val displayMode = prefs.getString("display_mode", "title") ?: "title"

        // 3. 告诉适配器改变显示内容
        adapter.setDisplayMode(displayMode)
    }

    private fun setupSpinners(spStyle: Spinner, spSeason: Spinner, spScene: Spinner) {
        val styles = listOf("所有风格", "休闲", "商务", "街头", "甜美", "复古")
        val seasons = listOf("所有季节", "夏季", "冬季", "春秋")
        val scenes = listOf("所有场景", "日常", "上班", "约会", "运动", "派对")

        bindSpinner(spStyle, styles) { selected ->
            viewModel.filterStyle.value = if (selected == "所有风格") "" else selected
            viewModel.applyFilters()
        }
        bindSpinner(spSeason, seasons) { selected ->
            viewModel.filterSeason.value = if (selected == "所有季节") "" else selected
            viewModel.applyFilters()
        }
        bindSpinner(spScene, scenes) { selected ->
            viewModel.filterScene.value = if (selected == "所有场景") "" else selected
            viewModel.applyFilters()
        }
    }

    private fun bindSpinner(spinner: Spinner, data: List<String>, onSelect: (String) -> Unit) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, data)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onSelect(data[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onFavoriteClick(outfit: Outfit, position: Int) {
        viewModel.toggleFavorite(outfit)
        val newOutfitState = outfit.copy(isFavorite = !outfit.isFavorite)
        adapter.updateItem(newOutfitState, position)
    }
}