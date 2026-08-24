package com.example.ui
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.aistudio.colorjeterp.pxlmjq.R

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerMainAppScreen(viewModel: InventoryViewModel) {
    val currentUserSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val customerId = currentUserSession?.customerId ?: 1 // Fallback

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                CustomerNavigationSidebar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onLogoutClick = { viewModel.logout() }
                )
                VerticalDivider(color = BackgroundGray, modifier = Modifier.width(1.dp).fillMaxHeight())
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    CustomerNavigationContent(
                        selectedTab = selectedTab,
                        viewModel = viewModel
                    )
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = BackgroundGray,
                topBar = {
                    CustomerTopNavigationBar(
                        customerName = currentUserSession?.fullName ?: "Customer",
                        onLogoutClick = { viewModel.logout() }
                    )
                },
                bottomBar = {
                    CustomerBottomNavigationBar(selectedTab) { selectedTab = it }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CustomerNavigationContent(
                        selectedTab = selectedTab,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerNavigationSidebar(selectedTab: Int, onTabSelected: (Int) -> Unit, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color.White)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.colorjet_logo),
                contentDescription = "COLORJET Logo",
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit
            )
            Column {
                Text("COLORJET ERP", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryBlue)
                Text("Customer Portal", fontSize = 10.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(color = BackgroundGray, modifier = Modifier.padding(vertical = 16.dp))
        val menuItems = listOf(
            CustomerNavItem("Home Dashboard", Icons.Default.Home, 0, "side_home"),
            CustomerNavItem("My Machines", Icons.Default.PrecisionManufacturing, 1, "side_machines"),
            CustomerNavItem("Support Service", Icons.Default.Build, 2, "side_service"),
            CustomerNavItem("Orders & Invoices", Icons.Default.ShoppingCart, 3, "side_orders"),
            CustomerNavItem("My Ledger", Icons.AutoMirrored.Filled.ListAlt, 4, "side_ledger")
        )
        menuItems.forEach { item ->
            val isSelected = selectedTab == item.index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { onTabSelected(item.index) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(item.icon, contentDescription = null, tint = if (isSelected) PrimaryBlue else TextSecondary)
                Text(item.label, color = if (isSelected) PrimaryBlue else TextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
fun CustomerTopNavigationBar(customerName: String, onLogoutClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(
                painter = painterResource(id = R.drawable.colorjet_logo),
                contentDescription = "COLORJET Logo",
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
            Column {
                Text("COLORJET ERP", fontWeight = FontWeight.Black, fontSize = 14.sp, color = PrimaryBlue)
                Text("Customer Portal", fontSize = 9.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }
        IconButton(onClick = onLogoutClick) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
        }
    }
}

@Composable
fun CustomerBottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        val items = listOf(
            CustomerNavItem("Home", Icons.Default.Home, 0, "bot_home"),
            CustomerNavItem("Machines", Icons.Default.PrecisionManufacturing, 1, "bot_machines"),
            CustomerNavItem("Service", Icons.Default.Build, 2, "bot_service"),
            CustomerNavItem("Orders", Icons.Default.ShoppingCart, 3, "bot_orders"),
            CustomerNavItem("Ledger", Icons.AutoMirrored.Filled.ListAlt, 4, "bot_ledger")
        )
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.index,
                onClick = { onTabSelected(item.index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = PrimaryBlue,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class CustomerNavItem(val label: String, val icon: ImageVector, val index: Int, val testTag: String)

@Composable
fun CustomerNavigationContent(selectedTab: Int, viewModel: InventoryViewModel) {
    when (selectedTab) {
        0 -> CustomerHomeTab(viewModel)
        1 -> CustomerMachinesTab(viewModel)
        2 -> CustomerServiceTab(viewModel)
        3 -> CustomerOrdersTab(viewModel)
        4 -> CustomerLedgerTab(viewModel)
    }
}

@Composable
fun CustomerHomeTab(viewModel: InventoryViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home Tab - To Be Restored", color = TextSecondary)
    }
}

@Composable
fun CustomerMachinesTab(viewModel: InventoryViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Machines Tab - To Be Restored", color = TextSecondary)
    }
}

@Composable
fun CustomerServiceTab(viewModel: InventoryViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Service Tab - To Be Restored", color = TextSecondary)
    }
}

@Composable
fun CustomerOrdersTab(viewModel: InventoryViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Orders Tab - To Be Restored", color = TextSecondary)
    }
}

@Composable
fun CustomerLedgerTab(viewModel: InventoryViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Ledger Tab - To Be Restored", color = TextSecondary)
    }
}
