# Technical details

## Wax block and Paeonia

- `patchwork:wax_block` is a decorative full block with honeycomb-block
  hardness and sound. Nine honeycomb in a filled 3x3 grid craft one block;
  obtaining honeycomb unlocks the recipe. It drops itself and appears beside
  the honeycomb block in Building Blocks. It does not add copper-waxing
  interactions or a reverse crafting recipe.
- `patchwork:paeonia` is a one-block flower with allium placement rules and
  suspicious-stew effects. It drops itself, supports flower pots, composts
  like other small flowers, feeds bees, and crafts one magenta dye.
- Its patch is added only to flower forests and meadows during vegetal
  decoration: a 1-in-24 chunk attempt with six nearby surface placements.
  Actual density depends on air, terrain, and valid flower support.
  Existing terrain is unchanged; explore new chunks to find it naturally.
  It appears beside allium in Natural Blocks.
- Supplied 160x160 images are normalized to 16x16 PNG using nearest-neighbor
  sampling, preserving the pixel art and Paeonia's transparent background.

## Soul Golems and ownership

- Entity ID: `patchwork:soul_golem`; 50 HP; hitbox 0.7 by 1.9 blocks,
  eye height 1.7. No natural spawn entry or death loot table.
- The Iron Golem superclass supplies movement and persistent defender
  behavior. Its melee and approach goals are replaced with `RangedAttackGoal`:
  a 16-block attack radius and a 40-tick firing interval. Direct melee attacks
  do no damage. The former quarter-melee-damage mixin is removed.
- Owner-defense and owner-assistance goals have priority over self-defense
  and ordinary hostile-mob targeting. They use the owner's most recent combat
  target and timestamp, following vanilla pet-target-goal conventions.
- Players and Creepers are eligible only through the owner's combat history.
  Owners, pets with the same owner, and teammates/allies are protected.
  Creative and spectator players are not targeted.
- The final head placement or soul-torch lighting records the builder during
  synchronous construction, including nested placement cleanup. A spawn egg
  records its player user. Commands and dispensers create ownerless golems.
- Gold claims an ownerless golem, even at full health, consuming one ingot.
  Subsequent gold repairs restore up to 25 HP per ingot. Full-health owned
  golems consume none. Other players may repair but cannot steal ownership.
  Creative consumes no gold.
- Owner references are persisted as `Owner`; the synced lantern flag is saved
  as `HasSoulLantern`. Missing fields preserve compatibility with older worlds:
  an old golem is ownerless and has its lantern.
- Shears drop one Soul Jack o'Lantern, damage the shears once, and clear the
  synced head flag. Repeat shearing gives nothing. Dispenser shearing uses the
  `Shearable` interface. The body still renders and shoots when sheared.
- Construction supports all four soul sand/soil combinations. Soul lanterns
  remain valid for Iron/Copper Golems, but are not added to the Snow Golem
  pattern predicate.
- Sounds use soul soil for steps/repairs, soul sand for damage, soul escape for
  death, and fire-charge use for shooting. The Snow Golem shear sound is reused.

## Soul Fire Charges

- Item: `patchwork:soul_fire_charge`; entity: `patchwork:soul_fireball`.
- A shaped 3x3 recipe consumes one central fire charge and eight tagged soul
  sand/soil items. It produces one charge; mixed surrounding ingredients work.
  Obtaining a fire charge unlocks the recipe.
- Player throws share regular fire-charge behavior: five-block unobstructed
  sight line, initial speed 0.65, acceleration power 0.03, 30-tick cooldown,
  survival consumption, and no explosion.
- Dispensers launch the soul projectile rather than vanilla's ordinary small
  fireball. Soul projectiles use a soul-flame particle trail.
- Regular thrown charges retain 2 HP direct damage. Soul projectiles deal
  3 HP direct damage and apply two seconds of fire after a successful hit.
  Armor, difficulty, immunity, and later burning can affect observed total
  damage. Soul Golem shots use this same projectile, not randomized melee rolls.
- The projectile carries its shooter for damage attribution and persistence.
  Golem shots skip collision with the owner, owner-owned pets, and allies.
  Fire placed in the world is environmental and can still hurt them.
- Block impacts light eligible candles/campfires; otherwise they attempt the
  hit face and then the top of a block for unsuccessful horizontal hits.
  Occupied spaces are never replaced.
- Ignition places `minecraft:soul_fire`, not a custom fire block. A persisted
  `patchwork_charge_placed` block-state flag lets charge-created fire survive
  on any sturdy top face as well as normal soul-fire supports. No support block
  is replaced. Ordinary soul fire defaults to false and retains vanilla survival.
  Neighbor updates preserve the flag; removing support extinguishes the fire.
  Vanilla soul-fire damage, particles, and nonspreading behavior apply.
  Floating fire/occupied positions are rejected without consuming a charge.
- Both fire charges are available in Combat regardless of the throwing switch,
  with the Soul Fire Charge immediately after the ordinary charge.
  Its original transparent 16x16 icon is shared by item and projectile rendering.
- Soul Golem ignition respects `mobGriefing`; player ignition does not.
  Soul Fire Charge direct block use consumes one charge except in Creative.

## Cauldrons, banners, and fuel

- Potion cauldrons store exact `PotionContents` in a synchronized block entity.
  Client data loading invalidates the terrain mesh after updating its contents,
  including first fill and data-only color changes, without a resource reload.
  Empty-cauldron insertion rejects water/empty contents; water-cauldron
  insertion rejects non-water potions. Vanilla water bottles and dyed-block
  washing keep their normal dispatch paths.
- Maximum potion fill is three. One bottle extracts one level; one arrow
  consumes one level. Matching contents refill; different contents never mix.
  Bottle exchanges use vanilla inventory/Creative conventions.
- Dropped stacks convert up to the available levels in one operation. The
  tipped entity retains identity, motion, age, pickup delay, and ownership.
  Unused plain arrows split off with the same motion and pickup state.
  Conversion itself produces no sound or pickup event.
- Banner pattern limit is nine in the server loom, client loom screen, and
  copying recipe. The result-slot guard also prevents direct menu selection
  from adding a tenth pattern.
- A charcoal block consumes nine charcoal and fuels a normal furnace for
  16,000 ticks: 800 seconds or 80 normal items. Smokers/blast furnaces use the
  vanilla faster cooking fuel rate.

## Unlit lights

Torches include standing and wall regular, soul, copper, and redstone variants.
They emit no light/flame particles/redstone power. Fire charges consume one
charge to relight; flint and steel uses one durability in Survival and none in
Creative. Wall facing and torch type are preserved.

Copper torches have separate four-stage weathering collections for lit/unlit
standing/wall forms, plus matching waxed forms. The original vanilla copper
torch IDs are the unaffected lit forms; their factories create weathering
subclasses without replacing registry entries. All transitions use Fabric's
oxidizable-block registry and vanilla weathering probability/neighborhood rules.
Only unwaxed, nonterminal stages random-tick. Honeycomb and axe interactions
are vanilla, and waxing recipes retain oxidation and lit state. Extinguishing,
relighting, drops, and wall item placement preserve the exact family/stage.
Aged lit and unlit stages have distinct original artwork matching their oxidation.

Lanterns include regular, soul, and eight copper variants. State conversion
preserves hanging/waterlogging, weathering, and wax. Copper mappings use Fabric's
oxidizable-block registry. Waterlogged lanterns cannot be relit.

All ten lantern items use generated 2D item models, not placed block models.
Waxed versions share models/textures with the matching unwaxed oxidation stage.
Original flat icons and block sheets are included; see [artwork](textures.md).

## Experience clumping

XP awards create one orb. Every 20 ticks, nearby orbs within an actual two-block
radius combine `value * count`, including pre-existing vanilla counted orbs.
Merges reset count to one, preserve the youngest age, and discard the donor.
Sums exceeding `Integer.MAX_VALUE` do not merge.

Pickup delegates to vanilla Mending before awarding the remaining full value.
Saving/loading uses full integers rather than vanilla's short value field.
Rendering grows logarithmically and is capped at three times normal size.
No performance benchmark is claimed.

## Other balance and behavior

| Feature        | Exact behavior                                                                                                                               |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| Wither health  | Easy/Peaceful 300 HP, Normal 450, Hard 600; difficulty changes preserve health percentage                                                    |
| Wither birth   | Adds 3 base damage; keeps radius, exposure, knockback and block interaction                                                                  |
| Chainmail      | Iron-chain armor patterns: 5 helmet, 8 chestplate, 7 leggings, 4 boots                                                                       |
| Slimeball      | 1 HP and Slowness I for 3s; Slimes instead heal 1 HP and gain Speed I for 3s; 20-tick cooldown                                               |
| Call horn      | Seated owned pets within configured radius; default 32, range 16-256; safe destinations only; recalled pets stand                            |
| Skeleton cover | Bow-wielding Skeletons with player within six blocks seek an adjacent reachable obstructing corner while reloading                           |
| Bees           | Breaking flowers within four blocks of an occupied, unsmoked hive releases defenders                                                         |
| Enderman       | One-in-three defensive placement chance; support, space, survival, and collision checked                                                     |
| Spider         | Pursuing targets two-eight blocks away: one-in-four web chance each five seconds; requires empty supported space                             |
| Slime cloud    | 3x3 supported-ground dust patch, five seconds; Slowness II while inside, fading after exit                                                   |
| Breeze         | Melee-response knockback around three blocks; ten-second cooldown; extinguishing range five                                                  |
| Hoglin         | Successful charged hit at speed at least 0.18 blocks/tick or while sprinting launches roughly three blocks; wall/ceiling collision adds 2 HP |
| Creepers       | Blast-damaged surviving Creepers ignite full fuse when the optional chain reaction is enabled                                                |

Wither reference unarmored birth damage changes from 35.5 to 37 on Easy, 69
to 72 on Normal, and 103.5 to 108 on Hard; armor, exposure, and distance matter.
Wolf banners preserve armor/collar, are owner-controlled, return on
replacement/removal, and drop on death. Sword sweep protection does not prevent
direct hits or another player's attacks.

[Feature overview](../README.md) | [Configuration](configuration.md)
