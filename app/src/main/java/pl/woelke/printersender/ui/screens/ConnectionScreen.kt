package pl.woelke.printersender.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import pl.woelke.printersender.R
import pl.woelke.printersender.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(navController: NavHostController, mainViewModel: MainViewModel) {

    val activity = LocalActivity.current

    var ipAddressInput by remember { mutableStateOf(TextFieldValue(mainViewModel.ipAddress)) }
    var portInput by remember { mutableStateOf(TextFieldValue(mainViewModel.port)) }
    val connectionStatus by mainViewModel.connectionStatus.collectAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(36.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ebs260),
                        contentDescription = "Logo aplikacji",
                        modifier = Modifier
                            .size(70.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "EBS-260 Komunikator",
                        color = Color.Blue,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            actions = {
                IconButton(onClick = {
//                    mainViewModel.resetActivation()
                    activity?.finishAndRemoveTask()
                    kotlin.system.exitProcess(0)


                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zamknij aplikację",
                        tint = Color.Red
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.Blue,
                actionIconContentColor = Color.White
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
                .padding(16.dp)

        ) {

            Text(
                text = "Konfiguracja połączenia z drukarką",
                color = Color.Blue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = ipAddressInput,
                onValueChange = { ipAddressInput = it },
                label = { Text("Adres IP drukarki") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = portInput,
                onValueChange = { portInput = it },
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Status połączenia: ${connectionStatus.first}",
                color = when {
                    connectionStatus.first == "Połączono" -> Color(50, 155, 50, 250)
                    connectionStatus.first.startsWith("Błąd") -> Color.Red
                    else -> Color.Gray
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            if (connectionStatus.first.startsWith("BŁĄD")) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = connectionStatus.second,
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { mainViewModel.connect() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Połącz ponownie")
                    }
                }
            } else {

                Button(
                    onClick = {
                        mainViewModel.updateIpAddress(ipAddressInput.text.trim())
                        mainViewModel.updatePort(portInput.text.trim())
                        mainViewModel.connect()
                    },
                    enabled = connectionStatus.first == "OFFLINE",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Połącz")
                }

                Button(
                    onClick = { mainViewModel.disconnect() },
                    enabled = connectionStatus.first == "Połączono",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rozłącz")
                }

                Button(
                    onClick = { navController.navigate("message") },
                    enabled = connectionStatus.first == "Połączono",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DALEJ")
                }

                LazyColumn {
                    items(mainViewModel.savedIpAddresses) { ip ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(ip, modifier = Modifier.weight(1f))
                            Button(onClick = { mainViewModel.updateIpAddress(ip) }) {
                                Text("Wybierz")
                            }
                            Button(onClick = { mainViewModel.removeIpAddress(ip) }) {
                                Text("Usuń")
                            }
                        }
                    }
                }
            }
        }
    }
}
