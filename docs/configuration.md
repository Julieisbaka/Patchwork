# Customize Patchwork

Make animals find food farther away, tone down combat, or disable features
you do not want. You do not need to edit anything to play: the defaults give
you Patchwork's standard experience.

## Quick start

### In game (recommended)

1. Install **Mod Menu** alongside Patchwork.
2. Open **Mods**, select **Patchwork**, and choose **Config**.
3. Toggle features or scroll down to **Gameplay tuning**. Hover over a setting
   for help; numeric fields show their default and allowed range.
4. Choose **Save**, then restart the game. **Cancel** leaves your settings alone.

### On a server or without Mod Menu

1. Start the game or server once so Patchwork creates its settings file.
2. Stop it, then open `config/patchwork.properties` inside that instance's
   game/server folder with a text editor.
3. Change the values after `=` and save the file.
4. Start the game or server again.

Do not edit a different launcher profile's file by mistake. **In multiplayer,
the server's settings control gameplay**, even if your local settings differ.
Ask the server owner to change them. Your local `wolfBanners` option can still
hide banner rendering on your own screen.

## Try these changes

These are examples to copy into the settings file, not built-in presets.
Keep each property only once, and restart after saving.

### Let animals find food farther away

```properties
animalDroppedFood=true
animalFoodSearchRadius=16
animalFoodEatCooldownTicks=100
```

Animals look up to 16 blocks away and wait at least five seconds between
meals. They still need suitable breeding food and `mobGriefing=true`.

### Make combat a little gentler

```properties
caveSpiderNauseaChance=0.1
breezeShockwaveCooldownTicks=400
hoglinImpactDamage=1
```

Nausea has a 10% chance, Breeze gusts wait 20 seconds between triggers, and
Hoglin collision damage drops to half a heart.

### Reduce repeated searches on a busy server

```properties
animalFoodSearchIntervalTicks=40
experienceMergeIntervalTicks=40
```

Animals and XP orbs scan every two seconds. They may react less quickly,
but repeated searches happen less often. Keep radii modest too.

## Feature switches

Use `true` to enable a feature and `false` to disable it. All switches default
to `true` except `creeperChainReactions`, which is **off by default** because
chain explosions can cause extra terrain damage.

| Property                   | Controls                                                 |
| -------------------------- | -------------------------------------------------------- |
| `witherDifficultyHealth`   | Difficulty-based Wither maximum health                   |
| `witherBirthExplosion`     | Increased Wither birth-explosion damage                  |
| `chainmailRecipes`         | Chainmail recipes and recipe-book unlocks                |
| `wolfBanners`              | Wolf banner interactions and local banner rendering      |
| `ownerSweepProtection`     | Owned wolf/cat/rabbit protection from sword sweeps       |
| `shulkerDyeing`            | Shulker recoloring                                       |
| `throwableSlimeballs`      | Slimeball throwing, effects, and cooldown                |
| `throwableFireCharges`     | Player projectiles from regular and Soul Fire Charges    |
| `soulFireCharges`          | Soul Fire Charge crafting, use, and projectile support    |
| `soulFireChargeDispenserProjectiles` | Soul Fire Charges fired by dispensers              |
| `soulGolemProjectiles`     | Soul Golem ranged Soul Fire Charge attacks                |
| `soulGolems`               | Soul Golem construction and Creative spawn egg            |
| `rabbitCarrotTaming`       | Rabbit carrot taming and owner commands                   |
| `unlitLights`              | Unlit lights, extinguishing, relighting, and drops        |
| `soulCopperLightBlocks`    | Soul and copper light blocks and recipes                  |
| `restoredPaintings`        | Earth, Wind, Water, and Fire painting availability        |
| `illusionerRaidSpawns`     | Illusioners joining raid waves from wave five             |
| `callHornRecall`           | Call goat horn recall of seated pets and staying rabbits |
| `cauldronCleaning`         | Washing wool, terracotta, stained glass, and dyed bundles |
| `beesDefendFlowers`        | Bees defending nearby flowers                            |
| `creeperChainReactions`    | Creeper blast chain reactions; off by default            |
| `endermanDefense`          | Defensive carried-block placement                        |
| `spiderWebs`               | Web placement during pursuit                             |
| `slimeSplitClouds`         | Slowing dust clouds when larger Slimes split             |
| `breezeShockwave`          | Breeze melee-response gust and campfire extinguishing    |
| `breezeTorchExtinguishing` | Gusts extinguishing torches/lanterns; requires shockwave |
| `hoglinCharge`             | Charged launches and collision damage                    |
| `skeletonCover`            | Bow Skeleton corner cover while reloading                |
| `potionCauldrons`          | Filling, refilling, bottle retrieval, and arrow dipping  |
| `pumpkinLanterns`          | Lighting placed carved pumpkins with torches             |
| `experienceClumping`       | XP clumping, full-value pickup, and larger orb rendering |
| `sweetBerryTrades`         | Sweet-berry trades added to wandering traders            |
| `elytraDyeing`             | Elytra dye recipe and dyeable appearance                 |
| `stoneToolMaterials`       | Stone added as a material for stone tools                |
| `playerHeadRecipe`         | Carved-pumpkin and leather player-head recipe            |
| `spiderCeilingClimbing`    | Spider and cave-spider ceiling traversal                 |
| `caveSpiderNausea`         | Cave-spider melee nausea chance                          |
| `loyalTridentVoidReturn`   | Loyalty tridents survive falling into the void           |
| `patchworkAdvancements`    | Patchwork-specific advancement data and awards           |
| `animalDroppedFood`        | Animals seeking/eating dropped breeding food            |

### Pet recall distance

`callHornRecallRadius` controls how far the Call goat horn searches for your
seated pets and staying rabbits. It defaults to **32 blocks** and accepts a
whole number from **16 to 256**. The `callHornRecall` switch must be enabled.

## Gameplay tuning reference

The tables below list the exact names used in the settings file. In Mod Menu,
the same settings have readable labels. Their defaults preserve the standard
Patchwork behavior.

- **Time:** 20 ticks = one second at normal server speed. Settings ending in
  `Ticks` need whole numbers.
- **Health:** 2 HP = one heart.
- **Chance:** `0` means never, `0.25` means 25%, and `1` means always.
- **Effect strength:** amplifier `0` is level I, `1` is level II, and so on.
- **Speed:** movement multipliers scale a creature's normal movement.
  Projectile and launch velocities are measured in blocks per tick.
- **Numbers:** decimals are allowed unless a range says **Integer**.

### Animal feeding and flower defense

| Property | Default | Allowed range | Controls |
| --- | --- | --- | --- |
| `animalFoodSearchRadius` | 8 | 1-64 | How far animals search for and pursue dropped food |
| `animalFoodEatDistance` | 1.5 | 0.1-8 | Distance at which food is consumed |
| `animalFoodSearchIntervalTicks` | 10 | 1-1200 | Delay between food searches |
| `animalFoodEatCooldownTicks` | 40 | 0-12000 | Minimum delay between meals |
| `animalFoodRepathIntervalTicks` | 10 | 1-1200 | Navigation update interval |
| `animalFoodMovementSpeed` | 1.1 | 0.1-4 | Food-seeking movement multiplier |
| `beeDefenseRadius` | 4 | Integer 1-16 | Flower-to-hive defense radius |

### Breeze, Spider, and Enderman behavior

| Property | Default | Allowed range | Controls |
| --- | --- | --- | --- |
| `breezeShockwaveRadius` | 5 | Integer 1-16 | Entity push and block extinguishing radius |
| `breezeShockwaveCooldownTicks` | 200 | 0-12000 | Delay between shockwaves |
| `breezeShockwaveHorizontalPush` | 1.2 | 0-4 | Horizontal push velocity |
| `breezeShockwaveVerticalPush` | 0.25 | 0-2 | Vertical push velocity |
| `caveSpiderNauseaChance` | 0.2 | 0-1 | Nausea chance on a successful melee hit |
| `caveSpiderNauseaDurationTicks` | 100 | 1-12000 | Nausea duration |
| `endermanDefenseChance` | 0.3333333333333333 | 0-1 | Defensive block placement chance |
| `spiderWebIntervalTicks` | 100 | 1-12000 | Web attempt interval |
| `spiderWebChance` | 0.25 | 0-1 | Web placement chance per eligible attempt |
| `spiderWebMinDistance` | 2 | 0-64 | Minimum prey distance |
| `spiderWebMaxDistance` | 8 | 0.1-64 | Maximum prey distance |

The Spider minimum distance must be less than or equal to its maximum distance.

### Throwables and fire charges

| Property | Default | Allowed range | Controls |
| --- | --- | --- | --- |
| `slimeballCooldownTicks` | 20 | 0-12000 | Throwing cooldown |
| `slimeballDamage` | 1 | 0-100 | Damage to non-Slime targets |
| `slimeballHealing` | 1 | 0-100 | Healing for Slimes |
| `slimeballEffectDurationTicks` | 60 | 1-12000 | Speed/Slowness duration |
| `slimeballEffectAmplifier` | 0 | Integer 0-10 | Speed/Slowness amplifier |
| `eggSnowballCooldownTicks` | 2 | 0-12000 | Egg and snowball throwing cooldown; 0 disables it |
| `fireChargeCooldownTicks` | 30 | 0-12000 | Player throwing cooldown for both charge types |
| `fireChargeSpeed` | 0.65 | 0.05-4 | Initial velocity for thrown charges and all Soul projectiles |
| `fireChargeAcceleration` | 0.03 | 0-1 | Acceleration for thrown charges and all Soul projectiles |
| `fireChargeClearance` | 5 | 0-16 | Player throw obstruction-check distance |
| `fireChargeDamage` | 2 | 0-100 | Direct damage of player-thrown regular charges |
| `soulFireChargeDamage` | 3 | 0-100 | Direct damage of all Soul projectiles |
| `soulFireChargeBurnSeconds` | 2 | 0-600 | Burning duration after a damaging Soul projectile hit |

Soul projectile damage and flight settings affect player throws, dispensers,
and Soul Golems. They do not change Ghast fireballs or regular dispenser fire
charges. Soul Fire Charges can place soul fire on solid tops, beside flammable
blocks, and on leaves. This does not make soul fire spread like regular fire.
`throwableFireCharges` is the player-projectile control;
`soulFireChargeDispenserProjectiles` and `soulGolemProjectiles` independently
control dispenser and Soul Golem launches. Each Soul projectile source also
requires `soulFireCharges`.

### Slime clouds and experience orbs

| Property | Default | Allowed range | Controls |
| --- | --- | --- | --- |
| `slimeCloudLifetimeTicks` | 100 | 1-12000 | Slime split cloud lifetime |
| `slimeCloudRadius` | 1 | Integer 0-8 | Square half-width; side length is `2 * radius + 1` |
| `slimeCloudEffectAmplifier` | 1 | Integer 0-10 | Cloud Slowness amplifier |
| `experienceMergeRadius` | 2 | 0.1-16 | Orb merging radius |
| `experienceMergeIntervalTicks` | 20 | 1-1200 | Delay between merge scans |

### Combat, Soul Golems, and movement

| Property | Default | Allowed range | Controls |
| --- | --- | --- | --- |
| `hoglinChargeMinSpeed` | 0.18 | 0-2 | Minimum horizontal speed for charge hits; sprinting also qualifies |
| `hoglinHorizontalLaunch` | 0.2 | 0-4 | Horizontal launch velocity |
| `hoglinVerticalLaunch` | 0.69 | 0-4 | Vertical launch velocity |
| `hoglinImpactWindowTicks` | 20 | 1-1200 | Player collision-damage tracking window |
| `hoglinImpactDamage` | 2 | 0-100 | Wall/ceiling collision damage |
| `witherEasyHealth` | 300 | 1-1024 | Easy/Peaceful maximum health |
| `witherNormalHealth` | 450 | 1-1024 | Normal maximum health |
| `witherHardHealth` | 600 | 1-1024 | Hard maximum health |
| `witherBirthBonusDamage` | 3 | 0-100 | Added birth-explosion entity damage |
| `soulGolemAttackIntervalTicks` | 40 | 1-1200 | Ranged attack interval |
| `soulGolemAttackRange` | 16 | 1-64 | Ranged attack distance |
| `soulGolemGoldHealing` | 25 | 0-100 | Healing per gold ingot |
| `skeletonCoverTargetDistance` | 6 | 1-64 | Maximum prey distance for cover behavior |
| `skeletonCoverMovementSpeed` | 1.15 | 0.1-4 | Cover/peek movement multiplier |
| `flyingSpeedEffectMultiplier` | 0.2 | 0-4 | Flying speed bonus per Speed effect level; 0 disables the bonus |

Tuning a disabled feature will not turn it on. Enable its feature switch too.
Large search radii, large clouds, and very short scan intervals can make a
busy server slower; an allowed value is not a performance guarantee.

## What happens when I turn something off?

| Change | What to expect |
| --- | --- |
| Disable wolf banners | New interactions stop, but equipped banners are not erased. Your client can hide them locally. |
| Disable potion cauldrons | New interactions stop; placed potion cauldrons remain. |
| Disable XP clumping | Vanilla spawning, merging, and pickup return. Saved XP values are not truncated. |
| Disable difficulty-based Wither health | New Withers use vanilla health. Existing saved Wither health is not reset. |
| Disable throwable fire charges | Player air throws stop. Dispenser and Soul Golem Soul Fire Charges have separate controls. |
| Disable Soul Fire Charges | Their recipe, direct block use, and all Soul projectile sources stop. Existing charges and placed soul fire remain. |
| Disable dispenser Soul Fire Charge projectiles | Dispensers stop launching Soul Fire Charges; the other sources are controlled separately. |
| Disable Soul Golem projectiles | Soul Golems stop ranged attacks; construction and other behavior remain. |
| Disable Soul Golems | Soul-lantern structures no longer create golems and the spawn egg is hidden from Creative. Existing golems remain. |
| Disable rabbit carrot taming | New taming and owner commands stop; existing owner data is retained but inactive. |
| Disable unlit lights | Extinguishing, relighting, unlit-light drops, and unlit recipes are disabled. Existing unlit blocks remain. |
| Disable Soul/copper light blocks | Their recipes and Creative entries are removed; placed blocks remain. |
| Disable restored paintings | Earth, Wind, Water, and Fire leave the normal placement pool and Creative painting list. |
| Disable Illusioner raid spawns | New raids no longer add Illusioners; existing raid members remain. |
| Disable pumpkin lantern lighting | Torch-lighting interactions stop. Crafting, placement, and Soul Golem construction remain. |

### Vanilla game rules still matter

With `mobGriefing=false`, animals do not eat dropped food, Endermen do not place
defensive blocks, Spiders do not place webs, Breezes do not extinguish blocks,
and Soul Golem projectiles do not ignite blocks. Player Soul Fire Charges can
still ignite blocks. Projectile damage is independent of that rule.

`breezeTorchExtinguishing` also needs `breezeShockwave=true`.

## Troubleshooting

**My changes did not apply.** Restart the affected game/server, confirm you
edited the right instance, and check that the feature itself is enabled.
On multiplayer servers, changing your own config does not override the server.

**Save is disabled in Mod Menu.** Look for a red number field and hover for its
range. Use a whole number for integer fields. Also check that the Spider minimum
distance is no greater than its maximum.

**The game/server will not start after editing.** Check the log for the named
setting. Use `true`/`false` for switches (not `yes` or `on`), stay within the
listed ranges, and do not use `NaN` or infinity. The invalid file is left
unchanged so you can correct it.

**How do I reset a setting?** Stop the game/server and remove that property's
line. On the next start, Patchwork adds the default. To reset everything,
back up the file and remove it; Patchwork creates a fresh file at startup.

**Will an update overwrite my settings?** No. New settings are added with their
defaults while existing values and unknown properties are preserved.

[Feature overview](../README.md)
