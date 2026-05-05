package es.iessaladillo.rafamartinez.supermanzanares

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import dagger.hilt.android.AndroidEntryPoint
import es.iessaladillo.rafamartinez.supermanzanares.ui.SuperManzanaresApp
import es.iessaladillo.rafamartinez.supermanzanares.ui.theme.SuperManzanaresTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperManzanaresTheme {
                SuperManzanaresApp()

            }
        }
    }
}
