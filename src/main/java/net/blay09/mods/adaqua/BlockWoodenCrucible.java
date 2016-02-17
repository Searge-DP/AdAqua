package net.blay09.mods.adaqua;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BlockWoodenCrucible extends BlockContainer {

    public BlockWoodenCrucible() {
        super(Material.wood);
        setCreativeTab(CreativeTabs.tabDecorations);
        setHardness(2f);
        setBlockName(AdAqua.MOD_ID + ".woodenCrucible");
        GameRegistry.registerTileEntity(TileEntityWoodenCrucible.class, getUnlocalizedName());
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        this.blockIcon = Blocks.planks.getIcon(0, 0);
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWoodenCrucible();
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntityWoodenCrucible tileEntity = (TileEntityWoodenCrucible) world.getTileEntity(x, y, z);
        return tileEntity.getLightLevel();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
        if (entityPlayer == null) {
            return false;
        }
        TileEntityWoodenCrucible tileEntity = (TileEntityWoodenCrucible) world.getTileEntity(x, y, z);
        if (entityPlayer.getCurrentEquippedItem() != null) {
            ItemStack heldItem = entityPlayer.getCurrentEquippedItem();
            if (heldItem != null && tileEntity.addItem(heldItem) && !entityPlayer.capabilities.isCreativeMode) {
                --heldItem.stackSize;
                if (heldItem.stackSize == 0) {
                    heldItem = null;
                }
            }

            FluidStack heldItemFluid = FluidContainerRegistry.getFluidForFilledItem(heldItem);
            if (heldItemFluid != null) {
                int available = tileEntity.fill(ForgeDirection.UP, heldItemFluid, false);
                if (available > 0) {
                    tileEntity.fill(ForgeDirection.UP, heldItemFluid, true);
                    if (!entityPlayer.capabilities.isCreativeMode) {
                        if (heldItem.getItem() == Items.potionitem && heldItem.getItemDamage() == 0) {
                            entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, new ItemStack(Items.glass_bottle, 1, 0));
                        } else {
                            entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, this.getContainer(heldItem));
                        }
                    }
                }
            } else if (FluidContainerRegistry.isContainer(heldItem)) {
                FluidStack fluidStack = tileEntity.drain(ForgeDirection.DOWN, Integer.MAX_VALUE, false);
                if (fluidStack != null) {
                    ItemStack filledStack = FluidContainerRegistry.fillFluidContainer(fluidStack, heldItem);
                    if (filledStack != null) {
                        FluidStack filledFluid = FluidContainerRegistry.getFluidForFilledItem(filledStack);
                        if (filledFluid != null) {
                            if (heldItem.stackSize > 1) {
                                boolean added = entityPlayer.inventory.addItemStackToInventory(filledStack);
                                if (!added) {
                                    return false;
                                }

                                heldItem.stackSize--;
                            } else {
                                entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, filledStack);
                            }

                            tileEntity.drain(ForgeDirection.DOWN, filledFluid.amount, true);
                            return true;
                        }
                    }
                }
            }
        }

        return true;
    }

    private ItemStack getContainer(ItemStack itemStack) {
        if (itemStack.stackSize == 1) {
            return itemStack.getItem().hasContainerItem(itemStack) ? itemStack.getItem().getContainerItem(itemStack) : null;
        } else {
            itemStack.splitStack(1);
            return itemStack;
        }
    }

}
