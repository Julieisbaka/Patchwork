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

The Gradle `test` task has no conventional unit tests. Gameplay validation uses
Fabric GameTests; a successful empty `test` task is not gameplay coverage.
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

### Running focused server coverage

Use the Fabric GameTest selector in game, or pass a wildcard filter to a
headless Loom server run. For example, from PowerShell:

```powershell
$env:JAVA_TOOL_OPTIONS = '-Dfabric-api.gametest -Dfabric-api.gametest.filter=patchwork:*rabbit*'
.\gradlew.bat runServer --args='--gameDir build/patchwork-rabbit-gametest --nogui'
Remove-Item Env:JAVA_TOOL_OPTIONS
```

Change the selector and game directory for each run. Useful selectors include
`patchwork:*configuration_automatically*`, `patchwork:*numeric_configuration*`,
`patchwork:*soul_fire_charge*`, `patchwork:*soul_golem*`,
`patchwork:*unlit*`, `patchwork:*restored*`, and `patchwork:*garden*`.
Read the runner output for the required-test count and failures; a server
starting successfully is not by itself evidence that the selected tests ran.

## Automated coverage

- All eight non-English language files match English keys, contain nonempty
  strings, and preserve format placeholders. Select with
  `patchwork:*translations_cover_every_key*`.
- Soul Charge support includes direct use and projectile ignition on the top
  and four sides of planks, logs, leaves, and wool, unsupported-side survival,
  removal after support loss, unchanged ordinary soul fire, and a live leaf-side
  projectile impact. Select with `patchwork:*soul_fire_charge*`.
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
- Client config-screen coverage checks every numeric input and translation,
  invalid fields and paired distances disabling Save, correction re-enabling
  Save, fractional value persistence, reopening, and Cancel discarding edits.
  Add `-Dpatchwork.configScreenTestOnly=true` to a client GameTest run to skip
  the other client groups. The test restores the original config afterward.
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
  solid supports and soul sand/soil, flammable sides and leaves, unchanged support blocks, charge-only
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
- All ten unlit campfire items: extinguished placement, normal/soul ignition,
  waterlogged campfire ignition rejection, drops, and reversible crafting
  conversions. Candles are omitted because vanilla candles are already unlit
  by default.
  Client checks cover all new block states and item models, vanilla campfire
  inventory sprites, distinct oxidation-colored campfire flames, texture
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
- Elytra crafting dye, sweet-berry trader offers, spider ceiling movement and
  upside-down rendering, owner-only wolf banner shearing, cave-spider Nausea,
  and the Getting Wood, Benchmarking, and Overpowered
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

The client entrypoint uses a small dispatcher and focused suites:

| Suite | Coverage |
| --- | --- |
| `PatchworkClientConfigGameTests` | Numeric fields, translation, validation, save/reopen, and cancel behavior. |
| `PatchworkClientPetGameTests` | Rabbit owner/stay synchronization, wolf banner state, and upside-down ceiling-spider rendering. |
| `PatchworkClientPotionGameTests` | Potion-cauldron first-fill and block-entity-only tint updates in screenshots. |
| `PatchworkClientLightGameTests` | Soul Golem and projectile renderers, light state/item models, and copper/Soul textures. |
| `PatchworkClientArtworkGameTests` | Garden models, texture dimensions and alpha, and vanilla-artwork preservation. |

Each group creates and closes its own temporary world where it needs one.
Screenshots are written to the run directory. The potion rendering regression
requires at least 100 strongly red/green pixels in the central world-view
region; it checks the rendered tint after two ordinary client ticks, without a
second pour or resource reload. This catches stale meshes after block-entity
data-only changes rather than checking only stored state. Client checks exercise
model loading and client mixins; a server-only build cannot validate them.

The current entrypoint runs the groups in sequence. Use
`-Dpatchwork.configScreenTestOnly=true` to run only config-screen coverage.
Other group-specific command-line selectors are not currently exposed; to add
one, route it through the dispatcher rather than skipping tests inside an
individual suite.

## Remaining manual checks

- Ranged cadence, pathing around obstacles, distant/offline owners, PvP team
  behavior, and listening to golem sounds.
- Configuration screen scrolling/tooltips, canceled edits, multiplayer server
  authority, and wolf banner placement while walking/sitting.
- Chainmail recipe-book UI, Wither birth-explosion damage under different
  exposure/armor, and difficulty transitions.
- Call horn crowded destinations, owner interactions, hotbar cooldown visuals,
  full inventories, waterlogged lights, and Creative interactions.
- Enderman defense, Spider webs, dust-cloud timing, Breeze cooldown/knockback,
  and Hoglin wall/ceiling collision damage.
- Potion persistence across an actual world save/restart.

[Feature overview](../README.md) | [Configuration guide](configuration.md)
