package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place
//ViewModel层
//存一些界面相关数据，通常和LiveData配合使用
class PlaceViewModel: ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    //相当于主界面给了一个搜索参数，然后从后台调数据刷新
    //一旦searchLiveData发生变化，switchMap立马执行
    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }
    //非仓库层的searchPlaces，而是将搜索参数值给searchLiveData
    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }
}