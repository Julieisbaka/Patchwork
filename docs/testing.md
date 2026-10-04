# Testing

Patchwork targets Minecraft 26.3, Fabric Loader 0.19.5, Fabric API
0.161.0+26.3, Loom 1.18.2, and Java 25. Client source is in the split client
source set. Mod Menu 21.0.0 is included.

## Build

From the repository root on Windows:

```powershell
.\gradlew.bat compileJava compileClientJava
.\gradlew.bat build
```

The Gradle unit-test task has no conventional unit tests. Gameplay validation
uses Fabric GameTests, not a passing empty `test` task.

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

- Soul Golem build combinations, invalid builds, torch lighting, head placement,
  spawn eggs, owner assignment, owner combat assistance/defense, ranged impact,
  no melee damage, gold-only repair, claim protection, shearing, and persistence.
- Soul Fire Charge shaped recipe with mixed materials, missing ingredients,
  throwing clearance/cooldown/consumption, real vanilla soul fire on soul
  sand/soil, rejection of ordinary supports, direct block use, dispenser factory/registration, automatic ranged
  goal firing, exact 2-versus-3 HP projectile damage, flight through a protected
  owner, `mobGriefing` suppression, and removal when support disappears.
- Snow Golem rejection of soul lanterns and preservation of ordinary pumpkins.
- Unlit torch conversion, drop, fire-charge relighting, flint-and-steel
  relighting, all standing/wall variants, and wall facing. Copper torch tests
  exercise actual seeded random weathering, honeycomb consumption, axe wax
  removal/one-stage scraping/durability, waxing recipes, and stage-preserving
  relighting/drops for lit/unlit standing/wall families.
- All ten unlit lantern conversions, relighting/drops, waterlogging,
  weathering/waxing/scraping mappings, and Breeze extinguishing.
- Empty potion-cauldron filling, water rejection, vanilla water bottles,
  matching/different refills, bottle extraction, offhand arrow use, and dropped
  arrow entity identity/quantities/pickup state.
- Nine-layer loom limit/copying, charcoal's 16,000-tick furnace fuel, washing,
  shulker dyeing, Wither maximum health, Slimeball hit effects, Hoglin launch,
  and regular thrown fire-charge side impacts.
- XP mixed values/counts, radius, Mending/full pickup, integer persistence,
  overflow protection, and enabled/disabled configuration.

## Client GameTest

The `fabric-client-gametest` entrypoint is dormant in normal play. Define a
Loom client run with `-Dfabric.client.gametest` and
`-Dfabric.client.gametest.modid=patchwork`.

The test creates a temporary world, checks headed/sheared Soul Golem render
states, validates all ten lantern items' generated-model thickness, exercises
Soul Fire Charge item/projectile rendering, copper torch item models, and writes
screenshots to the run directory. Potion tests pour one bottle into a rendered
empty cauldron and require at least 100 strongly red/green pixels in the central
world-view region of screenshots. They also change only the block entity color
while keeping the block state unchanged. No second pour or reload is used.
In-world checks exercise
resource reload, model loading, and client mixins; a server-only build cannot
validate those.

## Remaining manual checks

- Final supplied textures: see [texture handoff](textures.md).
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
