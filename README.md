# Pattern 

**Pattern** is a native Android personal analytics application that helps users understand their daily routines through data.

Instead of simply recording habits, Pattern analyzes historical data to establish a **personal baseline**, identify unusual patterns, visualize trends, and discover relationships between different daily metrics.

## Features

-  **Personal Analytics Dashboard** — View your recent activity and overall Pattern Score.
-  **Daily Data Logging** — Track sleep, screen time, study, exercise, mood, and productivity.
-  **Anomaly Detection** — Identify values that are unusually different from your personal baseline.
-  **Trend Analysis** — Visualize changes across 7, 14, and 30-day periods.
-  **Correlation Analysis** — Explore relationships between metrics such as sleep and productivity.
-  **History** — Review, edit, and delete previous daily records.
-  **Local Persistence** — Data is stored locally using Room Database.
-  **Professional Dark UI** — Built with a clean, modern Android interface.
-  **Statistical Testing** — Core analytical functions include tests for edge cases and statistical accuracy.

##  How It Works

Pattern uses the user's historical data to create a **personal baseline** rather than relying on fixed population-wide thresholds.

For anomaly detection, the application uses statistical analysis based on the **z-score**:

```text
z = |value - baseline mean| / standard deviation
```

The application classifies observations as:

| Classification | Z-score |
|---|---:|
| Normal | < 2.0 |
| Unusual | 2.0 – < 2.8 |
| Very Unusual | ≥ 2.8 |

When evaluating a particular observation, that observation is excluded from its own baseline calculation to avoid influencing its anomaly score.

When there is insufficient historical data, Pattern uses a percentage-deviation fallback.

## 📊 Metrics Tracked

Pattern currently analyzes:

- Sleep duration
- Screen time
- Study time
- Exercise duration
- Mood
- Productivity

These metrics can be viewed individually through charts and statistical summaries.

##  Technology Stack

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Room Database**
- **Android Architecture Components**
- **ViewModel**
- **Kotlin Coroutines**
- **Jetpack Navigation**
- **Custom statistical analysis**
- **Data visualization**

##  Architecture

Pattern follows a layered Android architecture:

```text
┌──────────────────────────┐
│     Jetpack Compose UI   │
└────────────┬─────────────┘
             │
┌────────────▼─────────────┐
│       ViewModels         │
└────────────┬─────────────┘
             │
┌────────────▼─────────────┐
│       Repository         │
└────────────┬─────────────┘
             │
┌────────────▼─────────────┐
│       Room Database      │
└──────────────────────────┘

        Analytics Layer
              │
     ┌────────▼────────┐
     │ Statistical     │
     │ Analysis        │
     ├─────────────────┤
     │ Mean            │
     │ Median          │
     │ Std. Deviation  │
     │ Z-Score         │
     │ Correlation     │
     │ Anomaly Score   │
     └─────────────────┘
```

##  Getting Started

### Requirements

- Android Studio
- JDK 17+
- Android SDK
- Android device or emulator

### Installation

Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
```

Open the project in Android Studio and allow Gradle to synchronize.

Then run the application on an Android emulator or connected Android device.

## 🧪 Testing

The project includes tests for core statistical functionality and important edge cases, including:

- Mean and median calculations
- Standard deviation
- Z-score calculations
- Anomaly thresholds
- Baseline exclusion
- Zero standard deviation
- Insufficient historical data
- Pearson correlation
- Pattern Score boundaries

## 🔒 Privacy

Pattern is designed around local-first data storage.

Daily personal data is stored locally using Room Database and does not require a user account or cloud backend.

## 🎯 Project Purpose

Pattern was built as a practical exploration of:

- Native Android development
- Kotlin and Jetpack Compose
- Local database architecture
- Statistical data analysis
- Anomaly detection
- Data visualization
- Correlation analysis
- Clean and maintainable application architecture

The goal was to build a small application where **data analysis is part of the core product**, rather than simply creating another CRUD-based mobile application.

## 📌 Future Improvements

Potential future enhancements include:

- More advanced anomaly detection algorithms
- Exporting personal data as CSV
- Additional metrics
- Custom metric creation
- Longer-term trend analysis
- Optional cloud backup
- More advanced statistical models

## 📄 License

This project is available for educational and personal use.
