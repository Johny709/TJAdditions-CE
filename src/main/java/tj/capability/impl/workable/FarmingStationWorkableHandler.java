package tj.capability.impl.workable;

import gregtech.api.GTValues;
import gregtech.api.items.IToolItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.blocks.wood.BlockGregLog;
import gregtech.common.items.MetaItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistryEntry;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;
import tj.capability.impl.handler.IFarmerHandler;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.List;


public class FarmingStationWorkableHandler extends AbstractWorkableHandler<IFarmerHandler> implements IItemFluidHandlerInfo {

    private static ItemStack RUBBER_REFERENCE;
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final BlockPos[] posCorner = new BlockPos[4];
    private final Object2ObjectMap<Item, ItemStack> itemType = new Object2ObjectOpenHashMap<>();
    private Harvester[] harvesters;
    private boolean initialized;
    private boolean outputTools;
    private boolean voidOutputs;
    private int fertilizerChance;
    private int outputIndex;
    private int range;
    private int radius;

    public FarmingStationWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int range) {
        super.initialize(range);
        this.range = range;
        this.radius = (int) Math.sqrt(range);
        this.fertilizerChance = this.handler.getTier() * 10;
        int amount = this.handler.getTier() >= GTValues.ZPM ? 4 : this.handler.getTier() >= GTValues.EV ? 2 : 1;
        this.harvesters = new Harvester[amount];
        for (int i = 0; i < this.harvesters.length; i++) {
            this.harvesters[i] = new Harvester();
        }
    }

    @Override
    protected boolean startRecipe() {
        this.maxProgress = this.range;
        this.energyPerTick = (long) (this.range / 4) * this.harvesters.length;
        return this.handler.getInputEnergyContainer().getEnergyStored() >= this.energyPerTick;
    }

    @Override
    protected void progressRecipe(int progress) {
        super.progressRecipe(progress);
        if (!this.initialized) {
            this.initialized = true;
            BlockPos pos = this.metaTileEntity.getPos();
            this.posCorner[0] = new BlockPos(pos.getX() - (this.radius / 2), pos.getY(), pos.getZ() - (this.radius / 2));
            if (this.harvesters.length > 1)
                this.posCorner[1] = new BlockPos(pos.getX() + (this.radius / 2), pos.getY(), pos.getZ() + (this.radius / 2));
            if (this.harvesters.length > 2)
                this.posCorner[2] = new BlockPos(pos.getX() + (this.radius / 2), pos.getY(), pos.getZ() - (this.radius / 2));
            if (this.harvesters.length > 3)
                this.posCorner[3] = new BlockPos(pos.getX() - (this.radius / 2), pos.getY(), pos.getZ() + (this.radius / 2));
            if (RUBBER_REFERENCE == null)
                RUBBER_REFERENCE = MetaItems.RUBBER_DROP.getStackForm();
        }
        if (this.progress > progress) {
            this.harvesters[0].onProgress(this.posCorner[0].getX() + (progress % this.radius), this.posCorner[0].getY(), this.posCorner[0].getZ() + (progress / this.radius));
            if (this.harvesters.length > 1)
                this.harvesters[1].onProgress(this.posCorner[1].getX() - (progress % this.radius), this.posCorner[1].getY(), this.posCorner[1].getZ() - (progress / this.radius));
            if (this.harvesters.length > 2)
                this.harvesters[2].onProgress(this.posCorner[2].getX() - (progress / this.radius), this.posCorner[2].getY(), this.posCorner[2].getZ() + (progress % this.radius));
            if (this.harvesters.length > 3)
                this.harvesters[3].onProgress(this.posCorner[3].getX() + (progress / this.radius), this.posCorner[3].getY(), this.posCorner[3].getZ() - (progress % this.radius));
        }
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = 0; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block instanceof IPlantable || stack.getItem() instanceof IPlantable) {
                this.itemOutputs.set(i, ItemStackHelper.insertIntoItemHandler(this.handler.getImportItemInventory(), stack, false));
            }
        }
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            if (this.voidOutputs || ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), this.itemOutputs.get(i), true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), this.itemOutputs.get(i), false);
                this.outputIndex++;
            } else return false;
        }
        this.outputIndex = 0;
        this.itemType.clear();
        this.itemOutputs.clear();
        return true;
    }

    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInputs;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        NBTTagList inputList = new NBTTagList(), outputList = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            inputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            outputList.appendTag(stack.serializeNBT());
        data.setTag("inputList", inputList);
        data.setTag("outputList", outputList);
        data.setInteger("outputIndex", this.outputIndex);
        data.setBoolean("outputTools", this.outputTools);
        data.setBoolean("voidOutputs", this.voidOutputs);
        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound data) {
        super.deserializeNBT(data);
        NBTTagList inputList = data.getTagList("inputList", 10), outputList = data.getTagList("outputList", 10);
        for (int i = 0; i < inputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(inputList.getCompoundTagAt(i)));
        for (int i = 0; i < outputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(outputList.getCompoundTagAt(i)));
        for (ItemStack stack : this.itemOutputs)
            this.itemType.put(stack.getItem(), stack);
        this.outputIndex = data.getInteger("outputIndex");
        this.outputTools = data.getBoolean("outputTools");
        this.voidOutputs = data.getBoolean("voidOutputs");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    public void setOutputTools(boolean outputTools) {
        this.outputTools = outputTools;
        this.metaTileEntity.markDirty();
    }

    public void setVoidOutputs(boolean voidOutputs) {
        this.voidOutputs = voidOutputs;
        this.metaTileEntity.markDirty();
    }

    public boolean isOutputTools() {
        return this.outputTools;
    }

    public boolean isVoidOutputs() {
        return this.voidOutputs;
    }

    private class Harvester {

        private final BlockPos.MutableBlockPos posHarvester = new BlockPos.MutableBlockPos();

        private void onProgress(int x, int y, int z) {
            this.posHarvester.setPos(x, y, z);
            World world = metaTileEntity.getWorld();
            IBlockState state = world.getBlockState(this.posHarvester);
            Block block = state.getBlock();
            if (block instanceof IGrowable && fertilizerChance >= Math.random() * 100) {
                for (int i = 0; i < handler.getFertilizerInventory().getSlots(); i++) {
                    ItemStack stack = handler.getFertilizerInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        ((IGrowable) block).grow(world, world.rand, this.posHarvester, state);
                        stack.shrink(1);
                        break;
                    }
                }
            }
            if (block == Blocks.AIR) {
                IBlockState itemState = null;
                ItemStack stack = ItemStack.EMPTY;
                for (int i = 0; i < handler.getImportItemInventory().getSlots(); i++) {
                    stack = handler.getImportItemInventory().getStackInSlot(i);
                    Block itemBlock = Block.getBlockFromItem(stack.getItem());
                    if (itemBlock instanceof IGrowable) {
                        if (!this.canPlantSaplings(this.posHarvester, world))
                            return;
                        itemState = itemBlock.getStateFromMeta(stack.getMetadata());
                        break;
                    } else if (stack.getItem() instanceof IPlantable) {
                        IPlantable plantable = ((IPlantable) stack.getItem());
                        if (!this.canPlantSeeds(this.posHarvester, plantable))
                            return;
                        itemState = plantable.getPlant(world, this.posHarvester);
                        break;
                    }
                }
                if (itemState != null) {
                    ItemStack hoeStack = handler.getToolInventory().getStackInSlot(0);
                    if (!hoeStack.isEmpty()) {
                        hoeStack.damageItem(1, FakePlayerFactory.getMinecraft((WorldServer) world));
                        world.setBlockState(this.posHarvester, itemState);
                        stack.shrink(1);
                    }
                }
            } else this.harvestBlock(this.posHarvester);
            this.posHarvester.setPos(this.posHarvester.getX(), this.posHarvester.getY() - 1, this.posHarvester.getZ());
            state = world.getBlockState(this.posHarvester);
            block = state.getBlock();
            if (block instanceof BlockFarmland && state.getValue(BlockFarmland.MOISTURE) < 7) {
                FluidStack drain = handler.getImportFluidTank().drain(100, false);
                if (drain != null && drain.amount == 100) {
                    handler.getImportFluidTank().drain(100, true);
                    world.setBlockState(this.posHarvester, Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, 7));
                }
            }
        }

        private boolean canPlantSaplings(BlockPos.MutableBlockPos pos, World world) {
            pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
            IBlockState state = metaTileEntity.getWorld().getBlockState(pos);
            Block block = state.getBlock();
            pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
            ItemStack hoeStack;
            if ((block instanceof BlockDirt || block instanceof BlockGrass) && !(hoeStack = handler.getToolInventory().getStackInSlot(0)).isEmpty()) {
                return this.damageTool(hoeStack, (WorldServer) world);
            }
            return false;
        }

        private boolean canPlantSeeds(BlockPos.MutableBlockPos pos, IPlantable plantable) {
            pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
            World world = metaTileEntity.getWorld();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            ItemStack hoeStack;
            if ((block instanceof BlockDirt || block instanceof BlockGrass) && !(hoeStack = handler.getToolInventory().getStackInSlot(0)).isEmpty()) {
                this.damageTool(hoeStack, (WorldServer) world);
                world.setBlockState(pos, Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, 7));
                state = world.getBlockState(pos);
            }
            boolean canPlantSeed = state.getBlock().canSustainPlant(state, world, pos, EnumFacing.UP, plantable);
            pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
            return canPlantSeed;
        }

        /**
         * Try to harvest a block. if successful, try to harvest the same kind of blocks in a 3x3 area. do the same again but above the block.
         * @param pos BlockPos
         */
        private void harvestBlock(BlockPos.MutableBlockPos pos) {
            ItemStack toolStack;
            World world = metaTileEntity.getWorld();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            boolean harvestable = false;
            double chance = 100;
            int count = 1;
            if (block instanceof BlockGregLog && !(toolStack = handler.getToolInventory().getStackInSlot(1)).isEmpty()) {
                harvestable = this.damageTool(toolStack, (WorldServer) world);
                if (harvestable && state.getValue(BlockGregLog.NATURAL))
                    harvestable = this.addItemDrop(RUBBER_REFERENCE.getItem(), 1 + world.rand.nextInt(2), RUBBER_REFERENCE.getMetadata());
            } else if (block instanceof BlockLog && !(toolStack = handler.getToolInventory().getStackInSlot(1)).isEmpty()) {
                harvestable = this.damageTool(toolStack, (WorldServer) world);
            } else if (block instanceof IShearable) {
                if (!(toolStack = handler.getToolInventory().getStackInSlot(2)).isEmpty() && this.damageTool(toolStack, (WorldServer) world)) {
                    harvestable = this.addItemDrop(block, 1, block.damageDropped(state));
                    chance = 0;
                } else if (!(toolStack = handler.getToolInventory().getStackInSlot(1)).isEmpty() && this.damageTool(toolStack, (WorldServer) world)) {
                    harvestable = true;
                    chance = 5;
                } else return;
            } else if (block instanceof BlockCrops) {
                BlockCrops crops = (BlockCrops)block;
                if (crops.isMaxAge(state) && !(toolStack = handler.getToolInventory().getStackInSlot(0)).isEmpty()) {
                    toolStack.damageItem(1, FakePlayerFactory.getMinecraft((WorldServer) world));
                    int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, toolStack);
                    IBlockState state1 = crops.withAge(0);
                    count += world.rand.nextInt(3 + fortune);
                    harvestable = this.addItemDrop(crops.getItemDropped(state1, world.rand, 0), 1 + world.rand.nextInt(2 + fortune), crops.getMetaFromState(state1));
                }
            }
            if (harvestable) {
                if (chance >= Math.random() * 100) {
                    Item item = block.getItemDropped(state, world.rand, 0);
                    this.addItemDrop(item, count, block.damageDropped(state));
                }
                world.destroyBlock(pos, false);
                if (!this.inRange(pos))
                    return;
                BlockPos.MutableBlockPos harvester = new BlockPos.MutableBlockPos(pos);
                for (int x = -1; x < 2; x++) {
                    for (int y = 0; y < 2; y++) {
                        for (int z = -1; z < 2; z++) {
                            harvester.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                            this.harvestBlock(harvester);
                        }
                    }
                }
            }
        }

        private boolean inRange(BlockPos.MutableBlockPos pos) {
            BlockPos mtePos = metaTileEntity.getPos();
            return pos.getX() > mtePos.getX() - radius && pos.getX() < mtePos.getX() + radius && pos.getZ() > mtePos.getZ() - radius && pos.getZ() < mtePos.getZ() + radius;
        }

        private <T extends IForgeRegistryEntry<T>> boolean addItemDrop(T type, int count, int meta) {
            if (type == null)
                return false;
            Item item = type instanceof Item ? (Item) type : Item.getItemFromBlock((Block) type);
            ItemStack stack = itemType.get(item);
            if (stack != null) {
                stack.grow(count);
            } else {
                ItemStack itemStack = type instanceof Block ? new ItemStack((Block) type, count, meta) : new ItemStack((Item) type, count, meta);
                itemType.put(item, itemStack);
                itemOutputs.add(itemStack);
            }
            return true;
        }

        private boolean damageTool(ItemStack stack, WorldServer world) {
            if (stack.getItem() instanceof IToolItem) {
                return ((IToolItem) stack.getItem()).damageItem(stack, 1, false) || (outputTools && this.outputTool(stack));
            } else if (stack.getItem().getMaxDamage() > 0) {
                stack.damageItem(1, FakePlayerFactory.getMinecraft(world));
                return true;
            }
            IEnergyStorage rf = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (rf != null) {
                return rf.extractEnergy(128, false) == 128 || (outputTools && this.outputTool(stack));
            }
            return outputTools && this.outputTool(stack);
        }

        private boolean outputTool(ItemStack stack) {
            ItemStack exportStack = stack.copy();
            if (ItemStackHelper.insertIntoItemHandler(handler.getExportItemInventory(), exportStack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(handler.getExportItemInventory(), exportStack, false);
                stack.shrink(1);
                return true;
            } else return false;
        }
    }
}
