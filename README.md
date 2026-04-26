# Location Reminder Application

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-007396?style=for-the-badge)
![Build](https://img.shields.io/badge/Build-Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Maps](https://img.shields.io/badge/Maps-OpenStreetMap-7EBC6F?style=for-the-badge&logo=openstreetmap&logoColor=white)

Location Reminder Application is an Android app for creating reminders linked to real geographic coordinates. The app monitors the user's GPS position and displays a reminder message when the user reaches the saved location.

## Project Overview

The goal of this project is to demonstrate how a mobile app can combine screen navigation, GPS access, local data storage, distance calculation, and map visualization to create a simple location-aware reminder system.

The user can log in, save a reminder with a title and target coordinates, view the current GPS location, see the current position on a map, and receive an alert when the saved location is reached.

## Core Features

| Feature | Description |
| --- | --- |
| Login Screen | Entry screen that validates user input and opens the app home screen. |
| Home Navigation | Central screen with navigation to reminder creation and current GPS location. |
| Add Reminder | Saves a reminder title, latitude, and longitude. |
| GPS Location | Reads the user's current latitude and longitude using GPS. |
| Map Display | Shows the current location on OpenStreetMap inside a `WebView`. |
| Reminder Detection | Calculates distance to the saved reminder location and alerts the user when nearby. |
| Local Storage | Stores reminder data locally using `SharedPreferences`. |

## App Flow

```text
Login Screen
     |
     v
Home Screen
     |
     |-- Add Reminder Screen
     |       |-- title
     |       |-- latitude
     |       |-- longitude
     |
     |-- Current GPS Location Screen
             |-- latitude / longitude
             |-- map preview
```

## Screens

Screenshots will be added after final testing on a physical device or emulator.

| Login | Home | Add Reminder | Current Location |
| --- | --- | --- | --- |
| _Coming soon_ | _Coming soon_ | _Coming soon_ | _Coming soon_ |

### Login Screen

The login screen is the first screen of the app. It validates user input and navigates to the home screen using an Android `Intent`.

### Home Screen

The home screen is the main navigation area. It displays the saved reminder, monitors GPS updates, calculates the distance to the saved location, and shows an alert when the user reaches the reminder area.

### Add Reminder Screen

The add reminder screen lets the user enter:

- Reminder title
- Latitude
- Longitude

The app validates that latitude and longitude are valid numeric coordinates before saving the reminder.

### Current GPS Location Screen

The current location screen displays the user's current GPS coordinates and shows the same location on a map using OpenStreetMap.

## Technical Implementation

### Main Components

| File | Responsibility |
| --- | --- |
| `MainActivity.java` | Handles the login screen and navigation to the home screen. |
| `HomeActivity.java` | Provides home navigation and monitors the saved reminder location. |
| `AddReminderActivity.java` | Handles reminder creation and coordinate validation. |
| `CurrentLocationActivity.java` | Displays current GPS coordinates and the map. |
| `ReminderStorage.java` | Saves and reads reminder data with `SharedPreferences`. |

### Android Concepts Used

- Activities
- Intents
- Runtime permissions
- GPS with `LocationManager`
- `LocationManager.GPS_PROVIDER`
- `Location.distanceBetween`
- `SharedPreferences`
- `WebView`
- OpenStreetMap

### Permissions

The app uses these permissions:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

`ACCESS_FINE_LOCATION` is used for GPS-based location. `INTERNET` is used to load the OpenStreetMap page inside the app.

## Reminder Logic

The app saves the reminder coordinates locally. While the home screen is active, it requests GPS updates and compares the current location with the saved reminder location.

Distance is calculated using:

```java
Location.distanceBetween(...)
```

When the user is within 50 meters of the saved location, the app displays an alert dialog containing the reminder message.

## Project Status

The application currently includes the complete lab functionality:

- Login flow
- Home navigation
- Reminder creation
- GPS coordinate display
- Map display
- Location-based reminder alert
- Improved visual interface

The installable APK package will be made available later after final testing and packaging.

## Academic Context

This project was developed as a lab project for the Mobile Application module at the University of Mohamed Khider Biskra.
