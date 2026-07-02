# LEXKART 🚀
An elegant, production-ready, offline-first Material 3 E-Commerce shopping application for Android. 

LEXKART is a fully featured Android e-commerce store built entirely with **Kotlin**, **Jetpack Compose**, and **Clean Architecture (MVVM)**. It leverages **Room Database** for high-performance localized persistence and integrates seamlessly with **Firebase Firestore** for scalable enterprise cloud synchronization.

---

## 🔥 Features

### 🛍️ Smart Shopping Experience
- **Interactive Shopfront:** Modern product browsing categorized by departments with support for smooth scrolling, search, and intuitive filters.
- **Wishlist & Cart:** Seamlessly add items to a persistent shopping cart or save them to your wishlist for later. 
- **Checkout Process:** Quick sandbox checkout flow that simulates item ordering, notification updates, and local database storage.

### 🛡️ Secure User Portal
- **Secure Authentication:** User registration and login screens, designed with Material 3 responsive entry flows.
- **Autofill Safe Sandbox:** Features an automated developer credential autofill for immediate test logins (`user@example.com` / `password123`).
- **Profile Center:** Customize user information and display active session statistics.

### 📊 Admin Panel & Advanced Analytics
- **Dynamic Inventory Controls:** Secure Portal for administrators to insert new products, edit pricing, update categories, and manage live stock quantities.
- **Analytics Dashboard:** Visual indicators and summary charts highlighting sales performance and user metrics.

### ☁️ Cloud Sync & Database Fault Tolerance
- **Offline-First Room DB:** Features localized caching using Android Jetpack Room with custom SQLite integrations under the hood.
- **Automatic Firestore Mirroring:** Sync and back up user profiles, products, and order data in real time with high-performance Firestore collections when connected.
- **Graceful Fallback:** Programmed to operate normally offline using localized SQLite databases if internet access or Firebase configurations are missing.

---

## 🛠️ Tech Stack & Key Libraries

- **UI Framework:** [Jetpack Compose](https://developer.android.com/compose) - Modern declarative UI toolkit for Android.
- **Programming Language:** [Kotlin](https://kotlinlang.org/) - Modern, expressive, and safe.
- **Local Persistence:** [Room SQLite](https://developer.android.com/training/data-storage/room) - Native offline-first database mapping.
- **Cloud Database:** [Firebase Firestore](https://firebase.google.com/docs/firestore) - Remote scalable NoSQL database.
- **Structured Architecture:** MVVM (Model-View-ViewModel) paired with Compose `StateFlow` reactive pipelines.
- **Build System:** Gradle (Kotlin DSL - `.gradle.kts`) with central **Version Catalogs (`libs.versions.toml`)**.
- **Unit Testing:** [Robolectric](https://github.com/robolectric/robolectric) for local JVM-based unit and state lifecycle checks.

---

## ⚡ Getting Started & Quick Setup

To configure NEXKART on your local machine or build environment, follow these simple steps:

### 1. Prerequisites
- Android Studio Ladybug (or newer versions).
- Android SDK version 34 (Compile & Target).
- Java Development Kit (JDK) 17.

### 2. Clone the Repository
```bash
git clone https://github.com/Serenity-02/NEXKART.git
cd NEXKART
```

### 3. Firebase Configuration (Optional)
To activate remote Firestore mirroring and manual cloud backups:
1. Create a project in the [Firebase Console](https://console.firebase.google.com).
2. Register your Android application with package name `com.example`.
3. Download the `google-services.json` config file.
4. Place `google-services.json` inside the `/app` directory.
5. Rebuild and launch the applet!

*Note: If no `google-services.json` is supplied, NEXKART detects the absence automatically and transitions into Local Room Sandbox mode with zero crashes.*

---

## 🏗️ Building & Run

To build the application manually via CLI:

```bash
# Compile and build debug APK
gradle assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```

---

## 📂 Architecture & Directory Structure

```text
/app/src/main/java/com/example
├── MainActivity.kt           # Main Entry Point hosting the Edge-To-Edge Container
├── data                      # Data Layer
│   ├── db                    # Room Database, DAOs, and Seeding Utilities
│   ├── firebase              # Firebase Firestore Service and Manager
│   ├── model                 # Entity blueprints (User, Product, Order, etc.)
│   └── repository            # Shopping Repository combining Local/Remote operations
└── ui                        # Presentation Layer
    ├── screens               # Responsive Compose View layouts (Shop, Profile, Cart, Admin)
    ├── theme                 # Material 3 Custom Colors, Dynamic Scheme, and Typography
    └── viewmodel             # Stateflow-equipped MVVM ViewModels
```

---

## 📄 License
This project is licensed under the Apache License 2.0. Feel free to use and adapt it for your retail or testing applications!
