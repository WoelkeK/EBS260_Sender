package pl.woelke.printersender.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode


const val ALL_TYPES = -1 // Specjalna stała "skanuj wszystkie typy".

// Lista dostępnych typów kodów
val barcodeTypes = mapOf(
    Barcode.FORMAT_QR_CODE to "QR Code",
    Barcode.FORMAT_CODE_128 to "Code 128",
    Barcode.FORMAT_CODE_39 to "Code 39",
    Barcode.FORMAT_EAN_13 to "EAN-13",
    Barcode.FORMAT_EAN_8 to "EAN-8",
    Barcode.FORMAT_UPC_A to "UPC-A",
    Barcode.FORMAT_UPC_E to "UPC-E",
    Barcode.FORMAT_ITF to "ITF",
    Barcode.FORMAT_PDF417 to "PDF-417",
    Barcode.FORMAT_AZTEC to "Aztec"
)

/**
 * Barcode type select component.
 * @param selectedType selected code (ALL_TYPES = all of available).
 * @param onTypeSelected Callback after code changed.
 */
@Composable
fun BarcodeTypeSelector(
    selectedType: Int,
    onTypeSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(16.dp)) {
        Text(
            "Wybierz typ kodu",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Center)
        )
        Button(onClick = { expanded = true }) {
            Text("Typ: ${if (selectedType == ALL_TYPES) "Wszystkie typy" else barcodeTypes[selectedType] ?: "Nieznany"}")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // Dodaj opcję "Wszystkie typy"
            DropdownMenuItem(
                text = { Text("Wszystkie typy") },
                onClick = {
                    onTypeSelected(ALL_TYPES)
                    expanded = false
                }
            )
            // Iteruj przez dostępne typy kodów
            barcodeTypes.forEach { (format, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onTypeSelected(format)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeTypeSelectorSelectField(
    selectedType: Int,
    onTypeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    val selectedTypeLabel =
        if (selectedType == ALL_TYPES) "Wszystkie typy"
        else barcodeTypes[selectedType] ?: "Nieznany"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
        },
        modifier = modifier.onFocusChanged { focusState ->
            isFocused = focusState.isFocused
            if (!focusState.isFocused) expanded = false
        }
    ) {

        TextField(
            value = selectedTypeLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Wybierz typ kodu") },

            trailingIcon = {
                androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(200, 200, 200, 200),
                unfocusedContainerColor = Color(200, 200, 200, 100),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Yellow,
                focusedIndicatorColor = Color.Cyan,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = Color.DarkGray,
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(1f)
                .focusRequester(focusRequester)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            }
        ) {
            DropdownMenuItem(
                text = { Text("Wszystkie typy") },
                onClick = {
                    onTypeSelected(ALL_TYPES)
                    expanded = false
                    focusManager.clearFocus()
                }
            )
            barcodeTypes.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        onTypeSelected(key)
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
