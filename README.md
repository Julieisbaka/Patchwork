# Patchwork

Patchwork is a Fabric mod for Minecraft Java Edition 26.3. It brings together
building and crafting additions, creature behaviors, and small quality-of-life
changes. Most gameplay features can be enabled or disabled individually.

## Features

### Building, crafting, and utility

- **Soul Golems:** Build one from two soul sand or soul soil blocks topped with
  a Soul Jack o'Lantern. They protect their owner, attack at range with Soul
  Fire Charges, have 50 health, can be healed with gold, and can be sheared.
- **Soul Fire Charges:** Craft one by surrounding a regular Fire Charge with
  eight soul sand or soul soil. Use charges to create soul fire, light
  campfires and candles, or launch them by hand or dispenser.
- **Potion cauldrons:** Pour a non-water potion into an empty cauldron, retrieve
  doses with glass bottles, refill with matching potions, or dip arrows. Each
  potion level can tip one arrow; different potions cannot be mixed.
- **Unlit lights:** Breezes can extinguish torches and lanterns. Fire Charges
  relight them, and flint and steel can relight torches. Copper variants keep
  their oxidation and wax state through extinguishing and relighting.
  Lit copper torches dim with oxidation: light levels 14, 12, 10, and 8.
  Waxed torches retain their stage's brightness.
- **Garden and building blocks:** Craft reversible wax blocks from honeycomb,
  find Paeonia in flower forests and meadows, and use it in flower pots or to
  craft magenta dye. Charcoal blocks provide a compact fuel source.
- **Crafting and decoration:** Craft chainmail armor, add up to nine banner
  patterns, dye shulkers, and wash wool, terracotta, and stained glass in
  cauldrons.
- **Restored paintings:** Earth, Wind, Water, and Fire are available in Creative
  Functional Blocks and the normal random painting placement pool.

### Creatures and quality of life

- **Pets:** Add banners to wolves, protect owned wolves and cats from sword
  sweeps, and use a goat horn to recall nearby seated pets.
- **Rabbit companions:** Tame ordinary adult or baby rabbits with one carrot.
  They follow their owner; the owner's empty-hand right-click toggles stay.
  Ownership and stay commands survive reloads. Call horns recall staying
  rabbits, and owner sword sweeps spare them. Feeding tamed rabbits keeps
  vanilla breeding and baby-growth behavior.
- **Experience orbs:** Nearby orbs can combine to reduce entity count while
  preserving experience values and Mending behavior.
- **Creature behaviors:** Configure tougher Withers, flower-defending bees,
  Enderman block defense, pursuit webs from Spiders, Slime split clouds,
  Breeze shockwaves, Hoglin charges, Skeleton cover, and Creeper chain
  reactions.
- **Illusioner raids:** One illusioner joins each raid wave from wave 5 onward,
  including bonus waves, alongside the existing raiders. Easy raids remain
  unchanged because they end before wave 5.

## Installation

Use Minecraft Java Edition 26.3 with Java 25 or newer, Fabric Loader 0.19.5 or
newer, and the matching Fabric API. Mod Menu 21.0.0 is optional and adds an
in-game configuration screen.

Install Patchwork and its required dependencies in the Fabric `mods` folder.
Gameplay options are controlled by the server in multiplayer; see the
[configuration guide](docs/configuration.md) for details.

## Building from source

Build with the Gradle wrapper from the repository root:

```sh
./gradlew build
```

On Windows, use `.\gradlew.bat build`. Gradle writes the mod jar to
`build/libs/`.

## Project documentation

- [Configuration guide](docs/configuration.md) — settings, multiplayer
  authority, and config migration.
- [Technical configuration details](docs/technical.md) — file format, restart
  requirements, setting dependencies, persistence, and troubleshooting.
- [Testing guide](docs/testing.md) — builds, automated GameTests, and manual
  checks.
