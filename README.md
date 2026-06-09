# Carpet Ledger

![Last Commit](https://img.shields.io/github/last-commit/krishna11-me/carpet-ledger)
![Repo Size](https://img.shields.io/github/repo-size/krishna11-me/carpet-ledger)
![Android](https://img.shields.io/badge/platform-Android-green)
![Kotlin](https://img.shields.io/badge/language-Kotlin-orange)
![Status](https://img.shields.io/badge/status-Prototype-yellow)

Lightweight Android application to manage carpet inventory, custom sizes/types/categories and transactions (sales), with local persistence and PDF-export capability.

**Highlights**
- Small MVVM Android app using Room for local storage.
- Compose-based UI entrypoint in [app/src/main/java/com/example/ui/CarpetHomeScreen.kt](app/src/main/java/com/example/ui/CarpetHomeScreen.kt#L1).
- Export transactions/invoices to PDF with [app/src/main/java/com/example/util/PdfExporter.kt](app/src/main/java/com/example/util/PdfExporter.kt#L1).
- Extensible data model: custom sizes, types, and categories.

**Status:** Prototype — no license file included.

**Quick links**
- Database: [app/src/main/java/com/example/data/AppDatabase.kt](app/src/main/java/com/example/data/AppDatabase.kt#L1)
- ViewModel: [app/src/main/java/com/example/ui/CarpetViewModel.kt](app/src/main/java/com/example/ui/CarpetViewModel.kt#L1)
- UI: [app/src/main/java/com/example/ui/CarpetHomeScreen.kt](app/src/main/java/com/example/ui/CarpetHomeScreen.kt#L1)
- PDF exporter: [app/src/main/java/com/example/util/PdfExporter.kt](app/src/main/java/com/example/util/PdfExporter.kt#L1)

## Project structure

- `app/` — Android application module
  - `src/main/java/com/example/data/` — Room entities, DAOs and `AppDatabase`
  - `src/main/java/com/example/ui/` — Compose screens and `CarpetViewModel`
  - `src/main/java/com/example/util/` — helpers like `PdfExporter`
  - `res/` — resources (themes, drawables, strings)

## Architecture

The app uses a simple MVVM pattern:

- Data layer: Room (`AppDatabase`, Entities, DAOs)
- Repository: `TransactionRepository` which exposes DB operations
- ViewModel: `CarpetViewModel` that holds UI state and handles business logic
- UI: Compose screens under `ui/` observe ViewModel state and trigger actions

See `docs/DIAGRAMS.md` for architecture diagrams and flows (Mermaid).

## Data model (summary)

- `CarpetImageEntity` — stores image metadata and related carpet info
- `TransactionEntity` — records a sale/transaction (price, items, metadata)
- `CustomCarpetSizeEntity` — user-defined sizes for carpets
- `CustomCarpetTypeEntity` — user-defined carpet types
- `CustomCategoryEntity` — organizational categories

## Typical flows

- Add a new carpet image → create a `TransactionEntity` for sale → `TransactionRepository` persists it → `CarpetViewModel` updates UI state → user exports transactions to PDF via `PdfExporter`.

## Build & run (local)

Prerequisites: JDK 11+, Android SDK, Android Studio or command-line Gradle.

From project root (macOS / Linux):

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

Install the generated APK with `adb` or run from Android Studio.

Run unit tests:

```bash
./gradlew test
```

Run instrumentation tests (connected device / emulator):

```bash
./gradlew connectedAndroidTest
```

## Where to start reading the code

- App database and DAOs: [app/src/main/java/com/example/data](app/src/main/java/com/example/data)
- ViewModel and UI: [app/src/main/java/com/example/ui](app/src/main/java/com/example/ui)
- PDF export helper: [app/src/main/java/com/example/util/PdfExporter.kt](app/src/main/java/com/example/util/PdfExporter.kt#L1)

## Extending the app

- Add new fields to an entity and create a migration for `AppDatabase`.
- Add DAO methods for new queries and wire them through `TransactionRepository`.
- Update `CarpetViewModel` to surface new data to the UI and update Compose screens.

## Contributing

- Fork, implement a small focused change, and open a PR describing the change and any migration steps.

## License

No license file is present. Add a `LICENSE` to clarify reuse terms.

---
For architecture and flow diagrams see `docs/DIAGRAMS.md`.

Generated on: 2026-06-09
