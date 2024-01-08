package com.updateninja.a3dprintingtimelapsecamera

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpClient(private val viewModel:MyViewModel) {
    private var isFinished = false
    private var jobName ="test"
    private var previousZ=0.0
    private var count = 0
    private var layerCountTimer = 0
    private var ipAddress : String? = null
    private var apiKey : String? = null



    fun fetchData(lifecycleOwner: LifecycleOwner,ipAddress:String?,apiKey:String?) {

        count++
        Log.d("Http Client","count : $count")
        GlobalScope.launch(Dispatchers.IO) {

            if (ipAddress.isNullOrEmpty() || apiKey.isNullOrEmpty()){
                Log.d("Http","Credentials are not inserted")
                return@launch
            }else{
                Log.d("Http","Credentials : IP: $ipAddress, API: $apiKey")
            }

            val url = URL("http://$ipAddress/api/v1/status")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("X-Api-Key", "$apiKey")

            // Set a timeout value in milliseconds (e.g., 5000 milliseconds for 5 seconds)
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            withContext(Dispatchers.Main) {
                // Update UI on the main thread
                viewModel.updateData(viewModel.getData().copy(connectionStatus = FetchDataState.CONNECTING))
            }

            try {



                val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                bufferedReader.close()
                val response = stringBuilder.toString()
                val jsonObject = JSONObject(response)

                var axisZ = jsonObject.getJSONObject("printer").getDouble("axis_z")
                val flow = jsonObject.getJSONObject("printer").getDouble("flow")
                val printerState = jsonObject.getJSONObject("printer").getString("state")
                val targetNozzle = jsonObject.getJSONObject("printer").getDouble("target_nozzle")

                if (jsonObject.has("printer")){
                    Log.d("Http","Connection Success")
                    axisZ = jsonObject.getJSONObject("printer").getDouble("axis_z")
                    withContext(Dispatchers.Main){
                        viewModel.updateData(viewModel.getData().copy(connectionStatus = FetchDataState.SUCCESS,ipAddress=ipAddress,apiKey=apiKey,axisZ=axisZ))
                    }

                }else{
                    Log.d("Http","Connection failed")
                    withContext(Dispatchers.Main){
                        viewModel.updateData(viewModel.getData().copy(connectionStatus = FetchDataState.FAILED))
                    }
                }

                if(jsonObject.has("job")){

                    jobName=jsonObject.getJSONObject("job").getString("id")
                    withContext(Dispatchers.Main) {
                        viewModel.updateData(viewModel.getData().copy(jobName=jobName))
                    }

                    if (axisZ>previousZ && flow==0.0){
                        layerCountTimer++
                    }

                    if (layerCountTimer>=2){
                        Log.d("Camera","Shoot camera")
                        viewModel.updateData(FetchData(connectionStatus = FetchDataState.TAKE_PHOTO))
                        previousZ=axisZ
                        layerCountTimer = 0
                    }

                }

                Log.d("Printer", "Axis Z: $axisZ, Flow : $flow, State : $printerState")



                if (printerState == "PRINTING"){
                    isFinished=false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Http","Catch block")
                withContext(Dispatchers.Main){
                    viewModel.updateData(viewModel.getData().copy(connectionStatus = FetchDataState.FAILED))
                }
            } finally {
                connection.disconnect()
                Log.d("Http","Final Block")
            }
        }


    }
}