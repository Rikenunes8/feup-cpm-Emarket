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
                val stream =
                    if (responseCode == HttpURLConnection.HTTP_OK) connection.inputStream
                    else connection.errorStream
                val resp = JSONObject(stream.bufferedReader().use(BufferedReader::readText))
                stream.close()

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    response = resp.toString()
                } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    val error = if (resp.has("error")) resp.getString("error") else connection.responseMessage
                    response = Gson().toJson(Response(responseCode, error))
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