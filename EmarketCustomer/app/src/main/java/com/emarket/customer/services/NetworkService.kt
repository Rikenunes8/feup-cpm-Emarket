package com.emarket.customer.services

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

enum class RequestType {
    GET, POST
}

class NetworkService {

    companion object {

        fun makeRequest(
            requestType: RequestType = RequestType.GET,
            endpoint: String,
            requestBody: String
        ): String {
            var connection: HttpURLConnection? = null
            var response: String? = null

            try {
                connection = URL(endpoint).openConnection() as HttpURLConnection
                connection.requestMethod = when (requestType) {
                    RequestType.GET -> "GET"
                    RequestType.POST -> "POST"
                }
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 5000 // 5 seconds

                if (requestType == RequestType.POST) {
                    connection.doOutput = true
                    val outputStream = DataOutputStream(connection.outputStream)
                    outputStream.writeBytes(requestBody)
                    outputStream.flush()
                    outputStream.close()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val resp = inputStream.bufferedReader().use(BufferedReader::readText)
                    val responseJson = JSONObject(resp)
                    response = responseJson.toString()
                    inputStream.close()
                } else {
                    response = "{" +
                            "\"code\": $responseCode ," +
                            "\"error\": \"${connection.responseMessage}\"" +
                            "}"
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                Log.e("NetworkService", e.message ?: "")
            } finally {
                connection?.disconnect()
            }

            return response ?: ("{" + "\"error\": \"Unexpected error requesting to the server\"" + "}")
        }
    }
}