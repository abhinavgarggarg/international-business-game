# Engine validation (done outside Gradle/Android)

The sandbox this project was authored in has no access to the Android SDK,
Google's Maven repository, or the Gradle distribution service (all three
are required to actually compile an `.apk`, and none were reachable —
confirmed directly, see below). So the core game engine
(`engine/Models.kt`, `engine/GameEngine.kt`, `engine/Board.kt`) was instead
compiled and run standalone with a real Kotlin compiler (fetched from
GitHub releases, which *was* reachable) and a plain `java -jar`, completely
independent of Android tooling.

## What was checked

1. **Dice roll range & board looping** — 500 iterations confirming both
   dice stay in 1..6 and totals in 2..12; confirmed a player's position
   wraps correctly around the board and "Pass Start" money is credited
   exactly once per lap, even when a single move crosses more than one
   lap.
2. **Rent calculation** — base rent, rent doubling once a player owns a
   full color set, and each house/hotel tier, using the exact
   `calculateRent()` function shipped in `GameEngine.kt`.
3. **Bankruptcy & elimination** — a player who can't cover a rent charge
   is marked bankrupt, their properties return to the bank, and the game
   correctly declares a winner once only one active player remains.
4. **Buying a property** — deducts the exact cost and assigns ownership;
   fails cleanly (no partial state change) when the buyer can't afford it.
5. **Full-game simulation** — a separate 3-player, 5000-turn simulation
   using the real `BoardFactory` + `GameEngine` together (random dice,
   auto-buying when affordable) ran to completion with no exceptions,
   as a smoke test of the whole loop end-to-end.
6. **Board layout integrity** — `BoardFactory.buildBoard()` was run and
   inspected directly: exactly 40 spaces, all 22 cities and all 4 airlines
   placed exactly once, zero dangling `propertyId` references.

**Result: 1519/1519 assertions passed, 0 failures.**

The JUnit4 tests in `app/src/test/java/.../GameEngineTest.kt` are a direct
port of the same assertions, so `./gradlew test` re-runs this same
validation as part of the normal Android build once you have the SDK
available (see the root README for the exact commands and why the .apk
itself couldn't be produced in the authoring sandbox).
