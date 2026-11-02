package pl.woelke.printersender.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.woelke.printersender.data.DataStoreManager
import pl.woelke.printersender.util.PasswordDecoder
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    //TODO  Replace with enviroment variable in prod profile
    private val passwordDecoder = PasswordDecoder("12345abcdef")
    private val _isActivated = MutableStateFlow(false)
    val isActivated: StateFlow<Boolean> = _isActivated.asStateFlow()
    var ipAddress by mutableStateOf("")
        private set
    var port by mutableStateOf("")
        private set
    var messages by mutableStateOf(mutableListOf<String>())
        private set

    private val _connectionStatus = MutableStateFlow(Pair("OFFLINE", "Brak połączenia"))
    val connectionStatus: StateFlow<Pair<String, String>> = _connectionStatus
    private val _savedIpAddresses = mutableStateListOf<String>()
    val savedIpAddresses: List<String> get() = _savedIpAddresses

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var isConnectionListenerRunning = false


    init {
        // Pobierz status aktywacji
        viewModelScope.launch {
            _isActivated.value = dataStoreManager.isActivated.firstOrNull() ?: false
        }

        dataStoreManager.savedIpAddresses
            .onEach { ips ->
                _savedIpAddresses.clear()
                _savedIpAddresses.addAll(ips)
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            ipAddress = dataStoreManager.lastIpAddress.firstOrNull() ?: ""
            port = dataStoreManager.lastPort.firstOrNull() ?: ""
        }

        viewModelScope.launch {
            connectionStatus.collect { status ->
                println("DEBUG: Status połączenia w ViewModel: ${status.first}, ${status.second}")
            }
        }
    }

    /**
     * Activation part / Generate activation code (to remove???)
     * @param   androidId -string data based on individual hardware android ID
     * return encrypted code
     */
    fun generateActivationCode(androidId: String): String {
        return passwordDecoder.generateCode(androidId)
    }

    /**
     * Verification codes method
     * @param   activationCode -received code
     * @param   expectedCode - source code
     */
    fun verifyActivationCode(activationCode: String, expectedCode: String): Boolean {
        return passwordDecoder.verifyCode(activationCode, expectedCode)
    }

    /**
     * Activate app, and keep key in the Android DataStore
     */
    fun activateApp(activationCode: String, deviceId: String) {
        val expectedCode = generateActivationCode(deviceId).substring(0, 6)
        println("DEBUG: Generated code: $expectedCode, entered code: $activationCode")
        if (verifyActivationCode(activationCode, expectedCode)) {
            viewModelScope.launch {
                dataStoreManager.setActivated(true)
                _isActivated.value = true
            }
            addMessage("Aplikacja została aktywowana.")
        } else {
            addMessage("Błędny kod aktywacyjny.")
        }
    }

    /**
     * Reset activation method only for testing and debug purpose
     * Not used in production
     */
    fun resetActivation() {
        viewModelScope.launch {
            dataStoreManager.setActivated(false)
            _isActivated.value = false
        }
    }

    /**
     * TCP Connection methods
     */
    fun updateIpAddress(newIpAddress: String) {
        ipAddress = newIpAddress.trim()
        saveLastIpAndPort()
    }

    fun updatePort(newPort: String) {
        port = newPort.trim()
        saveLastIpAndPort()
    }

    private fun saveLastIpAndPort() {
        viewModelScope.launch {
            dataStoreManager.saveLastIpAddress(ipAddress)
            dataStoreManager.saveLastPort(port)
        }
    }

    fun saveIpAddress() {
        if (ipAddress.isNotEmpty() && !_savedIpAddresses.contains(ipAddress)) {
            _savedIpAddresses.add(ipAddress)
            saveConnectionsToDataStore()
        }
    }

    fun removeIpAddress(ipAddress: String) {
        _savedIpAddresses.remove(ipAddress)
        saveConnectionsToDataStore()
    }

    private fun saveConnectionsToDataStore() {
        viewModelScope.launch {
            dataStoreManager.saveIpAddresses(_savedIpAddresses.toSet())
        }
    }

    fun connect() {
        if (ipAddress.isEmpty() || port.isEmpty()) {
            _connectionStatus.value = Pair("BŁĄD", "Adres IP lub port są puste")
            addMessage("Nie można połączyć: brak IP lub portu.")
            return
        }

        val portNumber = port.toIntOrNull()
        if (portNumber == null || portNumber !in 0..65535) {
            _connectionStatus.value = Pair("BŁĄD", "Nieprawidłowy port: $port")
            addMessage("Port musi być liczbą w zakresie 0–65535.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                socket = Socket(ipAddress, portNumber)
                outputStream = socket!!.getOutputStream()
                _connectionStatus.value = Pair("Połączono", "Połączenie z drukarką nawiązane.")
                addMessage("Połączono z $ipAddress:$port")
                startConnectionListener()
            } catch (e: UnknownHostException) {
                handleConnectionException(
                    "BŁĄD:INVALID_HOST",
                    "Nie można odnaleźć docelowego hosta."
                )
            } catch (e: SocketTimeoutException) {
                handleConnectionException(
                    "BŁĄD:TIMEOUT",
                    "Przekroczono czas oczekiwania na połączenie."
                )
            } catch (e: IOException) {
                handleConnectionException("BŁĄD:IO_ERROR", "Błąd połączenia: ${e.message}")
            } catch (e: Exception) {
                handleConnectionException("BŁĄD:UNKNOWN_ERROR", "Nieznany błąd: ${e.message}")
            }
        }
    }

    private fun handleConnectionException(code: String, message: String) {
        _connectionStatus.value = Pair(code, message)
        addMessage(message)
    }

    private fun startConnectionListener() {
        if (isConnectionListenerRunning) return
        isConnectionListenerRunning = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val buffer = ByteArray(1)
                while (socket != null && !socket!!.isClosed && socket!!.isConnected) {
                    try {
                        if (socket!!.getInputStream().read(buffer) == -1) break
                    } catch (e: IOException) {
                        break
                    }
                }
            } finally {
                _connectionStatus.value = Pair("OFFLINE", "Połączenie przerwane.")
                disconnectSocket()
            }
        }
    }

    fun sendMessage(message: String) {
        if (_connectionStatus.value.first != "Połączono") {
            addMessage("Brak połączenia – wiadomość nie została wysłana.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                outputStream?.apply {
                    write((message + "\r").toByteArray())
                    flush()
                }
                addMessage("Wysłano: $message")
            } catch (e: Exception) {
                _connectionStatus.value = Pair("BŁĄD:SEND_ERROR", "Nie można wysłać wiadomości.")
                addMessage("Błąd wysyłania: ${e.message}")
                disconnectSocket()
            }
        }
    }

    private fun disconnectSocket() {
        socket?.close()
        socket = null
        outputStream = null
        isConnectionListenerRunning = false
        _connectionStatus.value = Pair("OFFLINE", "Rozłączono.")
        addMessage("Socket został zamknięty.")
    }

    fun disconnect() {
        disconnectSocket()
    }

    private fun addMessage(message: String) {
        messages = messages.toMutableList().apply { add(message) }
    }
}
