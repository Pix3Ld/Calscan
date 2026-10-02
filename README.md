# Calorie Counter

## Overview
Android application for tracking meals using a local Gemma model. The first implementation step is a durable analysis queue.

## App loop

1. The user submits a photo or text, for example `pomidor 5 g`.
2. `AnalysisQueue` stores an `AnalysisJobEntity` in SQLite and schedules a `WorkManager` job.
3. `MealAnalysisWorker` loads the job, marks it as `PROCESSING`, and sends it to `MealAnalyzer`.
4. A successful result is stored as `MealSummaryEntity`; failed jobs retain an error and can be retried later.
5. The UI observes `MealRepository.observeSummaries()` and displays the history until the user deletes an item.

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