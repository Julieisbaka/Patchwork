# Patchwork

Patchwork is a Fabric mod for Minecraft Java Edition 26.3. It brings together
building and crafting additions, creature behaviors, and small quality-of-life
changes. Most gameplay features can be enabled or disabled individually.
Over 50 gameplay tuning settings cover animal food searches, mob ranges and
timing, damage, cooldowns, and more, in both the properties file and Mod Menu.
See the [configuration guide](docs/configuration.md) for defaults and ranges.

**New to Patchwork?** Start with [installation](#installation), browse the
[feature list](#features), or jump to [changing settings](docs/configuration.md).

## Features

### Building, crafting, and utility

- **Soul Golems:** Build one from two soul sand or soul soil blocks topped with
  a Soul Jack o'Lantern. They protect their owner, attack at range with Soul
  Fire Charges, have 30 health, can be healed with gold, and can be sheared.
  Construction and ranged attacks can be disabled independently.
- **Soul Fire Charges:** Craft one by surrounding a regular Fire Charge with
  eight soul sand or soul soil. Use charges to create soul fire, light
  campfires and candles, or launch them by hand or dispenser. Charge-created
  soul fire works on solid tops, beside flammable blocks, and on leaves.
  Player, dispenser, and Soul Golem projectile sources have separate controls.
- **Potion cauldrons:** Pour a non-water potion into an empty cauldron, retrieve
  doses with glass bottles, refill with matching potions, or dip arrows. Each
  potion level can tip one arrow; different potions cannot be mixed.
- **Unlit lights:** Breezes can extinguish torches and lanterns. Fire Charges
  relight them, and flint and steel can relight torches. Copper variants keep
  their oxidation and wax state through extinguishing and relighting.
  Lit copper torches dim with oxidation: light levels 14, 12, 10, and 8.
  Waxed torches retain their stage's brightness.
- **More candle and copper lights:** Soul Candles emit soul light and support
  candle cakes. Copper Candles (including cakes), Copper Campfires, and Copper
  Jack o'Lanterns have four oxidation stages, honeycomb waxing, and axe scraping.
  Their light decreases from 14 to 12, 10, and 8 as they oxidize; Soul Candles
  emit 10. Wax preserves the current stage.
  Separate unlit items cover every candle color and regular, soul, and copper
  campfires. Convert between normal and unlit items in a crafting grid.
  Unlit items always place extinguished blocks and can be relit normally.
  Unlit campfire icons reuse Minecraft's extinguished campfire model and texture.

- **Garden and building blocks:** Craft reversible wax blocks from honeycomb,
  find Paeonia in flower forests and meadows, and use it in flower pots or to
  craft magenta dye. Charcoal blocks provide a compact fuel source.
- **Woodcutter:** Process logs into planks, and planks into stairs, slabs,
  fences, gates, doors, trapdoors, buttons, pressure plates, or signs in a
  stonecutter-style workstation. Modpacks can add variants to the
  `patchwork:woodcutting/<wood>_logs` and
  `patchwork:woodcutting/<wood>_planks` input tags and add matching
  `minecraft:stonecutting` recipes with recipe IDs under `woodcutter/`.
- **Snow under leaves:** Snow can accumulate naturally on the ground beneath
  canopies up to two leaf blocks thick.
- **Iron golem gifts:** Baby villagers can pick up poppies offered by iron
  golems.
- **Blaze rods:** Dropped blaze rods survive fire and lava.
- **Pickaxe mining:** Glass blocks and panes, stained glass, tinted glass, and
  beacons are tagged for pickaxe mining. Silk Touch is still required to
  collect glass blocks.
- **Crafting and decoration:** Craft chainmail armor, add up to nine banner
  patterns, dye shulkers, and wash wool, terracotta, and stained glass in
  cauldrons.
- **Restored paintings:** Earth, Wind, Water, and Fire are available in Creative
  Functional Blocks and the normal random painting placement pool.

#### New light recipes

| Output | Ingredients |
| --- | --- |
| Soul Candle | String above honeycomb above soul sand or soul soil |
| Copper Candle | String above honeycomb above a copper nugget |
| Copper Campfire | Vanilla campfire pattern, replacing coal with a copper nugget |
| Copper Jack o'Lantern | Carved pumpkin + matching copper torch (keeps oxidation and wax) |
| Waxed copper light | Matching unwaxed light + honeycomb |
| Unlit candle/campfire item | Matching normal item alone; reversible |

Place a new candle on an uneaten cake to add its matching candle. Candles
stack up to four, including when using unlit items. Extinguished candles drop
their unlit items; extinguished campfires do so when harvested with Silk Touch.
Without Silk Touch, copper campfires drop two charcoal, like regular campfires.
Vanilla campfire and soul campfire drops are otherwise unchanged.

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
- **Hostile mob changes:** Spiders and cave spiders move faster, detect players
  from farther away, and take no fall damage. Soul Sand Valleys spawn bow-
  wielding wither skeletons instead of normal skeletons, and blue wither skulls
  home toward nearby players.
- **More hostile mob changes:** Elder guardians have 150 health, 8 armor, a
  boss bar, and immunity to fire, lava, magma blocks, and explosions. Rare
  zombie spawns include baby-zombie jockeys and banner-marked zombie leaders
  riding zombie horses. Wither damage can turn regular skeletons into wither
  skeletons; pillagers can fire from 15 blocks, and witches can use jump boost
  and leap when attacking elevated targets.
- **Anvils and terrain:** Renaming an item preserves its prior-work repair cost.
  Mountain steep-surface rules now recognize slopes facing all directions
  (MC-258859).
- **Illusioner raids:** One illusioner joins each raid wave from wave 5 onward,
  including bonus waves, alongside the existing raiders. Easy raids remain
  unchanged because they end before wave 5.

## Installation

Patchwork currently supports **Minecraft Java Edition 26.3**. You will need:

- Java 25 or newer
- Fabric Loader 0.19.5 or newer
- Fabric API for Minecraft 26.3

1. Install Fabric Loader for Minecraft 26.3.
2. Download the matching Patchwork jar from the
   [GitHub releases page](https://github.com/Julieisbaka/Patchwork/releases)
   and put it in your instance's `mods` folder.
3. Put the matching Fabric API jar in the same `mods` folder.
4. Launch the Fabric profile. Add Mod Menu if you want to change settings
   from the in-game Mods screen; it is optional.

For multiplayer, install Patchwork and its required dependencies on the server
and each player's client. The server's settings control gameplay for everyone;
see [changing settings](docs/configuration.md) for the config screen, file
location, and examples.

## Building from source

Build with the Gradle wrapper from the repository root:

```sh
./gradlew build
```

On Windows, use `.\gradlew.bat build`. Gradle writes the mod jar to
`build/libs/`.

## Project documentation

Patchwork includes English, German, French, Spanish, Brazilian Portuguese,
Simplified Chinese, Japanese, Korean, and Russian translations. Choose your
language in Minecraft's language settings; config property names stay the same.

- [Configuration guide](docs/configuration.md) — getting started, practical
  examples, all settings, multiplayer behavior, and troubleshooting.
- [Testing guide](docs/testing.md) — builds, automated GameTests, and manual
  checks.
