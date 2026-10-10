# Configuration

Patchwork creates `config/patchwork.properties` at startup. Development runs
use their run directory's `config` folder. Every boolean defaults to `true`
except `creeperChainReactions`, which defaults to `false`.

With Mod Menu 21.0.0 installed, use Patchwork's Config button for a scrolling
settings screen. Hover for descriptions. Save writes the file; Cancel
discards edits. Restart the game/server to apply changes.

## Settings

| Property                   | Controls                                                 |
| -------------------------- | -------------------------------------------------------- |
| `witherDifficultyHealth`   | Difficulty-based Wither maximum health                   |
| `witherBirthExplosion`     | Increased Wither birth-explosion damage                  |
| `chainmailRecipes`         | Chainmail recipes and recipe-book unlocks                |
| `wolfBanners`              | Wolf banner interactions and local banner rendering      |
| `ownerSweepProtection`     | Owned wolf/cat/rabbit protection from sword sweeps       |
| `shulkerDyeing`            | Shulker recoloring                                       |
| `throwableSlimeballs`      | Slimeball throwing, effects, and cooldown                |
| `throwableFireCharges`     | Player throwing of regular and Soul Fire Charges         |
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
| `callHornRecallRadius`     | Integer 16-256 blocks; default 32                        |
| `animalDroppedFood`        | Animals seeking/eating dropped breeding food            |

## Gameplay tuning

Values ending in `Ticks` are integers; 20 ticks equal one second at normal server speed. Distances are in blocks, damage/healing/health in HP (2 HP = one heart), and chances range from 0 (never) to 1 (always). Effect amplifiers are zero-based: 0 is level I. Movement speed multipliers scale the mob's normal navigation speed; launch velocities and initial projectile speed are in blocks per tick.

| Property | Default | Allowed range | Controls |
| --- | --- | --- | --- |
| `animalFoodSearchRadius` | 8 | 1-64 | Food search box inflation and maximum pursuit distance |
| `animalFoodEatDistance` | 1.5 | 0.1-8 | Distance at which food is consumed |
| `animalFoodSearchIntervalTicks` | 10 | 1-1200 | Delay between food searches |
| `animalFoodEatCooldownTicks` | 40 | 0-12000 | Minimum delay between meals |
| `animalFoodRepathIntervalTicks` | 10 | 1-1200 | Navigation update interval |
| `animalFoodMovementSpeed` | 1.1 | 0.1-4 | Food-seeking movement multiplier |
| `beeDefenseRadius` | 4 | Integer 1-16 | Flower-to-hive defense radius |
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
| `slimeCloudLifetimeTicks` | 100 | 1-12000 | Slime split cloud lifetime |
| `slimeCloudRadius` | 1 | Integer 0-8 | Square half-width; side length is `2 * radius + 1` |
| `slimeCloudEffectAmplifier` | 1 | Integer 0-10 | Cloud Slowness amplifier |
| `experienceMergeRadius` | 2 | 0.1-16 | Orb merging radius |
| `experienceMergeIntervalTicks` | 20 | 1-1200 | Delay between merge scans |
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

`spiderWebMinDistance` must not exceed `spiderWebMaxDistance`. Numeric settings
for switchable features take effect only when the corresponding feature is
enabled. Soul projectile settings also affect dispensers and Soul Golems;
they do not alter vanilla Ghast fireballs or regular dispenser fire charges.
The food radius keeps the original box-based search and spherical pursuit
limit rather than changing how animals select targets.

For example:

```properties
animalDroppedFood=true
animalFoodSearchRadius=16.0
animalFoodEatCooldownTicks=100
breezeShockwaveRadius=8
caveSpiderNauseaChance=0.5
eggSnowballCooldownTicks=0
```

Large search radii, large cloud areas, and short scan intervals can increase
server tick costs. The ranges bound those costs but are not performance
guarantees. Structural constants (registry IDs, block-state limits, mixin
targets, data formats, and rendering geometry) are not gameplay settings.

## Server authority and persistence

Multiplayer gameplay uses the server's configuration. Client Mod Menu changes
cannot change server gameplay. Clients use their own `wolfBanners` switch for
rendering existing banners. Disabling wolf banner interactions on the server
does not erase banners already equipped.

Unlit block items, Soul Golems, Soul Fire Charges, and their recipes have no
separate availability switch. Soul Candles and copper candles/candle cakes,
campfires, and Jack o'Lanterns are also always available, including oxidation,
waxing, recipes, and unlit items. The four restored paintings and illusioner raid
spawns are also always enabled. Illusioners join waves 5 and later, including
bonus waves; Easy raids have no illusioners.
Rabbit carrot taming is always enabled. One ordinary carrot tames an unowned
non-killer rabbit, including babies. Only the owner can toggle staying, using
an empty hand. Golden carrots still use vanilla feeding, and feeding already
tamed rabbits does not transfer ownership. Offspring are born wild.
`throwableFireCharges` gates player air throws
of both charge types, not Soul Golem attacks, crafting, or direct block use.
Both Fire Charge items remain available next to each other in Creative Combat
when throwing is disabled; disabling air throws does not remove the items.
`pumpkinLanterns` gates torch-lighting interactions for regular, soul, and copper
Jack o'Lanterns, not crafting or placing those blocks, or constructing a Soul
Golem with a Soul Jack o'Lantern.

Disabling `potionCauldrons` prevents new interactions but preserves placed
potion cauldrons. Disabling `experienceClumping` restores vanilla orb
spawning/merging/pickup without truncating values already saved.

`mobGriefing` governs animal dropped-food feeding, Enderman defense, Spider webs, Breeze extinguishing, and
Soul Golem projectile block ignition. Player Soul Fire Charges can still
ignite blocks. Direct projectile entity damage is independent of this rule.

## Requirements and errors

New files are initialized with every switch and numeric default.
Missing settings in existing files are automatically added with their
defaults and saved at startup, preserving existing values and unknown properties.
Invalid existing values still produce a startup error and leave the file
unchanged, including invalid distance combinations. The Call horn radius must
be an integer from 16 to 256. Other numeric ranges are listed above.
Fractional values are allowed only for non-integer settings; NaN and infinity
are rejected. Mod Menu marks invalid numeric entries red and disables Save
until all fields and the paired Spider distances are valid.

Disabling difficulty-based Wither health restores vanilla maximum health for
new Withers; it does not reset health saved on existing Withers.

[Feature overview](../README.md)
