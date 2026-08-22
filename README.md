# TrainLog Analyzer (Android)

Lab notebook for AI training runs — feature parity goals with the web app, plus researcher tools.

## Features

1. **Import log** — paste HF Trainer / train/loss logs → series, plateau, new run
2. **Loss charts** — train/eval curves on detail + import
3. **Compare runs** — side-by-side metrics
4. **Downstream checklist** — multi-task fields + forgetting note
5. **Budget / ETA** — sec/step in Lab FLOPs tab + import
6. **Scheduler playground** — cosine, linear, WSD, restarts
7. **Data mixture planner** — weights → tokens, D/N ratio
8. **Checkpoint picker** — notes `name:loss`, auto best
9. **Experiment notes** — hypothesis / change / parent run fields
10. **Report export** — PDF share + Markdown share
11. **Plateau alerts** — notification when import detects plateau
13. **Architecture presets** — model in data layer (`ArchPresets`)
14. **Unit tests** — `CalcTest` / `LogImporter` (JUnit)

*(12 cloud sync intentionally not included.)*

## Open

Android Studio → open `TrainLogAnalyzer` → Sync → Run (min SDK 26).

```bash
./gradlew test
./gradlew assembleDebug
```
