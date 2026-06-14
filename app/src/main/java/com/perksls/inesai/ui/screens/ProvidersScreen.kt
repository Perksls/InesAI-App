package com.perksls.inesai.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perksls.inesai.data.model.AIProvider
import com.perksls.inesai.data.model.ProvidersConfig
import com.perksls.inesai.ui.viewmodel.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    viewModel: ProviderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOrder: () -> Unit
) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingProvider by remember { mutableStateOf<AIProvider?>(null) }
    var deleteTarget by remember { mutableStateOf<AIProvider?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Providers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToOrder) {
                        Icon(Icons.Default.Sort, contentDescription = "Ordenar fallback")
                    }
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar provider")
                    }
                }
            )
        }
    ) { padding ->
        if (providers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🔌", style = MaterialTheme.typography.displayMedium)
                    Text("Sem providers configurados", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Adiciona um provider compatível com a API OpenAI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Button(onClick = { showAddSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar Provider")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(providers, key = { _, p -> p.id }) { index, provider ->
                    ProviderCard(
                        provider = provider,
                        index = index,
                        onEdit = { editingProvider = provider },
                        onDelete = { deleteTarget = provider },
                        onSetPrimary = { viewModel.setPrimary(provider.id) },
                        onModelSelected = { model -> viewModel.setActiveModel(provider.id, model) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet) {
        ProviderFormSheetPublic(
            title = "Novo Provider",
            onDismiss = { showAddSheet = false },
            onSave = { name, url, key, models, openAI ->
                viewModel.saveProvider(name, url, key, models, openAI)
                showAddSheet = false
            }
        )
    }

    editingProvider?.let { provider ->
        ProviderFormSheetPublic(
            title = "Editar Provider",
            initial = provider,
            onDismiss = { editingProvider = null },
            onSave = { name, url, key, models, openAI ->
                viewModel.saveProvider(name, url, key, models, openAI, id = provider.id)
                editingProvider = null
            }
        )
    }

    deleteTarget?.let { provider ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar provider?") },
            text = { Text("\"${provider.name}\" será removido permanentemente.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteProvider(provider.id); deleteTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderCard(
    provider: AIProvider,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit,
    onModelSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (index == 0) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(provider.name, style = MaterialTheme.typography.titleMedium)
                    if (index == 0) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                            Text(
                                "Principal",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Text(provider.baseUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            if (provider.models.size > 1) {
                var modelExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = modelExpanded, onExpandedChange = { modelExpanded = it }) {
                    OutlinedTextField(
                        value = provider.effectiveModel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Modelo ativo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(expanded = modelExpanded, onDismissRequest = { modelExpanded = false }) {
                        provider.models.forEach { model ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(model, style = MaterialTheme.typography.bodySmall)
                                        if (model == provider.effectiveModel) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = { onModelSelected(model); modelExpanded = false }
                            )
                        }
                    }
                }
            } else {
                Text("Modelo: ${provider.effectiveModel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            val keyPreview = if (provider.apiKey.length > 8)
                provider.apiKey.take(4) + "••••" + provider.apiKey.takeLast(4)
            else "••••••••"
            Text("API Key: $keyPreview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun PasteableOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(!isPassword) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        visualTransformation = if (isPassword && !isVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            Row {
                IconButton(onClick = {
                    val clip = clipboardManager.getText()
                    if (clip != null && clip.text.isNotBlank()) {
                        onValueChange(clip.text)
                        Toast.makeText(context, "✓ Colado!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "✗ Clipboard vazio", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Colar", modifier = Modifier.size(20.dp))
                }
                if (isPassword) {
                    IconButton(onClick = { isVisible = !isVisible }) {
                        Icon(
                            if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isVisible) "Ocultar" else "Mostrar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderFormSheetPublic(
    title: String,
    initial: AIProvider? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String, key: String, models: List<String>, isOpenAICompatible: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var baseUrl by remember { mutableStateOf(initial?.baseUrl ?: "") }
    var apiKey by remember { mutableStateOf(initial?.apiKey ?: "") }
    var modelsText by remember { mutableStateOf(initial?.models?.joinToString(", ") ?: "") }
    var isOpenAICompatible by remember { mutableStateOf(initial?.isOpenAICompatible ?: true) }
    var suggestionExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)

            ExposedDropdownMenuBox(expanded = suggestionExpanded, onExpandedChange = { suggestionExpanded = it }) {
                OutlinedTextField(
                    value = "Preencher a partir de sugestão…",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = suggestionExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
                ExposedDropdownMenu(expanded = suggestionExpanded, onDismissRequest = { suggestionExpanded = false }) {
                    ProvidersConfig.SUGGESTIONS.forEach { suggestion ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(suggestion.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(suggestion.baseUrl, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            },
                            onClick = {
                                name = suggestion.name
                                baseUrl = suggestion.baseUrl
                                modelsText = suggestion.models.joinToString(", ")
                                isOpenAICompatible = suggestion.isOpenAICompatible
                                suggestionExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            PasteableOutlinedTextField(value = name, onValueChange = { name = it }, label = "Nome *", placeholder = "ex: Meu OpenAI")

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Compatível com OpenAI", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (isOpenAICompatible) "URL base + /chat/completions automático" else "Introduz o endpoint completo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(checked = isOpenAICompatible, onCheckedChange = { isOpenAICompatible = it })
            }

            PasteableOutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = if (isOpenAICompatible) "Base URL *" else "Endpoint completo *",
                placeholder = if (isOpenAICompatible) "https://api.openai.com/v1" else "https://api.openai.com/v1/chat/completions"
            )

            PasteableOutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = "API Key *", placeholder = "sk-…", isPassword = true)

            PasteableOutlinedTextField(
                value = modelsText,
                onValueChange = { modelsText = it },
                label = "Modelos *",
                placeholder = "gpt-4o, gpt-4o-mini",
                supportingText = "Separados por vírgula",
                singleLine = false,
                minLines = 2
            )

            val models = modelsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val isValid = name.isNotBlank() && baseUrl.isNotBlank() && apiKey.isNotBlank() && models.isNotEmpty()

            Button(
                onClick = { onSave(name.trim(), baseUrl.trim(), apiKey.trim(), models, isOpenAICompatible) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
