package com.updateninja.a3dprintingtimelapsecamera

data class FetchData(
    val connectionStatus : FetchDataState? = null,
    val ipAddress: String? = null,
    val apiKey: String? = null,
    val jobName: String? = null,
    val axisZ: Double? = null,
    val flow: Double? = null,
    val printerState: String? = null
)
