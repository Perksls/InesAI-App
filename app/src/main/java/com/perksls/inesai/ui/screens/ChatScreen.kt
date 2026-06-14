package com.perksls.inesai.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perksls.inesai.ui.components.ChatMessageItem
import com.perksls.inesai.ui.components.ConversationDrawer
import com.perksls.inesai.ui.components.MessageInput
import com.perksls.inesai.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToProviders: () -> Unit,
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val messages by remember { derivedStateOf { viewModel.messages } }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val estimatedTokens by viewModel.estimatedTokens.collectAsStateWithLifecycle()
    val currentProvider by viewModel.currentProvider.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()
    val fallbackActive by viewModel.fallbackActive.collectAsStateWithLifecycle()
    val providerStatus by viewModel.providerStatus.collectAsStateWithLifecycle()
    val currentImage by viewModel.currentImage.collectAsStateWithLifecycle()
    val attachedFileName by viewModel.attachedFileName.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val currentConversationId by viewModel.currentConversationId.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            bitmap?.let { bmp -> viewModel.setImage(bmp) }
            inputStream?.close()
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                if (nameIndex >= 0) cursor.getString(nameIndex) else "ficheiro"
            } ?: "ficheiro"
            val content = try {
                context.contentResolver.openInputStream(it)?.bufferedReader()?.readText() ?: ""
            } catch (e: Exception) {
                "[Ficheiro binário: $fileName]"
            }
            viewModel.setAttachedFile(fileName, content)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            // Pequeno delay para garantir que o item foi renderizado antes do scroll
            kotlinx.coroutines.delay(50)
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationDrawer(
                conversations = conversations,
                currentConversationId = currentConversationId,
                onConversationClick = { id ->
                    viewModel.loadConversation(id)
                    scope.launch { drawerState.close() }
                },
                onNewConversation = {
                    viewModel.createNewConversation()
                    scope.launch { drawerState.close() }
                },
                onDeleteConversation = { id ->
                    viewModel.deleteConversation(id)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("InesAI")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currentProvider,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                AnimatedVisibility(
                                    visible = fallbackActive,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            "🔄 Fallback",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.regenerateLastResponse() },
                            enabled = !isLoading && messages.any {
                                it.role == com.perksls.inesai.data.model.MessageRole.ASSISTANT
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerar")
                        }
                        IconButton(onClick = onNavigateToProviders) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Providers")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Definições")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            // Layout em Column com weight — a única forma correta para chat
            // O messages ocupa o espaço disponível e o input fica sempre visível
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Área de mensagens — ocupa todo o espaço restante
                Box(modifier = Modifier.weight(1f)) {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("🤖", style = MaterialTheme.typography.displayLarge)
                                Text("InesAI", style = MaterialTheme.typography.headlineMedium)
                                Text(
                                    "Multi-provider AI Chat",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "📷 Suporta imagens  •  💾 Conversas guardadas localmente",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                Button(onClick = { viewModel.createNewConversation() }) {
                                    Text("Nova Conversa")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(items = messages, key = { it.id }) { message ->
                                ChatMessageItem(message = message, isDarkTheme = isDarkTheme)
                            }
                        }
                    }
                }

                // Status dos providers (fallback)
                AnimatedVisibility(
                    visible = isLoading && providerStatus.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        tonalElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            providerStatus.forEach { (name, status) ->
                                val color = when {
                                    status.startsWith("✅") -> MaterialTheme.colorScheme.primary
                                    status.startsWith("❌") -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.outline
                                }
                                Surface(
                                    color = color.copy(alpha = 0.15f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "$name $status",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }

                // Token counter
                Surface(tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            "~$estimatedTokens tokens",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Input — sempre visível, nunca escondido pelo teclado
                MessageInput(
                    onSendMessage = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.sendMessage(it)
                    },
                    onImagePick = { imagePicker.launch("image/*") },
                    onFilePick = { filePicker.launch("*/*") },
                    selectedImage = currentImage,
                    onClearImage = { viewModel.clearImage() },
                    attachedFileName = attachedFileName,
                    onClearFile = { viewModel.clearAttachedFile() },
                    isLoading = isLoading
                )
            }
        }
    }
}
