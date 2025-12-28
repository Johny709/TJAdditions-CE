package tj.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PlayerWorldIDData extends WorldSavedData {

    private final Object2IntOpenHashMap<UUID> playerId = new Object2IntOpenHashMap<>();
    private static PlayerWorldIDData INSTANCE;

    public PlayerWorldIDData(String name) {
        super(name);
    }

    public static PlayerWorldIDData getINSTANCE() {
        return INSTANCE;
    }

    public Object2IntMap<UUID> getPlayerWorldIdMap() {
        return this.playerId;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList playerWorldIDList = new NBTTagList();
        for (Object2IntMap.Entry<UUID> player : this.playerId.object2IntEntrySet()) {
            NBTTagCompound playerCompound = new NBTTagCompound();
            playerCompound.setUniqueId("uuid", player.getKey());
            playerCompound.setInteger("worldId", player.getIntValue());
            playerWorldIDList.appendTag(playerCompound);
        }

        nbt.setTag("playerList", playerWorldIDList);
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList playerWorldIDList = nbt.getTagList("playerList", Constants.NBT.TAG_COMPOUND);
        for (NBTBase compound : playerWorldIDList) {
            NBTTagCompound tag = (NBTTagCompound) compound;
            UUID uuid = tag.getUniqueId("uuid");
            int worldID = tag.getInteger("worldId");
            this.playerId.put(uuid, worldID);
        }
    }

    public void setInstance(PlayerWorldIDData instance) {
        INSTANCE = instance;
    }

}
