package com.example.model

enum class BackupType(val label: String, val badgeColorHex: Long) {
    FULL("Full Snapshot", 0xFF06B6D4),
    INCREMENTAL("Incremental", 0xFF10B981),
    DIFFERENTIAL("Differential", 0xFF8B5CF6)
}

enum class BackupStatus(val label: String) {
    PROTECTED("Vault Protected"),
    VERIFIED("Checksum Verified"),
    IN_PROGRESS("Syncing"),
    FAILED("Error Detected")
}

enum class StorageCategory(val label: String, val colorHex: Long) {
    MEDIA("Photos & Videos", 0xFF3B82F6),
    DOCUMENTS("Documents", 0xFF10B981),
    ARCHIVES("Archives & Zips", 0xFFF59E0B),
    SYSTEM("System & Configs", 0xFFA855F7),
    CONTACTS("Contacts & DB", 0xFFEC4899)
}

enum class StorageDestination(val label: String) {
    LOCAL_VAULT("Internal Safe Vault"),
    SECURE_SD("External Storage / SD"),
    CLOUD_SYNC("Clouvea Cloud Sync")
}

data class BackupSnapshot(
    val id: String,
    val title: String,
    val timestamp: Long,
    val type: BackupType,
    val fileCount: Int,
    val totalSizeBytes: Long,
    val checksumSha256: String,
    val isEncrypted: Boolean,
    val status: BackupStatus,
    val destination: StorageDestination = StorageDestination.LOCAL_VAULT,
    val notes: String = ""
)

data class BackupFileItem(
    val id: String,
    val snapshotId: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val category: StorageCategory,
    val checksum: String,
    val isEncrypted: Boolean,
    val timestamp: Long,
    val isDuplicate: Boolean = false
)

enum class BackupFrequency(val label: String) {
    HOURLY("Every Hour"),
    DAILY("Daily at Midnight"),
    WEEKLY("Weekly on Sunday")
}

data class AutoBackupConfig(
    val isEnabled: Boolean = true,
    val frequency: BackupFrequency = BackupFrequency.DAILY,
    val wifiOnly: Boolean = true,
    val chargingOnly: Boolean = false,
    val autoDeduplicate: Boolean = true,
    val autoPruneOld: Boolean = false,
    val retentionDays: Int = 30,
    val destination: StorageDestination = StorageDestination.LOCAL_VAULT
)

data class StorageHealthReport(
    val totalDeviceStorageBytes: Long,
    val usedDeviceStorageBytes: Long,
    val backupVaultBytes: Long,
    val duplicatesCount: Int,
    val potentialSavingsBytes: Long,
    val integrityScorePercent: Int
)

data class DuplicateGroup(
    val checksum: String,
    val fileName: String,
    val count: Int,
    val samplePaths: List<String>,
    val wastedBytes: Long
)

data class BackupJobProgress(
    val isRunning: Boolean = false,
    val stage: String = "",
    val currentFile: String = "",
    val processedItems: Int = 0,
    val totalItems: Int = 0,
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val error: String? = null
)
