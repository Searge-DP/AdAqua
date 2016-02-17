package net.blay09.mods.adaqua.client;

import exnihilo.blocks.models.ModelCrucible;
import net.blay09.mods.adaqua.AdAqua;
import net.minecraft.util.ResourceLocation;

public class RenderWoodenCrucible extends RenderCrucibleFixed {
    public static ResourceLocation texture = new ResourceLocation(AdAqua.MOD_ID, "textures/blocks/ModelWoodenCrucible.png");

    public RenderWoodenCrucible(ModelCrucible model) {
        super(model);
    }

    @Override
    public void bindCrucibleTexture() {
        bindTexture(texture);
    }

}
