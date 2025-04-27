# Vaultwarden Standalone Android App – Project Plan

## 1. Vision
Provide an all-in-one Android application that embeds the Vaultwarden Rust server and a lightweight client UI so that end-users can manage their passwords without running or configuring an external server. The app must remain 100 % API-compatible with Bitwarden so that other Bitwarden clients (desktop/browser extensions) can sync via the phone when both devices are on the same network.

## 2. High-Level Architecture

| Layer | Technology | Responsibility |
|-------|------------|----------------|
| UI (Android) | Kotlin + Jetpack Compose | Vault browsing, item CRUD, onboarding, backup/restore settings |
| Service (Android) | Android Foreground Service | Starts/Stops the embedded server binary; shows persistent notification; exposes binder API to UI |
| JNI/FFI | cargo-ndk, JNI, Rust `jni` crate | Thin wrapper that exposes `start_server`, `stop_server`, `status` functions implemented in Rust |
| Server | Vaultwarden (Rust) | Listens on `127.0.0.1:<dynamic_port>`; stores data in app-private storage using SQLite |

```
┌────────────┐   Binder / IPC   ┌──────────────────┐   HTTP (loopback) ┌──────────────┐
│  UI Layer  │ ───────────────▶ │ ForegroundService│ ────────────────▶ │ Vaultwarden  │
└────────────┘                  └──────────────────┘                    └──────────────┘
```

## 3. Codebase Layout (proposed)

```
/ (root)
 ├─ android/               # Gradle project
 │    ├─ app/              # Kotlin sources + resources
 │    └─ rust/             # Cargo crate that depends on vaultwarden
 ├─ src/                   # Existing Vaultwarden server code (unchanged)
 ├─ PLANNING.md            # This document
 └─ TASK.md                # Task tracker
```

*We will avoid duplicating Vaultwarden code by using Cargo workspace & features.*

## 4. Technical Decisions

1. **Cross-Compilation** – Use `cargo-ndk` to build `libvaultwarden.so` for `arm64-v8a`, `armeabi-v7a`, `x86_64`.
2. **Data at rest encryption** – Rely on Vaultwarden’s existing Argon2 + AES encryption for vault items; additionally store SQLite file under `Context.getNoBackupFilesDir()` with file-level encryption via Android KeyStore (TBD).
3. **HTTPS** – Use `https://127.0.0.1` with self-signed certificate generated at first run and pinned by the client code.
4. **Battery** – Server starts on demand and shuts down after configurable idle timeout (default 5 min) using a Handler in Service.
5. **Minimum SDK** – 26 (Android 8.0, matches requirement).

## 5. Milestones

1. **M0 – Repository bootstrap**
   • Add `android/` Gradle template
   • Add Cargo workspace entry and FFI crate skeleton
2. **M1 – Compile Vaultwarden for Android**
   • Build `libvaultwarden.so` for `arm64-v8a`
   • Smoke-test on device via `adb shell` & curl
3. **M2 – Service Integration**
   • Kotlin Service starting/stopping Rust binary through JNI
   • Persistent notification + basic status UI
4. **M3 – Basic Vault UI**
   • Minimal login, item list, add/edit dialogs
5. **M4 – Security Hardening & Backups**
   • HTTPS pinning, KeyStore encryption, backup export/import
6. **M5 – Polishing & Release**
   • CI, tests, documentation, Play Store assets

## 6. Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Large binary size | Strip symbols, split-ABI builds, R8 shrinking |
| NDK / Cross-compile pain | Use proven `cargo-ndk` workflows & CI |
| API drift with Bitwarden | Upstream sync on every release; integration tests |
| Battery drain | Idle shutdown timer, WorkManager for sync |

## 7. Open Questions
1. Do we need organisation sharing on-device or only personal vault? 

personal vault initally but plans for future use case of friends and family. 

2. Which backup destination(s) to support first?
all major cloud storage providers with a free tier

3. Should we proxy external Bitwarden clients via Wi-Fi only or also over cellular hotspot?

standalone first with local vault, wifi then cellular

*Please append new questions or decisions in this section as the project evolves.*

### Progress 2025-04-26

- Milestone 0 finished (Gradle skeleton + Rust FFI crate).
- Milestone 1 underway:
  - M1-1: cargo-ndk task defined and documented in `android/build.gradle`.
  - M1-2: Auto `cargoBuild` hooked into preBuild.
  - M1-3: Service logging added (completed).
  - M1-4: Instrumented test added (completed).
- Milestone 1 complete; next milestone will focus on HTTPS setup & idle shutdown.

*Questions:* none blocking.

### Progress 2025-04-26 (cont.)

- Milestone 2 started:
  - M2-1: Self-signed cert generation added to Rust FFI (completed).
  - Next: M2-2 configure Rocket TLS.
