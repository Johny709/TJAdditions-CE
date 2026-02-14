package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.IForgeRegistryEntry;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.List;

public class MinerWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private final Object2ObjectMap<String, ItemStack> itemType = new Object2ObjectOpenHashMap<>();
    private final BlockPos.MutableBlockPos miningPos = new BlockPos.MutableBlockPos();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final List<Chunk> chunks = new ArrayList<>();
    private boolean initialized;
    private Chunk currentChunk;
    private int outputIndex;
    private int chunkIndex;
    private int levelY;

    public MinerWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        this.initializeChunks();
        if (this.chunkIndex >= this.chunks.size())
            this.chunkIndex = 0;
        this.currentChunk = this.chunks.get(this.chunkIndex++);
        this.levelY = this.metaTileEntity.getPos().getY();
        this.setMaxProgress(this.levelY * 256);
        this.energyPerTick = 512;
        return true;
    }

    @Override
    protected void progressRecipe(int progress) {
        super.progressRecipe(progress);
        this.initializeChunks();
        if (this.currentChunk == null)
            this.currentChunk = this.chunks.get(this.chunkIndex);
        if (this.progress > progress) {
            progress = this.progress % 256;
            this.miningPos.setPos(this.currentChunk.x + (progress % 16), this.levelY, this.currentChunk.z + (progress / 16));
            IBlockState state = this.metaTileEntity.getWorld().getBlockState(this.miningPos);
            Block block = state.getBlock();
            if (state.getBlock() instanceof BlockDirt || state.getBlock() instanceof BlockGrass || state.getBlock() instanceof BlockStone || state.getBlock() instanceof BlockSand) {
                Item item = block.getItemDropped(state, this.metaTileEntity.getWorld().rand, 0);
                if (this.addItemDrop(item, 1, block.damageDropped(state)))
                    this.metaTileEntity.getWorld().destroyBlock(this.miningPos, false);
            }
            if (progress == 255)
                this.levelY--;
        }
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            if (ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.itemType.clear();
        this.itemOutputs.clear();
        return true;
    }

    private void initializeChunks() {
        if (!this.initialized) {
            this.initialized = true;
            boolean odd = this.handler.getTier() % 2 == 0;
            Chunk origin = this.metaTileEntity.getWorld().getChunk(this.metaTileEntity.getPos());
            int originX = origin.x;
            int originZ = origin.z;
            for (int x = -this.handler.getTier() + (odd ? 1 : 0) ; x < this.handler.getTier(); x++) {
                for (int z = -this.handler.getTier() + (odd ? 1 : 0); z < this.handler.getTier(); z++) {
                    this.chunks.add(this.metaTileEntity.getWorld().getChunk(originX + (x * 16), originZ + (z * 16)));
                }
            }
        }
    }

    private <T extends IForgeRegistryEntry<T>> boolean addItemDrop(T type, int count, int meta) {
        if (type == null)
            return false;
        String key = type.getRegistryName().toString() + ":" + meta;
        ItemStack stack = this.itemType.get(key);
        if (stack != null) {
            stack.grow(count);
        } else {
            ItemStack itemStack = type instanceof Block ? new ItemStack((Block) type, count, meta) : new ItemStack((Item) type, count, meta);
            this.itemType.put(key, itemStack);
            this.itemOutputs.add(itemStack);
        }
        return true;
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
        compound.setTag("itemOutputList", itemOutputList);
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
}
