package com.matjard.admin

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.matjard.core.LanguageStore
import com.matjard.admin.ui.AdminApp
import com.matjard.admin.ui.theme.MatjarAdminTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageStore(newBase).wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatjarAdminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AdminApp()
                }
            }
        }
    }
}
