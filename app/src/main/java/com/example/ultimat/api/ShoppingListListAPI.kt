package com.example.ultimat.api

import android.content.Context
import com.android.volley.Request
import org.json.JSONObject

// The context is used to grab settings and show error messages. The listUpdateCallback is executed after the list has
// been fetched from the server to allow any ui elements to update their appearance, and any other such events.
class ShoppingListListAPI(context: Context, val listUpdateCallback: (MutableList<ShoppingList>) -> Unit) :
    API(context) {
    var shoppingLists: MutableList<ShoppingList> = ArrayList()

    data class ShoppingList(val name: String, val id: Guid) {
        override fun toString(): String { // This is just to enable easier debugging
            return "{ShoppingList (name: $name, id: $id)}"
        }
    }

    // Api
    fun updateListofLists() {
        updateListofLists {}
    }

    fun updateListofLists(callback: (List<ShoppingList>) -> Unit) {
        sendRequestArray("", null, Request.Method.GET) {
            shoppingLists.clear()

            if (it.length() > 0) {
                for (i in 0 until it.length()) {

                    val shoppingListJSONObject: JSONObject = it.getJSONObject(i)
                    shoppingLists.add(
                        ShoppingList(
                            shoppingListJSONObject.getString("name"),
                            shoppingListJSONObject.getString("id")
                        )
                    )
                }
            }
            listUpdateCallback(shoppingLists)
            callback(shoppingLists)

        }
    }

    fun changeNameOfList(id: Guid, name: String) {
        sendRequest(id, mapOf("name" to name, "id" to id), Request.Method.PUT) { updateListofLists() }
    }

    fun deleteList(id: Guid) {
        sendRequestString(id, Request.Method.DELETE) { updateListofLists() }
    }

    fun addList(name: String) {
        addList(name) {}
    }

    fun addList(name: String, callback: (Guid) -> Unit) {
        sendRequest("", mapOf("name" to name), Request.Method.POST) {
            updateListofLists()
            callback(it.getString("id"))
            // This function has an additional callback element to enable switching to the newly created list
        }
    }
}


