# Patchwork

A starter Fabric mod for Minecraft Java Edition 26.3. The common initializer is
`src/main/java/com/example/patchwork/Patchwork.java`; client-only code belongs
under `src/client/java/`.

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

The Java package and Gradle `group` use `com.example.patchwork` as a
placeholder. Before publishing, change both to a namespace you control,
and add your own license and project description to `fabric.mod.json`.
