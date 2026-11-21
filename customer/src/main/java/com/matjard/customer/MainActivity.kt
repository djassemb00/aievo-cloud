package com.matjard.customer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.matjard.core.LanguageStore
import com.matjard.customer.ui.CustomerApp
import com.matjard.customer.ui.theme.MatjarCustomerTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageStore(newBase).wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatjarCustomerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CustomerApp()
                }
            }
        }
    }
}
