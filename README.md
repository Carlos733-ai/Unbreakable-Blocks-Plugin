# 🛡️ UnbreakableBlocks: Moderinth Security

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21+-blue.svg)](https://www.spigotmc.org/resources/spigot.5645/)
[![License](https://img.shields.io/github/license/Carlos/UnbreakableBlocks)](LICENSE)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/Carlos/UnbreakableBlocks/main.yml?branch=main)](https://github.com/Carlos/UnbreakableBlocks/actions)

## 🌟 Overview

**UnbreakableBlocks** provides a definitive, "set-it-and-forget-it" method for managing block integrity on your Minecraft server. It transforms designated block materials into **Moderinth Security** features, offering robust protection that cannot be bypassed by players, explosions, or pistons.

This plugin is ideal for server owners who need absolute control over specific structural elements, custom terrain, or secure player-placed assets.

## ✨ Key Features

* **Material-Level Protection:** Designate **any Minecraft block material** as unbreakable using a simple in-game command (`/unbreakable add`).
* **Persistent Block Ownership:** When a player places a tracked block, their **UUID ownership is recorded and saved to disk** (`placed_blocks.yml`). Only the original placer or an authorized admin can remove it.
* **Total Anti-Griefing:** Unbreakable blocks are immune to all common bypass methods:
    * 🛑 **Explosions:** Ignored by TNT, Creepers, and Wither blasts (`EntityExplodeEvent`).
    * 🚫 **Pistons:** Cannot be pushed or pulled (`BlockPistonExtend/RetractEvent`).
* **Admin Management:** Full command suite including `/unbreakable check` to instantly verify ownership and status.

## 🛠️ Installation & Usage (Source Code)

This guide is for compiling the project from source. If you are a server owner, please use the pre-compiled JAR available on Modrinth or Spigot.

### Prerequisites

* **Java Development Kit (JDK) 17 or newer**
* A recent **Spigot/Paper API JAR** (e.g., `spigot-api-1.21.jar`)

### Compilation Steps

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/Carlos/UnbreakableBlocks.git](https://github.com/Carlos/UnbreakableBlocks.git)
    cd UnbreakableBlocks
    ```

2.  **Ensure Dependencies are Present:**
    Place your Spigot/Paper API JAR (e.g., `spigot-api-1.21.jar`) in a convenient location (or modify the compilation command to point to it).

3.  **Compile the Source:**
    Assuming your source files are in `src/com/carlos/unbreakable/` and your API JAR is named `spigot-api-1.21.jar` in the root directory:
    ```bash
    # Step 3a: Compile the Java class
    javac -cp "spigot-api-1.21.jar" src/com/carlos/unbreakable/Main.java

    # Step 3b: Create the plugin.yml (ensure this file is in the root!)
    # (If using a tool like Maven/Gradle, this is handled automatically.)
    
    # Step 3c: Package into a JAR file
    jar cf UnbreakableBlocks.jar plugin.yml -C src/com/carlos/unbreakable .
    ```

4.  **Deployment:**
    Copy the resulting `UnbreakableBlocks.jar` file into your server's `plugins/` directory and restart/reload the server.

---

## 📜 Commands & Permissions

| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/unbreakable add [material]` | Makes the block you're looking at (or specified material) unbreakable. | `unbreakable.add` | `op` |
| `/unbreakable remove [material]` | Removes protection from a block type. | `unbreakable.remove` | `op` |
| `/unbreakable list` | Displays all currently unbreakable block types. | `unbreakable.list` | `true` |
| `/unbreakable check` | Checks ownership and status of the block you are looking at. | `unbreakable.check` | `op` |
| - | Allows a player to bypass all protection and break any unbreakable block. | `unbreakable.bypass` | `op` |
| - | General permission for using admin subcommands (`add`, `remove`, `check`). | `unbreakable.admin` | `op` |

---

## 🤝 Contributing

We welcome contributions! If you have suggestions for new features, bug reports, or want to contribute code, please open an issue or submit a pull request on GitHub.

---
*Created by Carlos*
