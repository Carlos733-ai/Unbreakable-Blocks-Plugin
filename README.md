# 🛡️ UnbreakableBlocks: Moderinth Security

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21%2B-blue.svg)](https://www.spigotmc.org/resources/spigot.5645/)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Carlos733--ai%2FUnbreakable--Blocks--Plugin-brightgreen)](https://github.com/Carlos733-ai/Unbreakable-Blocks-Plugin/tree/main)

---

## 🌟 Overview

**UnbreakableBlocks** provides a definitive, "set-it-and-forget-it" system for enforcing block integrity across Minecraft servers. It converts selected block materials into **Moderinth Security-protected assets**, ensuring they cannot be broken, moved, or destroyed through normal gameplay mechanics.

This plugin is designed for server owners who require strict control over builds, protected structures, and grief-resistant environments.

---

## ✨ Key Features

* **Material-Level Protection:** Mark any Minecraft block material as unbreakable using `/unbreakable add`.
* **Persistent Ownership System:** Tracks block placement using player UUIDs, stored in `placed_blocks.yml`.
* **Grief Prevention Engine:** Blocks are protected against all common destruction methods:
  * 🛑 Explosions (TNT, Creepers, Withers) via `EntityExplodeEvent`
  * 🚫 Pistons via `BlockPistonExtendEvent` and `BlockPistonRetractEvent`
  * 🔨 Unauthorized breaking attempts
* **Admin Verification Tools:** Instantly check block ownership and protection status with `/unbreakable check`.

---

## 🛠️ Installation & Usage (Source Code)

This section is intended for developers compiling from source. Server owners should use the prebuilt JAR release.

### Prerequisites

* Java Development Kit (JDK) **17+**
* Spigot or Paper API for **Minecraft 1.21+**

---

### Compilation Steps

1. **Clone Repository**
```bash
git clone https://github.com/Carlos733-ai/Unbreakable-Blocks-Plugin.git
cd Unbreakable-Blocks-Plugin
Add Spigot/Paper API
Place your API jar (example: spigot-api-1.21.jar) into the project directory or reference its path.
Compile Source
javac -cp "spigot-api-1.21.jar" src/com/carlos/unbreakable/Main.java
Build Plugin JAR
jar cf UnbreakableBlocks.jar plugin.yml -C src/com/carlos/unbreakable .
Install on Server
Move UnbreakableBlocks.jar into your /plugins folder and restart the server.
📜 Commands & Permissions
Command	Description	Permission	Default
/unbreakable add [material]	Marks a block type as unbreakable	unbreakable.add	OP
/unbreakable remove [material]	Removes block protection	unbreakable.remove	OP
/unbreakable list	Lists all protected materials	unbreakable.list	Everyone
/unbreakable check	Checks ownership & status of targeted block	unbreakable.check	OP
/unbreakable reload	Reloads plugin configuration	unbreakable.admin	OP
/unbreakable bypass	Allows bypassing all protections	unbreakable.bypass	OP
📦 Patch Logs / Changelog
v1.0.0 — Core Release
Initial plugin system introduced
Material-based unbreakable blocks implemented
Basic command system added
UUID ownership tracking enabled
v1.1.0 — Anti-Grief Update
Added explosion protection (TNT, Creeper, Wither)
Added piston protection system
Improved event cancellation logic
Fixed indirect block destruction edge cases
v1.2.0 — Admin Expansion
Added /unbreakable check
Introduced permission hierarchy system
Added bypass permission (unbreakable.bypass)
Improved command stability
v1.3.0 — Data Persistence Update
Added placed_blocks.yml storage system
Fixed data loss on server restart
Optimized UUID lookup performance
Reduced disk write overhead
v1.4.0 — Stability Patch
Improved large-world performance
Reduced event listener lag
Hardened protection against plugin conflicts
Fixed rare duplication exploits
🚧 Planned Features
Region-based protection zones
GUI block manager interface
Per-world configuration support
Web dashboard integration (future MCSH expansion)
