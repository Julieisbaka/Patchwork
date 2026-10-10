# Testing

Patchwork targets Minecraft 26.3, Fabric Loader 0.19.5, Fabric API
0.161.0+26.3, Loom 1.18.2, and Java 25. Client source is in the split client
source set. Mod Menu 21.0.0 is included.

## Build

Gradle's checked-in daemon JVM criteria select Java 25 even when the wrapper
is launched with an older supported Java version, including Java 21 in GitHub's
automatic dependency submission job. Linux x86-64 runners can automatically
download Temurin 25; on other platforms, install Java 25 before building.

From the repository root, run the Gradle wrapper for your platform:

```powershell
.\gradlew.bat compileJava compileClientJava
.\gradlew.bat build
```

```sh
./gradlew compileJava compileClientJava
./gradlew build
```

The Gradle unit-test task has no conventional unit tests. Gameplay validation
uses Fabric GameTests, not a passing empty `test` task.
Both Java source sets compile with `-Xlint:deprecation -Werror`; deprecated
API calls fail the build rather than being hidden or suppressed.

## Server GameTests

With cheats/operator permissions, run:

```text
/test run patchwork:patchwork_game_tests_unlit_torch_variants
```

Tab completion lists other tests. `/test runfailed` reruns failures;
`/test runthese` reruns nearby test structures.

For headless tests, define a Loom server run with VM argument
`-Dfabric-api.gametest`. Optional
`-Dfabric-api.gametest.filter=patchwork:*soul*` selects a wildcard group,
not a regular expression. Give each configuration its own run directory.
Use `--no-configuration-cache` when injecting temporary Gradle run definitions.
Do not commit generated run directories or temporary init scripts.

Feature tests fail explicitly if a required configuration switch is off.
Breeze extinguishing requires `mobGriefing=true`. The XP configuration test
is intentionally valid with clumping both on and off; run it in separate
directories with separate configurations.

## Automated coverage

- Configuration creation, empty-file initialization, automatic migration of
  each missing setting, preservation of custom/unknown values, unchanged complete
  files, and rejection of invalid booleans/radii without modifying the file.
  Select with `patchwork:*configuration_automatically*`.
- Every numeric setting's default, missing-key migration, exact lower/upper
  bounds, invalid/fractional/non-finite rejection, save/read round-trips,
  unknown-property preservation, and invalid paired Spider distances without
  rewriting the file. Select with `patchwork:*numeric_configuration*`.
- Animal dropped-food search/pursuit radius and enabled/disabled behavior.
  Select with `patchwork:*animal_food_uses_configured*`; also run with a
  non-default radius and `animalDroppedFood=false` in separate run directories.
  Gameplay tests with exact historical damage/timing expectations otherwise
  require the default numeric values.
- Rabbit carrot taming in either hand, baby/Creative behavior, exclusive owner
  commands, unchanged breeding, ownership/stay save-load and legacy wild saves,
  actual stay/follow movement, owner avoidance, safe/unsupported/leashed teleport
  checks, Call horn recall, and owner-versus-wild rabbit sweep protection.
  Select these with `patchwork:*rabbit*`.
- Copied block loot-table and description identities across garden blocks,
  charcoal, soul pumpkins, torches, copper families, lanterns, and cauldrons.
  Select with `patchwork:*copied_block_properties*`.
- Restored painting placement-tag membership, Creative presets, preservation of
  vanilla paintings, and normal placement on a 2x2 wall. Illusioner counts across
  Easy/Normal/Hard raid waves and bonus waves, unchanged pillager spawning,
  bow equipment, live raid membership, health accounting, and removal.
  Select these with `patchwork:*restored*`.
- Wax block's exact nine-honeycomb recipe, recovery of nine honeycomb from one
  wax block, incomplete/wrong ingredient rejection, and block drops.
- Paeonia ground support, flower tags, compost component, potting and pot
  drops, magenta dye recipe, actual feature placement, and biome registration
  in flower forests/meadows but not plains. Select these with `patchwork:*garden*`.
- Client garden block/item models, supplied texture dimensions/transparency,
  all original replacement texture resources, opaque Soul Golem torso UVs,
  and placed wax/flower/pot and lantern/copper-torch screenshots.
  Wax, Paeonia, charcoal, and soul pumpkin artwork is preserved. The regular
  unlit torch is losslessly reduced to 16x16, occupying the two central UV
  columns from row 6 through row 15. Other variants retain vanilla silhouettes,
  UV layout, metalwork, and wood grain. Unlit lanterns change only the flame,
  while soul charges and the golem retain vanilla detail with changed colors.

- Soul Golem build combinations, invalid builds, torch lighting, head placement,
  spawn eggs, owner assignment, owner combat assistance/defense, ranged impact,
  no melee damage, gold-only repair, claim protection, shearing, and persistence.
- Soul Fire Charge shaped recipe with mixed materials, missing ingredients,
  throwing clearance/cooldown/consumption, real vanilla soul fire on ordinary
  solid supports and soul sand/soil, unchanged support blocks, charge-only
  survival, block-state save/load, neighbor/support updates, Creative placement,
  rejection of floating/occupied positions, Combat tab adjacency, direct block use,
  actual redstone-triggered dispenser consumption/projectile ignition,
  dispenser factory/registration, automatic ranged goal firing,
  exact 2-versus-3 HP projectile damage, flight through a protected
  owner, `mobGriefing` suppression, and removal when support disappears.
- Snow Golem rejection of soul lanterns and preservation of ordinary pumpkins.
- Unlit torch conversion, drop, fire-charge relighting, flint-and-steel
  relighting, all standing/wall variants, and wall facing. Copper torch tests
  exercise actual seeded random weathering, honeycomb consumption, axe wax
  removal/one-stage scraping/durability, waxing recipes, and stage-preserving
  relighting/drops for lit/unlit standing/wall families.
  Lit standing/wall and waxed copper stages emit exactly 14/12/10/8 light;
  extinguished versions emit zero. Client checks require distinct, progressively
  dimmer flame pixels while preserving the vanilla silhouette and wood grain.
- Soul/copper candles and candle cakes, copper campfires and Jack o'Lanterns:
  exact stage light levels, seeded oxidation, waxing/scraping, property retention,
  survival recipes, and copper-torch pumpkin lighting. Copper campfire cooking
  inventories survive oxidation, waxing, and scraping.
  Every copper campfire cooks raw beef and preserves its variant/lit state in
  Silk Touch drops; ordinary drops remain two charcoal.
- All 36 unlit candle/campfire items: extinguished placement, candle stacking,
  candle-on-cake placement, normal/soul ignition, waterlogged campfire ignition
  rejection, candle drop counts, and reversible crafting conversions.
  Client checks cover all new block states and item models, vanilla texture
  silhouettes, animation-sheet dimensions, and an in-game light-family screenshot.
- All ten unlit lantern conversions, relighting/drops, waterlogging,
  weathering/waxing/scraping mappings, and Breeze extinguishing.
- Empty potion-cauldron filling, water rejection, vanilla water bottles,
  matching/different refills, bottle extraction, offhand arrow use, and dropped
  arrow entity identity/quantities/pickup state.
- Bundle undyeing, cactus flower and tall-flower potting, increased Creaking
  attributes, animal eating with mobGriefing enabled/disabled, and Snow Speed
  level, exclusivity, and snow movement.
- Iron and gold armor-wearing achievements, prismarine and shipwreck
  achievements, player-head crafting, and water-free sugar-cane growth on mud.
- Cauldron-washed bundles, animal-food and relighting advancements, Soul Golem
  summoning advancement, Loyal trident void recovery, and mixed stone/cobblestone
  recipes for every stone tool.
- Elytra crafting dye, sweet-berry trader offers, spider ceiling movement,
  cave-spider Nausea, and the Getting Wood, Benchmarking, and Overpowered
  advancement resources.
- Nine-layer loom limit/copying, charcoal's 16,000-tick furnace fuel, washing,
  shulker dyeing, Wither maximum health, Slimeball hit effects, Hoglin launch,
  regular thrown fire-charge side impacts, shield knockback, food-use
  interruption, short egg/snowball cooldowns, hotbar-swap attack strength,
  sprint preservation, and Speed's Happy Ghast flying-speed modifier.
- XP mixed values/counts, radius, Mending/full pickup, integer persistence,
  overflow protection, and enabled/disabled configuration.

## Client GameTest

The `fabric-client-gametest` entrypoint is dormant in normal play. Define a
Loom client run with `-Dfabric.client.gametest` and
`-Dfabric.client.gametest.modid=patchwork`.

The test creates a temporary world, checks headed/sheared Soul Golem render
states, validates all ten lantern items' generated-model thickness, exercises
Soul Fire Charge item/projectile rendering and original transparent icon,
all ordinary/charge soul-fire state models, copper torch item models, and writes
screenshots to the run directory. Potion tests pour one bottle into a rendered
empty cauldron and require at least 100 strongly red/green pixels in the central
world-view region of screenshots. They also change only the block entity color
while keeping the block state unchanged. Two ordinary client ticks let terrain
extraction submit dirty sections before waiting for rendering to finish.
No second pour or reload is used.
Removing the client mesh invalidation makes this regression fail with a stale
color, confirming it checks the rendered result rather than just stored data.
In-world checks exercise model loading and client mixins; a server-only build cannot
validate those.
It also tames a rabbit with a real connected server player, checks client
owner/stay synchronization, and verifies exactly one wolf banner layer plus
banner render-state extraction after the renderer API migration.

## Remaining manual checks

- Ranged cadence, pathing around obstacles, distant/offline owners, PvP team
  behavior, and listening to golem sounds.
- Configuration screen scrolling/tooltips, canceled edits, multiplayer server
  authority, and wolf banners while walking/sitting.
- Chainmail recipe-book UI, Wither birth-explosion damage under different
  exposure/armor, and difficulty transitions.
- Call horn crowded destinations, owner interactions, hotbar cooldown visuals,
  full inventories, waterlogged lights, and Creative interactions.
- Enderman defense, Spider webs, dust-cloud timing, Breeze cooldown/knockback,
  and Hoglin wall/ceiling collision damage.
- Potion persistence across an actual world save/restart.

[Feature overview](../README.md) | [Technical details](technical.md)
