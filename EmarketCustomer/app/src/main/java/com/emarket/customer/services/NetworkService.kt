package com.emarket.customer.services

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


data class Response(
    val code : Int?,
    val error : String
)
enum class RequestType {
    GET, POST
}

class NetworkService {

    companion object {

        fun makeRequest(
            requestType: RequestType = RequestType.GET,
            endpoint: String,
            requestBody: String? = null
        ): String {
            var connection: HttpURLConnection? = null
            var response: String? = null

            try {
                connection = URL(endpoint).openConnection() as HttpURLConnection
                connection.run {
                    requestMethod = when (requestType) {
                        RequestType.GET -> "GET"
                        RequestType.POST -> "POST"
                    }
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 5000 // 5 seconds
                    useCaches = false
                }

                if (requestType == RequestType.POST) {
                    connection.doOutput = true
                    DataOutputStream(connection.outputStream).run {
                        writeBytes(requestBody)
                        flush()
                        close()
                    }
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val resp = inputStream.bufferedReader().use(BufferedReader::readText)
                    val responseJson = JSONObject(resp)
                    response = responseJson.toString()
                    inputStream.close()
                } else {
                    response = Gson().toJson(Response(responseCode, connection.responseMessage))
                }
            } catch (e: Exception) {
                Log.e("NetworkService", e.message ?: "")
            } finally {
                connection?.disconnect()
            }

            return response ?: Gson().toJson(Response(null, "Unexpected error requesting to the server"))
        }
    }
}