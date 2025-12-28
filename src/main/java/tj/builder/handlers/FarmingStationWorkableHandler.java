package tj.builder.handlers;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.IForgeRegistryEntry;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.impl.AbstractWorkableHandler;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class FarmingStationWorkableHandler extends AbstractWorkableHandler<FarmingStationWorkableHandler> implements IItemFluidHandlerInfo {

    private static ItemStack RUBBER_REFERENCE;
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final BlockPos[] posCorner = new BlockPos[4];
    private final Object2ObjectMap<String, ItemStack> itemType = new Object2ObjectOpenHashMap<>();
    private Supplier<IItemHandlerModifiable> toolInventory;
    private Supplier<IItemHandlerModifiable> fertilizerInventory;
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

    public FarmingStationWorkableHandler setToolInventory(Supplier<IItemHandlerModifiable> toolInventory) {
        this.toolInventory = toolInventory;
        return this;
    }

    public FarmingStationWorkableHandler setFertilizerInventory(Supplier<IItemHandlerModifiable> fertilizerInventory) {
        this.fertilizerInventory = fertilizerInventory;
        return this;
    }

    @Override
    public FarmingStationWorkableHandler initialize(int range) {
        super.initialize(range);
        this.range = range;
        this.radius = (int) Math.sqrt(range);
        this.fertilizerChance = this.tierSupplier.getAsInt() * 10;
        int amount = this.tierSupplier.getAsInt() >= GTValues.ZPM ? 4 : this.tierSupplier.getAsInt() >= GTValues.EV ? 2 : 1;
        this.harvesters = new Harvester[amount];
        for (int i = 0; i < this.harvesters.length; i++) {
            this.harvesters[i] = new Harvester();
        }
        return this;
    }

    @Override
    protected boolean startRecipe() {
        this.maxProgress = this.range;
        this.energyPerTick = (long) (this.range / 4) * this.harvesters.length;
        return true;
    }

    @Override
    protected void progressRecipe(int progress) {
        super.progressRecipe(progress);
        if (this.progress > progress) {
            progress--;
            if (RUBBER_REFERENCE == null)
                RUBBER_REFERENCE = MetaItems.RUBBER_DROP.getStackForm();
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
            }
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
        IItemHandlerModifiable importItems = this.importItemsSupplier.get();
        IItemHandlerModifiable exportItems = this.exportItemsSupplier.get();
        for (int i = 0; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block instanceof IPlantable || stack.getItem() instanceof IPlantable) {
                this.itemOutputs.set(i, ItemStackHelper.insertIntoItemHandler(importItems, stack, false));
            }
        }
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            if (this.voidOutputs || ItemStackHelper.insertIntoItemHandler(exportItems, this.itemOutputs.get(i), true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(exportItems, this.itemOutputs.get(i), false);
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
                IItemHandlerModifiable fertilizer = fertilizerInventory.get();
                for (int i = 0; i < fertilizer.getSlots(); i++) {
                    ItemStack stack = fertilizer.getStackInSlot(i);
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
                IItemHandlerModifiable input = importItemsSupplier.get();
                for (int i = 0; i < input.getSlots(); i++) {
                    stack = input.getStackInSlot(i);
                    Block itemBlock = Block.getBlockFromItem(stack.getItem());
                    if (itemBlock instanceof IGrowable) {
                        if (!this.canPlantSaplings(this.posHarvester))
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
                    ItemStack hoeStack = toolInventory.get().getStackInSlot(0);
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
                IFluidHandler tank = importFluidsSupplier.get();
                FluidStack drain = tank.drain(100, false);
                if (drain != null && drain.amount == 100) {
                    tank.drain(100, true);
                    world.setBlockState(this.posHarvester, Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, 7));
                }
            }
        }

        private boolean canPlantSaplings(BlockPos.MutableBlockPos pos) {
            pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
            IBlockState state = metaTileEntity.getWorld().getBlockState(pos);
            Block block = state.getBlock();
            pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
            return block instanceof BlockDirt || block instanceof BlockGrass;
        }

        private boolean canPlantSeeds(BlockPos.MutableBlockPos pos, IPlantable plantable) {
            pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ());
            World world = metaTileEntity.getWorld();
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            ItemStack toolStack;
            if ((block instanceof BlockDirt || block instanceof BlockGrass) && !(toolStack = toolInventory.get().getStackInSlot(0)).isEmpty()) {
                toolStack.damageItem(1, FakePlayerFactory.getMinecraft((WorldServer) world));
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
            if (block instanceof BlockGregLog && !(toolStack = toolInventory.get().getStackInSlot(1)).isEmpty()) {
                harvestable = this.damageTool(toolStack, (WorldServer) world);
                if (harvestable && state.getValue(BlockGregLog.NATURAL))
                    this.addItemDrop(RUBBER_REFERENCE.getItem(), 1 + world.rand.nextInt(2), RUBBER_REFERENCE.getMetadata());
            } else if (block instanceof BlockLog && !(toolStack = toolInventory.get().getStackInSlot(1)).isEmpty()) {
                harvestable = this.damageTool(toolStack, (WorldServer) world);
            } else if (block instanceof IShearable) {
                IItemHandlerModifiable tool = toolInventory.get();
                if (!(toolStack = tool.getStackInSlot(2)).isEmpty() && this.damageTool(toolStack, (WorldServer) world)) {
                    this.addItemDrop(block, 1, block.damageDropped(state));
                    harvestable = true;
                    chance = 0;
                } else if (!(toolStack = tool.getStackInSlot(1)).isEmpty() && this.damageTool(toolStack, (WorldServer) world)) {
                    harvestable = true;
                    chance = 5;
                } else return;
            } else if (block instanceof BlockCrops) {
                BlockCrops crops = (BlockCrops)block;
                if (crops.isMaxAge(state) && !(toolStack = toolInventory.get().getStackInSlot(0)).isEmpty()) {
                    toolStack.damageItem(1, FakePlayerFactory.getMinecraft((WorldServer) world));
                    int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, toolStack);
                    IBlockState state1 = crops.withAge(0);
                    this.addItemDrop(crops.getItemDropped(state1, world.rand, 0), 1 + world.rand.nextInt(2 + fortune), crops.getMetaFromState(state1));
                    count += world.rand.nextInt(3 + fortune);
                    harvestable = true;
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

        private <T extends IForgeRegistryEntry<T>> void addItemDrop(T type, int count, int meta) {
            String key = type.getRegistryName().toString() + ":" + meta;
            ItemStack stack = itemType.get(key);
            if (stack != null) {
                stack.grow(count);
            } else {
                ItemStack itemStack = type instanceof Block ? new ItemStack((Block) type, count, meta) : new ItemStack((Item) type, count, meta);
                itemType.put(key, itemStack);
                itemOutputs.add(itemStack);
            }
        }

        private boolean damageTool(ItemStack stack, WorldServer world) {
            if (stack.getItem().getMaxDamage() > 0) {
                stack.damageItem(1, FakePlayerFactory.getMinecraft(world));
                return true;
            }
            IElectricItem eu = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (eu != null) {
                boolean success = eu.discharge(32, Integer.MAX_VALUE, true, false, false) == 32;
                if ((!success || eu.getCharge() == 0) && outputTools && this.outputTool(stack.copy())) {
                    stack.shrink(1);
                }
                return success;
            }
            IEnergyStorage rf = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (rf != null) {
                boolean success = rf.extractEnergy(128, false) == 128;
                if (!success && outputTools && this.outputTool(stack.copy())) {
                    stack.shrink(1);
                }
                return success;
            }
            return false;
        }

        private boolean outputTool(ItemStack stack) {
            IItemHandlerModifiable output = exportItemsSupplier.get();
            if (ItemStackHelper.insertIntoItemHandler(output, stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(output, stack, false);
                return true;
            } else return false;
        }
    }
}
