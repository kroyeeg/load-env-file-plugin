# load-env-file-plugin

![Build](https://github.com/kroyeeg/intellij-env-file-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/26061)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/26061)

## Features
<!-- Plugin description -->
Adds support for loading environment variables from a `.env` file into JetBrains run configurations.

- Automatically injects variables when run configurations are added or modified.
- Reads `.env` from the project root by default, or from a custom path set in **Settings | Tools | load-env-file**. If a directory is chosen, its `.env` is used; you can also specify a `.env` file directly.
- Supports variable expansion so `${VAR}` references are resolved.
- Currently supports Gradle and Kotlin run configurations.
<!-- Plugin description end -->

# Manual Installation

```shell
./gradlew buildPlugin
```

You can find the plugin zip file in `build/distributions` directory.
Install the plugin from disk in Jetbrains IDE.

# Development

You can run the plugin in the sandbox environment by running the following image.
Off course, you can use debug mode.

![img.png](img.png)
