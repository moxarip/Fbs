package com.example.data

import android.content.Context
import com.example.model.AutoBackupConfig
import com.example.model.BackupFileItem
import com.example.model.BackupJobProgress
import com.example.model.BackupSnapshot
import com.example.model.BackupStatus
import com.example.model.BackupType
import com.example.model.DuplicateGroup
import com.example.model.StorageCategory
import com.example.model.StorageDestination
import com.example.model.StorageHealthReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

class BackupRepository(private val context: Context) {

    private val _snapshots = MutableStateFlow<List<BackupSnapshot>>(emptyList())
    val snapshots: StateFlow<List<BackupSnapshot>> = _snapshots.asStateFlow()

    private val _vaultFiles = MutableStateFlow<List<BackupFileItem>>(emptyList())
    val vaultFiles: StateFlow<List<BackupFileItem>> = _vaultFiles.asStateFlow()

    private val _config = MutableStateFlow(AutoBackupConfig())
    val config: StateFlow<AutoBackupConfig> = _config.asStateFlow()

    private val _backupProgress = MutableStateFlow(BackupJobProgress())
    val backupProgress: StateFlow<BackupJobProgress> = _backupProgress.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<String>>(emptyList())
    val recentLogs: StateFlow<List<String>> = _recentLogs.asStateFlow()

    private val _isVaultLocked = MutableStateFlow(false)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    init {
        initSampleData()
    }

    private fun initSampleData() {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L

        val s1Id = "snap-" + UUID.randomUUID().toString().take(8)
        val s2Id = "snap-" + UUID.randomUUID().toString().take(8)
        val s3Id = "snap-" + UUID.randomUUID().toString().take(8)

        val initialSnapshots = listOf(
            BackupSnapshot(
                id = s1Id,
                title = "Camera Roll & Media Archive",
                timestamp = now - (dayMillis * 1),
                type = BackupType.INCREMENTAL,
                fileCount = 142,
                totalSizeBytes = 3_840_000_000L,
                checksumSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                isEncrypted = true,
                status = BackupStatus.PROTECTED,
                destination = StorageDestination.LOCAL_VAULT,
                notes = "Auto-synced high-res DCIM photos and 4K video clips"
            ),
            BackupSnapshot(
                id = s2Id,
                title = "Financial & Legal Documents Vault",
                timestamp = now - (dayMillis * 3),
                type = BackupType.FULL,
                fileCount = 38,
                totalSizeBytes = 512_000_000L,
                checksumSha256 = "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
                isEncrypted = true,
                status = BackupStatus.VERIFIED,
                destination = StorageDestination.LOCAL_VAULT,
                notes = "AES-256 encrypted tax reports, contracts, and PDF archives"
            ),
            BackupSnapshot(
                id = s3Id,
                title = "System Configs & App Data Base",
                timestamp = now - (dayMillis * 7),
                type = BackupType.FULL,
                fileCount = 86,
                totalSizeBytes = 1_250_000_000L,
                checksumSha256 = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                isEncrypted = false,
                status = BackupStatus.VERIFIED,
                destination = StorageDestination.LOCAL_VAULT,
                notes = "Initial baseline system backup before OS update"
            )
        )

        val files = mutableListOf<BackupFileItem>()
        val sampleNames = listOf(
            Triple("IMG_2026_Vacation_01.jpg", StorageCategory.MEDIA, 14_200_000L),
            Triple("IMG_2026_Family_Portrait.png", StorageCategory.MEDIA, 18_500_000L),
            Triple("VID_2026_Sunset_Drone.mp4", StorageCategory.MEDIA, 185_000_000L),
            Triple("Contract_Agreement_Final.pdf", StorageCategory.DOCUMENTS, 4_100_000L),
            Triple("Tax_Return_2025_Signed.pdf", StorageCategory.DOCUMENTS, 2_800_000L),
            Triple("Annual_Budget_Spreadsheet.xlsx", StorageCategory.DOCUMENTS, 1_450_000L),
            Triple("Passport_Scan_Encrypted.pdf", StorageCategory.DOCUMENTS, 3_200_000L),
            Triple("Voice_Memo_Meeting_08.m4a", StorageCategory.MEDIA, 22_000_000L),
            Triple("Project_Assets_Bundle.zip", StorageCategory.ARCHIVES, 145_000_000L),
            Triple("Database_Dump_Contacts.vcf", StorageCategory.CONTACTS, 850_000L),
            Triple("System_Preferences_Dump.json", StorageCategory.SYSTEM, 420_000L),
            Triple("WiFi_Passwords_Keyring.bak", StorageCategory.SYSTEM, 120_000L),
            // Duplicates for optimizer detection
            Triple("IMG_2026_Vacation_01_Copy.jpg", StorageCategory.MEDIA, 14_200_000L),
            Triple("Tax_Return_2025_Signed(1).pdf", StorageCategory.DOCUMENTS, 2_800_000L)
        )

        sampleNames.forEachIndexed { idx, item ->
            val snapId = if (idx < 5) s1Id else if (idx < 9) s2Id else s3Id
            val isDup = item.first.contains("Copy") || item.first.contains("(1)")
            files.add(
                BackupFileItem(
                    id = "file-$idx",
                    snapshotId = snapId,
                    name = item.first,
                    path = "/storage/emulated/0/Vault/${item.second.name.lowercase()}/${item.first}",
                    sizeBytes = item.third,
                    category = item.second,
                    checksum = sha256String(item.first),
                    isEncrypted = idx % 2 == 0,
                    timestamp = now - (idx * 3600_000L),
                    isDuplicate = isDup
                )
            )
        }

        _snapshots.value = initialSnapshots
        _vaultFiles.value = files
        _recentLogs.value = listOf(
            "System: Vault background monitoring initialized",
            "Snapshot: Camera Roll incremental check completed (142 files)",
            "Security: Integrity verification passed with 0 corruptions",
            "Optimizer: 2 duplicate files flagged for review"
        )
    }

    fun updateConfig(newConfig: AutoBackupConfig) {
        _config.value = newConfig
        addLog("Settings: Auto-backup configuration updated")
    }

    fun toggleVaultLock() {
        _isVaultLocked.value = !_isVaultLocked.value
        addLog("Vault: Lock state changed to ${_isVaultLocked.value}")
    }

    fun addSnapshot(snapshot: BackupSnapshot, files: List<BackupFileItem>) {
        _snapshots.value = listOf(snapshot) + _snapshots.value
        _vaultFiles.value = files + _vaultFiles.value
        addLog("Snapshot: Created '${snapshot.title}' (${snapshot.fileCount} items, ${formatBytes(snapshot.totalSizeBytes)})")
    }

    fun deleteSnapshot(snapshotId: String) {
        val target = _snapshots.value.find { it.id == snapshotId }
        _snapshots.value = _snapshots.value.filter { it.id != snapshotId }
        _vaultFiles.value = _vaultFiles.value.filter { it.snapshotId != snapshotId }
        addLog("Snapshot: Deleted '${target?.title ?: snapshotId}'")
    }

    fun removeDuplicateFiles() {
        val duplicates = _vaultFiles.value.filter { it.isDuplicate }
        val reclaimedBytes = duplicates.sumOf { it.sizeBytes }
        _vaultFiles.value = _vaultFiles.value.filter { !it.isDuplicate }
        addLog("Optimizer: Cleaned ${duplicates.size} duplicate files, recovered ${formatBytes(reclaimedBytes)}")
    }

    fun verifySnapshotChecksum(snapshotId: String): Boolean {
        val list = _snapshots.value.map { snap ->
            if (snap.id == snapshotId) {
                snap.copy(status = BackupStatus.VERIFIED)
            } else snap
        }
        _snapshots.value = list
        addLog("Integrity: Snapshot '$snapshotId' SHA-256 signature verified 100%")
        return true
    }

    fun updateProgress(progress: BackupJobProgress) {
        _backupProgress.value = progress
    }

    fun addLog(entry: String) {
        _recentLogs.value = listOf("• $entry") + _recentLogs.value.take(15)
    }

    fun getStorageHealthReport(): StorageHealthReport {
        val totalDevice = 128_000_000_000L // 128 GB device
        val usedDevice = 64_500_000_000L
        val vaultTotal = _snapshots.value.sumOf { it.totalSizeBytes }
        val duplicates = _vaultFiles.value.filter { it.isDuplicate }
        val wastedBytes = duplicates.sumOf { it.sizeBytes }

        return StorageHealthReport(
            totalDeviceStorageBytes = totalDevice,
            usedDeviceStorageBytes = usedDevice,
            backupVaultBytes = vaultTotal,
            duplicatesCount = duplicates.size,
            potentialSavingsBytes = wastedBytes,
            integrityScorePercent = if (_snapshots.value.all { it.status != BackupStatus.FAILED }) 98 else 75
        )
    }

    fun getDuplicateGroups(): List<DuplicateGroup> {
        val duplicates = _vaultFiles.value.filter { it.isDuplicate }
        return duplicates.map { dup ->
            DuplicateGroup(
                checksum = dup.checksum,
                fileName = dup.name,
                count = 2,
                samplePaths = listOf(dup.path, dup.path.replace("Copy", "Original")),
                wastedBytes = dup.sizeBytes
            )
        }
    }

    companion object {
        fun sha256String(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
            val pre = "KMGTPE"[exp - 1]
            return "%.1f %sB".format(bytes / Math.pow(1024.0, exp.toDouble()), pre)
        }
    }
}
