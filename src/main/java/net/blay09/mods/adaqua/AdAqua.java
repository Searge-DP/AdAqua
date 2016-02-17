package net.blay09.mods.adaqua;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import exnihilo.blocks.BlockCrucible;
import exnihilo.blocks.tileentities.TileEntityCrucible;
import exnihilo.registries.CrucibleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = "adaqua", name = "AdAqua", dependencies = "required-after:exnihilo")
public class AdAqua {

    private static final Pattern ITEMSTACK = Pattern.compile("([^@]+)(?:@([0-9]+|\\*))?");

    public static final Logger logger = LogManager.getLogger();
    public static final String MOD_ID = "adaqua";

    private final List<ItemStack> meltableWaterItems = Lists.newArrayList();

    public static Block blockWoodenCrucible;
    private Configuration config;
    private float woodenCrucibleSpeed;

    @Mod.Instance
    public static AdAqua instance;

    @SidedProxy(clientSide = "net.blay09.mods.adaqua.client.ClientProxy", serverSide = "net.blay09.mods.adaqua.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        blockWoodenCrucible = new BlockWoodenCrucible();
        GameRegistry.registerBlock(blockWoodenCrucible, MOD_ID + ".woodenCrucible");

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        if(config.getBoolean("Enable Wooden Crucible", "general", true, "Set this to false to disable the recipe for the wooden crucible.")) {
            GameRegistry.addRecipe(new ShapedOreRecipe(blockWoodenCrucible, "W W", "W W", "WSW", 'W', "logWood", 'S', "slabWood"));
        }

        woodenCrucibleSpeed = config.getFloat("Wooden Crucible Speed", "general", 0.6f, 0.01f, 10f, "The speed at which the wooden crucible extracts water. 0.1 is equivalent to a torch below a crucible, 0.3 is equivalent to fire below a crucible.");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        // Add additional meltables to Crucible
        String[] meltableWaterBlockList = config.getStringList("Meltable Water Blocks", "general", new String[] {
                "ore:treeLeaves",
                "minecraft:cactus"
        }, "Here you can specify additional blocks that will melt into water. Format: modid:name@meta, meta can be wildcard *, modid can be ore for OreDictionary");
        for (String meltable : meltableWaterBlockList) {
            Matcher matcher = ITEMSTACK.matcher(meltable);
            if (matcher.find()) {
                if(matcher.group(1).startsWith("ore:") && matcher.group(1).length() > 4) {
                    String oreName = matcher.group(1).substring(4);
                    List<ItemStack> itemStacks = OreDictionary.getOres(oreName, false);
                    if(!itemStacks.isEmpty()) {
                        for(ItemStack itemStack : itemStacks) {
                            if(itemStack.getItem() instanceof ItemBlock) {
                                CrucibleRegistry.register(((ItemBlock) itemStack.getItem()).field_150939_a, itemStack.getItem().getMetadata(itemStack.getItemDamage()), 2000f, FluidRegistry.WATER, 250f, ((ItemBlock) itemStack.getItem()).field_150939_a);
                            } else {
                                logger.warn("Skipping ore dictionary meltable water block due to not being a block: " + itemStack.getUnlocalizedName());
                            }
                        }
                    } else {
                        logger.warn("Skipping ore dictionary meltable water block due to no entries: " + oreName);
                    }
                } else {
                    Block block = (Block) Block.blockRegistry.getObject(matcher.group(1));
                    if (block == null || block == Blocks.air) {
                        logger.error("Could not load meltable water block due to block not found: " + matcher.group(1));
                        continue;
                    }
                    int meta = 0;
                    String metaString = matcher.group(2);
                    if (metaString != null) {
                        meta = metaString.equals("*") ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(metaString);
                    }
                    CrucibleRegistry.register(block, meta, 2000f, FluidRegistry.WATER, 250f, block);
                }
            } else {
                logger.error("Could not load meltable water block due to invalid format: " + meltable);
            }
        }

        String[] meltableWaterItemList = config.getStringList("Meltable Water Items", "general", new String[] {
                "ore:treeSapling",
                "minecraft:apple"
        }, "Here you can specify additional items that will melt into water. Items only result in half the amount of water. Format: modid:name@meta, meta can be wildcard *, modid can be ore for OreDictionary");
        for(String meltable : meltableWaterItemList) {
            Matcher matcher = ITEMSTACK.matcher(meltable);
            if (matcher.find()) {
                if(matcher.group(1).startsWith("ore:") && matcher.group(1).length() > 4) {
                    String oreName = matcher.group(1).substring(4);
                    List<ItemStack> itemStacks = OreDictionary.getOres(oreName, false);
                    if(!itemStacks.isEmpty()) {
                        for(ItemStack itemStack : itemStacks) {
                            meltableWaterItems.add(itemStack);
                        }
                    } else {
                        logger.warn("Skipping ore dictionary meltable water item due to no entries: " + oreName);
                    }
                } else {
                    Item item = (Item) Item.itemRegistry.getObject(matcher.group(1));
                    if (item == null) {
                        logger.error("Could not load meltable water item due to item not found: " + matcher.group(1));
                        continue;
                    }
                    int meta = 0;
                    String metaString = matcher.group(2);
                    if (metaString != null) {
                        meta = metaString.equals("*") ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(metaString);
                    }
                    meltableWaterItems.add(new ItemStack(item, 1, meta));
                }
            } else {
                logger.error("Could not load meltable water item due to invalid format: " + meltable);
            }
        }

        // Dummy Entry. Yes, portals will make water. Yes, I'm lazy. Don't tell anyone.
        CrucibleRegistry.register(Blocks.portal, 0, 1000f, FluidRegistry.WATER, 125f, Blocks.leaves);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            TileEntity tileEntity = event.world.getTileEntity(event.x, event.y, event.z);
            if(tileEntity instanceof TileEntityCrucible) {
                ItemStack heldItem = event.entityPlayer.getHeldItem();
                if(heldItem != null) {
                    if(heldItem.getItem() == Item.getItemFromBlock(Blocks.portal)) {
                        // You wish.
                        event.setCanceled(true);
                        return;
                    }
                    if(heldItem.getItem() instanceof ItemBlock) {
                        Block heldBlock = ((ItemBlock) heldItem.getItem()).field_150939_a;
                        if(CrucibleRegistry.containsItem(heldBlock, OreDictionary.WILDCARD_VALUE)) {
                            if(((TileEntityCrucible) tileEntity).addItem(new ItemStack(heldBlock, 1, OreDictionary.WILDCARD_VALUE))) {
                                if(!event.entityPlayer.capabilities.isCreativeMode) {
                                    heldItem.stackSize--;
                                }
                            }
                        }
                    } else {
                        for (ItemStack itemStack : meltableWaterItems) {
                            if ((itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE && itemStack.getItem() == heldItem.getItem()) || itemStack.isItemEqual(heldItem)) {
                                if (((TileEntityCrucible) tileEntity).addItem(new ItemStack(Item.getItemFromBlock(Blocks.portal), 1))) {
                                    if (!event.entityPlayer.capabilities.isCreativeMode) {
                                        heldItem.stackSize--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public float getWoodenCrucibleSpeed() {
        return woodenCrucibleSpeed;
    }
}
