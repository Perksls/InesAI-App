package com.perksls.inesai.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    onImagePick: () -> Unit,
    onFilePick: () -> Unit,
    selectedImage: Bitmap?,
    onClearImage: () -> Unit,
    attachedFileName: String?,
    onClearFile: () -> Unit,
    isLoading: Boolean
) {
    var message by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Chips de anexos activos
            if (selectedImage != null || attachedFileName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedImage != null) {
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("📷 Imagem", style = MaterialTheme.typography.labelSmall)
                                IconButton(onClick = onClearImage, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                    if (attachedFileName != null) {
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("📄 $attachedFileName", style = MaterialTheme.typography.labelSmall)
                                IconButton(onClick = onClearFile, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão único de anexo com dropdown
                Box {
                    IconButton(
                        onClick = { showAttachMenu = true },
                        enabled = !isLoading,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Anexar",
                            modifier = Modifier.size(20.dp),
                            tint = if (selectedImage != null || attachedFileName != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showAttachMenu,
                        onDismissRequest = { showAttachMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("📷  Imagem") },
                            onClick = {
                                showAttachMenu = false
                                onImagePick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📄  Ficheiro (txt, pdf, zip…)") },
                            onClick = {
                                showAttachMenu = false
                                onFilePick()
                            }
                        )
                    }
                }

                // Campo de texto
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Mensagem...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    minLines = 1,
                    maxLines = 5,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Botão enviar
                val canSend = (message.isNotBlank() || selectedImage != null || attachedFileName != null) && !isLoading
                FloatingActionButton(
                    onClick = {
                        if (canSend) {
                            onSendMessage(message)
                            message = ""
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = if (canSend) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        if (isLoading) Icons.Default.HourglassEmpty else Icons.Default.Send,
                        contentDescription = "Enviar",
                        modifier = Modifier.size(20.dp),
                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
