package com.perksls.inesai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perksls.inesai.data.model.AIProvider
import com.perksls.inesai.ui.viewmodel.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderOrderScreen(
    viewModel: ProviderViewModel,
    onNavigateBack: () -> Unit
) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    // Lista local mutável para o drag — sincroniza com a BD ao largar
    var list by remember(providers) { mutableStateOf(providers.toMutableList()) }
    var draggedIdx by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ordem de Fallback")
                        Text(
                            "Mantém premido e arrasta para reordenar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
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
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "O 1º provider é o principal. Em caso de falha, tenta os seguintes por ordem.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(list, key = { _, p -> p.id }) { index, provider ->
                    DraggableProviderItem(
                        provider = provider,
                        index = index,
                        isDragging = draggedIdx == index,
                        onDragStart = { draggedIdx = index },
                        onDrag = { deltaY ->
                            val fromIdx = draggedIdx ?: return@DraggableProviderItem
                            // Altura aproximada de cada item em px (72dp)
                            val itemHeight = 80
                            val moved = (deltaY / itemHeight).toInt()
                            if (moved != 0) {
                                val toIdx = (fromIdx + moved).coerceIn(0, list.lastIndex)
                                if (toIdx != fromIdx) {
                                    val newList = list.toMutableList()
                                    val item = newList.removeAt(fromIdx)
                                    newList.add(toIdx, item)
                                    list = newList
                                    draggedIdx = toIdx
                                }
                            }
                        },
                        onDragEnd = {
                            draggedIdx = null
                            viewModel.reorderProviders(list.map { it.id })
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableProviderItem(
    provider: AIProvider,
    index: Int,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    var cumulativeDrag by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer { alpha = if (isDragging) 0.85f else 1f }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        cumulativeDrag = 0f
                        onDragStart()
                    },
                    onDrag = { _, dragAmount ->
                        cumulativeDrag += dragAmount.y
                        onDrag(cumulativeDrag)
                    },
                    onDragEnd = {
                        cumulativeDrag = 0f
                        onDragEnd()
                    },
                    onDragCancel = {
                        cumulativeDrag = 0f
                        onDragEnd()
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.primaryContainer
            else if (index == 0)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (index == 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (index == 0) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(provider.name, style = MaterialTheme.typography.titleSmall)
                    if (index == 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "Principal",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                Text(
                    provider.effectiveModel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Arrastar",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        }
    }
}
