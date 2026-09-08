package tj.integration.hwyla.providers;

import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.LinkEntity;
import tj.capability.TJCapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class LinkedEntityInfoDataProvider extends CapabilityInfoDataProvider<LinkEntity> {

    public static final LinkedEntityInfoDataProvider INSTANCE = new LinkedEntityInfoDataProvider();

    @Override
    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<LinkEntity> getCapability() {
        return TJCapabilities.CAPABILITY_LINK_ENTITY;
    }

    @Override
    public String getConfigName() {
        return "tj.linked_entity";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, LinkEntity linkEntity) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(this.getConfigName());
        final NBTTagList linkPosList = compound.getTagList("linkPosList", 10);
        final boolean interdimensional = compound.getBoolean("interdimensional");
        tooltip.add("§b(" + 1 + "/" + linkPosList.tagCount() + ")");
        for (int i = 0; i < linkPosList.tagCount(); i++) {
            final NBTTagCompound posCompound = linkPosList.getCompoundTagAt(i);
            final int worldId = posCompound.getInteger("worldId");
            final World world = interdimensional ? DimensionManager.getWorld(worldId) : accessor.getWorld();
            final int entityId = posCompound.getInteger("entityId");
            final Entity entity = posCompound.hasKey("entityId") ? world.getEntityByID(entityId) : null;
            if (entity == null) continue;
            tooltip.add((entity.hasCustomName() ? entity.getCustomNameTag() :  entity.getName()) + " §b[" + (i + 1) + "] ");
            tooltip.add(I18n.format("tj.machine.universal.linked.dimension",
                    world.provider.getDimensionType().getName(), TJValues.thousandFormat.format(worldId)));
            tooltip.add(I18n.format("tj.machine.universal.linked.pos",
                    TJValues.thousandFormat.format(entity.posX), TJValues.thousandFormat.format(entity.posY), TJValues.thousandFormat.format(entity.posZ)));
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, LinkEntity linkEntity) {
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagList linkPosList = new NBTTagList();
        for (int i = 0; i < linkEntity.getPosSize(); i++) {
            final NBTTagCompound posCompound = new NBTTagCompound();
            final WorldServer worldServer = linkEntity.isInterDimensional() ? DimensionManager.getWorld(linkEntity.getDimension(i)) :
                    (WorldServer) linkEntity.world();
            final Entity entity = linkEntity.getEntity(i);
            posCompound.setInteger("worldId", worldServer.provider.getDimension());
            if (entity != null)
                posCompound.setInteger("entityId", entity.getEntityId());
            linkPosList.appendTag(posCompound);
        }
        compound.setTag("linkPosList", linkPosList);
        compound.setBoolean("interdimensional", linkEntity.isInterDimensional());
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }
}
