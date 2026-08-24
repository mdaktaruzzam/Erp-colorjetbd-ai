package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: InventoryViewModel) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskForDetail by remember { mutableStateOf<OfficeTask?>(null) }

    var statusFilter by remember { mutableStateOf("ALL") }
    var priorityFilter by remember { mutableStateOf("ALL") }

    val filteredTasks = allTasks.filter { task ->
        val matchesStatus = when (statusFilter) {
            "PENDING" -> task.status == "PENDING"
            "IN_PROGRESS" -> task.status == "IN_PROGRESS"
            "COMPLETED" -> task.status == "COMPLETED"
            else -> true
        }
        val matchesPriority = when (priorityFilter) {
            "HIGH" -> task.priority == "HIGH"
            "MEDIUM" -> task.priority == "MEDIUM"
            "LOW" -> task.priority == "LOW"
            else -> true
        }
        matchesStatus && matchesPriority
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "OFFICE TASKS",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                actions = {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
        ) {
            // Filters section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status filter
                FilterChip(
                    selected = statusFilter == "ALL",
                    onClick = { statusFilter = "ALL" },
                    label = { Text("All Status") }
                )
                FilterChip(
                    selected = statusFilter == "PENDING",
                    onClick = { statusFilter = "PENDING" },
                    label = { Text("Pending") }
                )
                FilterChip(
                    selected = statusFilter == "IN_PROGRESS",
                    onClick = { statusFilter = "IN_PROGRESS" },
                    label = { Text("In Progress") }
                )
                FilterChip(
                    selected = statusFilter == "COMPLETED",
                    onClick = { statusFilter = "COMPLETED" },
                    label = { Text("Completed") }
                )

                VerticalDivider(modifier = Modifier.height(32.dp).padding(horizontal = 4.dp))

                // Priority filter
                FilterChip(
                    selected = priorityFilter == "ALL",
                    onClick = { priorityFilter = "ALL" },
                    label = { Text("All Priority") }
                )
                FilterChip(
                    selected = priorityFilter == "HIGH",
                    onClick = { priorityFilter = "HIGH" },
                    label = { Text("High") }
                )
                FilterChip(
                    selected = priorityFilter == "MEDIUM",
                    onClick = { priorityFilter = "MEDIUM" },
                    label = { Text("Medium") }
                )
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No office tasks match selected criteria.", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskRow(
                            task = task,
                            onToggleStatus = {
                                val nextStatus = if (task.status == "COMPLETED") "PENDING" else "COMPLETED"
                                viewModel.updateOfficeTaskStatus(task, nextStatus)
                            },
                            onViewDetail = { selectedTaskForDetail = task }
                        )
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            AddTaskDialog(
                users = allUsers,
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { title, desc, userId, userName, priority, dueDays ->
                    val dueDate = System.currentTimeMillis() + (dueDays * 86400000L)
                    viewModel.addOfficeTask(title, desc, userId, userName, priority, dueDate)
                    showAddTaskDialog = false
                }
            )
        }

        // Detail Dialog
        if (selectedTaskForDetail != null) {
            val task = selectedTaskForDetail!!
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            AlertDialog(
                onDismissRequest = { selectedTaskForDetail = null },
                confirmButton = {
                    TextButton(onClick = { selectedTaskForDetail = null }) {
                        Text("CLOSE")
                    }
                },
                title = {
                    Text(task.title, fontWeight = FontWeight.Black, color = PrimaryBlue)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(task.description, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Assigned To:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                            Text(task.assignedToName ?: "Unassigned", fontSize = 12.sp, color = TextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Due Date:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                            Text(sdf.format(Date(task.dueDate)), fontSize = 12.sp, color = TextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Priority:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                            Text(task.priority, fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (task.priority == "HIGH") Color.Red else AccentOrange)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                            Text(task.status, fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (task.status == "COMPLETED") SuccessGreen else Color.Gray)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TaskRow(
    task: OfficeTask,
    onToggleStatus: () -> Unit,
    onViewDetail: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onViewDetail() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task status Checkbox
            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Status",
                    tint = if (isCompleted) SuccessGreen else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCompleted) Color.Gray else TextPrimary,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (task.priority) {
                                    "HIGH" -> Color.Red.copy(alpha = 0.1f)
                                    "MEDIUM" -> AccentOrange.copy(alpha = 0.1f)
                                    else -> Color.Gray.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority,
                            color = when (task.priority) {
                                "HIGH" -> Color.Red
                                "MEDIUM" -> AccentOrange
                                else -> Color.Gray
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "Due: ${sdf.format(Date(task.dueDate))}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = "• Assignee: ${task.assignedToName ?: "None"}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    users: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, userId: Int?, userName: String?, priority: String, dueDays: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var dueDays by remember { mutableStateOf("2") }

    var expandedUserDropdown by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Office Task", fontWeight = FontWeight.Black, color = PrimaryBlue) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Task Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Employee dropdown selector
                ExposedDropdownMenuBox(
                    expanded = expandedUserDropdown,
                    onExpandedChange = { expandedUserDropdown = !expandedUserDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedUser?.fullName ?: "Select Employee...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned To") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUserDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUserDropdown,
                        onDismissRequest = { expandedUserDropdown = false }
                    ) {
                        users.forEach { u ->
                            DropdownMenuItem(
                                text = { Text("${u.fullName} (${u.role})") },
                                onClick = {
                                    selectedUser = u
                                    expandedUserDropdown = false
                                }
                            )
                        }
                    }
                }

                // Priority Selection
                Column {
                    Text("Task Priority", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            val isSelected = priority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f))
                                    .clickable { priority = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, color = if (isSelected) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Due in Days
                OutlinedTextField(
                    value = dueDays,
                    onValueChange = { dueDays = it.filter { char -> char.isDigit() } },
                    label = { Text("Due in (Days)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            desc.trim(),
                            selectedUser?.id,
                            selectedUser?.fullName,
                            priority,
                            dueDays.toIntOrNull() ?: 2
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("CREATE TASK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
