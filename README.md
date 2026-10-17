# FBS - File Backup Suite (Android)

FBS is a modern, high-performance Android backup and secure file vault application built with **Kotlin** and **Jetpack Compose (Material 3)**.

## Core Features

- **Point-in-Time Backup Snapshots**:
  - Full, Incremental, and Differential snapshot creation
  - Category breakdown (Photos & Media, Documents, Archives, System Configs, Contacts)
  - Hardware-backed AES-256 encryption option
  - Real-time animated backup and restore pipeline with progress tracking

- **Encrypted File Vault**:
  - Secure browsing by category (Media, Docs, Archives, System, Contacts)
  - File details inspection with SHA-256 integrity signatures
  - Single-file instant extraction and restore
  - Biometric / PIN simulated vault lock & unlock

- **Storage Optimization & Deduplication Engine**:
  - Detects duplicate files across snapshots with byte-level cryptographic hashing
  - One-tap duplicate cleanup to recover device storage
  - Compression ratio analytics and space-saving insights

- **Integrity Doctor**:
  - Bit-rot verification tool that validates SHA-256 checksum signatures of all stored snapshots
  - Live integrity audit with certification report

- **Automated Background Schedule**:
  - Configurable frequency: Hourly, Daily, Weekly
  - Power & Network guards: Wi-Fi only mode and Charging only mode
  - Auto-prune policy for older temporary snapshots

## Technical Stack

- **Target Runtime**: Android SDK 36 (Android 15 / 16 preview)
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow
- **Design & Icons**: Custom adaptive launcher icon with glowing cybernetic shield emblem and Material Symbols
