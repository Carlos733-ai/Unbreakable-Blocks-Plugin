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
