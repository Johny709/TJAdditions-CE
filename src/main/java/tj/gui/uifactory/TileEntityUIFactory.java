package tj.gui.uifactory;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.util.AEPartLocation;
import appeng.parts.AEBasePart;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
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

public class TileEntityUIFactory extends UIFactory<TileEntityHolder> {

    public static final TileEntityUIFactory INSTANCE = new TileEntityUIFactory();

    private TileEntityUIFactory() {}

    public void init() {
        UIFactory.FACTORY_REGISTRY.register(4, new ResourceLocation(TJ.MODID, "tile_entity_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(TileEntityHolder tileEntityHolder, EntityPlayer entityPlayer) {
        return tileEntityHolder.createUI(entityPlayer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TileEntityHolder readHolderFromSyncData(PacketBuffer packetBuffer) {
        BlockPos pos = packetBuffer.readBlockPos();
        byte ordinal = packetBuffer.readByte();
        net.minecraft.tileentity.TileEntity tileEntity = Minecraft.getMinecraft().world.getTileEntity(pos);
        EnumFacing facing = ordinal != 100 ? EnumFacing.values()[ordinal] : null;
        if (facing != null) {
            IPart part = AEApi.instance().partHelper().getPart(Minecraft.getMinecraft().world, pos, AEPartLocation.fromFacing(facing));
            if (part instanceof AEBasePart) {
                tileEntity = ((AEBasePart) part).getTile();
            }
        }
        TileEntityHolder holder = new TileEntityHolder(tileEntity);
        holder.setFacing(facing);
        return holder;
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer packetBuffer, TileEntityHolder tileEntityHolder) {
        packetBuffer.writeBlockPos(tileEntityHolder.getTileEntity().getPos());
        packetBuffer.writeByte(tileEntityHolder.getFacing() != null ? tileEntityHolder.getFacing().ordinal() : 100);
    }
}
