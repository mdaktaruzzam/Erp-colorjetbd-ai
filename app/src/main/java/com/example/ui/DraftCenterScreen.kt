package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftCenterScreen(
    viewModel: InventoryViewModel,
    onClose: () -> Unit
) {
    val drafts by viewModel.allAiDrafts.collectAsStateWithLifecycle()
    var selectedDraft by remember { mutableStateOf<AiDraft?>(null) }
    
    val tabs = listOf("All Drafts", "Waiting for Approval", "Published")
    var selectedTab by remember { mutableStateOf(tabs[0]) }

    val filteredDrafts = drafts.filter { draft ->
        when (selectedTab) {
            "Waiting for Approval" -> draft.approvalStatus == "Waiting for Approval"
            "Published" -> draft.publishStatus == "Published"
            else -> true
        }
    }

    if (selectedDraft != null) {
        DraftPreviewScreen(
            draft = selectedDraft!!,
            viewModel = viewModel,
            onClose = { selectedDraft = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AI Draft Center", fontWeight = FontWeight.Bold, color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(BackgroundGray)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(selectedTab),
                    containerColor = Color.White,
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabs.indexOf(selectedTab) == index,
                            onClick = { selectedTab = title },
                            text = { Text(title, fontWeight = FontWeight.Bold) },
                            selectedContentColor = PrimaryBlue,
                            unselectedContentColor = TextSecondary
                        )
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDrafts) { draft ->
                        DraftCard(draft = draft, onClick = { selectedDraft = draft })
                    }
                }
            }
        }
    }
}

@Composable
fun DraftCard(draft: AiDraft, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(draft.draftNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                val statusColor = when (draft.approvalStatus) {
                    "Approved" -> SuccessGreen
                    "Rejected" -> Color.Red
                    "Waiting for Approval" -> AccentOrange
                    else -> TextSecondary
                }
                Text(draft.approvalStatus, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = statusColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(draft.intent, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Module: ${draft.module} | Action: ${draft.actionType}", fontSize = 12.sp, color = TextSecondary)
            if (draft.isHighRisk) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("High Risk Action", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftPreviewScreen(
    draft: AiDraft,
    viewModel: InventoryViewModel,
    onClose: () -> Unit
) {
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val isOwner = currentSession?.role == "OWNER"
    var showPublishDialog by remember { mutableStateOf(false) }
    
    val prettyJson = try {
        JSONObject(draft.payloadJson).toString(4)
    } catch (e: Exception) {
        draft.payloadJson
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(draft.draftNumber, fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (draft.publishStatus != "Published") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).windowInsetsPadding(WindowInsets.navigationBars),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateAiDraft(draft.copy(approvalStatus = "Cancelled"))
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.updateAiDraft(draft.copy(approvalStatus = "Waiting for Approval"))
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = TextPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Draft")
                    }
                    if (isOwner) {
                        Button(
                            onClick = { showPublishDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Publish")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundGray),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Intent", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 12.sp)
                        Text(draft.intent, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Command", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 12.sp)
                        Text(draft.command, fontSize = 14.sp, color = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (draft.warnings.isNotBlank()) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AccentOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(draft.warnings, color = Color(0xFF92400E))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Draft Payload", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(prettyJson, fontSize = 12.sp, color = TextPrimary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }
        }
        
        if (showPublishDialog) {
            var pin by remember { mutableStateOf("") }
            var pinError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showPublishDialog = false },
                title = { Text(if (draft.isHighRisk) "High Risk Action" else "Publish Action") },
                text = {
                    Column {
                        Text("Are you sure you want to execute this AI draft? This will impact the real system records.")
                        if (draft.isHighRisk) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("This is a HIGH RISK action. Please enter your PIN to confirm.", fontWeight = FontWeight.Bold, color = Color.Red)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { 
                                    pin = it
                                    pinError = false
                                },
                                label = { Text("Enter PIN (1234)") },
                                isError = pinError,
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (pinError) {
                                Text("Invalid PIN", color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (draft.isHighRisk && pin != "1234") {
                                pinError = true
                                return@Button
                            }
                            // Execute action based on intent here...
                            viewModel.updateAiDraft(draft.copy(publishStatus = "Published", approvalStatus = "Approved"))
                            showPublishDialog = false
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (draft.isHighRisk) Color.Red else PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm Publish")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPublishDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
