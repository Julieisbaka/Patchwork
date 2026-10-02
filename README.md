# Patchwork

## Configuration

Patchwork creates `config/patchwork.properties` on startup (`run/config/` in
the development environment). Set any feature switch to `false` to disable
it; every switch defaults to `true` except `creeperChainReactions`, which is
`false` by default. With Mod Menu 21.0.0 installed on the
client, open Patchwork's Config button to edit the switches and recall radius
in a scrolling settings screen. Hover over each option for a detailed
description. Save writes the local config file; Cancel discards your edits.
Restart the game/server after saving or editing the file.
On multiplayer servers, gameplay switches are controlled by the server's
config; the client's Mod Menu screen cannot change server settings. Clients
use their own `wolfBanners` setting to control whether
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
| `throwableFireCharges` | Fire-charge throwing, impact fire, and cooldown |
| `callHornRecall` | Call goat horn pet recall |
| `cauldronCleaning` | Wash dyed wool, terracotta, and stained glass in water cauldrons |
| `beesDefendFlowers` | Bees defend flowers near occupied hives |
| `creeperChainReactions` | Creeper blast chain reactions (off by default) |
| `endermanDefense` | Endermen place carried blocks defensively |
| `spiderWebs` | Spiders spin cobwebs while chasing prey |
| `slimeSplitClouds` | Five-second slowing particle cloud when larger Slimes split |
| `breezeShockwave` | Breeze melee shockwave and extinguished torches/campfires |
| `unlitTorches` | Breeze gusts create recoverable unlit torch items (requires shockwave) |
| `hoglinCharge` | Charging Hoglins launch victims, with extra wall/ceiling impact damage |
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

## Pets

Right-click a tamed wolf you own with a banner to fly its colors and patterns
as a flag above its back. You can change the banner while keeping its wolf
armor and collar, or sneak-right-click with an empty hand to take the banner
back. Banners are returned when replaced or removed, and drop if the wolf
dies. Only the owner can equip or remove a banner.

Owned tamed wolves and cats are excluded from the indirect sweep of their
owner's sword attacks, including Sweep Edge damage and knockback. Direct hits
on a pet and other players' attacks are unchanged.

Using the Call variant of the goat horn instantly recalls your sitting tamed
pets within the configured radius to safe spaces around you. The default is
32 blocks. Set `callHornRecallRadius` in `config/patchwork.properties` to an
integer from 16 to 256 and restart the server
to apply it. Invalid or missing values produce a startup error rather than
silently using a different distance. The server controls this setting in
multiplayer; clients do not need to configure it.

Recalled pets stand up so they can follow you. Pets that cannot find a safe
landing spot remain seated; other goat horn variants are unchanged.

## Throwable slimeballs

Use the existing slimeball item to throw it like a snowball. The projectile
shows the vanilla slimeball texture, deals 1 HP on a successful hit, and gives
living targets Slowness I for 3 seconds. On slimes, it instead restores 1 HP
and grants Speed I for 3 seconds without dealing damage. Each throw consumes
one slimeball (except in creative mode) and starts a 1-second (20-tick)
cooldown shown on the hotbar. Attempts during the cooldown do not throw or
consume another slimeball. Ordinary snowballs are unchanged.

## Throwable fire charges

Use a fire charge in the air to launch a Ghast-style fireball with a moderate
initial speed, but only when no block is in your sights within five blocks.
If a block is in the way, using the charge in the air does nothing: no charge
is consumed and no cooldown begins. This prevents accidental launches while
aiming at a block just outside normal placement range.
It deals 2 HP of direct damage when it hits an entity. A block hit lights an
unlit candle or campfire, or places fire on the impacted face if fire can
survive there. On side hits against nonflammable blocks, it tries the top
surface instead. Obstructed surfaces and surfaces that cannot support fire
will not ignite. The thrown fireball does not explode. Each throw consumes
one fire charge (except in creative mode) and starts a 1.5-second (30-tick)
hotbar cooldown. Using a fire charge directly on a block still follows
vanilla fire-lighting behavior.

## Dye

Use any dye on a living shulker to change it to that color. This uses the
vanilla shulker colors and changes appearance only. One dye is consumed when
the color changes (unless the player has infinite materials); using the same
color again does not consume a dye.

Right-click a water cauldron while holding dyed wool, terracotta, or stained
glass to clean one block into white wool, plain terracotta, or plain glass.
Each wash consumes one water level. A held stack is handled one block at a
time; the cleaned item goes into your hand if the stack runs out, or into
your inventory (and drops if it is full). Item components are preserved.
Minecraft already cleans colored shulker boxes into undyed shulker boxes
this way, including their contents, so Patchwork leaves that vanilla
interaction unchanged. Disabling `cauldronCleaning` turns off only the
three new block types.

## Mob behavior

Breaking a flower within four blocks of an occupied beehive or bee nest
releases its bees, which attack if close enough. Smoke from a campfire keeps
them calm.

When `creeperChainReactions` is enabled, a Creeper that survives damage from
another Creeper's explosion ignites its full fuse. This is **off by default**
because multiple explosions may destroy more terrain.

An Enderman carrying a block has a one-in-three chance to place it between
itself and a living attacker after being hurt, if there is empty space,
solid support, no obstructing entities, and the block can survive there.
It respects the `mobGriefing` game rule.

Spiders chasing living targets two to eight blocks away have a one-in-four
chance every five seconds to place a cobweb at the target's feet when the
space is empty and has solid support. This also respects `mobGriefing`.

When a medium or large Slime dies and splits, it leaves a 3x3 patch of
green slime dust and slime particles on solid ground for five seconds.
Players standing in the cloud
receive Slowness II while inside it, fading shortly after they leave. The
cloud does not create or replace any blocks; gaps and uneven or unsupported
ground are skipped.

When a Breeze survives a melee hit, it immediately releases a gust that
pushes nearby entities away by approximately three blocks. It cannot do so
again for 10 seconds. If `mobGriefing` is enabled, the gust also extinguishes
lit campfires within five blocks. With the separate `unlitTorches` setting
enabled, it extinguishes ordinary, soul, copper, and *lit* redstone torches
within five blocks as well. Unlit torches give off no light, flame particles,
or redstone power. They use distinct unlit models and textures and drop
matching unlit torch items on breaking. Each item places on floors and walls;
fire charges relight them into their original torch variants. Disabling
`unlitTorches` prevents gusts from creating more and hides the items from
the creative inventory, but preserves any already placed or collected unlit
torches. With the setting enabled, ordinary, soul, and copper unlit torches
appear beside their lit counterparts in Functional Blocks; unlit redstone
torches also appear in Redstone Blocks. There is no crafting recipe for the items.
Lanterns remain lit because vanilla lanterns have no unlit state.

An adult Hoglin running at least 0.18 blocks per tick (or marked sprinting)
that lands a successful melee hit launches its target upward about three blocks
and slightly away. A player flung into a wall or ceiling during the next
second takes an extra 2 HP of blunt collision damage. Landing on the ground
does not trigger this extra damage. Ordinary Hoglin hits are unaffected.
