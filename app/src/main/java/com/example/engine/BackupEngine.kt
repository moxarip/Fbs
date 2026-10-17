package com.example.engine

import com.example.data.BackupRepository
import com.example.model.BackupFileItem
import com.example.model.BackupJobProgress
import com.example.model.BackupSnapshot
import com.example.model.BackupStatus
import com.example.model.BackupType
import com.example.model.StorageCategory
import com.example.model.StorageDestination
import kotlinx.coroutines.delay
import java.util.UUID

class BackupEngine(private val repository: BackupRepository) {

    suspend fun runBackup(
        title: String,
        type: BackupType,
        destination: StorageDestination,
        encrypt: Boolean,
        selectedCategories: Set<StorageCategory>
    ) {
        val candidateItems = listOf(
            Triple("DCIM_Photo_${System.currentTimeMillis() % 10000}.jpg", StorageCategory.MEDIA, 12_500_000L),
            Triple("Video_Capture_${System.currentTimeMillis() % 1000}.mp4", StorageCategory.MEDIA, 84_000_000L),
            Triple("Client_Invoice_Oct2026.pdf", StorageCategory.DOCUMENTS, 1_850_000L),
            Triple("Portfolio_Summary.docx", StorageCategory.DOCUMENTS, 3_400_000L),
            Triple("System_KeyStore_Export.dat", StorageCategory.SYSTEM, 240_000L),
            Triple("WhatsApp_Chat_Backup.crypt14", StorageCategory.ARCHIVES, 98_000_000L),
            Triple("Contacts_vCard_${System.currentTimeMillis() % 100}.vcf", StorageCategory.CONTACTS, 640_000L)
        ).filter { selectedCategories.contains(it.second) }

        val totalCount = candidateItems.size
        val totalBytes = candidateItems.sumOf { it.third }

        repository.updateProgress(
            BackupJobProgress(
                isRunning = true,
                stage = "1/4: Discovering and indexing files...",
                processedItems = 0,
                totalItems = totalCount,
                progress = 0.05f
            )
        )
        delay(600)

        val newFiles = mutableListOf<BackupFileItem>()
        val snapshotId = "snap-" + UUID.randomUUID().toString().take(8)

        candidateItems.forEachIndexed { index, item ->
            val progressVal = 0.15f + ((index.toFloat() / totalCount.toFloat()) * 0.70f)
            repository.updateProgress(
                BackupJobProgress(
                    isRunning = true,
                    stage = "2/4: Hashing & Compressing: ${item.first}",
                    currentFile = item.first,
                    processedItems = index + 1,
                    totalItems = totalCount,
                    progress = progressVal
                )
            )
            delay(350)

            newFiles.add(
                BackupFileItem(
                    id = "file-" + UUID.randomUUID().toString().take(8),
                    snapshotId = snapshotId,
                    name = item.first,
                    path = "/storage/emulated/0/${item.second.name.lowercase()}/${item.first}",
                    sizeBytes = item.third,
                    category = item.second,
                    checksum = BackupRepository.sha256String(item.first + snapshotId),
                    isEncrypted = encrypt,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        repository.updateProgress(
            BackupJobProgress(
                isRunning = true,
                stage = "3/4: Encrypting metadata & Writing manifest...",
                processedItems = totalCount,
                totalItems = totalCount,
                progress = 0.90f
            )
        )
        delay(500)

        val newSnapshot = BackupSnapshot(
            id = snapshotId,
            title = title.ifBlank { "Backup Snapshot #${repository.snapshots.value.size + 1}" },
            timestamp = System.currentTimeMillis(),
            type = type,
            fileCount = totalCount,
            totalSizeBytes = totalBytes,
            checksumSha256 = BackupRepository.sha256String(snapshotId + totalBytes),
            isEncrypted = encrypt,
            status = BackupStatus.PROTECTED,
            destination = destination,
            notes = "Completed with ${selectedCategories.size} categories protected"
        )

        repository.addSnapshot(newSnapshot, newFiles)

        repository.updateProgress(
            BackupJobProgress(
                isRunning = false,
                stage = "Backup successfully completed!",
                processedItems = totalCount,
                totalItems = totalCount,
                progress = 1.0f,
                isCompleted = true
            )
        )
        delay(800)
        repository.updateProgress(BackupJobProgress())
    }

    suspend fun runRestore(snapshot: BackupSnapshot, onComplete: () -> Unit) {
        val files = repository.vaultFiles.value.filter { it.snapshotId == snapshot.id }
        val count = files.size.coerceAtLeast(1)

        repository.updateProgress(
            BackupJobProgress(
                isRunning = true,
                stage = "Validating checksums for restore...",
                progress = 0.1f
            )
        )
        delay(600)

        files.forEachIndexed { index, file ->
            val progress = 0.2f + ((index.toFloat() / count.toFloat()) * 0.75f)
            repository.updateProgress(
                BackupJobProgress(
                    isRunning = true,
                    stage = "Restoring: ${file.name}",
                    currentFile = file.name,
                    processedItems = index + 1,
                    totalItems = count,
                    progress = progress
                )
            )
            delay(280)
        }

        repository.updateProgress(
            BackupJobProgress(
                isRunning = false,
                stage = "Restore completed successfully!",
                progress = 1.0f,
                isCompleted = true
            )
        )
        repository.addLog("Restore: Successfully recovered ${files.size} files from '${snapshot.title}'")
        delay(700)
        repository.updateProgress(BackupJobProgress())
        onComplete()
    }
}
