package com.example.ultimat.api

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

typealias Guid = String

open class API(val context: Context) {

    // Create network-related objects
    private val cache = DiskBasedCache(File("cache"), 1024 * 1024)
    private val network = BasicNetwork(HurlStack())
    private val queue = RequestQueue(cache, network).apply {
        start()
    }


    // Common/helper functions

    private fun baseUrl() : String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val serverAddress = sharedPreferences.getString("server_address", "")
        val serverPort = sharedPreferences.getString("server_port", "")

        return "http://$serverAddress:$serverPort/api/ShoppingList/"
    }


    // Request functions. These unfortunately have to be different depending on the expected response type (JSON object,
    // array or a value) due to how the underlying library handles network requests.

    fun sendRequest(
        urlSuffix: String,
        parameters: Map<String, String>?,
        method: Int,
        responseCallback: (JSONObject) -> Unit
    ) {
        val url = baseUrl() + urlSuffix
        val parametersJSON = parameters?.let { JSONObject(it) }
        val req = JsonObjectRequest(
            method, url, parametersJSON,
            Response.Listener<JSONObject>(responseCallback),
            Response.ErrorListener { error -> showToast("Error: $error") })
        queue.add(req)
    }

    fun sendRequestArray(
        urlSuffix: String,
        parameters: MutableCollection<String>?,
        method: Int,
        responseCallback: (JSONArray) -> Unit
    ) {
        val url = baseUrl() + urlSuffix
        val parametersJSON = JSONArray(parameters)
        val req = JsonArrayRequest(
            method, url, parametersJSON,
            Response.Listener<JSONArray>(responseCallback),
            Response.ErrorListener { error -> showToast("Error: $error") })
        queue.add(req)
    }

    fun sendRequestString(urlSuffix: String, method: Int, responseCallback: (String) -> Unit) {
        val url = baseUrl() + urlSuffix
        val req = StringRequest(
            method, url,
            Response.Listener<String>(responseCallback),
            Response.ErrorListener { error -> showToast("Error: $error") })
        queue.add(req)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
