package com.langamy.base.kotlin

import android.content.Context
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.bignerdranch.android.main.R

fun updateSupportActionBar(supportActionBar: ActionBar, string: String) {
    supportActionBar.title = string
}

fun includeConnectivityErrorLayout(response:String, content: ViewGroup, layoutInflater: LayoutInflater, context: Context) {
    val view: View = layoutInflater.inflate(R.layout.connectivity_error, content)
    val textView = view.findViewById<TextView>(R.id.error_message)
    textView.text = context.getString(R.string.connectivity_error, response)
    val emoji = view.findViewById<ImageView>(R.id.embarrassed_emoji)
    val drawable = emoji.drawable
    if (drawable is Animatable) {
        (drawable as Animatable).start()
    }
}