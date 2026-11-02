package pl.woelke.printersender.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import pl.woelke.printersender.viewmodel.MainViewModel

@Composable
fun MessageScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    val connectionStatus by mainViewModel.connectionStatus.collectAsState() // Pobranie statusu połączenia
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 66.dp)
        ) {

            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Wprowadź tekst do wysłania....") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    mainViewModel.sendMessage(messageText.text)
                    messageText = TextFieldValue("")
                },
                enabled = connectionStatus.first == "Połączono"
                        && messageText.text.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (connectionStatus.first == "Połączono") {
                        Color.Green
                    } else {
                        Color.Gray
                    },
                    contentColor = Color.Black
                )
            ) {
                Text("Wyślij do drukarki")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {

                Text(
                    text = "Skanuj kod",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.Black,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = {
                        navController.navigate("barcode")
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Skanuj kod kreskowy",
                        tint = Color.Black, // Kolor ikony.
                        modifier = Modifier.size(68.dp)
                    )
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("POWRÓT")
            }

            Text("Wiadomości:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            mainViewModel.messages.forEach { message ->
                Text(text = message, modifier = Modifier.padding(4.dp))
            }
        }
    }
}
