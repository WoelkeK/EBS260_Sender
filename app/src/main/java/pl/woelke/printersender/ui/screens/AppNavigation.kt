package pl.woelke.printersender.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.woelke.printersender.viewmodel.MainViewModel


@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    startDestination: String
) {
    val isActivatedState = mainViewModel.isActivated.collectAsState(initial = null)

    if (isActivatedState.value == null) {
        LoadingScreen()
    } else {
        NavHost(
            navController = navController,
            startDestination = if (isActivatedState.value == true) "start_screen" else "activation"
        ) {
            composable("activation") { ActivationScreen(navController, mainViewModel) }
            composable("start_screen") { StartScreen(navController) }
            composable("connection") { ConnectionScreen(navController, mainViewModel) }
            composable("message") { MessageScreen(navController, mainViewModel) }
            composable("barcode") { ScannerScreen(navController, mainViewModel) }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
