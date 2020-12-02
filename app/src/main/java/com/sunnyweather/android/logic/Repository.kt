package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

//仓库层
//在数据发生变化时通知给观察者（PlaceFragment里的placeLiveData.observe）
object Repository {


    fun searchPlaces(query: String)= fire(Dispatchers.IO) {
        //val result=try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
       /* }catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }*/
       // emit(result)
    }

    fun refreshWeather(lng: String, lat: String, placeName: String) = fire(Dispatchers.IO) {
       // val result = try {
            coroutineScope {
                //这样两个方法协程调用，同时完成，才进行下一步，提升效率
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                   //这方法用来封装Weather对象。后可以用emit发射出去。
                    Result.success(weather)
                } else {
                    Result.failure(
                            RuntimeException(
                                    "realtime response status is ${realtimeResponse.status}" +
                                            "daily response status is ${dailyResponse.status}"
                            )
                    )
                }
            }

       /* } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)*/
    }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
            liveData<Result<T>>(context) {
                val result = try {
                    block()
                } catch (e: Exception) {
                    Result.failure<T>(e)
                }
                emit(result)
            }
}