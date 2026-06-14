package com.perksls.inesai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perksls.inesai.data.local.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val fallbackEnabled by preferencesManager.fallbackEnabled.collectAsStateWithLifecycle(initialValue = false)
    val contextWindow by preferencesManager.contextWindow.collectAsStateWithLifecycle(initialValue = 10)
    val temperature by preferencesManager.temperature.collectAsStateWithLifecycle(initialValue = 0.7)
    val systemPrompt by preferencesManager.systemPrompt.collectAsStateWithLifecycle(initialValue = "You are a helpful assistant.")
    val theme by preferencesManager.theme.collectAsStateWithLifecycle(initialValue = "system")

    var fallbackEnabledInput by remember { mutableStateOf(fallbackEnabled) }
    var contextWindowInput by remember { mutableStateOf(contextWindow.toString()) }
    var temperatureInput by remember { mutableStateOf(temperature.toString()) }
    var systemPromptInput by remember { mutableStateOf(systemPrompt) }
    var themeInput by remember { mutableStateOf(theme) }

    LaunchedEffect(fallbackEnabled) { fallbackEnabledInput = fallbackEnabled }
    LaunchedEffect(contextWindow) { contextWindowInput = contextWindow.toString() }
    LaunchedEffect(temperature) { temperatureInput = temperature.toString() }
    LaunchedEffect(systemPrompt) { systemPromptInput = systemPrompt }
    LaunchedEffect(theme) { themeInput = theme }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Definições") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Fallback
            Text("🔄 Fallback Automático", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ativar Fallback", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Se o provider principal falhar, tenta outros por ordem",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(checked = fallbackEnabledInput, onCheckedChange = { fallbackEnabledInput = it })
            }

            HorizontalDivider()

            // Contexto
            Text("📚 Contexto", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Slider(
                value = contextWindowInput.toFloatOrNull() ?: 10f,
                onValueChange = { contextWindowInput = it.toInt().toString() },
                valueRange = 1f..50f,
                steps = 49,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Últimas $contextWindowInput mensagens", style = MaterialTheme.typography.labelMedium)

            HorizontalDivider()

            // Temperatura
            Text(
                "🌡️ Temperatura: $temperatureInput",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = temperatureInput.toFloatOrNull() ?: 0.7f,
                onValueChange = { temperatureInput = String.format("%.1f", it) },
                valueRange = 0f..2f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // System Prompt
            Text("🤖 System Prompt", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(
                value = systemPromptInput,
                onValueChange = { systemPromptInput = it },
                label = { Text("Instrução de sistema") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // Tema
            Text("🎨 Tema", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            listOf("system" to "Automático", "light" to "Claro", "dark" to "Escuro").forEach { (value, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = themeInput == value, onClick = { themeInput = value })
                    Text(label)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        preferencesManager.saveFallbackEnabled(fallbackEnabledInput)
                        preferencesManager.saveContextWindow(contextWindowInput.toIntOrNull() ?: 10)
                        preferencesManager.saveTemperature(temperatureInput.toDoubleOrNull() ?: 0.7)
                        preferencesManager.saveSystemPrompt(systemPromptInput.trim())
                        preferencesManager.saveTheme(themeInput)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💾 Guardar")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
