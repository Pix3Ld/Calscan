# Calorie Counter

## Overview
Android application for tracking meals using a local Gemma model. The first implementation step is a durable analysis queue.

## App loop

1. The user submits a photo or text, for example `pomidor 5 g`.
2. `AnalysisQueue` stores an `AnalysisJobEntity` in SQLite and schedules a `WorkManager` job.
3. `MealAnalysisWorker` loads the job, marks it as `PROCESSING`, and sends it to `MealAnalyzer`.
4. A successful result is stored as `MealSummaryEntity`; failed jobs retain an error and can be retried later.
5. The UI observes `MealRepository.observeSummaries()` and displays the history until the user deletes an item.

The `GemmaMealAnalyzer` is intentionally an adapter boundary. The exact Gemma 4 E4B Android runtime and its image/text prompt contract should be added there, without coupling Room or WorkManager to the model SDK.

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
1. **Clone the repository**:
   ```
   git clone <repository-url>
   cd calorie-counter-app
   ```

2. **Open the project** in your preferred IDE.

3. **Build the project**:
   ```
   ./gradlew build
   ```

4. **Run the application** on an emulator or physical device.

## Usage
- Launch the app and navigate to the camera feature.
- Take a picture of your meal.
- The app will process the image and display the recognized food items along with their calorie counts.
- You can correct the response/add aditional information to the image process stream via a prompt box 

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License. See the LICENSE file for details.