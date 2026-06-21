# 🛡️ UnbreakableBlocks: Moderinth Security (26.1.2 Compatible)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2-blue.svg)](https://papermc.io/)
[![Spigot/Paper API](https://img.shields.io/badge/API-Spigot%2FPaper%2026.1.2-orange.svg)](https://papermc.io/downloads/paper)
[![GitHub Repository](https://img.shields.io/badge/GitHub-Carlos733--ai%2FUnbreakable--Blocks--Plugin-brightgreen)](https://github.com/Carlos733-ai/Unbreakable-Blocks-Plugin/tree/main)

---

## 🌟 Overview

**UnbreakableBlocks** is a high-security Minecraft server plugin built for **Paper/Spigot 26.1.2**.  
It enforces strict block integrity rules, turning selected blocks into **Moderinth Security-protected structures** that cannot be broken, moved, or destroyed through normal gameplay mechanics.

Designed for modern 26.x server architecture, it provides stable, event-driven protection for large-scale servers and custom worlds.

---

## ✨ Key Features

* **Material-Level Protection:** Mark any block material as unbreakable using `/unbreakable add`
* **Persistent Ownership System:** Tracks block placement using player UUIDs stored in `placed_blocks.yml`
* **Full Anti-Grief Protection System:**
  * 🛑 Explosions blocked (`EntityExplodeEvent`)
  * 🚫 Pistons blocked (`BlockPistonExtendEvent` / `BlockPistonRetractEvent`)
  * 🔨 Break attempts canceled for non-owners
* **Ownership Verification:** Check who placed a block using `/unbreakable check`
* **Admin Bypass System:** Trusted operators can override protections when needed
* **Optimized for 26.1.2 API:** Built against updated server internals and event handling changes

---

## 🛠️ Installation & Usage (26.1.2)

### Requirements
* Java **25+** (recommended for 26.x runtime)
* Paper/Spigot **26.1.2**
* Basic plugin installation knowledge
