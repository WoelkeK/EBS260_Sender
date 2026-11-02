package pl.woelke.printersender.ui.screens

import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import pl.woelke.printersender.viewmodel.MainViewModel

@Composable
fun ActivationScreen(navController: NavHostController, mainViewModel: MainViewModel) {

    val context = LocalContext.current
    var activationCode by remember { mutableStateOf("") }
    val deviceId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Aktywacja aplikacji",
            style = MaterialTheme.typography.headlineLarge, color = Color.Blue,
            modifier = Modifier
                .padding(bottom = 26.dp)
             )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Twój Identyfikator aktywacji:",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "${deviceId.take(8)} ",
            style = MaterialTheme.typography.headlineSmall, color = Color.Red,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Wprowadź kod aktywacyjny, aby odblokować aplikację.",
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = activationCode,
            onValueChange = { activationCode = it.trim() },
            label = { Text("Kod aktywacyjny") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Blue,
                disabledTextColor = Color.Gray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                mainViewModel.activateApp(activationCode, deviceId.take(8))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aktywuj")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val messages = mainViewModel.messages
        if (messages.isNotEmpty()) {
            Text(
                text = messages.joinToString("\n"),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
