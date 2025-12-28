package tj.integration.theoneprobe;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.LinkPos;
import tj.capability.TJCapabilities;

public class LinkedPosInfoProvider extends CapabilityInfoProvider<LinkPos> {

    @Override
    protected Capability<LinkPos> getCapability() {
        return TJCapabilities.CAPABILITY_LINK_POS;
    }

    @Override
    protected void addProbeInfo(LinkPos capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        int pageIndex = capability.getPageIndex();
        int pageSize = capability.getPageSize();
        int size = capability.getPosSize();

        IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.text(TextStyleClass.INFO + "§b(" +(pageIndex + 1) + "/" + size + ")");

        for (int i = pageIndex; i < pageIndex + pageSize && i < size; i++) {
            WorldServer world = capability.isInterDimensional() ? DimensionManager.getWorld(capability.getDimension(i)) : (WorldServer) capability.world();
            DimensionType worldType = world.provider.getDimensionType();
            int worldID = world.provider.getDimension();
            BlockPos pos = capability.getPos(i);
            if (pos != null) {
                TileEntity entity = world.getTileEntity(pos);
                MetaTileEntity gregEntity = BlockMachine.getMetaTileEntity(world, pos);

                IProbeInfo nameInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                nameInfo.text(TextStyleClass.INFO + "§b[" + (i + 1) + "]§r ");

                if (entity != null || gregEntity != null) {
                    nameInfo.item(gregEntity != null ? gregEntity.getStackForm() : new ItemStack(entity.getBlockType()));
                    nameInfo.text(TextStyleClass.INFO + (gregEntity != null ? " {*" + gregEntity.getMetaFullName() + "*}" : "{*" + entity.getBlockType().getTranslationKey() + ".name*}"));

                    IProbeInfo posInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                    int x = gregEntity != null ? gregEntity.getPos().getX() : entity.getPos().getX();
                    int y = gregEntity != null ? gregEntity.getPos().getY() : entity.getPos().getY();
                    int z = gregEntity != null ? gregEntity.getPos().getZ() : entity.getPos().getZ();
                    posInfo.text(TextStyleClass.INFO + I18n.translateToLocalFormatted("machine.universal.linked.dimension", worldType.getName(), worldID));
                    posInfo.text(TextStyleClass.INFO + I18n.translateToLocalFormatted("machine.universal.linked.pos", x, y, z));
                }
            }
        }
    }

    @Override
    public String getID() {
        return "tj:linked_pos_provider";
    }
}
