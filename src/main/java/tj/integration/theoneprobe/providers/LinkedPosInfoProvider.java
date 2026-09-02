package tj.integration.theoneprobe.providers;

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
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.LinkPos;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementTJText;


public class LinkedPosInfoProvider extends CapabilityInfoProvider<LinkPos> {

    @Override
    protected Capability<LinkPos> getCapability() {
        return TJCapabilities.CAPABILITY_LINK_POS;
    }

    @Override
    protected void addProbeInfo(LinkPos capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final int pageIndex = capability.getPageIndex();
        final int pageSize = capability.getPageSize();
        final int size = capability.getPosSize();

        final IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.text(TextStyleClass.INFO + "§b(" +(pageIndex + 1) + "/" + size + ")");

        for (int i = pageIndex; i < pageIndex + pageSize && i < size; i++) {
            final WorldServer world = capability.isInterDimensional() ? DimensionManager.getWorld(capability.getDimension(i)) : (WorldServer) capability.world();
            final DimensionType worldType = world.provider.getDimensionType();
            final int worldID = world.provider.getDimension();
            final BlockPos pos = capability.getPos(i);
            if (pos != null) {
                final TileEntity entity = world.getTileEntity(pos);
                final MetaTileEntity gregEntity = BlockMachine.getMetaTileEntity(world, pos);

                final IProbeInfo nameInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                nameInfo.text(TextStyleClass.INFO + "§b[" + (i + 1) + "]§r ");

                if (entity != null || gregEntity != null) {
                    nameInfo.item(gregEntity != null ? gregEntity.getStackForm() : new ItemStack(entity.getBlockType()));
                    nameInfo.text(TextStyleClass.INFO + (gregEntity != null ? " {*" + gregEntity.getMetaFullName() + "*}" : "{*" + entity.getBlockType().getTranslationKey() + ".name*}"));

                    final IProbeInfo posInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                    final int x = gregEntity != null ? gregEntity.getPos().getX() : entity.getPos().getX();
                    final int y = gregEntity != null ? gregEntity.getPos().getY() : entity.getPos().getY();
                    final int z = gregEntity != null ? gregEntity.getPos().getZ() : entity.getPos().getZ();
                    posInfo.element(new ElementTJText(String.format("{*machine.universal.linked.dimension[*%s;%s*]*}", worldType.getName(), TJValues.thousandFormat.format(worldID))));
                    posInfo.element(new ElementTJText(String.format("{*machine.universal.linked.pos[*%s;%s;%s*]*}", TJValues.thousandFormat.format(x), TJValues.thousandFormat.format(y), TJValues.thousandFormat.format(z))));
                }
            }
        }
    }

    @Override
    public String getID() {
        return "tj:linked_pos_provider";
    }
}
