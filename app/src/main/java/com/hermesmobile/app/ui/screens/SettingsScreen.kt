package com.hermesmobile.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hermesmobile.app.data.ChatVm
import com.hermesmobile.app.ui.theme.Accent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: ChatVm) {
    val s = vm.settings
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Koneksi gateway", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = s.baseUrl,
                    onValueChange = { s.putBaseUrl(it) },
                    label = { Text("Alamat server") },
                    placeholder = { Text("http://192.168.1.10:8642") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = s.apiKey,
                    onValueChange = { s.putApiKey(it) },
                    label = { Text("API key (API_SERVER_KEY)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = s.profile,
                    onValueChange = { s.putProfile(it) },
                    label = { Text("Profil (opsional)") },
                    placeholder = { Text("kosong = profil utama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Streaming respons", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    Switch(checked = s.streaming, onCheckedChange = { s.putStreaming(it) })
                }
                Button(onClick = { vm.testConnection { status = it } }, modifier = Modifier.fillMaxWidth()) {
                    Text("Uji koneksi")
                }
                if (status.isNotEmpty()) {
                    Text(
                        status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status.startsWith("Terhubung")) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                    )
                }
                Text(
                    "Di laptop: hermes gateway · API_SERVER_ENABLED=true · API_SERVER_KEY=… · port 8642",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text("Tampilan", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Mode", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = s.darkMode == 0, onClick = { s.putDarkMode(0) }, label = { Text("Sistem") })
                    FilterChip(selected = s.darkMode == 1, onClick = { s.putDarkMode(1) }, label = { Text("Terang") })
                    FilterChip(selected = s.darkMode == 2, onClick = { s.putDarkMode(2) }, label = { Text("Gelap") })
                }
                Text("Warna aksen", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Accent.entries.forEach { a ->
                        FilterChip(
                            selected = s.accent == a.name,
                            onClick = { s.putAccent(a.name) },
                            label = { Text(a.label) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
