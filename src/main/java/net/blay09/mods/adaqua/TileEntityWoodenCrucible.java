package net.blay09.mods.adaqua;

import exnihilo.blocks.tileentities.TileEntityCrucible;
import exnihilo.registries.CrucibleRegistry;
import exnihilo.registries.HeatRegistry;
import exnihilo.registries.helpers.Meltable;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Random;

public class TileEntityWoodenCrucible extends TileEntityCrucible {

    private static final Random random = new Random();

    @Override
    public boolean addItem(ItemStack itemStack) {
        Meltable meltable = CrucibleRegistry.getItem(Block.getBlockFromItem(itemStack.getItem()), itemStack.getItemDamage());
        return !(meltable == null || meltable.fluid != FluidRegistry.WATER) && super.addItem(itemStack);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        Block targetBlock = worldObj.getBlock(xCoord, yCoord - 1, zCoord);
        int targetMeta = worldObj.getBlockMetadata(xCoord, yCoord - 1, zCoord);
        float heat = HeatRegistry.getSpeed(targetBlock, targetMeta);
        if(heat > 0.1f || targetBlock == Blocks.flowing_lava) {
            // They tried to put something hot below. I bet they thought they could speed it up.
            // Let's prove them wrong.
            if(random.nextFloat() <= 0.025f) {
                // Bwahahahaha.
                int offsetX = random.nextInt(4) - 2;
                int offsetZ = random.nextInt(4) - 2;
                if(worldObj.isAirBlock(xCoord + offsetX, yCoord, zCoord + offsetZ)) {
                    worldObj.setBlock(xCoord + offsetX, yCoord, zCoord + offsetZ, Blocks.fire);
                }
            }
        }
    }

    @Override
    public float getMeltSpeed() {
        return AdAqua.instance.getWoodenCrucibleSpeed();
    }

}
