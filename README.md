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
| `breezeTorchExtinguishing` | Breeze gusts extinguish torches and lanterns (requires shockwave) |
| `hoglinCharge` | Charging Hoglins launch victims, with extra wall/ceiling impact damage |
| `skeletonCover` | Nearby bow Skeletons take corner cover while reloading |
| `potionCauldrons` | Fill empty cauldrons with potions, retrieve them with bottles, and tip arrows |
| `pumpkinLanterns` | Light placed carved pumpkins using torches |
| `experienceClumping` | Nearby XP orbs merge and their full value is collected at once |
| `callHornRecallRadius` | Recall radius in blocks, 16–256 (default: 32) |

Existing config files receive the new switches automatically; invalid values
produce a startup error instead of silently changing behavior. Disabling
`witherDifficultyHealth` restores vanilla maximum health for newly spawned
Withers, but does not reset health already saved on existing Withers.
The old `unlitTorches` switch is migrated to `breezeTorchExtinguishing` without
changing its value. This switch controls Breeze behavior, not the availability
of unlit torch items.

## Game tests

With cheats enabled (or as an operator), use `/test run
patchwork:patchwork_game_tests_unlit_torch_variants` to run one test. Tab
completion after `/test run patchwork:` lists the other Patchwork tests.
`/test runfailed` reruns failures; `/test runthese` reruns nearby test
structures. The tests use Fabric's built-in GameTest API and its empty
structure. They run in-game, not through the Gradle unit-test task.

The automated tests cover four unlit torch conversions and wall facing,
an unlit item drop, fire-charge relighting, washing wool/terracotta/glass
using one cauldron level each, shulker dyeing, difficulty-based Wither
health, the Breeze extinguishing a torch and campfire, a charging Hoglin
launch, slimeballs healing/speeding a Slime and damaging/slowing a non-Slime,
and a thrown fire charge igniting the top of a solid block after a side impact.
They also check potion filling of empty cauldrons, rejection of water cauldrons,
matching-potion refills, bottle retrieval, offhand arrow dipping, in-place
dropped-arrow dipping and stack conservation, and lighting both kinds
of carved pumpkin with the correct facing, torch consumption, and soul variant
drop, plus rejection of Soul Jack o'Lantern Snow Golem construction,
preservation of vanilla Snow Golem construction, Soul Golem construction,
gold repairs and spawn eggs,
Soul Golem player safety, 50 HP, exact quarter-damage rolls and two-second fire,
and a charcoal block's 16,000-tick furnace burn duration. Additional tests cover
the nine-layer loom limit and banner copying, all ten unlit lantern variants'
relighting and drops, waterlogging and copper mappings, and Breeze lantern
extinguishing. XP tests cover mixed values and vanilla counts, the two-block
merge radius, full-value pickup with Mending, large-value persistence, and
overflow-safe merging. Tests requiring a
switch fail with an instruction to enable it rather than silently passing
without exercising the feature. `breezeExtinguishesLights` also requires
`mobGriefing=true`.

The Fabric client GameTest (`fabric-client-gametest` entrypoint) creates a
temporary world, renders a Soul Golem with its Soul Jack o'Lantern head and
spawn egg, checks its render state, and saves a `soul-golem` screenshot.
Enable it with the JVM flag `-Dfabric.client.gametest`; it does not run during
normal gameplay.

The remaining interactions need manual in-game checks: craft and unlock all
four chainmail recipes; compare Wither birth-explosion damage at each
difficulty; equip/remove a wolf banner and verify a stranger
cannot do so; confirm owned cats and wolves ignore indirect sword sweeps;
throw slimeballs and check the hotbar cooldown;
verify the fire-charge launch clearance, 2 HP hit, cooldown, and no
explosion; recall seated pets with the Call horn at the configured radius;
check bees at an occupied unsmoked hive, Enderman carried-block defense,
Spider webs, and the optional Creeper chain reaction; verify the Slime
cloud's Slowness II and five-second expiry; test the Breeze's knockback
and ten-second cooldown; and collide with a wall/ceiling after a Hoglin
launch to check its 2 HP impact. Check all four unlit items in Creative and
verify unlit redstone torches supply no signal. Check throwable slimeballs and
fire charges in the Combat creative tab when their respective features are
enabled. Check the unlit lantern textures, nine-layer loom screen, and enlarged
XP orb appearance on a client. Mod Menu switches and client banner
rendering (including walking and sitting wolves) also require a client. Test Skeletons seeking a reachable wall
corner and returning to their firing position against a close player, and
verify unlike potions cannot mix and potion contents survive a save/reload.
These behaviors are not claimed as covered
by the automated tests.

## Skeletons and potion cauldrons

When a bow-wielding Skeleton has fired and a player is within six blocks, it
looks for a reachable adjacent corner with a solid obstruction between it and
the player. It retreats there while reloading, then returns to its former
position to draw and fire. Without suitable cover it uses vanilla movement.

Use a drinkable non-water potion on an empty cauldron to fill it with one level
of that potion. Water cauldrons cannot accept potions. Up to two matching
potions refill it to three levels; different contents do not mix. Use a glass
bottle in either hand to retrieve one level with its exact potion contents.
Removing the last level leaves an empty cauldron. Water bottles keep their
vanilla filling and retrieval behavior.
Right-click the potion cauldron with ordinary arrows in either hand, or drop
them inside it, to convert one arrow into a tipped arrow carrying the potion's
contents. Each arrow consumes one level. Dropped stacks can use up the remaining
levels, with one level per arrow; a full cauldron produces at most three tipped
arrows. The converted dropped item stays in place, keeping its entity identity,
motion, age, and pickup timer; any unused arrows split off at the same location.
Conversion does not emit a sound or pickup animation.
Already tipped arrows are unaffected. Potion
cauldrons tint their liquid surface to match the potion;
breaking one drops a normal empty cauldron. Disabling `potionCauldrons`
prevents filling, refills, retrieval, and dipping but does not remove existing
potion cauldrons from saved worlds.

## Charcoal blocks

Craft a Block of Charcoal from nine charcoal in a crafting grid. It burns for
16,000 ticks in a normal furnace (800 seconds, enough to smelt 80 items), matching
a coal block. Blast furnaces and smokers use vanilla's faster burn rate.

## Soul Golem

Stack two blocks of soul sand or soul soil (mixed stacks also work), then place
a Soul Jack o'Lantern on top. Lighting a carved pumpkin on that stack with a
soul torch also works when `pumpkinLanterns` is enabled. All three blocks are
consumed to create a persistent, friendly Soul Golem.

It uses the Snow Golem model with an original brown-and-cyan soul-themed body
texture and a Soul Jack o'Lantern on its head, with a smaller matching hitbox.
It has 50 HP (half an iron golem's health), deals one quarter
of an iron golem's rolled melee damage, and ignites successfully hit targets for
two seconds. It inherits hostile-mob targeting (excluding Creepers) and
gold-ingot repairs (25 HP per ingot; iron does not work). It uses soul-soil
footsteps and repair sounds, soul-sand hurt/damage sounds, soul escape on death,
and a fire-charge attack sound, rather than iron-golem sounds.
Built and spawn-egg-created Soul Golems do not
attack players. Construction does not require the `pumpkinLanterns` switch
when placing an already obtained Soul Jack o'Lantern. It has no natural spawns
or death drops yet. A Soul Golem Spawn Egg is available in the Spawn Eggs
Creative tab. Soul Jack o'Lanterns cannot construct Snow Golems; ordinary
carved pumpkins and Jack o'Lanterns still can. The body texture uses the Snow
Golem's 64x64 UV layout and can be replaced with a custom texture later.
Its static in-game appearance is covered by the client render test; listening
to sounds and checking walking and melee animations still need a manual check.

## Banner customization

The loom accepts up to nine pattern layers instead of six. Its menu and client
screen use the same limit, and a tenth pattern cannot be added. Nine-layer
banners can also be copied in the crafting grid without losing their patterns.

## Experience orb clumping

With `experienceClumping` enabled (the default), each XP award creates one orb.
Orbs check once every 20 ticks for other orbs within two blocks and combine their
XP, even when their values differ. Existing vanilla orb stacks are included in
the total. Collecting a combined orb grants its entire value in one pickup,
using vanilla Mending repairs before granting the remaining XP.

Orb appearance grows with its XP value, capped at three times the normal scale.
Large values are saved as full integers rather than vanilla's short integer,
so save/reload does not truncate them. A merge that would exceed the maximum
integer value leaves the orbs separate without losing XP. Disabling the feature
restores vanilla spawning, merging, and pickup; already combined values remain
intact. This reduces orb entity counts and repeated pickup delays; no performance
benchmark is claimed.

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
lit campfires within five blocks. With the separate `breezeTorchExtinguishing` setting
enabled, it extinguishes ordinary, soul, copper, and *lit* redstone torches
within five blocks as well. Unlit torches give off no light, flame particles,
or redstone power. They use distinct unlit models and textures and drop
matching unlit torch items on breaking. Each item places on floors and walls;
fire charges relight them into their original torch variants. Disabling
`breezeTorchExtinguishing` prevents gusts from creating more but does not
remove already placed or collected unlit torches. Regardless of this setting,
ordinary, soul, and copper unlit torches
appear beside their lit counterparts in Functional Blocks; unlit redstone
torches also appear in Redstone Blocks. There is no crafting recipe for the items.
The same gusts also extinguish regular, soul, and all eight copper lantern
variants. Unlit lanterns preserve hanging and waterlogged states, oxidation,
and waxing; they emit no light and drop matching placeable unlit items. A fire
charge restores the corresponding lit lantern and consumes one charge (except
in Creative), but waterlogged lanterns must be drained first. Copper variants
retain oxidation, waxing, unwaxing, and axe scraping. All variants appear beside
their lit counterparts in Functional Blocks. Their original dark, flameless
textures are placeholders that can be replaced later. No crafting recipes are
added; obtain them through Breeze gusts or Creative.

## Pumpkin lanterns

Right-click a placed carved pumpkin with a torch to turn it into a vanilla
Jack o'Lantern, or with a soul torch to turn it into a Soul Jack o'Lantern
with blue flames and light level 10. The pumpkin keeps its facing and one
torch is consumed (except in creative mode). Breaking the soul variant drops
its own item, which also appears next to the vanilla Jack o'Lantern in the
Functional Blocks creative tab and can be placed directly.
Soul Jack o'Lanterns also work in Iron Golem and Copper Golem patterns, but
not Snow Golem patterns. Two stacked soul sand/soil blocks instead create a
Soul Golem. The `pumpkinLanterns` setting controls only the lighting interaction; it does not
hide or delete existing items or blocks.

An adult Hoglin running at least 0.18 blocks per tick (or marked sprinting)
that lands a successful melee hit launches its target upward about three blocks
and slightly away. A player flung into a wall or ceiling during the next
second takes an extra 2 HP of blunt collision damage. Landing on the ground
does not trigger this extra damage. Ordinary Hoglin hits are unaffected.
