# Technical configuration details

This guide explains how Patchwork settings are loaded, which settings apply
in multiplayer, and what happens to existing world data when features are
disabled. For the complete property list, see the
[configuration guide](configuration.md).

## File location and format

Patchwork reads `config/patchwork.properties` from the game or server instance
at startup. Each instance has its own file; changing a separate client,
server, or launcher profile's file does not change the instance you are using.

The file uses Java properties syntax, with one setting per line:

```properties
callHornRecall=true
callHornRecallRadius=32
creeperChainReactions=false
```

Property names are case-sensitive. Boolean values accept `true` or `false`,
ignoring capitalization and surrounding whitespace. Values such as `yes`,
`on`, or `1` are invalid.

`callHornRecallRadius` must be a whole number from 16 through 256, inclusive.
Its default is 32 blocks. Every boolean defaults to `true` except
`creeperChainReactions`, which defaults to `false`.

Gameplay tuning values use the validated ranges and units in the
[configuration guide](configuration.md). Tick counts and other integer
settings reject fractional values. Decimal settings accept finite numbers;
NaN and infinity are invalid. Spider web minimum distance must not exceed
maximum distance. All tuning defaults retain the previous gameplay values.

Numeric definitions share a single key/default/range catalog used by file
loading, saving, the Mod Menu editor, and gameplay. Loaded numeric maps are
immutable. Missing keys are added only after the complete configuration has
passed validation, so invalid numeric values or distance combinations do not
rewrite the original file. Unknown properties survive migration and saving.

## Applying changes

Restart the affected game or server after editing settings. Editing the file
does not reload settings in a running instance.

With Mod Menu installed, Patchwork's configuration screen can write the same
file. Save writes the selected values; Cancel discards the edits. Saving does
not remove the restart requirement.

In multiplayer, gameplay changes require editing and restarting the server,
not just the connecting client.

## Client and server authority

The server controls gameplay interactions, creature behavior, and recipes.
A client's local settings cannot override the server's choices.

Wolf banner rendering also uses the client's local `wolfBanners` setting.
This allows a client to hide equipped banners without removing them from
wolves or changing the server's banner interactions.

## Dependencies and game rules

| Setting or rule | Effect |
| --- | --- |
| `breezeTorchExtinguishing` | Requires `breezeShockwave` to be enabled for Breeze gusts to extinguish torches and lanterns. |
| `callHornRecallRadius` | Controls recall distance only when `callHornRecall` is enabled. |
| `ownerSweepProtection` | Protects the attacking owner's wolves, cats, and tamed rabbits from sword sweep damage; it does not disable direct attacks. |
| `animalDroppedFood` | Enables seeking/eating dropped breeding food; its radius, eating distance, timing, and movement speed are configurable. |
| `mobGriefing` | Governs animal dropped-food feeding, Enderman defensive placement, Spider webs, Breeze extinguishing, and Soul Golem projectile block ignition. |
| `spiderCeilingClimbing` | Controls spider and cave-spider ceiling adhesion and traversal. |
| `caveSpiderNausea` | Enables melee nausea using the configured chance and duration (defaults: 20%, 100 ticks). |
| `patchworkAdvancements` | Controls Patchwork advancement resources and gameplay-triggered custom awards. |

Player-thrown Soul Fire Charges can still ignite blocks when `mobGriefing` is
disabled. Projectile damage to entities is independent of that rule.

## Disabling features and existing world data

Feature switches generally control behavior, not whether saved items or
entities remain available.

| Setting disabled | What remains |
| --- | --- |
| `wolfBanners` on the server | Banners already equipped on wolves are retained. Clients may still render them according to their local setting. |
| `throwableFireCharges` | Regular and Soul Fire Charges remain craftable and usable directly on blocks. Soul Golem attacks and dispenser launches are not disabled by this player-throwing switch. |
| `pumpkinLanterns` | Existing Soul Jack o'Lanterns can still be placed and used to construct Soul Golems. Only torch-lighting interactions are disabled. |
| `potionCauldrons` | Placed potion cauldrons are preserved, but new filling, retrieval, and arrow-dipping interactions are disabled. |
| `experienceClumping` | Vanilla orb spawning, merging, and pickup behavior is restored without truncating experience values already saved. |
| `witherDifficultyHealth` | New Withers use vanilla maximum health. Existing Withers' saved health is not reset. |

Disabling a feature is not the same as removing the mod. Back up worlds before
removing a mod that supplies saved blocks, items, or entities.

## Features without configuration switches

The following additions are always available:

- Unlit block items, Soul Golems, Soul Fire Charges, and their recipes.
- Soul Candles and copper candles/candle cakes, campfires, and Jack o'Lanterns,
  including oxidation, waxing, recipes, and unlit items. `pumpkinLanterns` only
  gates lighting placed carved pumpkins with torches; crafting and placing
  these lights remains available.
- Earth, Wind, Water, and Fire paintings in Creative and normal placement.
- One illusioner in each raid wave from wave 5 onward, including bonus waves.
  Easy raids end before wave 5 and remain unchanged.
- Carrot taming for ordinary adult and baby rabbits. Ownership and stay
  commands persist after reloads; offspring are born wild. Recall and sweep
  protection still depend on their respective settings.

## Startup errors and upgrades

If the file does not exist, Patchwork creates it with all default settings.
Missing settings in an existing file are automatically added with their
defaults, logged, and saved at startup. Existing values and unrecognized
properties are preserved. A complete, valid file is not rewritten.

Upgrades that add settings therefore need no manual additions. Invalid
existing values still stop startup; they are not replaced with defaults.
Correct the reported value using the [property list](configuration.md#settings)
and restart. If validation fails, the file is left unchanged.

To regenerate defaults instead, stop the instance and rename the configuration
file to keep a backup. Start the instance to create a new file, then reapply
your preferred values and restart again.

Read or write failures report the configuration path. Check that the correct
instance's `config` folder exists and that the game or server account has
permission to read and write it.

[Configuration guide](configuration.md) | [Feature overview](../README.md)
