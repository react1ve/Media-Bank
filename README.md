# Media bank

Media bank is an application for viewing images and videos organized into albums. It allows users to
easily browse, organize, and share media files stored on their device.

## Features

- **Albums**: View images and videos organized by folders. Each folder displays its name and the
  number of media files.
- **All Images**: View all images on the device in one place, excluding cached files, thumbnails,
  and non-media files.
- **All Videos**: View all videos on the device in one place.
- **Camera**: View all photos taken with the device's camera.
- **Detail View**: View an image or video in full size with the option to share the file.

## Technologies

### Languages and Tools

- **Kotlin**: The primary language for application development.
- **Jetpack Compose**: A modern toolkit for building native UI on Android.
- **ExoPlayer**: A library for playing video and audio on Android.
- **Coil**: An image loading library for Jetpack Compose.

### Architecture

- **MVVM (Model-View-ViewModel)**: Implementing the architectural pattern to separate the logic and
  the UI.

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later.
- Kotlin 1.5.21 or later.

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/react1ve/Mediabank.git
   ```

2. Open the project in Android Studio.

3. Build the project and run it on an Android device or emulator.

### Permissions

The application requires the following permissions:

- **READ_EXTERNAL_STORAGE**: To read images and videos from the device storage.