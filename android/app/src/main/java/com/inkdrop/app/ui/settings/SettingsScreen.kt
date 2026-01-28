package com.inkdrop.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val serverUrl by viewModel.serverUrl.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val focusManager = LocalFocusManager.current

    var editedUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ink Drop") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "How to use",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "1. Enter your Ink Drop server URL below\n" +
                                "2. Test the connection\n" +
                                "3. Share Twitter/X article links to this app\n" +
                                "4. Articles will be sent to your Kindle!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Server URL input
            Text(
                text = "Server Configuration",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = editedUrl,
                onValueChange = { editedUrl = it },
                label = { Text("Server URL") },
                placeholder = { Text("https://your-server.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.saveServerUrl(editedUrl)
                    }
                )
            )

            // Save and Test buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.saveServerUrl(editedUrl)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = editedUrl != serverUrl && editedUrl.isNotBlank()
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.testConnection()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = connectionStatus !is ConnectionStatus.Testing
                ) {
                    if (connectionStatus is ConnectionStatus.Testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Test Connection")
                }
            }

            // Connection status
            ConnectionStatusCard(status = connectionStatus)

            Spacer(modifier = Modifier.weight(1f))

            // Version info
            Text(
                text = "Ink Drop v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ConnectionStatusCard(status: ConnectionStatus) {
    when (status) {
        is ConnectionStatus.Idle -> { /* Show nothing */ }
        is ConnectionStatus.Testing -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Testing connection...")
                }
            }
        }
        is ConnectionStatus.Success -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Connected successfully!",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        is ConnectionStatus.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = status.message,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
