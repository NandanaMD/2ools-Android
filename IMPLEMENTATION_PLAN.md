# 2ools – Android Implementation Plan & Technical Architecture

**Version:** 1.0  
**Target Platform:** Native Android (Kotlin + Jetpack Compose)  
**Minimum SDK:** Android 9.0 (API level 28)  
**Target SDK:** Android 15 (API level 35)  
**Target APK Size:** < 30 MB (Well within the < 100 MB budget)  
**App Philosophy:** 100% Offline-First, Zero-Login, Modular "Tool Registry", Native Material 3  

---

## 1. Executive Summary & Architectural Decisions

Based on our product alignment:
1. **Size & Performance Budget:** Under 100 MB budget gives us freedom to bundle reliable offline models (e.g., Google ML Kit's bundled barcode model) while avoiding cloud dependencies.
2. **QR / Barcode Engine:** **Google ML Kit (Bundled)** + **CameraX** for scanning; **ZXing Core** for QR code bitmap generation.
3. **Dependency Injection:** **Koin** (`koin-androidx-compose`) for clean, readable, boilerplate-free DI that is resilient to AI-assisted code evolution.
4. **Storage & Privacy:** Zero dangerous permissions. No `MANAGE_EXTERNAL_STORAGE`. 100% **Storage Access Framework (SAF)** with transient URIs and streaming to app-private cache directories (`context.cacheDir`).

---

## 2. Technology Stack & Dependency Matrix

| Layer | Technology / Library | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ | Modern type-safe native Android development |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative, dynamic UI with Material You support |
| **Architecture** | Single-Activity MVVM + Tool Registry | Clean, modular, easily expandable |
| **Dependency Injection** | Koin (`koin-androidx-compose`) | Simple, reflection-free runtime DI |
| **Navigation** | Navigation Compose | Type-safe declarative screen routing |
| **Asynchronous** | Kotlin Coroutines & Flow | Background file and math processing |
| **Local Storage / Prefs** | Jetpack DataStore (Preferences) | User favorites, recent tools, tool configurations |
| **Image Loading** | Coil (`coil-compose`) | High-performance asynchronous image rendering |
| **Camera & QR Scan** | CameraX (`camera-camera2`, `camera-view`) + ML Kit Barcode Scanning | Real-time offline QR & barcode detection |
| **QR Generation** | `com.google.zxing:core:3.5.3` | Lightweight offline QR bitmap generation |
| **Image Utilities** | Android Native `BitmapFactory`, `Bitmap`, `ExifInterface` | Resizing, compression, format conversion, EXIF stripping |
| **PDF Utilities** | Android Native `android.graphics.pdf.PdfDocument` & `PdfRenderer` | Images $\rightarrow$ PDF creation, PDF $\rightarrow$ Images rendering |

---

## 3. Modular Architecture: The "Tool Registry" Pattern

The app is built around an extensible **Tool Registry**. The Home Screen, search engine, and favorite system do not hardcode individual tools. They query the registry.

```
app/
├── core/
│   ├── common/             // Result wrapper, dispatchers, extensions
│   ├── designsystem/       // Theme, Typography, Color, Shape, Reusable Components
│   │   ├── components/     // AppTopBar, ToolScaffold, ResultSheet, ActionButton
│   │   └── theme/          // Color.kt, Theme.kt, Type.kt
│   ├── model/              // Tool, ToolCategory, ToolResult, ProcessState
│   ├── registry/           // ToolRegistry.kt (Central Catalog)
│   └── storage/            // SAF Helpers, UriStreamer, CacheCleaner
├── data/
│   ├── datastore/          // UserPreferences (Favorites, Recents)
│   └── repository/         // PreferencesRepository
└── features/
    ├── home/               // Main dashboard, universal search, category tabs, favorites
    ├── pdf/
    │   ├── imagestopdf/    // ImagesToPdfScreen + ViewModel
    │   └── pdftoimages/    // PdfToImagesScreen + ViewModel
    ├── image/
    │   ├── compress/       // ImageCompressorScreen + ViewModel
    │   ├── resize/         // ImageResizerScreen + ViewModel
    │   ├── converter/      // FormatConverterScreen + ViewModel
    │   └── exif/           // ExifInspectorScreen + ViewModel
    ├── qr/
    │   ├── scanner/        // QrScannerScreen + CameraX Analyzer
    │   └── generator/      // QrGeneratorScreen + ViewModel (Wi-Fi, URL, Text)
    ├── calculators/
    │   ├── percentage/     // PercentageCalculatorScreen + ViewModel
    │   ├── dateage/        // DateAgeCalculatorScreen + ViewModel
    │   └── emi/            // EmiCalculatorScreen + ViewModel
    ├── converters/
    │   └── unit/           // UnitConverterScreen (Length, Weight, Data, Temp)
    └── text/
        ├── inspector/      // TextInspectorScreen (Word/Char/Line count)
        ├── caseconverter/  // CaseConverterScreen (upper, lower, camel, snake)
        └── hash/           // HashGeneratorScreen (MD5, SHA-1, SHA-256)
```

### Tool Definition Contract
```kotlin
enum class ToolCategory(val title: String, val icon: ImageVector) {
    PDF("Documents & PDF", Icons.Default.PictureAsPdf),
    IMAGE("Image Utilities", Icons.Default.Image),
    QR("QR & Barcodes", Icons.Default.QrCode),
    CALCULATOR("Calculators", Icons.Default.Calculate),
    CONVERTER("Unit Converters", Icons.Default.SwapHoriz),
    TEXT("Text & Security", Icons.Default.TextFields)
}

data class Tool(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val route: String,
    val keywords: List<String> = emptyList() // For fast in-app search
)
```

---

## 4. File I/O & Storage Access Framework (SAF) Strategy

To guarantee **100% Play Store approval** and zero permission friction:
1. **Input Selection:**
   * Images: `ActivityResultContracts.PickMultipleVisualMedia()` (Android 13+ Photo Picker with backwards-compatible Jetpack support).
   * PDFs/Generic Files: `ActivityResultContracts.OpenMultipleDocuments()` or `OpenDocument()`.
2. **Processing Pipeline:**
   * Read source streams using `contentResolver.openInputStream(uri)`.
   * Stream directly to internal cache directory: `File(context.cacheDir, "temp_${System.currentTimeMillis()}.tmp")`.
   * Perform image/PDF transformations using bounded memory streams.
3. **Output & Export:**
   * **Direct Share Sheet:** Allow sending output to WhatsApp, Drive, Gmail via `FileProvider` (`content://` URI) with `Intent.ACTION_SEND`.
   * **Save to Device:** Use `ActivityResultContracts.CreateDocument(mimeType)` so the user selects their exact destination (Downloads, Documents, etc.) with system dialog.
4. **Cache Cleanup:**
   * Background cache cleanup runs on app launch or tool completion to prevent temporary files from piling up.

---

## 5. Detailed Feature Specifications for MVP

### 5.1 Document & PDF Tools
* **Images to PDF:**
  * Select 1 to 50 images from gallery.
  * Drag-to-reorder or list-reorder pages.
  * Quality settings: Original, Medium (70%), Compact (50%).
  * Page margins and fit options (Fit to Page, Fill Page).
  * Output: Standard `.pdf` created via native `android.graphics.pdf.PdfDocument`.
* **PDF to Images:**
  * Pick a `.pdf` file.
  * View thumbnail previews of all pages.
  * Select specific pages or "Extract All".
  * Format options: JPG or PNG.
  * Export as individual image files or share directly.

### 5.2 Image Tools
* **Image Compressor:**
  * Pick an image; inspect original file size and dimensions.
  * Presets: Target percentage reduction (25%, 50%, 75%) or target file size (e.g., "Under 500 KB").
  * Real-time preview of estimated size vs visual fidelity.
* **Image Resizer:**
  * Resize by pixel dimensions (Width × Height) with aspect ratio lock.
  * Resize by percentage (e.g., 50% scale).
* **Format Converter:**
  * Convert between JPEG, PNG, and WEBP.
* **EXIF Metadata Inspector & Stripper:**
  * View metadata: Camera model, exposure, focal length, timestamp, GPS location.
  * "Remove Metadata" action generates a sanitized copy without sensitive location tags.

### 5.3 QR & Barcode Tools
* **QR & Barcode Scanner:**
  * CameraX viewfinder with real-time ML Kit analysis.
  * Auto-detect format (URL, Wi-Fi, Text, Contact, EAN-13, Code 128).
  * Contextual actions: "Open Link", "Copy Text", "Connect to Wi-Fi", "Share".
  * "Scan from Gallery Image" option.
* **QR Code Generator:**
  * Input types:
    * **Wi-Fi:** Network name (SSID), Security type (WPA/WPA2/WEP/None), Password.
    * **Web Link (URL).**
    * **Plain Text.**
  * High-res preview with "Save Image (PNG)" and "Share QR".

### 5.4 Calculators
* **Percentage Calculator:**
  * Three modes: "What is X% of Y?", "X is what % of Y?", "% increase / decrease from X to Y".
* **Date & Age Calculator:**
  * Exact age in years, months, and days from date of birth.
  * Duration/difference between two selected calendar dates.
  * Add/subtract days from a date.
* **Loan / EMI Calculator:**
  * Principal amount, annual interest rate, tenure (months/years).
  * Calculates: Monthly EMI, Total Interest Payable, Total Amount.

### 5.5 Unit Converters
* **Unified Clean Grid Converter:**
  * **Length:** mm, cm, m, km, inch, foot, yard, mile.
  * **Mass/Weight:** mg, g, kg, metric ton, ounce, pound.
  * **Temperature:** Celsius, Fahrenheit, Kelvin.
  * **Digital Data:** B, KB, MB, GB, TB, PB (Binary & Decimal standards).

### 5.6 Text & Security Tools
* **Text Inspector:**
  * Real-time counters: Words, Characters (with and without spaces), Sentences, Lines.
  * Estimated Reading Time and Speaking Time.
* **Case Converter:**
  * Convert input text into UPPERCASE, lowercase, Title Case, camelCase, snake_case, kebab-case.
  * One-tap copy to clipboard.
* **Hash & Checksum Generator:**
  * Generate cryptographic hashes for text or verification: MD5, SHA-1, SHA-256.

---

## 6. Implementation Milestones

### Milestone 1: Foundation & Design System (Sprint 1)
- [ ] Initialize Android project with Kotlin 2.0, Compose, and Gradle version catalog (`libs.versions.toml`).
- [ ] Configure Material 3 theme (dynamic color, dark mode, typography, shapes).
- [ ] Implement `Tool` model, `ToolCategory`, and central `ToolRegistry`.
- [ ] Build Home Screen with:
  - Universal Search bar (instant filter by name & keywords).
  - Category selector/chips.
  - Pinned Favorites row (backed by DataStore).
  - Responsive tools grid.
- [ ] Set up Compose Navigation graph.

### Milestone 2: Offline Calculators, Converters & Text Utilities (Sprint 2)
- [ ] Build Text Inspector, Case Converter, and Hash Generator.
- [ ] Build Percentage, Date/Age, and Loan/EMI Calculators.
- [ ] Build Multi-category Unit Converter.
- [ ] Verify zero-lag UI responsiveness and keyboard interaction.

### Milestone 3: QR & Barcode Engine (Sprint 3)
- [ ] Add CameraX + Google ML Kit Barcode Scanning dependencies.
- [ ] Build `QrScannerScreen` with viewfinder overlay and permission handling.
- [ ] Implement QR scan result bottom sheet (with quick actions: open URL, connect Wi-Fi, copy).
- [ ] Implement Gallery image QR scanning.
- [ ] Build `QrGeneratorScreen` using ZXing Core (Wi-Fi, URL, Text) with export & share options.

### Milestone 4: Image Utilities & EXIF Stripper (Sprint 4)
- [ ] Implement Android Photo Picker integration.
- [ ] Build background bitmap processing pipeline (coroutine-based downsampling & stream writing).
- [ ] Build Image Compressor (quality slider & target size estimator).
- [ ] Build Image Resizer & Format Converter (JPG/PNG/WEBP).
- [ ] Build EXIF Inspector & Privacy Stripper using AndroidX `ExifInterface`.
- [ ] Build unified "Result Screen" with "Save to Storage" (SAF) and "Share".

### Milestone 5: Document & PDF Utilities (Sprint 5)
- [ ] Build Images-to-PDF tool using native `android.graphics.pdf.PdfDocument`.
- [ ] Implement image reordering and preview list.
- [ ] Build PDF-to-Images tool using `android.graphics.pdf.PdfRenderer`.
- [ ] Add thumbnail generation and batch page extraction.

### Milestone 6: Quality Gates, Memory Testing & Release Prep (Sprint 6)
- [ ] OOM stress test: Test converting 30 high-resolution camera photos to PDF.
- [ ] Android 9.0 through Android 15 compatibility validation.
- [ ] Edge-to-edge system bar compliance (`enableEdgeToEdge`).
- [ ] Configure Proguard/R8 rules to optimize APK size and strip unused code.
- [ ] Prepare app icon, splash screen, and Play Store metadata.

---

## 7. Quality Gates & Risk Mitigation

| Risk | Impact | Mitigation Strategy |
| :--- | :--- | :--- |
| **`OutOfMemoryError` during Image/PDF operations** | App Crash | Always decode using `inSampleSize`. Stream directly to file cache using buffers ($16\text{ KB}$), never hold multi-megapixel bitmaps in memory arrays. |
| **Play Store Rejection for Storage Access** | Cannot Publish | Strictly use Storage Access Framework (`ActivityResultContracts`). Never declare `READ_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`. |
| **Camera Permission Friction** | User Abandonment | Camera permission is requested **only** when tapping "Scan with Camera", with an educational rationale and fallback to "Pick from Gallery". |
| **UI Lag during Heavy Math / Image Tasks** | ANR / Stutter | All file transformations, hashing, and conversions execute strictly on `Dispatchers.Default` or `Dispatchers.IO`. UI retains 60/120fps progress states. |
