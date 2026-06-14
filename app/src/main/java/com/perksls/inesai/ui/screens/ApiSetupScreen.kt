package com.perksls.inesai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.perksls.inesai.ui.viewmodel.ProviderViewModel

@Composable
fun ApiSetupScreen(
    providerViewModel: ProviderViewModel,
    onSetupComplete: () -> Unit
) {
    // O setup inicial é feito diretamente pelo ecrã de Providers.
    // Este ecrã serve apenas de boas-vindas e redireciona para lá.
    var showProviderForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🤖", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("InesAI", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Adiciona o teu primeiro provider para começar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { showProviderForm = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar Provider")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onSetupComplete) {
            Text("Explorar primeiro →")
        }
    }

    if (showProviderForm) {
        // Reutiliza o bottom sheet do ProvidersScreen inline
        ProviderFormSheetPublic(
            title = "Novo Provider",
            onDismiss = { showProviderForm = false },
            onSave = { name, url, key, models, openAI ->
                providerViewModel.saveProvider(name, url, key, models, openAI)
                showProviderForm = false
                onSetupComplete()
            }
        )
    }
}
