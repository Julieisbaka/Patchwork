# Patchwork

## Configuration

Patchwork creates `config/patchwork.properties` on startup (`run/config/` in
the development environment). Set any feature switch to `false` to disable
it; every switch defaults to `true`. Restart the game/server after editing.
On multiplayer servers, gameplay switches are controlled by the server's
config. Clients use their own `wolfBanners` setting to control whether
existing wolf banners are rendered; changing the server switch prevents
equipping or removing banners but does not delete banners already on wolves.

| Property | Feature |
| --- | --- |
| `witherDifficultyHealth` | Difficulty-based Wither maximum health |
| `witherBirthExplosion` | Increased Wither birth explosion damage |
| `chainmailRecipes` | Chainmail crafting recipes and recipe-book unlocks |
| `wolfBanners` | Wolf banner interactions and client-side banner rendering |
| `ownerSweepProtection` | Owned wolf/cat protection from indirect sword sweeps |
| `shulkerDyeing` | Recoloring shulkers with dye |
| `throwableSlimeballs` | Slimeball throwing, hit effects, and cooldown |
| `callHornRecall` | Call goat horn pet recall |
| `callHornRecallRadius` | Recall radius in blocks, 16–256 (default: 32) |

Existing config files receive the new switches automatically; invalid values
produce a startup error instead of silently changing behavior. Disabling
`witherDifficultyHealth` restores vanilla maximum health for newly spawned
Withers, but does not reset health already saved on existing Withers.

## Wither

The Wither's maximum health depends on world difficulty: Easy has 300 HP,
Normal has 450 HP, and Hard has 600 HP. Peaceful keeps the vanilla 300 HP
maximum (Withers cannot normally exist there). Existing Withers adjust when
the difficulty changes, retaining the same percentage of health rather than
healing to full.

The birth explosion deals 3 more base damage than vanilla at any given
distance and exposure, while retaining the vanilla radius, knockback, and
block interaction. For the vanilla reference values, this changes unarmored
player damage from 35.5 to 37 HP on Easy, 69 to 72 HP on Normal, and 103.5
to 108 HP on Hard. Cover, distance, and armor still affect damage.

## Chainmail armor

Craft chainmail helmets, chestplates, leggings, and boots using iron chains in the
same crafting-grid patterns as the corresponding iron armor. Each recipe uses
the vanilla number of ingredients: 5, 8, 7, and 4 chains, respectively.
The recipes appear in the recipe book after obtaining an iron chain.

## Wolf banners

Right-click a tamed wolf you own with a banner to fly its colors and patterns
as a flag above its back. You can change the banner while keeping its wolf
armor and collar, or sneak-right-click with an empty hand to take the banner
back. Banners are returned when replaced or removed, and drop if the wolf
dies. Only the owner can equip or remove a banner.

## Sweeping attacks and pets

Owned tamed wolves and cats are excluded from the indirect sweep of their
owner's sword attacks, including Sweep Edge damage and knockback. Direct hits
on a pet and other players' attacks are unchanged.

## Shulker colors

Use any dye on a living shulker to change it to that color. This uses the
vanilla shulker colors and changes appearance only. One dye is consumed when
the color changes (unless the player has infinite materials); using the same
color again does not consume a dye.

## Throwable slimeballs

Use the existing slimeball item to throw it like a snowball. The projectile
shows the vanilla slimeball texture, deals 1 HP on a successful hit, and gives
living targets Slowness I for 3 seconds. On slimes, it instead restores 1 HP
and grants Speed I for 3 seconds without dealing damage. Each throw consumes
one slimeball (except in creative mode) and starts a 1-second (20-tick)
cooldown shown on the hotbar. Attempts during the cooldown do not throw or
consume another slimeball. Ordinary snowballs are unchanged.

## Call goat horn

Using the Call variant of the goat horn instantly recalls your sitting tamed
pets within the configured radius to safe spaces around you. The default is
32 blocks. Set `callHornRecallRadius` in `config/patchwork.properties` to an
integer from 16 to 256 and restart the server
to apply it. Invalid or missing values produce a startup error rather than
silently using a different distance. The server controls this setting in
multiplayer; clients do not need to configure it.

Recalled pets stand up so they can follow you. Pets that cannot find a safe
landing spot remain seated; other goat horn variants are unchanged.
