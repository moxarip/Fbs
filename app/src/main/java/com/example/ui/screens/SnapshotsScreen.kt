package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackupSnapshot
import com.example.model.BackupType
import com.example.ui.FBSViewModel
import com.example.ui.components.InspectFilesSheet
import com.example.ui.components.NewBackupDialog
import com.example.ui.components.RestoreConfirmDialog
import com.example.ui.components.SnapshotItemCard

@Composable
fun SnapshotsScreen(
    viewModel: FBSViewModel,
    modifier: Modifier = Modifier
) {
    val snapshots by viewModel.filteredSnapshots.collectAsState()
    val vaultFiles by viewModel.vaultFiles.collectAsState()
    val searchQuery by viewModel.snapshotSearchQuery.collectAsState()

    var selectedTypeFilter by remember { mutableStateOf<BackupType?>(null) }
    var showNewBackupDialog by remember { mutableStateOf(false) }
    var snapshotToRestore by remember { mutableStateOf<BackupSnapshot?>(null) }
    var snapshotToInspect by remember { mutableStateOf<BackupSnapshot?>(null) }

    val displayedSnapshots = remember(snapshots, selectedTypeFilter) {
        if (selectedTypeFilter == null) snapshots
        else snapshots.filter { it.type == selectedTypeFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("snapshots_screen")
    ) {
        // Top Bar: Search and Create
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Backup Snapshots",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${snapshots.size} point-in-time state archives",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showNewBackupDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_snapshot_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSnapshotSearchQuery(it) },
                placeholder = { Text("Search by name, note, or ID...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSnapshotSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("snapshot_search_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Type filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All (${snapshots.size})", fontSize = 11.sp) }
                )
                BackupType.values().forEach { type ->
                    val count = snapshots.count { it.type == type }
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == type) null else type
                        },
                        label = { Text("${type.label} ($count)", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Snapshot list
        if (displayedSnapshots.isEmpty()) {
            BoxPlaceholder(
                title = "No Snapshots Found",
                subtitle = if (searchQuery.isNotBlank()) "No snapshots matched '$searchQuery'" else "Create your first backup snapshot to begin!"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedSnapshots, key = { it.id }) { snapshot ->
                    SnapshotItemCard(
                        snapshot = snapshot,
                        onRestore = { snapshotToRestore = it },
                        onVerify = { viewModel.verifyChecksum(it) },
                        onViewFiles = { snapshotToInspect = it },
                        onDelete = { viewModel.deleteSnapshot(it) }
                    )
                }
            }
        }
    }

    if (showNewBackupDialog) {
        NewBackupDialog(
            onDismiss = { showNewBackupDialog = false },
            onStart = { title, type, dest, encrypt, cats ->
                showNewBackupDialog = false
                viewModel.startBackup(title, type, dest, encrypt, cats)
            }
        )
    }

    snapshotToRestore?.let { snap ->
        RestoreConfirmDialog(
            snapshot = snap,
            onDismiss = { snapshotToRestore = null },
            onConfirm = {
                snapshotToRestore = null
                viewModel.restoreSnapshot(snap)
            }
        )
    }

    snapshotToInspect?.let { snap ->
        val filesInSnap = vaultFiles.filter { it.snapshotId == snap.id }
        InspectFilesSheet(
            snapshot = snap,
            files = filesInSnap,
            onDismiss = { snapshotToInspect = null }
        )
    }
}

@Composable
fun BoxPlaceholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
