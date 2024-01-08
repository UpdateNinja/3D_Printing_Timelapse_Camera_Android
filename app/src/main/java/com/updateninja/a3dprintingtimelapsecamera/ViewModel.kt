package com.updateninja.a3dprintingtimelapsecamera

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MyViewModel: ViewModel() {
    private val _data = MutableLiveData<FetchData>()
    val data: LiveData<FetchData> get() = _data

    public var handlerStarted = false
    public var layerCounter = 0
    public var previousZ:Double = 0.0

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val httpClient = HttpClient(this)


    public fun fetchData(lifecycleOwner: LifecycleOwner,ipAddress:String?,apiKey:String?) {
        val result = httpClient.fetchData(lifecycleOwner,ipAddress,apiKey)
    }

    // Update LiveData based on connection success
    fun updateData(fetchData : FetchData) {

        _data.postValue(fetchData)

    }


    fun getData(): FetchData {
        return _data.value ?: FetchData()
    }

}