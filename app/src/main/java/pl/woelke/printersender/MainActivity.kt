package pl.woelke.printersender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.firstOrNull
import pl.woelke.printersender.data.DataStoreManager
import pl.woelke.printersender.ui.theme.PrinterSenderTheme
import pl.woelke.printersender.viewmodel.MainViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dataStoreManager = DataStoreManager(applicationContext)
        val viewModelFactory = viewModelFactory {
            initializer { MainViewModel(dataStoreManager) }
        }

        val mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        var startDestination = "start_screen"

        runBlocking {
            startDestination = if (dataStoreManager.isActivated.firstOrNull() == true) {
                "barcode"
            } else {
                "start_screen"
            }
        }

        setContent {
            PrinterSenderTheme {
                val navController = rememberNavController()
                pl.woelke.printersender.ui.screens.AppNavigation(
                    navController,
                    mainViewModel,
                    startDestination
                )
            }
        }
    }
}
