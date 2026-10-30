package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.AbstractWorkableHandler;
import tj.capability.impl.handler.IPosHandler;
import tj.util.TJItemUtils;
import tj.util.map.Strategies;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static tj.machines.singleblock.MetaTileEntitySuperFisher.STRING;

public class FisherWorkableHandler extends AbstractWorkableHandler<IPosHandler> {

    private final Object2ObjectMap<ItemStack, ItemStack> itemMapSorter = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY_NBT);
    private final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private boolean foundRecipe;
    private boolean voiding;
    private int outputIndex;

    public FisherWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            this.foundRecipe = this.findAndConsumeInputs(itemInputs, true) > 0;
            if (!this.foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                this.foundRecipe = this.findAndConsumeInputs(itemInputs, true) > 0;
                if (this.foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            this.foundRecipe = this.findAndConsumeInputs(itemInputs, true) > 0;
        }
        if (this.foundRecipe && this.handler.getInputEnergyContainer().getEnergyStored() >= GAValues.V[this.handler.getTier()] && this.checkWaterAround()) {
            this.handler.getInputEnergyContainer().removeEnergy(GAValues.V[this.handler.getTier()]);
            final int amount = this.findAndConsumeInputs(itemInputs, false);
            final World world = this.metaTileEntity.getWorld();
            for (int i = 0; i < amount; i++) {
                final List<ItemStack> lootOutput = world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING)
                        .generateLootForPools(world.rand, new LootContext.Builder((WorldServer) world).build());
                for (ItemStack stack : lootOutput) {
                    this.itemMapSorter.computeIfAbsent(stack, k -> k)
                            .grow(stack.getCount());
                }
            }
            this.itemOutputs.addAll(this.itemMapSorter.keySet());
            this.setMaxProgress(this.calculateOverclock(30, 800, 2));
            this.energyPerTick = 0;
            return true;
        } else return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            final ItemStack stack = this.itemOutputs.get(i);
            if (this.voiding || TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.foundRecipe = false;
        this.outputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.itemMapSorter.clear();
        return true;
    }

    private int findAndConsumeInputs(IItemHandlerModifiable itemInputs, boolean simulate) {
        int amount = 0;
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < itemInputs.getSlots() && availableParallels > 0; i++) {
            if (!itemInputs.getStackInSlot(i).isItemEqual(STRING)) continue;
            final ItemStack stack = itemInputs.extractItem(i, availableParallels, simulate).copy();
            if (stack.isEmpty()) continue;
            if (!simulate)
                this.itemInputs.add(stack);
            amount += stack.getCount();
            availableParallels -= stack.getCount();
        }
        return amount;
    }

    private boolean checkWaterAround() {
        final BlockPos pos = this.handler.getBlockPos();
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                this.blockPos.setPos(pos.getX() + x, pos.getY(), pos.getZ() + z);
                if (this.metaTileEntity.getWorld().getBlockState(this.blockPos) != Blocks.WATER.getDefaultState())
                    return false;
            }
        }
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList itemInputList = new NBTTagList(), itemOutputList = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            itemInputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            itemOutputList.appendTag(stack.serializeNBT());
        compound.setTag("itemInputs", itemInputList);
        compound.setTag("itemOutputs", itemOutputList);
        compound.setInteger("outputIndex", this.outputIndex);
        compound.setBoolean("voiding", this.voiding);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        final NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputList = compound.getTagList("itemOutputs", 10);
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputList.getCompoundTagAt(i)));
        this.outputIndex = compound.getInteger("outputIndex");
        this.voiding = compound.getBoolean("voiding");
    }

    @Override
    protected boolean otherConditions() {
        return this.foundRecipe;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInputs;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }

    public void setVoiding(boolean voiding) {
        this.voiding = voiding;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoiding() {
        return this.voiding;
    }
}
