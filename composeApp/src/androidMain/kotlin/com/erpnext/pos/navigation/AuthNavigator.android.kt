package com.erpnext.pos.navigation

import android.content.Intent
import android.net.Uri
import com.erpnext.pos.AppContext

actual fun provideAutNavigator(): AuthNavigator = AndroidAuthNavigator()

class AndroidAuthNavigator() : AuthNavigator {
    private val context = AppContext.get()

    override fun openAuthPage(authUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}