# 🐉 Shenron Dragon Balls Guide

This guide covers what the mod includes, how to build it, how to install it, and how to use the in-game admin commands.

## 📦 What The Mod Includes

### 🟠 Dragon Balls

- Seven unique Dragon Balls from one star through seven stars.
- Dragon Balls scatter into the Overworld when the first player joins a new world.
- New-world placement keeps the Dragon Balls within a reasonable early-game search range.
- Dragon Balls can be collected as items.
- Placed and spawned Dragon Balls render as 3D world objects.
- During the ritual, the Dragon Balls appear on the ground in a 2-3-2 pattern, glow, and connect to Shenron with yellow light.

### 📡 Dragon Radar

- Tracks the nearest active Dragon Ball in the Overworld.
- Changes its display based on direction and distance.
- Uses a first-person held-item transform inspired by vanilla locator-map handling.
- Shows a centered signal when the player is close to the target.

### 🐉 Shenron

- Summoned by gathering all seven Dragon Balls and typing a summon phrase in chat.
- Rises from the Dragon Ball pattern with a staged animation.
- Appears as a large coiled dragon with horns, whiskers, facial details, body segments, belly plates, and spines.
- Listens only to the closest player near him.
- Grants vanilla item wishes typed in chat.
- Disappears after granting a wish.
- Dissolves into gold light that reforms into seven Dragon Balls before they launch upward and scatter again.

### 🎁 Wish Rules

- If no amount is written, Shenron grants one item.
- Rewards are capped at two stacks by default.
- Operators can adjust the stack cap with `/shenron_test wish_limit <stacks>`.
- Dragon Ball mod items are blocked from wishes so the collection loop cannot be skipped.

## ✅ Requirements

| Tool | Version |
| --- | --- |
| Minecraft | `1.20.1` |
| Forge | `47.x` |
| Java | `17` |

## 🚀 Install The Release Jar

1. Download the latest `shenron-1.0.0.jar` from the GitHub **Releases** page.
2. Install Minecraft Forge for Minecraft `1.20.1`.
3. Place the jar in the client `mods` folder.
4. For multiplayer, place the same jar in the server `mods` folder.
5. Start Minecraft using the Forge profile.

## 🏗️ Build From Source

Clone the repository:

```bash
git clone https://github.com/clovicdev/dragon-ball-mod.git
cd dragon-ball-mod
```

Build with Java `17`:

```bash
./gradlew build
```

The compiled mod jar will be created here:

```text
build/libs/shenron-1.0.0.jar
```

On macOS, if multiple Java versions are installed, run the build with an explicit Java 17 path:

```bash
JAVA_HOME=/path/to/jdk-17 ./gradlew build
```

## 🎮 How To Use In Game

### 1. Find The Dragon Balls

Create or load an Overworld. When the first player joins a new world, the Dragon Balls initialize around that player. Craft a Dragon Radar and follow the signal to each active Dragon Ball.

Dragon Radar recipe:

```text
R E R
G C G
R E R
```

| Symbol | Ingredient |
| --- | --- |
| `R` | Redstone |
| `E` | Eye of Ender |
| `G` | Gold Ingot |
| `C` | Compass |

### 2. Summon Shenron

Gather all seven Dragon Balls, then type one of these chat phrases:

```text
arise shenron
come forth shenron
shenron grant my wish
eternal dragon arise
eternal dragon come forth
```

The Dragon Balls are consumed, placed in the ritual pattern, and Shenron rises from the center.

### 3. Make A Wish

Stand near Shenron and type a vanilla item name in chat.

Examples:

```text
diamond sword
64 emeralds
netherite ingot and golden apples
```

If no number is included, Shenron grants one item. After the wish is granted, Shenron disappears and the Dragon Balls scatter again.

## 🛡️ Admin Commands

All commands use:

```text
/shenron_test
```

The command requires Minecraft permission level `2` or higher. On a server, the player must be an operator or have equivalent command permissions.

| Command | What It Does |
| --- | --- |
| `/shenron_test give_balls` | Gives the executing player all seven Dragon Balls. |
| `/shenron_test give_radar` | Gives the executing player a Dragon Radar. |
| `/shenron_test summon` | Summons Shenron immediately at the player's location without consuming Dragon Balls. |
| `/shenron_test ritual` | Starts the full Dragon Ball ritual animation and Shenron rise sequence. |
| `/shenron_test wish_limit` | Shows the current wish reward stack limit. |
| `/shenron_test wish_limit <stacks>` | Sets the global wish reward stack limit for the world. Accepted range: `1` to `54`. |
| `/shenron_test scatter` | Removes nearby loaded Dragon Balls and scatters a fresh set from the player's location. |
| `/shenron_test locate` | Prints the nearest tracked Dragon Ball coordinates and distance. |

## 🧪 Recommended QA Checklist

1. Run `/shenron_test give_balls`.
2. Run `/shenron_test give_radar`.
3. Hold the radar and verify the display updates.
4. Run `/shenron_test ritual` and watch the full summon sequence.
5. Type `diamond` near Shenron and confirm only one diamond is granted.
6. Run `/shenron_test wish_limit 4`.
7. Repeat a wish with a larger amount and confirm the new cap applies.
8. Confirm Shenron disappears after granting the wish.
9. Confirm the Dragon Balls reform, launch upward, leave light streaks, and scatter.
10. Run `/shenron_test locate` to verify the new tracked location.

## 🗂️ Included Assets

- Dragon Ball item textures from one star through seven stars.
- Dragon Radar item textures.
- Shenron entity texture.
- 3D Dragon Ball renderer.
- Portfolio showcase graphics.
- Portfolio logo exports.

## 🧰 Troubleshooting

| Issue | Fix |
| --- | --- |
| The jar does not load | Confirm Minecraft `1.20.1`, Forge `47.x`, and Java `17`. |
| Admin commands do not work | Confirm the player has permission level `2` or higher. |
| The radar has no target | Use `/shenron_test scatter` or create a new Overworld to initialize Dragon Balls. |
| Shenron does not answer a wish | Make sure the closest player near Shenron is the one typing the wish. |
| A requested item is not granted | Use the vanilla item name, such as `diamond sword` or `oak logs`. |
