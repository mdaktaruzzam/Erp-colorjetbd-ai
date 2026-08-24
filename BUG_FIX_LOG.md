# COLORJET Management Suite - Bug Fix Log

This log chronicles the precise code adjustments and engineering modifications implemented during development.

## Bug Fix Entries

### 1. [FIXED] KPICard Overload & Layout Separation (ERR-01)
- **Problem:** The original `KPICard` function was modified, causing compile-time failures across 30+ dashboard cards due to missing or mismatched positional arguments.
- **Resolution:** Implemented an overloaded `KPICard` constructor in `KPICard.kt` that maintains backwards compatibility with standard signatures (`title`, `value`, `icon`, `iconColor`, `modifier`, `onClick`). The layout has been moved from inline code blocks in `DashboardScreen.kt` to its own dedicated UI file (`com/example/ui/KPICard.kt`) for better maintainability.

### 2. [FIXED] Seeding Brand Protection (ERR-02)
- **Problem:** Supplier seeding in the database pointed to Indian competitors, violating the strict brand ownership rule defined by MD Aktaruzzaman.
- **Resolution:** Replaced all Indian references in `InventoryData.kt` and `OwnerForms.kt` with realistic China and Singapore digital printing supplier networks.

### 3. [FIXED] Splashscreen Runtime Initialization (ERR-03)
- **Problem:** The app crashed upon loading because of a mismatched theme and missing Jetpack SplashScreen API integration.
- **Resolution:** 
  1. Added `implementation(libs.androidx.core.splashscreen)` inside `app/build.gradle.kts`.
  2. Integrated `Theme.App.Starting` in `themes.xml` with parent `Theme.SplashScreen`.
  3. Set `android:theme="@style/Theme.App.Starting"` on `.MainActivity` inside `AndroidManifest.xml`.
  4. Invoked `installSplashScreen()` in `MainActivity.kt` immediately before `super.onCreate()`.
