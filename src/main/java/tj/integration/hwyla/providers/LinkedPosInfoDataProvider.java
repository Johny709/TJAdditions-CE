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
import tj.TJValues;
import tj.capability.LinkPos;
import tj.capability.TJCapabilities;

import javax.annotation.Nonnull;
import java.util.List;

public class LinkedPosInfoDataProvider implements IWailaDataProvider {

    public static final LinkedPosInfoDataProvider INSTANCE = new LinkedPosInfoDataProvider();

    public void register(IWailaRegistrar registrar) {
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.addConfig("TJ", "tj.linked_pos");
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (!(te instanceof MetaTileEntityHolder))
            return tag;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) te).getMetaTileEntity();
        if (metaTileEntity == null)
            return tag;
        final LinkPos linkPos = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_LINK_POS, null);
        if (linkPos == null)
            return tag;
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
        tag.setTag("tj.linked_pos", compound);
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig("tj.linked_pos"))
            return tooltip;
        if (!(accessor.getTileEntity() instanceof MetaTileEntityHolder))
            return tooltip;
        final MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) accessor.getTileEntity()).getMetaTileEntity();
        if (metaTileEntity == null)
            return tooltip;
        final LinkPos linkPos = metaTileEntity.getCapability(TJCapabilities.CAPABILITY_LINK_POS, null);
        if (linkPos == null)
            return tooltip;
        final NBTTagCompound compound = accessor.getNBTData().getCompoundTag("tj.linked_pos");
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
}
