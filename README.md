# Aero Client
Standalone Fabric 1.21.11 client. It has **no Meteor Client dependency**: the module, settings, event and render layer lives in `com.aero.client.core`. It only needs **Fabric API**.

- **Right Shift** opens/closes the Aero ClickGUI (COMBAT / MISC / DONUT / RENDER), light-blue theme, Xuong font.
- Left click = toggle, right click = that module's settings screen, drag header = move panel.
- Module on/off state and settings are saved to `config/aero-client.json`.
- Includes the full P3nis module set (finders, ESPs, spawner tools, staff tools, auto relog/rtp, freecam, Spotify HUD, region map, ...).
- Not included: FakePay, FakeScoreboard (balance/stat forger) and FakeSpawner. `aero-scoreboard` is a plain custom overlay instead.

Build: `./gradlew build` (JDK 21) -> `build/libs/aero-client-1.0.0.jar` -> `.minecraft/mods` with Fabric API.
Font: TNH Xuong, SIL OFL (assets/aero/OFL-Xuong.txt).
