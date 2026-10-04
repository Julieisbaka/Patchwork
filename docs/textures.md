# Texture handoff

The remaining texture pass needs supplied original or licensed artwork.
Reference vanilla's visual language: crisp pixels, restrained shading,
consistent material colors, and matching silhouettes. Do not repaint the metal
frame, wood, oxidation, or wax presentation simply to dim the glowing pixels.
For unlit variants, keep the normal silhouette and materials and reduce the
brightness of only the normally luminous area. Supply your original/licensed
lit source images if you want that edit applied here; Minecraft texture files
are referenced by models, not copied or modified into this repository.

All paths below are relative to
`src/main/resources/assets/patchwork/textures/`.

## Priority assets

| Asset                                    | Size/layout                                                             | Destination                                           |
| ---------------------------------------- | ----------------------------------------------------------------------- | ----------------------------------------------------- |
| Soul Golem body                          | 64x64, Snow Golem UVs; exposed head also needs a finished face          | `entity/soul_golem.png`                               |
| Soul Golem spawn egg                     | 16x16, vanilla-style egg silhouette and highlights; brown/cyan identity | `item/soul_golem_spawn_egg.png`                       |
| Six unlit lantern block textures         | 16x16, vanilla lantern block UV layout; dim luminous pixels only        | `block/unlit_*lantern.png`                            |
| Six unlit lantern item icons             | 16x16, flat lantern icon; dim luminous pixels only                      | `item/unlit_*lantern.png`                             |
| Six unlit torch textures                 | 16x16, vanilla torch UV layout; dim luminous pixels only                | `block/unlit_*torch.png`                              |
| Three lit oxidized copper torch textures | 16x16, same copper torch UV layout and green flame; age the copper only | `block/{exposed,weathered,oxidized}_copper_torch.png` |

The six lantern families are `unlit_lantern`, `unlit_soul_lantern`,
`unlit_copper_lantern`, `unlit_exposed_copper_lantern`,
`unlit_weathered_copper_lantern`, and `unlit_oxidized_copper_lantern`.
Waxed variants reuse their unwaxed family's assets; do not create duplicate
waxed artwork.

The six needed unlit torch files are:

- `block/unlit_torch.png`
- `block/unlit_soul_torch.png`
- `block/unlit_copper_torch.png`
- `block/unlit_exposed_copper_torch.png`
- `block/unlit_weathered_copper_torch.png`
- `block/unlit_oxidized_copper_torch.png`

The unlit redstone torch already references vanilla's off texture and does not
need a new image. Wall torches and waxed variants reuse the corresponding
standing/unwaxed image. No separate torch item icons are required.
New copper oxidation stages currently share the unaffected texture until the
three lit and three unlit aged copper images arrive; wire those six models to
their matching stage textures when supplied. The unaffected lit copper torch
already uses vanilla artwork and does not need a replacement.

The Soul Golem model uses UV origins `(0,0)` for the head, `(32,0)` for arms,
`(0,16)` for the upper body, and `(0,36)` for the lower body. The head must look
intentional after the Soul Jack o'Lantern has been sheared off.

## Other artwork to review

- `block/soul_jack_o_lantern.png`: preserve a pumpkin face with soul lighting.
- `block/charcoal_block.png`: match charcoal's material without looking like
  recolored polished stone.
- `block/potion_cauldron_liquid.png`: keep neutral/tintable; potion color is
  applied at runtime, so avoid baking a specific potion color into it.
- Custom particle artwork, if supplied: preserve readability without large,
  noisy visual effects. Reused vanilla assets are references, not copied files.

## Temporary model wiring

Lantern items now use `minecraft:item/generated`. Their `layer0` temporarily
references the corresponding vanilla lit item icons, as requested for
readability while waiting for artwork. These temporary icons still show a
flame; the actual blocks remain unlit. Once proper item icons arrive,
change the six item-model texture references to `patchwork:item/unlit_*lantern`.
Placed lanterns continue to use their block textures and 3D geometry.

The Soul Fire Charge now uses an original transparent 16x16 cyan ember icon at
`item/soul_fire_charge.png`, with a generated item model. No supplied charge
texture is needed. The same model renders its projectile.
Charge-placed fire uses the real vanilla soul-fire block and its existing
textures; no new fire texture is needed.
The Soul Golem and egg still use their existing temporary textures.

## Acceptance checks

- Compare every lit/unlit pair in inventory, both hands, on the ground, and
  placed/hanging. Materials and silhouette should match; only lighting changes.
- Compare all oxidation stages and waxed pairs under the same lighting.
- Inspect the Soul Golem's front/back/sides, arms, walking pose, and bare head.
- Check egg/charge item transparency, hotbar readability, and thrown charge.
- Reload resources and check for missing models/textures; run the client
  GameTest documented in [testing](testing.md).
