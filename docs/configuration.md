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
| `cauldronCleaning`         | Washing wool, terracotta, and stained glass              |
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
| `callHornRecallRadius`     | Integer 16-256 blocks; default 32                        |

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

`mobGriefing` governs Enderman defense, Spider webs, Breeze extinguishing, and
Soul Golem projectile block ignition. Player Soul Fire Charges can still
ignite blocks. Direct projectile entity damage is independent of this rule.

## Requirements and errors

New files are initialized with every switch's default value and the default
radius. Missing settings in existing files are automatically added with their
defaults and saved at startup, preserving existing values and unknown properties.
Invalid existing values still produce a startup error and leave the file
unchanged. The radius must be an integer from 16 to 256.

Disabling difficulty-based Wither health restores vanilla maximum health for
new Withers; it does not reset health saved on existing Withers.

[Feature overview](../README.md)
