package com.erpnext.pos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.erpnext.pos.di.initKoin
import com.erpnext.pos.views.login.LoginViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppContext.init(this@MainActivity)

        initKoin({ androidContext(this@MainActivity) }, listOf(androidModule))
        setContent {
            App()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null && uri.toString().startsWith("")) {
            val code = uri.getQueryParameter("code")
            if (code != null)
                loginViewModel.onAuthCodeReceived(code)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}