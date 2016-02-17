package net.blay09.mods.adaqua.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import exnihilo.blocks.models.ModelCrucible;
import exnihilo.blocks.renderers.RenderCrucible;
import exnihilo.blocks.tileentities.TileEntityCrucible;
import net.blay09.mods.adaqua.AdAqua;
import net.blay09.mods.adaqua.CommonProxy;
import net.blay09.mods.adaqua.TileEntityWoodenCrucible;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy {

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        ModelCrucible model = new ModelCrucible();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWoodenCrucible.class, new RenderWoodenCrucible(model));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrucible.class, new RenderCrucibleFixed(model));
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(AdAqua.blockWoodenCrucible), new ItemRenderWoodenCrucible(model));
    }

}
