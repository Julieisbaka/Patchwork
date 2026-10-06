# Technical overview

Patchwork is a Fabric mod organized around small feature modules. Common
gameplay code and data are kept separate from client-only rendering and UI
code.

## Project layout

| Path | Purpose |
| --- | --- |
| `src/main/java` | Mod initialization, gameplay features, configuration, and mixins |
| `src/client/java` | Renderers, client mixins, and the Mod Menu configuration screen |
| `src/main/resources` | Mod metadata, mixin configuration, language strings, models, textures, recipes, and tags |
| `src/main/resources/data/patchwork` | Patchwork recipes, advancements, and other data-driven content |
| `docs` | Player configuration and contributor testing guidance |

The Gradle build uses separate main and client source sets. The client source
set is packaged with the mod but is only loaded in a client environment.

## Runtime initialization

The `Patchwork` initializer loads configuration before registering gameplay
features. Feature classes register their blocks, items, entities, callbacks, or
other related behavior. Mixin configurations apply targeted changes to vanilla
gameplay where a Fabric event or extension point is not sufficient.

The client initializer registers entity renderers and block colors. The
optional Mod Menu integration opens the configuration screen. Client-only
render state and rendering mixins are kept under `src/client/java`.

## Configuration and multiplayer

Settings are stored in `config/patchwork.properties` and exposed as an
immutable settings record. The server's settings govern multiplayer gameplay;
client settings affect client-only presentation where applicable. See the
[configuration guide](configuration.md) for defaults, dependencies between
options, migration behavior, and error handling.

## Resources

Language entries, block states, item definitions, and models use the
`patchwork` namespace under `src/main/resources/assets`. Recipes and
advancements are data-driven under `src/main/resources/data/patchwork`.
Patchwork also adds selected vanilla tags for interoperability with existing
game content.

The vanilla `painting_variant/placeable` tag is extended, not replaced, with
Earth, Wind, Water, and Fire. Vanilla random placement and Creative painting
presets both read this tag. `RaidMixin` adds one illusioner after each wave
from wave 5 onward through vanilla `joinRaid`, preserving spawn equipment,
wave membership, health accounting, persistence, and raid completion.

## Validation

Gameplay and rendering behavior are tested with Fabric server and client
GameTests. See the [testing guide](testing.md) for build commands, test
selection, automated coverage, and remaining manual checks.
