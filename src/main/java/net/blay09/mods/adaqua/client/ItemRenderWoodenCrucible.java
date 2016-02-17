package net.blay09.mods.adaqua.client;

import exnihilo.blocks.models.ModelCrucible;
import exnihilo.blocks.renderers.blockItems.ItemRenderCrucible;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;

public class ItemRenderWoodenCrucible extends ItemRenderCrucible {
    public ItemRenderWoodenCrucible(ModelCrucible model) {
        super(model);
    }

    @Override
    protected void bindTexture() {
        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        if(texturemanager != null) {
            texturemanager.bindTexture(RenderWoodenCrucible.texture);
        }
    }
}
