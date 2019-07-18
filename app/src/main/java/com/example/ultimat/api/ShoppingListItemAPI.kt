package com.example.ultimat.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import org.json.JSONObject

// The context is used to grab settings and show error messages. The listUpdateCallback is executed after the list has
// been fetched from the server to allow any ui elements to update their appearance, and any other such events.
class ShoppingListItemAPI(context: Context, val listUpdateCallback: (ShoppingList) -> Unit) : API(context) {
    // Empty shopping list is default value to avoid null problems
    var shoppingList: ShoppingList = ShoppingList("", "", emptyList())


    data class ShoppingList(var name: String, var id: Guid, var items: List<ShoppingListItem>) {
        override fun toString(): String { // This is just to enable easier debugging
            return "{ShoppingList (name: $name, id: $id)}"
        }
    }

    data class ShoppingListItem(val description: String, val id: Guid, val checked: Boolean)


    // Api
    fun updateList(listId: Guid) {
        updateList(listId) {}
    }

    fun updateList(listId: Guid, callback: (ShoppingList) -> Unit) {
        sendRequest(listId, null, Request.Method.GET) {
            val items = ArrayList<ShoppingListItem>()

            if (it.getJSONArray("items").length() > 0) {
                for (i in 0 until it.getJSONArray("items").length()) {

                    val shoppingListItemJSONObject: JSONObject = it.getJSONArray("items").getJSONObject(i)
                    items.add(
                        ShoppingListItem(
                            shoppingListItemJSONObject.getString("description"),
                            shoppingListItemJSONObject.getString("id"),
                            shoppingListItemJSONObject.getBoolean("checked")
                        )
                    )
                }
            }

            shoppingList.name = it.getString("name")
            shoppingList.id = it.getString("id")
            shoppingList.items = items

            listUpdateCallback(shoppingList)
            callback(shoppingList)
        }
    }

    fun changeItem(listId: Guid, itemId: Guid, description: String, checked: Boolean) {
        sendRequest(
            "$listId/items/$itemId",
            mapOf("description" to description, "itemId" to itemId, "checked" to checked.toString()),
            Request.Method.PUT
        ) { updateList(listId) }
    }

    fun deleteItem(listId: Guid, itemId: Guid) {
        sendRequestString("$listId/items/$itemId", Request.Method.DELETE) { updateList(listId) }
    }

    fun addItem(listId: Guid, name: String) {
        sendRequest(listId, mapOf("description" to name), Request.Method.POST) { updateList(listId) }
    }
}