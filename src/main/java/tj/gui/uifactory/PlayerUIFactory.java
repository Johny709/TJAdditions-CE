package tj.gui.uifactory;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJ;

public class PlayerUIFactory extends UIFactory<PlayerHolder> {

    public static final PlayerUIFactory INSTANCE = new PlayerUIFactory();

    public void init() {
        UIFactory.FACTORY_REGISTRY.register(3, new ResourceLocation(TJ.MODID, "player_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(PlayerHolder holder, EntityPlayer player) {
        return holder.createUI(player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected PlayerHolder readHolderFromSyncData(PacketBuffer syncData) {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
        BlockPos pos = syncData.readBlockPos();
        int type = syncData.readVarInt();
        Object holder = null;
        if (type == 0) {
            holder = BlockMachine.getMetaTileEntity(entityPlayer.world, pos);

        } else if (type == 1) {
            EnumFacing attachedSide = EnumFacing.VALUES[syncData.readByte()];
            TileEntity tileEntity = Minecraft.getMinecraft().world.getTileEntity(pos);
            ICoverable coverable = tileEntity == null ? null : tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            holder = coverable == null ? null : coverable.getCoverAtSide(attachedSide);
        }
        return new PlayerHolder(entityPlayer, holder);
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer syncData, PlayerHolder holder) {
        if (holder.holder instanceof MetaTileEntity) {
            BlockPos pos = ((MetaTileEntity) holder.holder).getPos();
            syncData.writeBlockPos(pos);
            syncData.writeVarInt(0);

        } else if (holder.holder instanceof CoverBehavior) {
            CoverBehavior cover = (CoverBehavior) holder.holder;
            syncData.writeBlockPos(cover.coverHolder.getPos());
            syncData.writeVarInt(1);
            syncData.writeByte(cover.attachedSide.ordinal());
        }
    }
}
