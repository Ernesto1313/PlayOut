```markdown
# PlayOut 🏃

A native Android application for discovering, exploring and evaluating
sports and leisure facilities in Aachen, Germany.

Born from a real need detected during an Erasmus stay: many facilities
are poorly documented, hard to find, or lack up-to-date information
about their condition and availability.

## Features

- 🗺️ Interactive map with geolocated facility markers
- 📋 Filterable list by sport category
- 📍 Detail view with photos, condition, amenities and ratings
- 🧭 One-tap navigation to any facility via Google Maps
- ⭐ User experience rating system (1–5)
- 💧 Amenity indicators: water fountains, seating areas
- 📡 Offline-first — no internet connection required
- 📸 Photo gallery per facility (field data collected with QField)

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Repository pattern
- **Database:** Room (prepopulated from QGIS GeoPackage)
- **Maps:** Google Maps Compose
- **DI:** Hilt
- **Images:** Coil
- **Async:** Kotlin Coroutines + StateFlow
- **Field data collection:** QGIS + QField

## Data

Facility data was collected through original field work in Aachen using
QField for on-site inventory. Each facility includes geolocation,
condition assessment, available amenities and photographic documentation.

## Project Structure

```
com.ernesto.playout/
├── data/
│   ├── model/          # Room entities
│   ├── db/             # DAOs and AppDatabase
│   └── repository/     # Repository layer
├── ui/
│   ├── map/            # Map screen + ViewModel
│   ├── list/           # List screen + ViewModel
│   ├── detail/         # Detail screen + ViewModel
│   ├── onboarding/     # Onboarding flow
│   └── components/     # Shared composables
└── di/                 # Hilt modules
```

## Requirements

- Android 9.0 (API 28) or higher
- Google Play Services (for Maps)

## Author

**Ernesto Gimeno García**  
Computer Engineering · Universitat de València  
[github.com/Ernesto1313](https://github.com/Ernesto1313)

## License

This project was developed as a Final Year Project (TFG) at the
Escola Tècnica Superior d'Enginyeria — Universitat de València.
```
