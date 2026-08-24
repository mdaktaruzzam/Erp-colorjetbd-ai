package com.example
import com.aistudio.colorjeterp.pxlmjq.R
import androidx.compose.foundation.Image
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.LoginScreen
import com.example.ui.DashboardScreen
import com.example.ui.TasksScreen
import com.example.ui.ServiceScreen
import com.example.ui.MoreScreen
import com.example.ui.CustomerMainAppScreen
import com.example.ui.AiBusinessAssistantScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    
    // Initialize Firebase Services gracefully
    
    enableEdgeToEdge()
    setContent {
      ColorJetTheme {
        MainAppScreen()
      }
    }
  }
}

@Composable
fun MainAppScreen() {
  val inventoryViewModel: InventoryViewModel = viewModel()
  val currentUserSession by inventoryViewModel.currentUserSession.collectAsStateWithLifecycle()
  val isGlobalLoading by inventoryViewModel.isGlobalLoading.collectAsStateWithLifecycle()
  val globalLoadingMessage by inventoryViewModel.globalLoadingMessage.collectAsStateWithLifecycle()

  var versionConfig by remember { mutableStateOf<AppVersionConfig?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }
  var isCriticalUpdate by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val config = AppVersionChecker.fetchVersionConfig()
    if (config != null && AppVersionChecker.isUpdateAvailable(config)) {
      versionConfig = config
      isCriticalUpdate = AppVersionChecker.isCriticalUpdate(config)
      showUpdateDialog = true
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (currentUserSession == null) {
      LoginScreen(viewModel = inventoryViewModel)
    } else if (currentUserSession?.role == "CUSTOMER") {
      CustomerMainAppScreen(viewModel = inventoryViewModel)
    } else {
      var selectedTab by remember { mutableIntStateOf(0) }
      var showAiAssistant by remember { mutableStateOf(false) }

      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        if (isWideScreen) {
          Row(modifier = Modifier.fillMaxSize()) {
            NavigationSidebar(
              selectedTab = selectedTab,
              onTabSelected = { selectedTab = it },
              onLogoutClick = { inventoryViewModel.logout() }
            )
            VerticalDivider(color = BackgroundGray, modifier = Modifier.width(1.dp).fillMaxHeight())
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
              NavigationContent(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                viewModel = inventoryViewModel
              )
            }
          }
        } else {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BackgroundGray,
            topBar = {
              TopNavigationBar(
                ownerName = currentUserSession?.fullName ?: "User",
                onProfileClick = { /* Profile Click */ }
              )
            },
            bottomBar = {
              BottomNavigationBar(selectedTab) { selectedTab = it }
            },
            floatingActionButton = {
              FloatingActionButton(
                onClick = { showAiAssistant = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
              }
            }
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              NavigationContent(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                viewModel = inventoryViewModel
              )
            }
          }
        }
      }
      
      if (showAiAssistant) {
        AiBusinessAssistantScreen(
          viewModel = inventoryViewModel,
          onClose = { showAiAssistant = false }
        )
      }
    }

    // Global Loader Overlay with elegant Material 3 style and corporate COLORJET Branding
    if (isGlobalLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.5f))
          .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
          ),
          modifier = Modifier
            .padding(28.dp)
            .widthIn(max = 320.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator(
              color = AccentOrange,
              strokeWidth = 4.dp,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
              text = globalLoadingMessage,
              color = TextPrimary,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Please keep the connection active",
              color = TextSecondary,
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    if (showUpdateDialog && versionConfig != null) {
      AppUpdateDialog(
        config = versionConfig!!,
        isCritical = isCriticalUpdate,
        onDismiss = { showUpdateDialog = false }
      )
    }
  }
}

@Composable
fun TopNavigationBar(ownerName: String, onProfileClick: () -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(PrimaryBlue, DeepNavy)
          )
        )
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .windowInsetsPadding(WindowInsets.statusBars),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Branding
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(id = R.drawable.colorjet_logo),
          contentDescription = "COLORJET Logo",
          modifier = Modifier.size(36.dp),
          contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text("COLORJET", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)
          Text("BANGLADESH ERP", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp)
        }
      }

      // Profile Section
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { /* Notifications */ }) {
          Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White.copy(alpha = 0.9f))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AccentOrange)
            .clickable { onProfileClick() },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = ownerName.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      }
    }
    // Orange accent line
    HorizontalDivider(color = AccentOrange, thickness = 3.dp)
  }
}





@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
  val searchTerms by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val supplies by viewModel.filteredSupplies.collectAsStateWithLifecycle()

  var showAddDialog by remember { mutableStateOf(false) }
  var selectedItemForDetail by remember { mutableStateOf<SupplyItem?>(null) }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
          .background(PrimaryBlue)
          .padding(horizontal = 20.dp, vertical = 24.dp)
          .padding(top = 16.dp)
      ) {
        Text(
          text = "Inventory Management",
          color = Color.White,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Black,
          letterSpacing = (-0.5).sp,
          fontSize = 24.sp
        )
        Text(
          text = "INDUSTRIAL SUPPLIES & LOGISTICS",
          color = Color.White.copy(alpha = 0.7f),
          style = MaterialTheme.typography.labelSmall,
          letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        TextField(
          value = searchTerms,
          onValueChange = { viewModel.searchQuery.value = it },
          modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .testTag("inventory_search_input"),
          placeholder = { Text("Search supply name or SKU...", color = Color.Gray, fontSize = 14.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlue) },
          trailingIcon = {
            if (searchTerms.isNotEmpty()) {
              IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
              }
            }
          },
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(16.dp),
          singleLine = true
        )
      }

      // Categories horizontal scroll list
      val categories = listOf("All", "Low Stock", "Inks", "Spare Parts", "Consumables", "DTF Supplies")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp)
          .padding(horizontal = 16.dp)
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          val isSelected = selectedCat == cat
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.selectedCategory.value = cat },
            label = {
              Text(
                text = cat.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentOrange,
              selectedLabelColor = Color.White,
              containerColor = Color.White,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              selectedBorderColor = AccentOrange,
              borderColor = Color.LightGray.copy(alpha = 0.5f)
            )
          )
        }
      }

      // List of items
      if (supplies.isEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "No supplies found",
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 18.sp
          )
          Text(
            text = "Try adjusting your search terms or filters.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (selectedCat == "Low Stock") {
            item {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(16.dp)
              ) {
                Row(
                  modifier = Modifier.padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = "THRESHOLD ALERTS DETECTED",
                      fontWeight = FontWeight.Black,
                      fontSize = 12.sp,
                      color = Color.Red,
                      letterSpacing = 0.5.sp
                    )
                    Text(
                      text = "The supplies below have stock levels at or below their critical limits.",
                      fontSize = 11.sp,
                      color = Color.Black.copy(alpha = 0.7f)
                    )
                  }
                }
              }
            }
          }

          items(supplies, key = { it.id }) { item ->
            SupplyItemRow(
              item = item,
              onItemClick = { selectedItemForDetail = item },
              onIncrement = { viewModel.updateStock(item, item.stockLevel + 1) },
              onDecrement = { viewModel.updateStock(item, item.stockLevel - 1) }
            )
          }
        }
      }
    }

    // Floating Action Button
    if (viewModel.canAddSupply()) {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = AccentOrange,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(24.dp)
          .testTag("add_supply_fab"),
        shape = RoundedCornerShape(16.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add supply item", modifier = Modifier.size(28.dp))
      }
    }
  }

  // Add dialog
  if (showAddDialog) {
    AddSupplyDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { sku, name, category, stock, limit, loc, unit ->
        viewModel.addSupplyItem(sku, name, category, stock, limit, loc, unit)
        showAddDialog = false
      }
    )
  }

  // Detail & Edit dialog
  selectedItemForDetail?.let { item ->
    DetailSupplyDialog(
      item = item,
      viewModel = viewModel,
      onDismiss = { selectedItemForDetail = null },
      onUpdate = { updatedItem ->
        viewModel.updateSupplyItem(updatedItem)
        selectedItemForDetail = null
      },
      onDelete = { itemToDelete ->
        viewModel.deleteSupplyItem(itemToDelete)
        selectedItemForDetail = null
      }
    )
  }
}

@Composable
fun SupplyItemRow(
  item: SupplyItem,
  onItemClick: () -> Unit,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit
) {
  val isLowStock = item.stockLevel <= item.threshold

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(2.dp, RoundedCornerShape(24.dp))
      .clickable { onItemClick() }
      .testTag("supply_item_card_${item.sku}"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PrimaryBlue.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
          Text(
            text = item.category.uppercase(),
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
          )
        }

        Text(
          text = item.sku,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = item.name,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TextPrimary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.LocationOn,
          contentDescription = "Location",
          tint = TextSecondary,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = item.location,
          fontSize = 12.sp,
          color = TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = BackgroundGray, thickness = 1.dp)
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "STOCK LEVEL",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "${item.stockLevel} ${item.unit}",
              fontWeight = FontWeight.Black,
              fontSize = 18.sp,
              color = if (isLowStock) Color.Red else SuccessGreen
            )

            if (isLowStock) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color.Red.copy(alpha = 0.1f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "ALERT: MIN ${item.threshold}",
                  color = Color.Red,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black
                )
              }
            } else {
              Text(
                text = "• Min ${item.threshold}",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilledIconButton(
            onClick = onDecrement,
            colors = IconButtonDefaults.filledIconButtonColors(
              containerColor = BackgroundGray,
              contentColor = TextPrimary
            ),
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
          }

          FilledIconButton(
            onClick = onIncrement,
            colors = IconButtonDefaults.filledIconButtonColors(
              containerColor = PrimaryBlue.copy(alpha = 0.1f),
              contentColor = PrimaryBlue
            ),
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Increment", modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

@Composable
fun AddSupplyDialog(
  onDismiss: () -> Unit,
  onConfirm: (sku: String, name: String, category: String, stock: Int, threshold: Int, location: String, unit: String) -> Unit
) {
  var sku by remember { mutableStateOf("") }
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Inks") }
  var stockLevel by remember { mutableStateOf("10") }
  var threshold by remember { mutableStateOf("5") }
  var location by remember { mutableStateOf("Main Warehouse") }
  var unit by remember { mutableStateOf("Pcs") }

  val categories = listOf("Inks", "Spare Parts", "Consumables", "DTF Supplies")

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .shadow(16.dp, RoundedCornerShape(28.dp)),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Text(
            text = "Add New Supply",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
          Text(
            text = "REGISTER NEW INDUSTRIAL ASSETS",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(color = BackgroundGray)
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Item Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              placeholder = { Text("e.g. UV-6090 Magenta Ink 1L") },
              modifier = Modifier.fillMaxWidth().testTag("dialog_name_input"),
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("SKU ID Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            OutlinedTextField(
              value = sku,
              onValueChange = { sku = it },
              placeholder = { Text("e.g. CJ-UV-MAG1L") },
              modifier = Modifier.fillMaxWidth().testTag("dialog_sku_input"),
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              categories.forEach { cat ->
                val isSelected = category == cat
                FilterChip(
                  selected = isSelected,
                  onClick = { category = cat },
                  label = { Text(cat, fontWeight = FontWeight.Bold) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    containerColor = BackgroundGray,
                    labelColor = TextSecondary
                  )
                )
              }
            }
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Current Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              OutlinedTextField(
                value = stockLevel,
                onValueChange = { stockLevel = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("dialog_stock_input"),
                shape = RoundedCornerShape(12.dp)
              )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Unit of Measure", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                placeholder = { Text("Pcs, Liters") },
                modifier = Modifier.fillMaxWidth().testTag("dialog_unit_input"),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Low-Stock Alert Limit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            OutlinedTextField(
              value = threshold,
              onValueChange = { threshold = it },
              placeholder = { Text("Alert when stock hits or drops below this") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth().testTag("dialog_threshold_input"),
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Storage Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            OutlinedTextField(
              value = location,
              onValueChange = { location = it },
              placeholder = { Text("e.g. Shelf A-1, Cabinet B-2") },
              modifier = Modifier.fillMaxWidth().testTag("dialog_location_input"),
              shape = RoundedCornerShape(12.dp)
            )
          }
        }

        item {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("CANCEL", fontWeight = FontWeight.Black, color = TextSecondary)
            }

            Button(
              onClick = {
                onConfirm(
                  sku,
                  name,
                  category,
                  stockLevel.toIntOrNull() ?: 0,
                  threshold.toIntOrNull() ?: 0,
                  location,
                  unit
                )
              },
              modifier = Modifier.weight(1f).testTag("dialog_save_button"),
              colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("SAVE ITEM", fontWeight = FontWeight.Black)
            }
          }
        }
      }
    }
  }
}

@Composable
fun DetailSupplyDialog(
  item: SupplyItem,
  viewModel: InventoryViewModel,
  onDismiss: () -> Unit,
  onUpdate: (SupplyItem) -> Unit,
  onDelete: (SupplyItem) -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }

  var sku by remember { mutableStateOf(item.sku) }
  var name by remember { mutableStateOf(item.name) }
  var category by remember { mutableStateOf(item.category) }
  var stockLevel by remember { mutableStateOf(item.stockLevel.toString()) }
  var threshold by remember { mutableStateOf(item.threshold.toString()) }
  var location by remember { mutableStateOf(item.location) }
  var unit by remember { mutableStateOf(item.unit) }

  val categories = listOf("Inks", "Spare Parts", "Consumables", "DTF Supplies")

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .shadow(16.dp, RoundedCornerShape(28.dp)),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isEditing) "Edit Item" else "Supply Details",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )

            if (viewModel.canEditSupply()) {
              IconButton(onClick = { isEditing = !isEditing }) {
                Icon(
                  imageVector = if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                  contentDescription = "Toggle edit mode",
                  tint = PrimaryBlue
                )
              }
            }
          }
          Text(
            text = item.sku,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(color = BackgroundGray)
        }

        if (!isEditing) {
          item {
            DetailItemView("NAME", item.name, Icons.Default.Label)
          }
          item {
            DetailItemView("CATEGORY", item.category.uppercase(), Icons.Default.Category)
          }
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(modifier = Modifier.weight(1f)) {
                DetailItemView(
                  "STOCK LEVEL",
                  "${item.stockLevel} ${item.unit}",
                  Icons.Default.Inventory,
                  valueColor = if (item.stockLevel <= item.threshold) Color.Red else SuccessGreen
                )
              }
              Box(modifier = Modifier.weight(1f)) {
                DetailItemView("ALERT THRESHOLD", "At or below ${item.threshold} ${item.unit}", Icons.Default.NotificationsActive)
              }
            }
          }
          item {
            DetailItemView("STORAGE LOCATION", item.location, Icons.Default.LocationOn)
          }

          item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              if (viewModel.canDeleteSupply()) {
                Button(
                  onClick = { onDelete(item) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.weight(1f).testTag("dialog_delete_button")
                ) {
                  Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("DELETE", fontWeight = FontWeight.Black)
                }
              }

              Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("CLOSE", fontWeight = FontWeight.Black)
              }
            }
          }
        } else {
          item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Item Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("SKU ID Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                categories.forEach { cat ->
                  val isSelected = category == cat
                  FilterChip(
                    selected = isSelected,
                    onClick = { category = cat },
                    label = { Text(cat, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = PrimaryBlue,
                      selectedLabelColor = Color.White,
                      containerColor = BackgroundGray,
                      labelColor = TextSecondary
                    )
                  )
                }
              }
            }
          }

          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Current Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                  value = stockLevel,
                  onValueChange = { stockLevel = it },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )
              }

              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                  value = unit,
                  onValueChange = { unit = it },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )
              }
            }
          }

          item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Alert Limit Threshold", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text("Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
              )
            }
          }

          item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              OutlinedButton(
                onClick = { isEditing = false },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("CANCEL", fontWeight = FontWeight.Black, color = TextSecondary)
              }

              Button(
                onClick = {
                  onUpdate(
                    item.copy(
                      sku = sku,
                      name = name,
                      category = category,
                      stockLevel = stockLevel.toIntOrNull() ?: 0,
                      threshold = threshold.toIntOrNull() ?: 0,
                      location = location,
                      unit = unit
                    )
                  )
                },
                modifier = Modifier.weight(1f).testTag("dialog_update_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("SAVE", fontWeight = FontWeight.Black)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun DetailItemView(
  label: String,
  value: String,
  icon: ImageVector,
  valueColor: Color = TextPrimary
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(BackgroundGray)
      .padding(12.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 0.5.sp)
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
  }
}











@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
  NavigationBar(
    containerColor = Color.White,
    tonalElevation = 8.dp,
    modifier = Modifier
      .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
      .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    NavigationBarItem(
      selected = selectedTab == 0,
      onClick = { onTabSelected(0) },
      icon = { Icon(Icons.Default.Home, contentDescription = null) },
      label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimaryBlue,
        selectedTextColor = PrimaryBlue,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
      )
    )
    NavigationBarItem(
      selected = selectedTab == 1,
      onClick = { onTabSelected(1) },
      icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
      label = { Text("Tasks", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimaryBlue,
        selectedTextColor = PrimaryBlue,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
      )
    )
    NavigationBarItem(
      selected = selectedTab == 2,
      onClick = { onTabSelected(2) },
      icon = { Icon(Icons.Default.Build, contentDescription = null) },
      label = { Text("Service", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimaryBlue,
        selectedTextColor = PrimaryBlue,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
      )
    )
    NavigationBarItem(
      selected = selectedTab == 3,
      onClick = { onTabSelected(3) },
      icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
      label = { Text("Stock", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimaryBlue,
        selectedTextColor = PrimaryBlue,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
      )
    )
    NavigationBarItem(
      selected = selectedTab == 4,
      onClick = { onTabSelected(4) },
      icon = { Icon(Icons.Default.Menu, contentDescription = null) },
      label = { Text("More", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimaryBlue,
        selectedTextColor = PrimaryBlue,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
      )
    )
  }
}

@Composable
fun NavigationContent(
  selectedTab: Int,
  onTabSelected: (Int) -> Unit,
  viewModel: InventoryViewModel
) {
  when (selectedTab) {
    0 -> DashboardScreen(
      viewModel = viewModel,
      onNavigateToLowStock = {
        viewModel.selectedCategory.value = "Low Stock"
        onTabSelected(3)
      },
      onNavigateToTasks = { onTabSelected(1) },
      onNavigateToService = { onTabSelected(2) },
      onNavigateToMore = { onTabSelected(4) }
    )
    1 -> TasksScreen(viewModel = viewModel)
    2 -> ServiceScreen(viewModel = viewModel)
    3 -> InventoryScreen(viewModel = viewModel)
    4 -> MoreScreen(
      viewModel = viewModel,
      onLogoutClick = {
        viewModel.logout()
      }
    )
  }
}

@Composable
fun NavigationSidebar(
  selectedTab: Int,
  onTabSelected: (Int) -> Unit,
  onLogoutClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .width(260.dp)
      .fillMaxHeight()
      .background(Color.White)
      .padding(vertical = 24.dp, horizontal = 16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      // Brand Header
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(PrimaryBlue, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "C",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "COLORJET",
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "BANGLADESH ERP",
            color = AccentOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Menu Items
      val items = listOf(
        SidebarNavigationItem("Home", Icons.Default.Home, 0),
        SidebarNavigationItem("Tasks", Icons.Default.Assignment, 1),
        SidebarNavigationItem("Service", Icons.Default.Build, 2),
        SidebarNavigationItem("Stock", Icons.Default.Inventory, 3),
        SidebarNavigationItem("More", Icons.Default.Menu, 4)
      )

      items.forEach { item ->
        val isSelected = selectedTab == item.index
        val bgContainerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent
        val contentColor = if (isSelected) PrimaryBlue else TextSecondary

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgContainerColor)
            .clickable { onTabSelected(item.index) }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("sidebar_tab_${item.index}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(16.dp))
          Text(
            text = item.label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
          )
        }
      }
    }

    Column {
      // Profile Section
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(BackgroundGray)
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AccentOrange),
          contentAlignment = Alignment.Center
        ) {
          Text("U", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) // Update with real initials if passed
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text("User Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          Text("Admin", fontSize = 11.sp, color = TextSecondary)
        }
      }
      
      Spacer(modifier = Modifier.height(16.dp))
      
      // Logout at Bottom
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { onLogoutClick() }
          .padding(horizontal = 16.dp, vertical = 14.dp)
          .testTag("sidebar_logout_button"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.ExitToApp,
          contentDescription = "Logout",
          tint = Color.Red,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
          text = "Logout",
          color = Color.Red,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

data class SidebarNavigationItem(
  val label: String,
  val icon: ImageVector,
  val index: Int
)
