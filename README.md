# Calorie Counter

## Overview
Android application for tracking meals using a local Gemma model. The first implementation step is a durable analysis queue.

## App loop

1. The user submits a photo or text, for example `pomidor 5 g`.
2. `AnalysisQueue` stores an `AnalysisJobEntity` in SQLite and schedules a `WorkManager` job.
3. `MealAnalysisWorker` loads the job, marks it as `PROCESSING`, and sends it to `MealAnalyzer`.
4. A successful result is stored as `MealSummaryEntity`; failed jobs retain an error and can be retried later.
5. The UI observes `MealRepository.observeSummaries()` and displays the history until the user deletes an item.
6. The **Ruch** tab can track a walk or run using the device step counter and GPS, display the route on OpenStreetMap, and save the session locally.

The `GemmaMealAnalyzer` keeps Room and WorkManager independent of the LiteRT-LM model SDK.

## On-device meal analysis

Photo analysis uses the Gemma 4 E4B instruction-tuned LiteRT-LM model locally. The model is about 3.66 GB and is not bundled in the APK. Download `gemma-4-E4B-it.litertlm` from the [LiteRT Community model repository](https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm), then copy it into the installed app's private files directory at:
in android studio(connect youre phone via usb debug) -> Device Explorer -> data -> data -> search com.example.cal -> files -> create models catalog and upload the model into the katalog(the model is also in the `modelfolder` for conviniance)
```text
files/models/gemma-4-E4B-it.litertlm
```

For a debug installation, use Android Studio's Device Explorer to create the `models` directory under the app's `data/data/com.example.caloriecounter/files/` directory and copy the file there. The application reports a missing-model state in scan details and allows the analysis to be retried after installing the file. No network connection is used for inference.

Gemma 4 E4B requires several gigabytes of available storage and memory; performance and compatibility depend on the device. Nutrition values are model-generated estimates, not measurements or a substitute for verified nutritional information.

## Stored data

- input type and original text or image bytes,
- processing status and failure message,
- calories as `Int`, macros as `Float` grams,
- vitamins as structured JSON until a stable nutrient schema is selected,
- original image bytes attached to the resulting summary.
- activity type, elapsed time, step count, GPS distance, estimated calories, and route coordinates for completed activity sessions.

## Activity tracking

Open **Ruch**, choose a walk or run, enter body weight, and start tracking. The app requests precise location access and continues tracking through a location foreground service until the session is stopped. A device step-counter sensor is needed to count distance: a GPS segment is accepted only when steps were recorded during that interval, GPS accuracy is at most 35 m, and the calculated speed is at most 8 m/s. This reduces false distance while driving; it is a heuristic, not reliable transport-mode detection.

Calories are an estimate based on activity type, duration, and entered weight (MET 3.5 for walking and 8.3 for running). Completed sessions and their route coordinates are stored on-device. The activity tab requests location access so it can show the user's position on the map; GPS must also be enabled to record a route. Map tiles are loaded online from OpenStreetMap with an app-identifying User-Agent and local tile caching. Attribution is provided on the map, and tiles may be unavailable offline. Tile use follows the [OpenStreetMap Tile Usage Policy](https://operations.osmfoundation.org/policies/tiles/); the public tile server may refuse requests from networks it has blocked.

## Project Structure
The project is organized into several packages and directories:

- **data**: Contains data-related classes, including models for calorie data and data sources (e.g., local database or API).
- **domain**: Contains domain logic, including use cases for calorie tracking and image recognition.
- **ui**: Contains UI-related classes, including activities and fragments for displaying calorie information and user interactions.
- **recognition**: Contains classes related to image recognition, such as models and utilities for processing images and recognizing food items.

## Setup Instructions
### Requirements

- Android Studio with Android SDK Platform 36 and Android Build Tools 36.1.0.
- JDK 17 or newer. The JDK bundled with Android Studio is recommended.
- An Android emulator or a device with USB debugging enabled to run the app.

### Open and run

1. Clone the repository and open its root folder in Android Studio.
2. Set the Gradle JDK to the Android Studio bundled JDK, then sync the project. Android Studio can download missing SDK components and creates the machine-specific `local.properties` file.
3. Select an emulator or connected device and click **Run**.

To build from a terminal, use the Gradle Wrapper included in the repository:

```powershell
gradlew.bat :app:assembleDebug
```

On macOS or Linux, run `./gradlew :app:assembleDebug` instead.

## Usage
- Launch the app and navigate to the camera feature.
- Take a picture of your meal.
- The app will process the image and display the recognized food items along with their calorie counts.
- You can correct the response/add aditional information to the image process stream via a prompt box 

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the Apache License, Version 2.0. See the LICENSE file for details.