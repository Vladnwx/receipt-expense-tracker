package com.vladnwx.receiptexpensetracker.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vladnwx.receiptexpensetracker.data.local.entity.CategoryEntity

private val catColors = listOf(
    0xFF1B5E20.toInt(), 0xFF2E7D32.toInt(), 0xFF388E3C.toInt(), 0xFF43A047.toInt(),
    0xFF1565C0.toInt(), 0xFF1976D2.toInt(), 0xFF1E88E5.toInt(), 0xFF42A5F5.toInt(),
    0xFFE65100.toInt(), 0xFFEF6C00.toInt(), 0xFFF57C00.toInt(), 0xFFFB8C00.toInt(),
    0xFF6A1B9A.toInt(), 0xFF7B1FA2.toInt(), 0xFF8E24AA.toInt(), 0xFFAB47BC.toInt(),
    0xFFC62828.toInt(), 0xFFD32F2F.toInt(), 0xFFE53935.toInt(), 0xFFEF5350.toInt(),
    0xFF37474F.toInt(), 0xFF546E7A.toInt(), 0xFF78909C.toInt(), 0xFF90A4AE.toInt(),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerGrid(selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        catColors.forEach { color ->
            val isSelected = color == selected
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .then(
                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(color) }
            )
        }
    }
}

@Composable
fun CategoriesScreen(viewModel: CategoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить категорию")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            state.parents.forEach { parent ->
                item(key = "parent_${parent.id}") {
                    ParentCategoryCard(
                        category = parent,
                        onClick = { viewModel.showEditDialog(parent) },
                        onDelete = { viewModel.delete(parent) }
                    )
                }
                val children = state.children[parent.id] ?: emptyList()
                items(children, key = { "child_${it.id}" }) { child ->
                    ChildCategoryCard(
                        category = child,
                        onClick = { viewModel.showEditDialog(child) },
                        onDelete = { viewModel.delete(child) }
                    )
                }
            }
        }
    }

    if (state.showDialog) {
        CategoryDialog(
            editing = state.editingCategory,
            parentOptions = state.parents,
            onDismiss = { viewModel.dismissDialog() },
            onSave = { name, parentId, color, family ->
                viewModel.save(name, parentId, color, family)
            }
        )
    }
}

@Composable
private fun ParentCategoryCard(category: CategoryEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(category.color ?: 0))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (category.isFamilyDefault) {
                Text(
                    text = "Семейный",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ChildCategoryCard(category: CategoryEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                    .background(Color(category.color ?: 0))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить", modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    editing: CategoryEntity?,
    parentOptions: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Long?, Int, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var selectedParentId by remember { mutableStateOf(editing?.parentId) }
    var selectedColor by remember { mutableStateOf(editing?.color ?: catColors[0]) }
    var isFamily by remember { mutableStateOf(editing?.isFamilyDefault ?: false) }
    var parentExpanded by remember { mutableStateOf(false) }

    val isEditing = editing != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Редактировать категорию" else "Новая категория") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = parentExpanded,
                    onExpandedChange = { parentExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedParentId == null) "Корневая категория"
                                else parentOptions.find { it.id == selectedParentId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Родительская категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = parentExpanded,
                        onDismissRequest = { parentExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Корневая категория") },
                            onClick = {
                                selectedParentId = null
                                parentExpanded = false
                            }
                        )
                        parentOptions.forEach { parent ->
                            DropdownMenuItem(
                                text = { Text(parent.name) },
                                onClick = {
                                    selectedParentId = parent.id
                                    parentExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Цвет", style = MaterialTheme.typography.labelLarge)
                ColorPickerGrid(selected = selectedColor) { selectedColor = it }

                if (selectedParentId == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Семейный по умолчанию", modifier = Modifier.weight(1f))
                        Switch(checked = isFamily, onCheckedChange = { isFamily = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedParentId, selectedColor, isFamily) },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Сохранить" else "Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
