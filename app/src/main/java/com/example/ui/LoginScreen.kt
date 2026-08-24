package com.example.ui
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.aistudio.colorjeterp.pxlmjq.R

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showServerSettings by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("cj-md-001") }
    var pinCode by remember { mutableStateOf("1234") }
    var errorMessage by remember { mutableStateOf("") }

    val quickAccessAccounts = listOf(
        Triple("Aktaruzzaman (MD)", "cj-md-001", "1234"),
        Triple("Al-amin (PARTNER)", "cj-mgmt-002", "1234"),
        Triple("Atiq Faisal (OPS)", "cj-mgmt-006", "1234"),
        Triple("Zahed (ENG)", "cj-tech-007", "1234")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enterprise Logo & Branding
            Image(
                painter = painterResource(id = R.drawable.colorjet_logo_text),
                contentDescription = "COLORJET Logo",
                modifier = Modifier.width(200.dp).height(60.dp),
                contentScale = ContentScale.Fit
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Management Suite",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = DeepNavy
                )
                Text(
                    text = "BANGLADESH LOGISTICS PORTAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 4.dp))

            // Form Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Username",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            username = it
                            errorMessage = "" 
                        },
                        placeholder = { Text("Enter enterprise username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = com.example.ui.theme.colorJetTextFieldColors()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Security Passcode / Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { 
                            if (it.length <= 32) {
                                pinCode = it
                                errorMessage = ""
                            }
                        },
                        placeholder = { Text("Enter passcode") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_pincode_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = com.example.ui.theme.colorJetTextFieldColors()
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Submit / Login Button
            Button(
                onClick = {
                    if (username.isBlank() || pinCode.isBlank()) {
                        errorMessage = "Please enter both fields."
                    } else {
                        viewModel.login(username, pinCode) { success, message ->
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            } else {
                                errorMessage = message
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "AUTHENTICATE SESSION",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Quick Bypass Selector for Testing Roles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BackgroundGray)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DEMO ACCOUNTS (TAP TO AUTOFILL)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAccessAccounts.take(2).forEach { (label, u, p) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable {
                                    username = u
                                    pinCode = p
                                    errorMessage = ""
                                    Toast.makeText(context, "Autofilled: $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAccessAccounts.drop(2).forEach { (label, u, p) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable {
                                    username = u
                                    pinCode = p
                                    errorMessage = ""
                                    Toast.makeText(context, "Autofilled: $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // VPS Connection Config Button
            OutlinedButton(
                onClick = { showServerSettings = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                    Text("VPS SERVER SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        }
    }

    // VPS Server Settings Dialog
    if (showServerSettings) {
        var serverModeState by remember { mutableStateOf(com.example.data.VpsService.getServerMode(context)) }
        var serverUrlInput by remember { mutableStateOf(com.example.data.VpsService.getServerUrl(context)) }
        var serverTokenInput by remember { mutableStateOf(com.example.data.VpsService.getServerToken(context)) }
        var isTestingConnection by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showServerSettings = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = PrimaryBlue)
                    Text("VPS SERVER SETTINGS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Configure the app's bi-directional replication target to synchronize with your custom VPS backend.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    // Environment Toggle
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Server Environment", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("QA", "PRODUCTION").forEach { mode ->
                                val isSelected = (mode == "QA" && serverModeState == "QA") || (mode == "PRODUCTION" && serverModeState == "PROD")
                                Button(
                                    onClick = {
                                        serverModeState = if (mode == "QA") "QA" else "PROD"
                                        // Reset default URLs to make it user-friendly
                                        serverUrlInput = if (mode == "QA") {
                                            "https://ais-dev-bmklslqqqnkhqj75qrfrau-160697771856.asia-southeast1.run.app/backend/"
                                        } else {
                                            "http://YOUR_VPS_IP_OR_DOMAIN/backend/"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) PrimaryBlue else BackgroundGray,
                                        contentColor = if (isSelected) Color.White else TextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(mode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // URL Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("API Base URL (containing api.php)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            placeholder = { Text("http://123.456.78.9/backend/") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Token Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Security Token (X-ColorJet-Token)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = serverTokenInput,
                            onValueChange = { serverTokenInput = it },
                            placeholder = { Text("Enter secret token") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Test Connection Button
                    Button(
                        onClick = {
                            isTestingConnection = true
                            // Save temporary settings to test connection
                            com.example.data.VpsService.saveServerSettings(context, serverModeState, serverUrlInput, serverTokenInput)
                            coroutineScope.launch {
                                val result = com.example.data.VpsService.testConnection(context)
                                isTestingConnection = false
                                val status = result.optString("status", "error")
                                val message = result.optString("message", "Connection failed")
                                if (status == "success") {
                                    Toast.makeText(context, "✅ SUCCESS: $message", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "❌ FAILED: $message", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isTestingConnection,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isTestingConnection) "TESTING..." else "TEST VPS CONNECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        com.example.data.VpsService.saveServerSettings(context, serverModeState, serverUrlInput, serverTokenInput)
                        Toast.makeText(context, "VPS Settings Saved Successfully!", Toast.LENGTH_SHORT).show()
                        showServerSettings = false
                    }
                ) {
                    Text("SAVE & CLOSE", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerSettings = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}
