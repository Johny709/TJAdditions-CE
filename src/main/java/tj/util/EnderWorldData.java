package tj.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import tj.capability.impl.workable.BasicEnergyHandler;
import tj.items.covers.EnderCoverProfile;
import tj.items.handlers.LargeItemStackHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import java.util.Map;

public class EnderWorldData extends WorldSavedData {

    private static EnderWorldData INSTANCE = new EnderWorldData("dummyClient"); // don't return null and crash when being referenced in GUI creation.
    private final Object2ObjectMap<String, EnderCoverProfile<FluidTank>> fluidTankPlayerMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, EnderCoverProfile<LargeItemStackHandler>> itemChestPlayerMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, EnderCoverProfile<BasicEnergyHandler>> energyContainerPlayerMap = new Object2ObjectOpenHashMap<>();

    public EnderWorldData(String name) {
        super(name);
        this.fluidTankPlayerMap.putIfAbsent(null, new EnderCoverProfile<>(null, new Object2ObjectOpenHashMap<>(), this.getFluidTankPlayerMap()));
        this.itemChestPlayerMap.putIfAbsent(null, new EnderCoverProfile<>(null, new Object2ObjectOpenHashMap<>(), this.getItemChestPlayerMap()));
        this.energyContainerPlayerMap.putIfAbsent(null, new EnderCoverProfile<>(null, new Object2ObjectOpenHashMap<>(), this.getEnergyContainerPlayerMap()));
    }

    public static EnderWorldData getINSTANCE() {
        return INSTANCE;
    }

    public Object2ObjectMap<String, EnderCoverProfile<FluidTank>> getFluidTankPlayerMap() {
        return this.fluidTankPlayerMap;
    }

    public Object2ObjectMap<String, EnderCoverProfile<LargeItemStackHandler>> getItemChestPlayerMap() {
        return this.itemChestPlayerMap;
    }

    public Object2ObjectMap<String, EnderCoverProfile<BasicEnergyHandler>> getEnergyContainerPlayerMap() {
        return this.energyContainerPlayerMap;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        final NBTTagList fluidFrequencies = new NBTTagList();
        for (Map.Entry<String, EnderCoverProfile<FluidTank>> playerEntry : this.fluidTankPlayerMap.entrySet()) {
            final NBTTagCompound playerCompound = new NBTTagCompound();
            final NBTTagList fluidChannels = new NBTTagList();
            for (Map.Entry<String, FluidTank> entry : playerEntry.getValue().getChannels().entrySet()) {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.setString("key", entry.getKey());
                compound.setLong("capacity", entry.getValue().getCapacity());
                entry.getValue().writeToNBT(compound);
                fluidChannels.appendTag(compound);
            }
            if (playerEntry.getKey() != null)
                playerCompound.setString("id", playerEntry.getKey());
            playerCompound.setTag("fluidChannels", fluidChannels);
            playerEntry.getValue().writeToNBT(playerCompound);
            fluidFrequencies.appendTag(playerCompound);
        }
        final NBTTagList itemFrequencies = new NBTTagList();
        for (Map.Entry<String, EnderCoverProfile<LargeItemStackHandler>> playerEntry : this.itemChestPlayerMap.entrySet()) {
            final NBTTagCompound playerCompound = new NBTTagCompound();
            final NBTTagList itemChannels = new NBTTagList();
            for (Map.Entry<String, LargeItemStackHandler> entry : playerEntry.getValue().getChannels().entrySet()) {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.setString("key", entry.getKey());
                compound.setLong("capacity", entry.getValue().getCapacity());
                compound.setTag("stack", entry.getValue().serializeNBT());
                itemChannels.appendTag(compound);
            }
            if (playerEntry.getKey() != null)
                playerCompound.setString("id", playerEntry.getKey());
            playerCompound.setTag("itemChannels", itemChannels);
            playerEntry.getValue().writeToNBT(playerCompound);
            itemFrequencies.appendTag(playerCompound);
        }
        final NBTTagList energyFrequencies = new NBTTagList();
        for (Map.Entry<String, EnderCoverProfile<BasicEnergyHandler>> playerEntry : this.energyContainerPlayerMap.entrySet()) {
            final NBTTagCompound playerCompound = new NBTTagCompound();
            final NBTTagList energyChannels = new NBTTagList();
            for (Map.Entry<String, BasicEnergyHandler> entry : playerEntry.getValue().getChannels().entrySet()) {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.setString("key", entry.getKey());
                entry.getValue().writeToNBT(compound);
                energyChannels.appendTag(compound);
            }
            if (playerEntry.getKey() != null)
                playerCompound.setString("id", playerEntry.getKey());
            playerCompound.setTag("energyChannels", energyChannels);
            playerEntry.getValue().writeToNBT(playerCompound);
            energyFrequencies.appendTag(playerCompound);
        }
        nbt.setTag("fluidFrequencies", fluidFrequencies);
        nbt.setTag("itemFrequencies", itemFrequencies);
        nbt.setTag("energyFrequencies", energyFrequencies);
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        final NBTTagList fluidFrequencies = nbt.getTagList("fluidFrequencies", 10);
        for (int i = 0; i < fluidFrequencies.tagCount(); i++) {
            final NBTTagCompound playerCompound = fluidFrequencies.getCompoundTagAt(i);
            final NBTTagList fluidChannels = playerCompound.getTagList("fluidChannels", 10);
            final Object2ObjectMap<String, FluidTank> fluidTankMap = new Object2ObjectOpenHashMap<>();
            for (int j = 0; j < fluidChannels.tagCount(); j++) {
                final NBTTagCompound compound = fluidChannels.getCompoundTagAt(j);
                fluidTankMap.put(compound.getString("key"), new FluidTank(compound.getInteger("capacity")).readFromNBT(compound));
            }
            final String id = playerCompound.hasKey("id") ? playerCompound.getString("id") : null;
            this.fluidTankPlayerMap.put(id, EnderCoverProfile.fromNBT(playerCompound, fluidTankMap, this.getFluidTankPlayerMap()));
        }
        final NBTTagList itemFrequencies = nbt.getTagList("itemFrequencies", 10);
        for (int i = 0; i < itemFrequencies.tagCount(); i++) {
            final NBTTagCompound playerCompound = itemFrequencies.getCompoundTagAt(i);
            final NBTTagList itemChannels = playerCompound.getTagList("itemChannels", 10);
            final Object2ObjectMap<String, LargeItemStackHandler> itemChestMap = new Object2ObjectOpenHashMap<>();
            for (int j = 0; j < itemChannels.tagCount(); j++) {
                final NBTTagCompound compound = itemChannels.getCompoundTagAt(j);
                final LargeItemStackHandler itemStackHandler = new LargeItemStackHandler(1, compound.getInteger("capacity"));
                itemStackHandler.deserializeNBT(compound.getCompoundTag("stack"));
                itemChestMap.put(compound.getString("key"), itemStackHandler);
            }
            final String id = playerCompound.hasKey("id") ? playerCompound.getString("id") : null;
            this.itemChestPlayerMap.put(id, EnderCoverProfile.fromNBT(playerCompound, itemChestMap, this.getItemChestPlayerMap()));
        }
        final NBTTagList energyFrequencies = nbt.getTagList("energyFrequencies", 10);
        for (int i = 0; i < energyFrequencies.tagCount(); i++) {
            final NBTTagCompound playerCompound = energyFrequencies.getCompoundTagAt(i);
            final NBTTagList energyChannels = playerCompound.getTagList("energyChannels", 10);
            final Object2ObjectMap<String, BasicEnergyHandler> energyContainerMap = new Object2ObjectOpenHashMap<>();
            for (int j = 0; j < energyChannels.tagCount(); j++) {
                final NBTTagCompound compound = energyChannels.getCompoundTagAt(j);
                final BasicEnergyHandler energyHandler = new BasicEnergyHandler(0);
                energyHandler.readFromNBT(compound);
                energyContainerMap.put(compound.getString("key"), energyHandler);
            }
            final String id = playerCompound.hasKey("id") ? playerCompound.getString("id") : null;
            this.energyContainerPlayerMap.put(id, EnderCoverProfile.fromNBT(playerCompound, energyContainerMap, this.getEnergyContainerPlayerMap()));
        }
    }

    public void setInstance(EnderWorldData instance) {
        INSTANCE = instance;
    }
}
