# 🐉 Shenron Mod+

A Forge mod for Minecraft that adds the seven Dragon Balls, a Dragon Radar, and a cinematic Shenron wish encounter.

![Shenron Dragon Balls logo](portfolio_gfx/shenron-readme-logo.png)

## ✨ What It Includes

- 🟠 Seven collectible Dragon Balls with one-star through seven-star item textures.
- 📡 Dragon Radar item that tracks the nearest active Dragon Ball.
- 🌍 New-world Dragon Ball scattering near the first Overworld player.
- 🧭 Persistent Dragon Ball tracking through world saved data.
- 🐉 Large custom Shenron entity with a coiled body, horns, whiskers, facial details, and staged rise animation.
- 🔥 Summoning ritual with a 2-3-2 Dragon Ball ground pattern, glowing balls, yellow light beams, and Shenron rising from the center.
- 💬 Chat-based wish handling for the closest player near Shenron.
- 🎁 Item wishes with a default quantity of one item when no amount is written.
- 🧱 Two-stack default wish reward cap, configurable by operators.
- ✨ Post-wish effect where Shenron dissolves into gold light, reforms into seven Dragon Balls, then launches and scatters them with trail streaks.
- 🛠️ Operator-only test commands for QA, setup, and client demos.
- 🖼️ Portfolio-ready showcase graphics and logo assets.

## 🚀 Download

Download the ready-to-install jar from the repository **Releases** page.

Install the jar into both the client and server `mods` folder when playing multiplayer.

## ✅ Version Support

| Requirement | Version |
| --- | --- |
| Minecraft | `1.20.1` |
| Forge | `47.x` |
| Java | `17` |

Minecraft and Forge do not keep a stable binary mod API across major Minecraft versions. This release is built and verified for Minecraft `1.20.1` on the Forge `47.x` loader line.

## 📘 User Guide

See [GUIDE.md](GUIDE.md) for:

- 🧩 Full feature overview
- 🏗️ Build instructions
- 🕹️ In-game usage
- 🛡️ Admin commands and permissions
- 📦 Release/install notes

## 🧪 Build From Source

Use Java `17`, then run:

```bash
./gradlew build
```

The compiled jar is created at:

```text
build/libs/shenron-1.0.0.jar
```

## 🎮 Quick Gameplay Flow

1. Create or join an Overworld.
2. Dragon Balls initialize near the first player in a new world.
3. Craft and hold the Dragon Radar to locate active Dragon Balls.
4. Gather all seven Dragon Balls.
5. Type a summon phrase in chat, such as `arise shenron`.
6. Make a wish by typing a vanilla item name near Shenron.
7. Shenron grants the wish, disappears, and the Dragon Balls scatter again.

## 🛠️ Admin Testing

Operators can use `/shenron_test` commands in-game with permission level `2` or higher.

Common examples:

```text
/shenron_test give_balls
/shenron_test give_radar
/shenron_test ritual
/shenron_test summon
/shenron_test wish_limit 4
/shenron_test scatter
/shenron_test locate
```

The full command list is documented in [GUIDE.md](GUIDE.md).

## 📁 Project Structure

```text
src/main/java/        Mod source
src/main/resources/   Assets, models, textures, recipes, and metadata
portfolio_gfx/        Showcase graphics and logo files
GUIDE.md              Player, admin, and build guide
CLIENT_README.md      Client-friendly handoff notes
```

## 🔒 Notes

- Dragon Balls are tracked in the Overworld.
- The wish parser grants vanilla registry items by readable item names, such as `diamond sword`, `oak logs`, or `enchanted golden apple`.
- Mod Dragon Ball items are excluded from wishes so the collection loop cannot be bypassed.
