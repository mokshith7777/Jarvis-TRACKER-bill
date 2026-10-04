# Jarvis Expense ⚡

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-cyan.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite%20(Air--Gapped)-orange.svg)](https://developer.android.com/training/data-storage/room)
[![Offline](https://img.shields.io/badge/Architecture-100%25%20Offline-brightgreen.svg)]()

**Jarvis Expense** is an air-gapped, local-first Android expense tracker built with **Jetpack Compose** and **Room SQLite**, featuring a cybernetic **Iron Man Mark-LXXXV HUD aesthetic**. 

The app operates **100% offline** with zero external cloud dependencies, zero tracking, and zero authentication needed, equipped with an **atomic duplicate shield** to eliminate duplicate transaction copies.

---

## 🚀 Key Features

### 1. Futuristic HUD Expense Logging Screen
- **Holographic Digital Amount Field**: Cyan/matrix glowing readout with real-time formatting and quick delta increments (`+100`, `+500`, `+1,000`, `+2,000`, `+5,000`, and `CLR`).
- **Interactive Cyber Category Grid**: Neon-bordered category tiles with custom iconography for Food, Transport, Energy & Cloud Utilities, Shopping, Holo-Sim & Media, Healthcare & Armor, Investments, and Influx/Salary.
- **Dedicated Chronometer Date Field**:
  - Live locked timestamp display (`YYYY-MM-DD`).
  - Quick date presets (`[TODAY]`, `[YESTERDAY]`, `[-2 DAYS]`, `[-1 WEEK]`).
  - Interactive **HUD Date Picker Dialog** with cyber calendar navigation and date locking.
- **Protocol & Vendor Inputs**: Quick tags for vendors and payment protocols (`UPI`, `Credit Card`, `Debit Card`, `Cash`, `Net Banking`, `Crypto`).

### 2. 🛡️ Anti-Duplicate Shield
- **Atomic Duplicate Detection**: Checks incoming transactions against recently logged entries (matching title, amount, category, type, and timestamp within a 6-second window) and rejects duplicates.
- **Debounced Commit**: Single-tap submit protection disables the button during write operations to prevent accidental double-taps.
- **Deduplication Engine**: All query flows enforce strict `distinctBy { it.id }`.
- **Database Purge Tool**: Built-in scanner to identify and clean any duplicate records directly from the Offline Vault or Settings screen.

### 3. 🔒 100% Air-Gapped Local Architecture (Zero Cloud Services)
- **Local-First SQLite Room**: All transaction mutations and budgets are stored locally on the device.
- **Zero Authentication**: No login screens, Google accounts, or API keys required.
- **Offline Vault Screen**: Live telemetry on active local records, zero network traffic confirmation, and local backup export (`.json`).

### 4. 📁 Local Device Storage Clearance
- Uses runtime storage permissions (`READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`) with a futuristic **HUD Storage Clearance Pop-up Dialog**.
- Allows offline local receipt image attachments and encrypted ledger file exports without cloud storage.

---

## 🛠️ Tech Stack & Architecture

- **UI**: Jetpack Compose, Material Design 3 (M3) with custom HUD Cyber theme.
- **Language**: 100% Kotlin with Coroutines & StateFlow.
- **Database**: Android Room (SQLite) with KSP compiler.
- **Architecture**: Clean MVVM (Model-View-ViewModel) + Repository Pattern.
- **Target SDK**: Android 14+ / SDK 36 (Min SDK: 24 / Android 7.0+).

---

## 📦 How to Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or newer
- Android SDK 36

### Build Commands
```bash
# Clone the repository
git clone <your-repo-url>
cd jarvis-expense

# Build the debug APK
gradle :app:assembleDebug

# Run Unit & Robolectric Tests
gradle :app:testDebugUnitTest
```

---

## 📄 License
Open source under the [Apache 2.0 License](LICENSE).
