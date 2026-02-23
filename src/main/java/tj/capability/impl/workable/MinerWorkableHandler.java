package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.IForgeRegistryEntry;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.impl.handler.IMinerHandler;
import tj.util.ItemStackHelper;
import tj.util.pair.IntPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinerWorkableHandler extends AbstractWorkableHandler<IMinerHandler> implements IItemFluidHandlerInfo {

    private final Object2ObjectMap<String, IntPair<ItemStack>> itemType = new Object2ObjectOpenHashMap<>();
    private final BlockPos.MutableBlockPos miningPos = new BlockPos.MutableBlockPos();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final List<Chunk> chunks = new ArrayList<>();
    private boolean initialized;
    private boolean blacklist = true;
    private boolean silkTouch;
    private Chunk currentChunk;
    private int miningSpeed;
    private int outputIndex;
    private int chunkIndex;
    private int levelY;

    public MinerWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int busCount) {
        super.initialize(busCount);
        this.miningSpeed = 1 << this.handler.getTier() - 1;
        this.energyPerTick = GAValues.VA[this.handler.getTier()];
    }

    @Override
    protected boolean startRecipe() {
        this.initializeChunks();
        if (this.chunkIndex >= this.chunks.size())
            this.chunkIndex = 0;
        this.currentChunk = this.chunks.get(this.chunkIndex);
        this.levelY = this.metaTileEntity.getPos().offset(EnumFacing.DOWN).getY();
        this.setMaxProgress(this.levelY * 256);
        return this.handler.getInputEnergyContainer().getEnergyStored() >= this.energyPerTick;
    }

    @Override
    protected void progressRecipe(int progress) {
        super.progressRecipe(progress);
        this.initializeChunks();
        if (this.currentChunk == null) {
            if (this.chunkIndex >= this.chunks.size())
                this.chunkIndex = 0;
            this.currentChunk = this.chunks.get(this.chunkIndex);
        }
        if (this.progress > progress && this.progress < this.maxProgress) {
            if (!this.handler.getDrillingFluid().isFluidStackIdentical(this.handler.getImportFluidTank().drain(this.handler.getDrillingFluid(), true))) {
                this.progress--;
                return;
            }
            int progressed = -1;
            for (int i = 0; i < this.miningSpeed; i++) {
                progress = (this.progress + i) % 256;
                this.miningPos.setPos(this.currentChunk.x + (progress % 16), this.levelY, this.currentChunk.z + (progress / 16));
                IBlockState state = this.metaTileEntity.getWorld().getBlockState(this.miningPos);
                Block block = state.getBlock();
                if (block != Blocks.AIR) {
                    if (this.silkTouch ? this.addItemDrop(block, 1, block.getMetaFromState(state)) : this.addItemDrop(block.getItemDropped(state, this.metaTileEntity.getWorld().rand, this.handler.getFortuneLvl()), 1, block.damageDropped(state))) {
                        this.metaTileEntity.getWorld().playEvent(2001, this.miningPos, Block.getStateId(state));
                        this.metaTileEntity.getWorld().setBlockState(this.miningPos, Blocks.AIR.getDefaultState());
                    }
                }
                if (this.progress + ++progressed == this.maxProgress) break;
                if (progress == 255) this.levelY--;
            }
            this.progress += progressed;
        }
    }

    @Override
    protected boolean completeRecipe() {
        IItemHandlerModifiable itemHandlerModifiable = this.handler.getExportItemInventory();
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            if (ItemStackHelper.insertIntoItemHandler(itemHandlerModifiable, stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(itemHandlerModifiable, stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.chunkIndex++;
        this.outputIndex = 0;
        this.itemType.clear();
        this.itemOutputs.clear();
        return true;
    }

    private void initializeChunks() {
        if (!this.initialized) {
            this.initialized = true;
            int reminder = this.handler.getDiameter() % 2;
            int start = this.handler.getDiameter() / 2;
            int end = start + reminder;
            Chunk origin = this.metaTileEntity.getWorld().getChunk(this.metaTileEntity.getPos());
            int originX = origin.x * 16;
            int originZ = origin.z * 16;
            for (int i = -start; i < end; i++) {
                for (int j = -start; j < end; j++) {
                    this.chunks.add(this.metaTileEntity.getWorld().getChunk(originX + (16 * (j % this.handler.getDiameter())), originZ + (16 * (j / this.handler.getDiameter())) + (i * 16)));
                }
            }
        }
    }

    private <T extends IForgeRegistryEntry<T>> boolean addItemDrop(T type, int count, int meta) {
        if (type == null)
            return false;
        String key = type.getRegistryName().toString() + ":" + meta;
        IntPair<ItemStack> stackPair = this.itemType.get(key);
        if (stackPair != null) {
            if (this.blacklist == (this.handler.getOreDictionaryItemFIlter().matchItemStack(stackPair.getValue()) != null))
                return false;
            if (!this.silkTouch && this.handler.getFortuneLvl() < 1 && OreDictUnifier.getPrefix(stackPair.getValue()) == OrePrefix.crushed)
                count = this.getFortune(stackPair.getKey());
            stackPair.getValue().grow(count);
        } else {
            ItemStack itemStack = type instanceof Block ? new ItemStack((Block) type, count, meta) : new ItemStack((Item) type, count, meta);
            if (this.blacklist == (this.handler.getOreDictionaryItemFIlter().matchItemStack(itemStack) != null))
                return false;
            if (!this.silkTouch && this.handler.getFortuneLvl() > 1) {
                Recipe recipe = RecipeMaps.FORGE_HAMMER_RECIPES.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList(), 0);
                if (recipe != null) {
                    itemStack = recipe.getResultItemOutputs(Integer.MAX_VALUE, this.metaTileEntity.getWorld().rand, 0).get(0).copy();
                    if (OreDictUnifier.getPrefix(itemStack) == OrePrefix.crushed) {
                        itemStack.setCount(this.getFortune(itemStack.getCount()));
                    }
                }
            }
            this.itemType.put(key, IntPair.of(itemStack.getCount(), itemStack));
            this.itemOutputs.add(itemStack);
        }
        return true;
    }

    private int getFortune(int count) {
        int fortuneLevel = this.handler.getFortuneLvl();
        if (fortuneLevel > 3) fortuneLevel = 3;
        int i = (this.metaTileEntity.getWorld().rand.nextFloat() <= (fortuneLevel / 3.0) ? 2 : 1);
        count *= i;
        if (count == 0) count = 1;
        return count;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        NBTTagList itemOutputList = new NBTTagList();
        for (ItemStack stack : this.itemOutputs)
            itemOutputList.appendTag(stack.serializeNBT());
        compound.setInteger("outputIndex", this.outputIndex);
        compound.setInteger("chunkIndex", this.chunkIndex);
        compound.setInteger("levelY", this.levelY);
        compound.setInteger("x", this.miningPos.getX());
        compound.setInteger("y", this.miningPos.getY());
        compound.setInteger("z", this.miningPos.getZ());
        compound.setBoolean("blacklist", this.blacklist);
        compound.setBoolean("silkTouch", this.silkTouch);
        compound.setTag("itemOutputList", itemOutputList);
        this.handler.getOreDictionaryItemFIlter().writeToNBT(compound);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        NBTTagList itemOutputList = compound.getTagList("itemOutputList", 10);
        for (int i = 0; i < itemOutputList.tagCount(); i++) {
            this.itemOutputs.add(new ItemStack(itemOutputList.getCompoundTagAt(i)));
        }
        this.outputIndex = compound.getInteger("outputIndex");
        this.chunkIndex = compound.getInteger("chunkIndex");
        this.levelY = compound.getInteger("levelY");
        this.miningPos.setPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
        this.blacklist = compound.getBoolean("blacklist");
        this.silkTouch = compound.getBoolean("silkTouch");
        this.handler.getOreDictionaryItemFIlter().readFromNBT(compound);
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }

    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
        this.metaTileEntity.markDirty();
    }

    public boolean isBlacklist() {
        return this.blacklist;
    }

    public void setSilkTouch(boolean silkTouch) {
        this.silkTouch = silkTouch;
        this.metaTileEntity.markDirty();
    }

    public boolean isSilkTouch() {
        return this.silkTouch;
    }

    public int getChunkIndex() {
        return this.chunkIndex;
    }

    public int getChunkSize() {
        return this.chunks.size();
    }

    public int getX() {
        return this.miningPos.getX();
    }

    public int getY() {
        return this.miningPos.getY();
    }

    public int getZ() {
        return this.miningPos.getZ();
    }

    public int getMiningSpeed() {
        return this.miningSpeed;
    }
}
