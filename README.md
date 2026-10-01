# Patchwork

A starter Fabric mod for Minecraft Java Edition 26.3. The common initializer is
`src/main/java/com/JulieISBaka/patchwork/Patchwork.java`; client-only code belongs
under `src/client/java/`.

## Wither health

The Wither's maximum health depends on world difficulty: Easy has 300 HP,
Normal has 450 HP, and Hard has 600 HP. Peaceful keeps the vanilla 300 HP
maximum (Withers cannot normally exist there). Existing Withers adjust when
the difficulty changes, retaining the same percentage of health rather than
healing to full.

## Getting started

Install JDK 25 and open this folder as a Gradle project in your IDE. On Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Use `.\gradlew.bat runServer` for a development server. The distributable mod
JAR is produced in `build/libs/` (not the `-sources.jar` or `-dev.jar`).
Install Fabric Loader and Fabric API for Minecraft 26.3 when running the mod
outside the development environment.

Mod Menu 21.0.0 for Minecraft 26.3 is optional. When installed, it lists
Patchwork using the name, author, version, and description in `fabric.mod.json`.
Patchwork does not yet have configurable settings, so it does not expose a
Configure button.

The Java package and Gradle `group` use `com.JulieISBaka.patchwork`. Before
publishing, add your own license and project description to `fabric.mod.json`.
