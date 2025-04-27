# TASK Tracker

## Active Tasks

- [x] M0-1: Create Android Gradle project skeleton (app + rust module) — 2025-04-26
- [x] M0-2: Add Cargo workspace & FFI crate skeleton — 2025-04-26

## Discovered During Work

*(empty)*

## Upcoming – Milestone 2 (Service Integration & HTTPS)

- [x] M2-1: Generate self-signed cert/key in Rust FFI on first start — 2025-04-26
- [x] M2-2: Configure Rocket TLS in Rust FFI using generated cert/key — 2025-04-26
- [x] M2-3: Implement `ServerService` idle shutdown timer — 2025-04-26
- [x] M2-4: Update instrumented test client to trust self-signed cert — 2025-04-26

## Upcoming – Milestone 3 (Basic UI & Control)

- [ ] M3-1: Create basic MainActivity UI (Start/Stop buttons) — 2025-04-26
- [ ] M3-2: Implement MainActivity logic to bind/display ServerService status — 2025-04-26
- [ ] M3-3: Display server URL (https://127.0.0.1:PORT) in UI when running — 2025-04-26
- [ ] M3-4: Refine ServerService for status query/idle timer reset from UI — 2025-04-26

## Upcoming – Milestone 1 (Rust/NDK integration)

- [ ] M1-1: Add `cargo-ndk` toolchain config & document build command (arm64-v8a) — 2025-04-26
- [ ] M1-2: Create Gradle task `cargoBuild` to compile JNI lib and copy into `app/src/main/jniLibs` — 2025-04-26
- [x] M1-3: Extend `ServerService` to log success/failure of JNI `startServer` — 2025-04-26
- [x] M1-4: Instrumented device test that hits `https://127.0.0.1:8087/alive` endpoint — 2025-04-26

## Completed

M0-1, M0-2, M1-3, M1-4, M2-1, M2-2, M2-3, M2-4
