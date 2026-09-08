package tj.integration.hwyla.providers;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.LinkPos;
import tj.capability.TJCapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class LinkedPosInfoDataProvider extends CapabilityInfoDataProvider<LinkPos> {

    public static final LinkedPosInfoDataProvider INSTANCE = new LinkedPosInfoDataProvider();

    @Override
    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<LinkPos> getCapability() {
        return TJCapabilities.CAPABILITY_LINK_POS;
    }

    @Override
    public String getConfigName() {
        return "tj.linked_pos";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, LinkPos linkPos) {
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag(this.getConfigName());
        final NBTTagList linkPosList = compound.getTagList("linkPosList", 10);
        final boolean interdimensional = compound.getBoolean("interdimensional");
        tooltip.add("§b(" + 1 + "/" + linkPosList.tagCount() + ")");
        for (int i = 0; i < linkPosList.tagCount(); i++) {
            final NBTTagCompound posCompound = linkPosList.getCompoundTagAt(i);
            final int worldId = posCompound.getInteger("worldId");
            final World world = interdimensional ? DimensionManager.getWorld(worldId) : accessor.getWorld();
            final boolean posExists = posCompound.hasKey("x") && posCompound.hasKey("y") && posCompound.hasKey("z");
            final BlockPos pos = posExists ? new BlockPos(posCompound.getInteger("x"), posCompound.getInteger("y"), posCompound.getInteger("z")) :
                    null;
            if (pos == null) continue;
            final TileEntity tileEntity = world.getTileEntity(pos);
            final MetaTileEntity mte = tileEntity instanceof MetaTileEntityHolder ? ((MetaTileEntityHolder) tileEntity).getMetaTileEntity() : null;
            if (tileEntity == null) continue;
            final ResourceLocation location = tileEntity.getBlockType().getRegistryName();
            if (location == null) continue;
            final int meta = mte != null ? GregTechAPI.META_TILE_ENTITY_REGISTRY.getIdByObjectName(mte.metaTileEntityId) : tileEntity.getBlockMetadata();
            final ItemStack stack = mte != null ? mte.getStackForm() : new ItemStack(tileEntity.getBlockType(), 1, tileEntity.getBlockMetadata());
            tooltip.add(SpecialChars.getRenderString("waila.stack", "0", location.toString(), "1", String.valueOf(meta)) +
                    " " + stack.getDisplayName() + " §b[" + (i + 1) + "] ");
            tooltip.add(I18n.format("tj.machine.universal.linked.dimension",
                    world.provider.getDimensionType().getName(), TJValues.thousandFormat.format(worldId)));
            tooltip.add(I18n.format("tj.machine.universal.linked.pos", pos.getX(), pos.getY(), pos.getZ()));
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, LinkPos linkPos) {
        final NBTTagCompound compound = new NBTTagCompound();
        final NBTTagList linkPosList = new NBTTagList();
        for (int i = 0; i < linkPos.getPosSize(); i++) {
            final NBTTagCompound posCompound = new NBTTagCompound();
            final WorldServer worldServer = linkPos.isInterDimensional() ? DimensionManager.getWorld(linkPos.getDimension(i)) : (WorldServer) linkPos.world();
            final BlockPos blockPos = linkPos.getPos(i);
            posCompound.setInteger("worldId", worldServer.provider.getDimension());
            if (blockPos != null) {
                posCompound.setInteger("x", blockPos.getX());
                posCompound.setInteger("y", blockPos.getY());
                posCompound.setInteger("z", blockPos.getZ());
            }
            linkPosList.appendTag(posCompound);
        }
        compound.setTag("linkPosList", linkPosList);
        compound.setBoolean("interdimensional", linkPos.isInterDimensional());
        tag.setTag(this.getConfigName(), compound);
        return tag;
    }
}
