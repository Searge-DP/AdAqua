package net.blay09.mods.adaqua.client;

import exnihilo.blocks.models.ModelCrucible;
import exnihilo.blocks.models.ModelCrucibleInternal;
import exnihilo.blocks.tileentities.TileEntityCrucible;
import exnihilo.registries.ColorRegistry;
import exnihilo.registries.CrucibleRegistry;
import exnihilo.registries.helpers.Color;
import exnihilo.registries.helpers.Meltable;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

public class RenderCrucibleFixed extends TileEntitySpecialRenderer {

    private static boolean fixColor = true;
    private static Field fieldContent;
    private static Field fieldContentMeta;

    private ModelCrucible model;
    private ModelCrucibleInternal internal;

    public RenderCrucibleFixed(ModelCrucible model) {
        this.model = model;
        internal = new ModelCrucibleInternal();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        drawCrucible(tileEntity, x, y, z, f);
        drawContents(tileEntity, x, y, z, f);
    }

    private void drawCrucible(TileEntity tileEntity, double x, double y, double z, float f) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5f, (float) y + 1.5f, (float) z + 0.5f);
        GL11.glScalef(-1f, -1f, 1f);
        bindCrucibleTexture();
        model.simpleRender(0.0625F);
        GL11.glPopMatrix();
    }

    private void drawContents(TileEntity tileEntity, double x, double y, double z, float f) {
        TileEntityCrucible tileCrucible = (TileEntityCrucible) tileEntity;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5f, (float) y + tileCrucible.getAdjustedVolume(), (float) z + 0.5f);
        GL11.glScalef(0.92f, 1f, 0.92f);
        bindInternalTexture();
        if(tileCrucible.hasSolids()) {
            renderSolid(tileCrucible);
        }
        if(tileCrucible.getFluidVolume() > 0) {
            GL11.glTranslatef(0f, -tileCrucible.getAdjustedVolume() + 0.2f + tileCrucible.getFluidVolume() / 10000f, 0f);
            renderFluid(tileCrucible);
        }
        GL11.glPopMatrix();
    }

    private void renderSolid(TileEntityCrucible tileEntity) {
        IIcon icon = tileEntity.getContentIcon();
        Color color = getInternalColor(tileEntity);
        this.internal.render(color, icon, false);
    }

    private void renderFluid(TileEntityCrucible tileEntity) {
        Fluid content = tileEntity.fluid.getFluid();
        IIcon icon = content.getIcon();
        Color color = new Color(content.getColor());
        this.internal.render(color, icon, true);
    }

    public void bindCrucibleTexture() {
        this.bindTexture(ModelCrucible.textures[0]);
    }

    public void bindInternalTexture() {
        ResourceLocation fluidTexture = TextureMap.locationBlocksTexture;
        this.bindTexture(fluidTexture);
    }

    public Color getInternalColor(TileEntityCrucible tileEntity) {
        if(!fixColor || tileEntity.fluid.getFluid() != FluidRegistry.WATER) {
            return ColorRegistry.color("white");
        }
        if(fieldContent == null) {
            try {
                fieldContent = TileEntityCrucible.class.getDeclaredField("content");
                fieldContent.setAccessible(true);
                fieldContentMeta = TileEntityCrucible.class.getDeclaredField("contentMeta");
                fieldContentMeta.setAccessible(true);
            } catch (NoSuchFieldException e) {
                fixColor = false;
            }
        }
        try {
            Block block = (Block) fieldContent.get(tileEntity);
            int meta = fieldContentMeta.getInt(tileEntity);
            if(block != null) {
                Meltable meltable = CrucibleRegistry.getItem(block, meta);
                if(meltable != null) {
                    return new Color(meltable.appearance.getBlockColor());
                }
            }
        } catch (IllegalAccessException e) {
            fixColor = false;
        }
        return ColorRegistry.color("white");
    }
}
