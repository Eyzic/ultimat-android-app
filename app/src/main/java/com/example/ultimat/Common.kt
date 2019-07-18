package com.example.ultimat

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

fun dialogForName(title: String, context: Context, layoutInflater : LayoutInflater, callback: (String) -> Unit) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(title)
    val dialogLayout = layoutInflater.inflate(R.layout.dialog_change_name, null)
    val editText = dialogLayout.findViewById<EditText>(R.id.editText)
    builder.setView(dialogLayout)
    builder.setPositiveButton("OK") { _, _ ->
        callback(editText.text.toString())
    }
    builder.show()
}