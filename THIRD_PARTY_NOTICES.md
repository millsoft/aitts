## Third-Party Notices

This project uses third-party libraries and assets.

### Runtime dependencies (from Gradle metadata)

- `androidx.appcompat:appcompat:1.7.0` — Apache License 2.0
- `androidx.constraintlayout:constraintlayout:2.1.4` — Apache License 2.0
- `androidx.legacy:legacy-support-v4:1.0.0` — Apache License 2.0
- `org.jetbrains:annotations:23.0.0` — Apache License 2.0
- `com.android.volley:volley:1.2.1` — Apache License 2.0
- `commons-codec:commons-codec:1.12` — Apache License 2.0
- transitive AndroidX/Kotlin/JetBrains dependencies are Apache License 2.0 in this build graph

### Test-only dependencies

- `junit:junit:4.13.2` — Eclipse Public License 1.0
- `androidx.test:*` and Espresso dependencies — Apache License 2.0

### Vendored binary (`app/libs/jlayer.jar`)

- `jlayer.jar` is committed directly in this repository.
- No bundled `LICENSE` or `NOTICE` file was found inside the jar.
- The package names indicate this is JavaZoom JLayer (`javazoom.jl.*`), commonly distributed under LGPL.
- Action recommended before wider redistribution:
  - replace with a Maven dependency that has clearly traceable coordinates and metadata, or
  - add the exact upstream source URL/version and include the corresponding license text in this repository.
