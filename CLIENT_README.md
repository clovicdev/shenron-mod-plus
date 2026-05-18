# 🐉 Shenron Dragon Balls Client Guide

This guide is written for players, server owners, and client reviewers who only need to install and use the mod.

## 🚀 Quick Install

1. Download the latest `shenron-1.0.0.jar` from the GitHub **Releases** page.
2. Install Minecraft `1.20.1`.
3. Install Forge for Minecraft `1.20.1` using the Forge `47.x` loader line.
4. Place the jar in the client `mods` folder.
5. For multiplayer, place the same jar in the server `mods` folder.
6. Launch Minecraft using the Forge profile.

The mod requires Java `17`, which is the standard Java version for Minecraft `1.20.1` Forge.

## ✨ What The Mod Adds

- Seven collectible Dragon Balls with one-star through seven-star designs.
- A Dragon Radar item for finding the nearest active Dragon Ball.
- A cinematic Shenron summoning ritual.
- A large custom Shenron entity with a chat-based wish system.
- Dragon Balls that reform, launch upward, and scatter after a wish is granted.
- Yellow ritual light, glowing Dragon Balls, and scatter trail effects.
- Operator commands for testing, setup, and client demos.

## 🟠 How Dragon Balls Work

When the first player joins a new Overworld, the seven Dragon Balls are placed randomly near the starting player. They are kept within a reasonable search area so the gameplay loop starts quickly.

The Dragon Balls can be picked up as items or placed close together for the summoning ritual. Held Dragon Balls appear as regular inventory items. Placed and spawned Dragon Balls use a 3D in-world model.

## 📡 Dragon Radar

The Dragon Radar points toward the nearest active Dragon Ball in the Overworld. Hold it in hand and follow the signal.

Crafting recipe:

```text
R E R
G C G
R E R
```

- `R`: Redstone
- `E`: Eye of Ender
- `G`: Gold Ingot
- `C`: Compass

## 🐉 Summoning Shenron

Gather all seven Dragon Balls, then type one of these phrases in chat:

```text
arise shenron
come forth shenron
shenron grant my wish
eternal dragon arise
eternal dragon come forth
```

When the phrase succeeds, the Dragon Balls appear on the ground in a ceremonial 2-3-2 pattern. They glow and connect to Shenron with yellow light as he rises from the center, curves upward, settles above the ritual, and waits for the closest player to make a wish.

## 🎁 Making A Wish

After Shenron appears, the closest player can type the name of a vanilla Minecraft item in chat.

Examples:

```text
diamond sword
64 emeralds
netherite ingot and golden apples
```

If no number is written, Shenron grants one of the requested item.

Wish rewards are limited to two stacks by default. An operator can adjust this with:

```text
/shenron_test wish_limit <stacks>
```

After the wish is granted, Shenron dissolves into gold light. The light gathers into seven Dragon Balls, then the Dragon Balls launch into the sky with light streaks and scatter across the Overworld again.

## 🛡️ Admin Test Commands

The command `/shenron_test` requires operator permission level `2` or higher.

| Command | Result |
| --- | --- |
| `/shenron_test give_balls` | Gives all seven Dragon Balls. |
| `/shenron_test give_radar` | Gives a Dragon Radar. |
| `/shenron_test summon` | Summons Shenron immediately. |
| `/shenron_test ritual` | Starts the full Dragon Ball pattern and Shenron-rise animation. |
| `/shenron_test wish_limit` | Shows the current wish reward stack limit. |
| `/shenron_test wish_limit <stacks>` | Changes the wish reward stack limit. |
| `/shenron_test scatter` | Scatters a fresh set of Dragon Balls from the player's location. |
| `/shenron_test locate` | Shows the nearest tracked Dragon Ball location. |

## 📘 More Documentation

For build instructions, QA steps, and developer notes, see [GUIDE.md](GUIDE.md).
