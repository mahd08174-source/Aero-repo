# Aero Client
Fabric 1.21.11 client. Meteor Client is bundled inside the jar (jar-in-jar), so it is a single file; it needs **Fabric API** next to it.
Do NOT also install a separate Meteor jar (duplicate mod id).

- **Right Shift** opens/closes the Aero ClickGUI (COMBAT / MISC / DONUT / RENDER), light-blue theme, Xuong font.
- Left click = toggle, right click = that module's settings, drag header = move panel.
- Includes the full P3nis module set (finders, ESPs, spawner tools, staff tools, auto relog/rtp, freecam, Spotify HUD, region map, ...).
- Not included: FakePay, FakeScoreboard (balance/stat forger) and FakeSpawner. `aero-scoreboard` is a plain custom overlay instead.

Build: `./gradlew build` (JDK 21) -> `build/libs/aero-client-1.0.0.jar` -> `.minecraft/mods` with Fabric API.
Font: TNH Xuong, SIL OFL (assets/aero/OFL-Xuong.txt).
