package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IMachineHandler;
import tj.util.Counter;
import tj.util.TJItemUtils;
import tj.util.map.Strategies;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FisherWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private final Object2ObjectMap<ItemStack, Counter> itemMapSorter = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY_NBT);
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private int outputIndex;

    public FisherWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        if (this.handler.getInputEnergyContainer().getEnergyStored() >= GAValues.V[this.handler.getTier()]) {
            this.handler.getInputEnergyContainer().removeEnergy(GAValues.V[this.handler.getTier()]);
            final World world = this.metaTileEntity.getWorld();
            for (int i = 0; i < this.handler.getParallel(); i++) {
                final List<ItemStack> lootOutput = world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING)
                        .generateLootForPools(world.rand, new LootContext.Builder((WorldServer) world).build());
                for (ItemStack stack : lootOutput) {
                    this.itemMapSorter.computeIfAbsent(stack, k -> new Counter(0))
                            .increment(stack.getCount());
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
            if (TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.outputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.itemMapSorter.clear();
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
}
