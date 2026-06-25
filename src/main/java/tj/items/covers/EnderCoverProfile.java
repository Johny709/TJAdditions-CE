package tj.items.covers;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import tj.capability.IEnderNotifiable;

import java.util.*;

public class EnderCoverProfile<V> {

    private final UUID owner;
    /**
     * Permission Index:
     * 0 -> can see entries: true = 1 / false = 0
     * 1 -> can modify entries: true = 1 / false = 0
     * 2 -> can use entry: true = 1 / false = 0
     * 3 -> can see channels: true = 1 / false = 0
     * 4 -> can modify channels: true = 1 / false = 0
     * 5 -> can use channel: true = 1 / false = 0
     * 6 -> max throughput: 1 - max long
     */
    private final Object2ObjectMap<UUID, long[]> allowedUsers = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, Set<IEnderNotifiable<V>>> notifyMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, V> channels = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, EnderCoverProfile<V>> playerMap;
    private boolean isPublic = true;

    public EnderCoverProfile(UUID owner, Object2ObjectMap<String, V> channels, Object2ObjectMap<String, EnderCoverProfile<V>> playerMap) {
        this.owner = owner;
        this.channels.putAll(channels);
        this.allowedUsers.put(this.owner, new long[]{1, 1, 1, 1, 1, 1, Long.MAX_VALUE});
        this.playerMap = playerMap;
        for (String key : channels.keySet())
            this.notifyMap.put(key, new HashSet<>());
    }

    public static <V> EnderCoverProfile<V> fromNBT(NBTTagCompound nbt, Object2ObjectMap<String, V> entries, Object2ObjectMap<String, EnderCoverProfile<V>> playerMap) {
        final NBTTagCompound compound = nbt.getCompoundTag("coverProfile");
        final UUID uuid = compound.hasUniqueId("owner") ? compound.getUniqueId("owner") : null;
        final EnderCoverProfile<V> enderCoverProfile = new EnderCoverProfile<>(uuid, entries, playerMap);
        enderCoverProfile.readFromNBT(nbt);
        return enderCoverProfile;
    }

    public void addToNotifiable(String key, IEnderNotifiable<V> notifiable) {
        final Set<IEnderNotifiable<V>> set = this.notifyMap.get(key);
        if (set != null)
            set.add(notifiable);
    }

    public void removeFromNotifiable(String key, IEnderNotifiable<V> notifiable) {
        this.notifyMap.getOrDefault(key, new HashSet<>()).remove(notifiable);
    }

    public boolean hasPermission(UUID uuid, int permission) {
        return this.owner == null || this.allowedUsers.get(uuid) != null && this.allowedUsers.get(uuid)[permission] == 1;
    }

    public boolean removeChannel(String key, String id) {
        final UUID uuid = UUID.fromString(id);
        if (this.owner != null && (this.allowedUsers.get(uuid) == null || this.allowedUsers.get(uuid)[1] != 1))
            return false;
        final Set<IEnderNotifiable<V>> set = this.notifyMap.remove(key);
        this.channels.remove(key);
        for (IEnderNotifiable<V> notifiable : set) {
            notifiable.setChannel(null);
            notifiable.setHandler(null);
            notifiable.markToDirty();
        }
        return true;
    }

    public long maxThroughPut(UUID uuid) {
        return this.owner == null ? Long.MAX_VALUE : this.allowedUsers.get(uuid) != null ? this.getAllowedUsers().get(uuid)[6] : 1;
    }

    public boolean setChannel(String key, String lastEntry, String id, IEnderNotifiable<V> notifiable) {
        UUID uuid = UUID.fromString(id);
        if (this.owner != null && (this.allowedUsers.get(uuid) == null || this.allowedUsers.get(uuid)[2] != 1))
            return false;
        this.removeFromNotifiable(lastEntry, notifiable);
        this.addToNotifiable(key, notifiable);
        return true;
    }

    public boolean setChannel(String key, String uuid, IEnderNotifiable<V> notifiable) {
        if (this.setChannel(key, notifiable.getChannel(), uuid, notifiable)) {
            notifiable.setHandler(this.getChannels().get(key));
            notifiable.setChannel(key);
            return true;
        } else return false;
    }

    public void editChannel(String key, String id, V handler) {
        final UUID uuid = UUID.fromString(id);
        if (!this.channels.containsKey(key)) return;
        if (this.owner != null && (this.allowedUsers.get(uuid) == null || this.allowedUsers.get(uuid)[1] != 1)) return;
        for (IEnderNotifiable<V> cover : this.notifyMap.get(key))
            cover.setHandler(handler);
    }

    public void editChannel(String newKey, String id) {
        final int index = id.lastIndexOf(":");
        final String oldKey = id.substring(0, index);
        final UUID uuid = UUID.fromString(id.substring(index + 1));
        if (!this.channels.containsKey(oldKey)) return;
        if (this.owner != null && (this.allowedUsers.get(uuid) == null || this.allowedUsers.get(uuid)[1] != 1)) return;
        final Set<IEnderNotifiable<V>> set = this.notifyMap.remove(oldKey);
        this.channels.put(newKey, this.channels.remove(oldKey));
        this.notifyMap.put(newKey, set);
        for (IEnderNotifiable<V> notifiable : set) {
            notifiable.setChannel(newKey);
            notifiable.markToDirty();
        }
    }

    public void addChannel(String key, String id, IEnderNotifiable<V> notifiable) {
        final UUID uuid = UUID.fromString(id);
        if (key == null) return;
        if (this.owner != null && (this.allowedUsers.get(uuid) == null || this.allowedUsers.get(uuid)[1] != 1)) return;
        this.channels.putIfAbsent(key, notifiable.createHandler());
        this.notifyMap.putIfAbsent(key, new HashSet<>());
        notifiable.markToDirty();
    }

    public boolean editFrequency(String key, UUID uuid) {
        if (this.owner == null || (this.allowedUsers.get(uuid) == null || this.allowedUsers.get(uuid)[4] != 1))
            return false;
        for (Map.Entry<String, Set<IEnderNotifiable<V>>> entry : this.notifyMap.entrySet()) {
            for (IEnderNotifiable<V> notifiable : entry.getValue()) {
                notifiable.setFrequency(key);
                notifiable.markToDirty();
            }
        }
        return true;
    }

    public boolean removeFrequency(String id) {
        UUID uuid = UUID.fromString(id);
        if (this.owner == null || (this.allowedUsers.get(uuid) == null || this.allowedUsers.get(uuid)[4] != 1))
            return false;
        for (Map.Entry<String, Set<IEnderNotifiable<V>>> entry : this.notifyMap.entrySet()) {
            for (IEnderNotifiable<V> notifiable : entry.getValue()) {
                notifiable.setChannel(null);
                notifiable.setHandler(null);
                notifiable.setFrequency(null);
                notifiable.markToDirty();
            }
        }
        return true;
    }

    public boolean removeFrequency(String key, String uuid, IEnderNotifiable<V> notifiable) {
        if (this.playerMap.get(key).removeFrequency(uuid)) {
            this.playerMap.remove(key);
            notifiable.markToDirty();
            return true;
        } else return false;
    }

    public void addFrequency(String key, String uuid, IEnderNotifiable<V> notifiable) {
        if (this.getOwner() == null || this.getAllowedUsers().containsKey(UUID.fromString(uuid))) {
            this.playerMap.putIfAbsent(key, new EnderCoverProfile<>(UUID.fromString(uuid), new Object2ObjectOpenHashMap<>(), this.playerMap));
            notifiable.markToDirty();
        }
    }

    public boolean setFrequency(String key, String id, IEnderNotifiable<V> notifiable) {
        final EnderCoverProfile<V> profile = this.playerMap.getOrDefault(key, this.playerMap.get(null));
        final UUID uuid = UUID.fromString(id);
        if (!key.equals(notifiable.getFrequency()) && (profile.isPublic() || profile.getAllowedUsers().get(uuid) != null && profile.getAllowedUsers().get(uuid)[3] == 1)) {
            profile.addToNotifiable(notifiable.getChannel(), notifiable);
            this.removeFromNotifiable(notifiable.getChannel(), notifiable);
            notifiable.setFrequency(key);
            return true;
        } else return false;
    }

    public void renameFrequency(String key, String id, IEnderNotifiable<V> notifiable) {
        final int index = id.lastIndexOf(":");
        final String uuid = id.substring(index + 1);
        final String oldKey = id.substring(0, index);
        final EnderCoverProfile<V> profile = this.playerMap.get(oldKey);
        if (profile != null && this.editFrequency(key, UUID.fromString(uuid))) {
            this.playerMap.put(key, this.playerMap.remove(oldKey));
            notifiable.markToDirty();
        }
    }

    public boolean addUser(UUID uuid, UUID owner) {
        if (this.allowedUsers.get(owner) != null && this.allowedUsers.get(owner)[4] == 1 && !this.allowedUsers.containsKey(uuid)) {
            this.allowedUsers.put(uuid, new long[]{0, 0, 0, 0, 0, 0, 0});
            return true;
        } else return false;
    }

    public boolean removeUser(UUID uuid, UUID owner) {
        if (this.allowedUsers.get(owner) != null && this.allowedUsers.get(owner)[4] == 1) {
            this.allowedUsers.remove(uuid);
            return true;
        } else return false;
    }

    public void setPublic(boolean isPublic) {
        if (this.owner != null)
            this.isPublic = isPublic;
    }

    public void onClear(String uuid, IEnderNotifiable<V> notifiable) {
        this.editChannel(notifiable.getChannel(), uuid, notifiable.createHandler());
        notifiable.markToDirty();
    }

    public Map<String, V> getChannels() {
        return this.channels;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Map<UUID, long[]> getAllowedUsers() {
        return this.allowedUsers;
    }

    public void setPublic(boolean isPublic, String uuid) {
        if (this.getOwner() != null && this.getOwner().equals(UUID.fromString(uuid))) {
            this.setPublic(isPublic);
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagList userList = new NBTTagList();
        for (Map.Entry<UUID, long[]> entry : this.allowedUsers.entrySet()) {
            if (entry.getKey() != null) {
                NBTTagCompound compound1 = new NBTTagCompound();
                NBTTagList permissionList = new NBTTagList();
                for (long permission : entry.getValue()) {
                    permissionList.appendTag(new NBTTagLong(permission));
                }
                compound1.setUniqueId("user", entry.getKey());
                compound1.setTag("permissionList", permissionList);
                userList.appendTag(compound1);
            }
        }
        if (this.owner != null)
            compound.setUniqueId("owner", this.owner);
        compound.setBoolean("public", this.isPublic);
        compound.setTag("userList", userList);
        nbt.setTag("coverProfile", compound);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        final NBTTagCompound compound = nbt.getCompoundTag("coverProfile");
        final NBTTagList userList = compound.getTagList("userList", 10);
        for (int i = 0; i < userList.tagCount(); i++) {
            NBTTagList permissionList = userList.getCompoundTagAt(i).getTagList("permissionList", 4);
            long[] permissions = new long[permissionList.tagCount()];
            for (int j = 0; j < permissionList.tagCount(); j++) {
                permissions[j] = ((NBTTagLong) permissionList.get(j)).getLong();
            }
            this.allowedUsers.put(userList.getCompoundTagAt(i).getUniqueId("user"), permissions);
        }
        this.isPublic = compound.getBoolean("public");
    }
}
