# Texture handoff

The final texture pass is pending supplied original or licensed artwork.
Do not replace placeholders with another procedurally generated approximation.
Reference vanilla's visual language: crisp pixels, restrained shading,
consistent material colors, and matching silhouettes. Do not repaint the metal
frame, wood, oxidation, or wax presentation simply to remove a flame.

All paths below are relative to
`src/main/resources/assets/patchwork/textures/`.

## Priority assets

| Asset | Size/layout | Destination |
| --- | --- | --- |
| Soul Golem body | 64x64, Snow Golem UVs; exposed head also needs a finished face | `entity/soul_golem.png` |
| Soul Golem spawn egg | 16x16, vanilla-style egg silhouette and highlights; brown/cyan identity | `item/soul_golem_spawn_egg.png` |
| Soul Fire Charge | 16x16, vanilla-style charge silhouette, cyan soul flame | `item/soul_fire_charge.png` |
| Six unlit lantern block textures | 16x16, vanilla lantern block UV layout; remove flame/glow only | `block/unlit_*lantern.png` |
| Six unlit lantern item icons | 16x16, flat vanilla-style lantern icon; flame off | `item/unlit_*lantern.png` |
| Four unlit torch textures | 16x16, vanilla torch UV layout; remove flame/glow only | `block/unlit_*torch.png` |

The six lantern families are `unlit_lantern`, `unlit_soul_lantern`,
`unlit_copper_lantern`, `unlit_exposed_copper_lantern`,
`unlit_weathered_copper_lantern`, and `unlit_oxidized_copper_lantern`.
Waxed variants reuse their unwaxed family's assets; do not create duplicate
waxed artwork. The four torch families are regular, soul, copper, and redstone.
Wall torches reuse standing-variant artwork.

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

The Soul Fire Charge temporarily references the vanilla fire-charge item asset.
Once supplied, point its item model to `patchwork:item/soul_fire_charge`.
The same item model renders its projectile, so one icon updates both.
The Soul Golem and egg still use their existing temporary textures.

## Acceptance checks

- Compare every lit/unlit pair in inventory, both hands, on the ground, and
  placed/hanging. Materials and silhouette should match; only lighting changes.
- Compare all oxidation stages and waxed pairs under the same lighting.
- Inspect the Soul Golem's front/back/sides, arms, walking pose, and bare head.
- Check egg/charge item transparency, hotbar readability, and thrown charge.
- Reload resources and check for missing models/textures; run the client
  GameTest documented in [testing](testing.md).
