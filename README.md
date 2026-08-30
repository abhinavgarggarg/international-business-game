# International Business — Android (Kotlin + Jetpack Compose)

A kid-friendly, Monopoly-style board game with a fully automated digital
banker: no manual money math, animated wallets, a sliding property
portfolio, tap-to-roll dice, and auto-save after every turn.

## Why there's no `.apk` in this download

Producing a real Android `.apk` requires the Android SDK build tools plus
network access to **Google's Maven repository** (`dl.google.com` /
`maven.google.com`) and the **Gradle distribution service**
(`services.gradle.org`) — the Android Gradle Plugin and every `androidx.*`
dependency come from there. The sandbox this project was built in has
neither installed, and its network allowlist doesn't include those hosts
(confirmed directly — both return `403 host_not_allowed`). That's a fixed
property of the authoring environment, not something retry or a different
framework choice gets around: the same blocker applies whether the app is
written in Kotlin or Flutter, because both ultimately need the same Android
toolchain to produce an `.apk`.

What *was* possible in that sandbox: the Kotlin compiler itself is
distributed via GitHub releases, which **is** reachable, so the entire game
engine (dice, board movement, rent, bankruptcy, buying) was written,
compiled, and run for real — see `VALIDATION.md` for the full test run
(1519/1519 assertions passed) plus a 5000-turn full-game simulation with no
crashes. That validated engine is exactly what's wired into the Compose UI
in this project.

## Build it yourself (any machine with normal internet access)

You'll need [Android Studio](https://developer.android.com/studio)
(Koala or newer) **or** just a JDK 17+ and the command line:

```bash
# From the project root:
./gradlew assembleDebug
# APK will be at: app/build/outputs/apk/debug/app-debug.apk

# Run the unit tests (same assertions already validated standalone — see VALIDATION.md):
./gradlew test

# Install straight to a connected device/emulator:
./gradlew installDebug
```

Or in Android Studio: **Open** this folder → let it sync Gradle → **Run ▶**.

## What's implemented

- **Digital wallet & bank** — every player's balance is always visible and
  animates up/down (`WalletBar.kt`); the engine (`GameEngine.kt`) is the
  sole source of truth for money, so there's no manual math anywhere.
- **Property portfolio drawer** — sliding bottom sheet grouped by color
  set, showing upgrade status per property (`PropertyDashboard.kt`).
- **Visual board** — 40 spaces (22 world cities across 8 color groups, 4
  airlines, taxes, Chance/Surprise, Jail) laid out around a square
  perimeter (`BoardScreen.kt`), with player token dots.
- **Tap to roll** — animated dice with a spin, auto-prompts "Buy" or shows
  the rent that was just paid (`DiceRoller.kt`, `GamePrompts.kt`).
- **Banker automation** — buying, rent transfer, bankruptcy, and win
  detection are all enforced inside `GameEngine.kt`; the UI only ever
  displays what already happened, it never computes money itself.
- **Auto-save** — every turn is serialized to a local Room database
  (`GameSaveEntity.kt`, `GameStateSerializer.kt`) so the game resumes
  where you left off after closing the app.
- **Automated tests** — `app/src/test/java/.../GameEngineTest.kt` (JUnit4)
  covering dice/board-looping, rent calculation with monopolies and
  upgrades, and bankruptcy/elimination, per the QA requirements. These are
  a direct port of the assertions already run and passed outside Gradle
  (see `VALIDATION.md`).

## Project layout

```
app/src/main/java/com/soprasteria/ib/
  engine/       — pure Kotlin game engine (no Android deps): Models, GameEngine, Board
  data/         — Room entity/DAO + JSON save/restore + repository
  ui/           — GameViewModel (StateFlow) + Compose screens/components
app/src/test/java/com/soprasteria/ib/engine/
  GameEngineTest.kt — JUnit4 port of the validated engine tests
VALIDATION.md   — the standalone compiler run this engine was checked against
```

## Known simplifications (fair to flag)

- Chance/Surprise spaces are laid out on the board but don't yet draw a
  card — landing on one is currently a no-op. Wiring in a card deck is a
  natural next step and slots into `GameEngine.evaluateSpace()`.
- Jail is currently a "pass through" space (landing on Go-To-Jail moves
  you there per `sendToJail()`, but there's no roll-to-get-out-of-jail
  turn logic yet).
- House/hotel *purchasing* isn't wired into the UI yet (the engine
  supports arbitrary `houses` values and rent calculates correctly off
  them — `buyProperty`/rent are live; a "build a house" button is the
  remaining piece).
- No trading between players yet.

None of these affect the parts that were specifically asked to be
tested (dice, board looping, rent with upgrades/monopolies, bankruptcy) —
those are complete and validated.
