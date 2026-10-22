# Aero Client
Standalone Fabric client for Minecraft 1.21.11. **Only dependency: Fabric API** (no Meteor).

- **Right Shift** opens the Aero ClickGUI (COMBAT / MISC / DONUT / RENDER). Left click = toggle, right click = module settings, drag a header to move a panel.
- Font: TNH Xuong (SIL OFL, `assets/aero/OFL-Xuong.txt`).
- Includes the P3nis module set (finders, ESPs, spawner tools, staff tools, auto relog/rtp, freecam, Spotify HUD, region map) plus Auto Totem, Auto Log, Auto Sprint, Fullbright, Player/Storage ESP and a custom scoreboard overlay.
- Commands: `/aero list`, `/aero toggle <module>`, `/aero set <module> <setting> <value>` (strings and hex colors).
- Settings are saved to `config/aero-client.json`.
- Not included: FakePay, FakeScoreboard (balance/stat forger), FakeSpawner.

Build: push -> GitHub Actions -> download the `aero-client` artifact. Put the jar and Fabric API in `.minecraft/mods`.
