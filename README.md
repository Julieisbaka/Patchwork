# Patchwork

A Fabric mod for Minecraft 26.3 that adds small gameplay features, friendlier
pet interactions, new soul-themed tools, and tougher encounters.

## Soul Golems

Stack two soul sand or soul soil blocks, then place a Soul Jack o'Lantern on
top. Mixed stacks work too. You can also light a carved pumpkin on the stack
with a soul torch, or use a Soul Golem Spawn Egg.

The player completing the build or using the egg becomes the golem's owner.
Give an ownerless golem a gold ingot to claim it. Gold also repairs damaged
golems; iron does not work, and repairs cannot transfer ownership.

Soul Golems have half an Iron Golem's health. They shoot Soul Fire Charges
instead of using melee attacks. They defend their owner and assist against
the owner's targets, including hostile players. They do not deliberately
target their owner, the owner's pets, or allies. Unprovoked players and
Creepers are left alone.

They use the Snow Golem body model with a Soul Jack o'Lantern head. Shears
remove and drop the lantern without destroying the golem or changing its
owner. They have soul-themed sounds and no natural spawns or death drops.

## Soul Fire Charges

Craft one by putting a fire charge in the center of a crafting table and
surrounding it with eight soul sand or soul soil blocks. Mixed materials work.

Throw them like regular fire charges: they deal one extra point of direct
damage and create soul fire instead of ordinary fire, without an explosion.
Soul fire from these charges remains lit on ordinary solid supports, not just
soul blocks. They can also light blocks directly. Both charge types can light
candles and campfires.

## Lanterns, torches, and pumpkins

- Breeze gusts can extinguish ordinary, soul, copper, and lit redstone torches,
  plus regular, soul, and all copper lantern variants.
- Unlit variants drop placeable items. Lanterns retain hanging, waterlogging,
  oxidation, and wax states. Unlit lantern inventory/held models use the same
  flat generated-item style as lit lanterns.
- Fire charges relight unlit torches and lanterns. Flint and steel also relights
  all unlit torch variants, including wall torches. Drain a lantern before
  relighting it.
- Light a carved pumpkin with a torch for a Jack o'Lantern, or a soul torch for
  a Soul Jack o'Lantern. Facing is preserved.
- Soul Jack o'Lanterns work in Soul, Iron, and Copper Golem builds, but cannot
  create Snow Golems.

## Potion cauldrons

Pour drinkable non-water potions into empty cauldrons. Water-filled cauldrons
reject them. Matching potions refill the cauldron; different potions cannot
mix. Glass bottles retrieve the stored potion.

Use ordinary arrows in either hand, or drop them inside, to make tipped
arrows. Each potion level converts one arrow. Dropped arrows convert in place
without a replacement-item pop or conversion sound. The liquid color matches
the potion, and breaking the block drops a normal cauldron.

## Crafting and customization

- **Charcoal blocks:** compact nine charcoal into a furnace fuel block.
- **Chainmail:** craft chainmail armor using iron chains in the usual armor
  patterns, with recipe-book unlocks.
- **Banners:** create and copy banners with up to nine pattern layers.
- **Shulkers:** recolor living shulkers using dyes.
- **Cauldron washing:** wash dyed wool, terracotta, and stained glass into
  their plain versions.

## Pets and throwable items

- Equip an owned wolf with a banner, replace it, or sneak with an empty hand
  to remove it. Its collar and armor remain intact.
- Sword sweeps do not accidentally hit the attacker's owned cats and wolves.
- The Call goat horn recalls nearby seated pets to safe landing spots.
- Throw slimeballs to damage and slow enemies, or heal and speed up Slimes.
- Throw regular fire charges for a non-explosive ranged attack that lights
  supported surfaces.

## Mobs and encounters

- Wither health scales with difficulty, and its birth explosion hits harder.
- Bees defend flowers near occupied, unsmoked hives.
- Endermen may place carried blocks defensively.
- Spiders can lay webs while pursuing prey.
- Larger Slimes leave a short-lived slowing dust cloud when they split.
- Breezes answer melee hits with a knockback gust and can extinguish lights.
- Charging Hoglins launch victims; collisions make the impact worse.
- Nearby bow Skeletons look for corner cover while reloading.
- Optional Creeper chain reactions ignite surviving Creepers caught in
  another Creeper's blast. This feature is off by default.

## Experience clumping

Nearby XP orbs combine into larger orbs, including orbs with different values.
One pickup collects the combined value, with Mending applied first. This
reduces entity counts and repeated pickup delays without intentionally losing
XP.

## Documentation and artwork

- [Configuration](docs/configuration.md): switches, Mod Menu, multiplayer, and
  migration behavior.
- [Technical details](docs/technical.md): exact balance values, persistence,
  implementation choices, and limitations.
- [Testing](docs/testing.md): build and GameTest instructions, coverage, and
  manual checks.
- [Texture handoff](docs/textures.md): requested assets and vanilla-style art
  requirements.

Artwork is still provisional. The final vanilla-consistent texture pass is
waiting for supplied original or licensed textures; current placeholders are
not claimed to match vanilla faithfully. Unlit lantern item icons temporarily
reuse their readable lit vanilla icons, including the flame; the 2D model fix
is complete, but the final unlit artwork is pending.

## Suggested next improvements

These are suggestions, not implemented features:

- Owner commands for Soul Golems: follow, stay, and patrol, with a visible
  ownership indicator.
- Restore a sheared golem's lantern by giving it a Soul Jack o'Lantern.
- A projectile-friendly-fire option and finer fire-placement controls for
  multiplayer servers.
- Complete the texture pass before expanding the unlit variants further.
- Continue the anvil, villager-trade, and Ender Dragon work in [TODO](TODO.md).
