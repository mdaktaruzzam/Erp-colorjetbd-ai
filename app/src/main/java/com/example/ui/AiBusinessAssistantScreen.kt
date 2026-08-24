package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiBusinessAssistantScreen(
    viewModel: InventoryViewModel,
    onClose: () -> Unit
) {
    var commandText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf<String?>(null) }
    
    val currentSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Business Assistant", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp).windowInsetsPadding(WindowInsets.ime)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commandText,
                        onValueChange = { commandText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask AI to create a draft (e.g., Create quotation for Digiprint...)") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (commandText.isNotBlank()) {
                                isProcessing = true
                                val prompt = """
                                    You are the COLORJET ERP AI Assistant. 
                                    The user commanded: "${commandText}"
                                    
                                    Create a business draft.
                                    Return a valid JSON object ONLY, with these fields:
                                    - intent: A short string (e.g. "Create Quotation")
                                    - module: (e.g. "Sales", "Service", "Report")
                                    - actionType: (e.g. "CREATE", "CALCULATE", "COMMUNICATE")
                                    - payloadJson: A JSON string representation of the draft data.
                                    - warnings: Any business rule warnings or high-risk notices.
                                    - isHighRisk: boolean
                                """.trimIndent()
                                scope.launch {
                                    val response = GeminiClient.askGemini(prompt)
                                    // Parse response to create a draft
                                    try {
                                        val jsonString = response.substringAfter("{").substringBeforeLast("}") + "}"
                                        val json = JSONObject("{${jsonString}}")
                                        val draft = AiDraft(
                                            draftNumber = "DRF-" + System.currentTimeMillis().toString().takeLast(6),
                                            command = commandText,
                                            intent = json.optString("intent", "Unknown Intent"),
                                            module = json.optString("module", "General"),
                                            actionType = json.optString("actionType", "CREATE"),
                                            createdBy = currentSession?.fullName ?: "Unknown",
                                            payloadJson = json.optString("payloadJson", "{}"),
                                            warnings = json.optString("warnings", ""),
                                            isHighRisk = json.optBoolean("isHighRisk", false)
                                        )
                                        viewModel.insertAiDraft(draft)
                                        aiResponse = "Draft created successfully! Check the Draft Center."
                                    } catch (e: Exception) {
                                        aiResponse = "Error parsing AI response: ${e.message}\nRaw: $response"
                                    }
                                    isProcessing = false
                                    commandText = ""
                                }
                            }
                        },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGray)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (aiResponse != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(aiResponse ?: "", color = TextPrimary)
                    }
                }
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("How can I help you today?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("I can create drafts for quotations, invoices, reports, stock transfers, and more.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = TextSecondary)
            }
        }
    }
}
