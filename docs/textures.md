# Artwork

Patchwork now includes the previously pending texture set. Custom images live
under `src/main/resources/assets/patchwork/textures/`.

## Sources and layout

- **Supplied artwork:** wax block and Paeonia. The supplied 160x160 images were
  converted to 16x16 PNG with nearest-neighbor sampling; Paeonia keeps its alpha.
- **Original artwork:** Soul Golem body and egg, Soul Fire Charge icon,
  Soul Jack o'Lantern face, charcoal block, six unlit lantern block sheets and
  flat icons, six unlit torch sheets, and three aged lit copper torch sheets.
- **Vanilla references:** unchanged models still reference pumpkin side/top,
  flower-pot geometry/materials, unlit redstone torch, and other vanilla
  resources. Vanilla files are not redistributed as custom artwork.

Lantern/torch textures use their existing vanilla UV layouts. Their original
material palettes keep crisp pixels and restrained shading; unlit luminous
areas are dark, with no drawn flame. Because these are original replacements
rather than edits of vanilla images, they are not pixel-identical to vanilla.
Aged lit/unlit copper torches share a material design, changing only the
luminous region between each lit/unlit pair.

Waxed and wall variants reuse their family's assets. Lantern item models use
flat generated icons rather than 3D block models. No duplicate waxed textures,
wall textures, or separate torch item icons are needed.

The 64x64 Soul Golem sheet uses Snow Golem UVs: head `(0,0)`, arms `(32,0)`,
upper body `(0,16)`, and lower body `(0,36)`. Its bare head has a face after
shearing. The lantern worn on its head uses the block model.

Soul Fire Charge is a transparent 16x16 cyan ember; the projectile uses its
item model. Charge-created fire uses real vanilla soul fire and needs no
separate fire textures. Potion cauldron liquid stays neutral for runtime tinting.

## Visual checks

- Inspect inventory, both hands, dropped items, and placed/hanging lanterns.
- Compare copper stages and waxed pairs under identical lighting.
- Inspect Soul Golem front/back/sides, arms, walking pose, and sheared head.
- Check transparent flower/icons and hotbar readability.
- Reload resources and run the client GameTest in [testing](testing.md),
  which checks texture dimensions, icon alpha, and model availability.
