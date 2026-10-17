package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.FBSViewModel
import com.example.ui.components.BackupProgressOverlay
import com.example.ui.screens.OptimizerScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SnapshotsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.FBSTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FBSViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FBSTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: FBSViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val backupProgress by viewModel.backupProgress.collectAsState()

    // Handle back button: return to Overview if on another tab
    BackHandler(enabled = currentTab != AppTab.OVERVIEW) {
        viewModel.selectTab(AppTab.OVERVIEW)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.OVERVIEW,
                    onClick = { viewModel.selectTab(AppTab.OVERVIEW) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.OVERVIEW) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Overview"
                        )
                    },
                    label = { Text("Overview", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_item_overview")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.SNAPSHOTS,
                    onClick = { viewModel.selectTab(AppTab.SNAPSHOTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.SNAPSHOTS) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "Snapshots"
                        )
                    },
                    label = { Text("Snapshots", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_item_snapshots")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.VAULT,
                    onClick = { viewModel.selectTab(AppTab.VAULT) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.VAULT) Icons.Filled.Lock else Icons.Outlined.Lock,
                            contentDescription = "Vault"
                        )
                    },
                    label = { Text("Vault", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_item_vault")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.OPTIMIZER,
                    onClick = { viewModel.selectTab(AppTab.OPTIMIZER) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.OPTIMIZER) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "Optimizer"
                        )
                    },
                    label = { Text("Optimizer", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_item_optimizer")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        AppTab.OVERVIEW -> OverviewScreen(
                            viewModel = viewModel,
                            onNavigateTab = { viewModel.selectTab(it) }
                        )
                        AppTab.SNAPSHOTS -> SnapshotsScreen(viewModel = viewModel)
                        AppTab.VAULT -> VaultScreen(viewModel = viewModel)
                        AppTab.OPTIMIZER -> OptimizerScreen(viewModel = viewModel)
                        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }

                if (backupProgress.isRunning) {
                    BackupProgressOverlay(
                        progress = backupProgress,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
