package tj.integration.theoneprobe.providers;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.LinkEntity;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementTJText;


public class LinkEntityInfoProvider extends CapabilityInfoProvider<LinkEntity> {

    @Override
    protected Capability<LinkEntity> getCapability() {
        return TJCapabilities.CAPABILITY_LINK_ENTITY;
    }

    @Override
    protected void addProbeInfo(LinkEntity capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final int pageIndex = capability.getPageIndex();
        final int pageSize = capability.getPageSize();
        final int size = capability.getPosSize();

        final IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.text(TextStyleClass.INFO + "§b(" +(pageIndex + 1) + "/" + size + ")");

        for (int i = pageIndex; i < pageIndex + pageSize && i < size; i++) {
            final WorldServer world = capability.isInterDimensional() ? DimensionManager.getWorld(capability.getDimension(i)) : (WorldServer) capability.world();
            final DimensionType worldType = world.provider.getDimensionType();
            final int worldID = world.provider.getDimension();
            final Entity entity = capability.getEntity(i);
            if (entity != null) {

                final IProbeInfo nameInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                nameInfo.text(TextStyleClass.INFO + "§b[" + (i + 1) + "]§r ");

                final IProbeInfo entityInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                entityInfo.text(TextStyleClass.INFO + (entity.hasCustomName() ? entity.getCustomNameTag() : entity.getName()));

                final IProbeInfo posInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                posInfo.element(new ElementTJText(TextStyleClass.INFO + String.format("{*tj.machine.universal.linked.dimension[*%s;%s*]*}", worldType.getName(), TJValues.thousandFormat.format(worldID))));
                posInfo.element(new ElementTJText(TextStyleClass.INFO + String.format("{*tj.machine.universal.linked.pos[*%s;%s;%s*]*}", TJValues.thousandFormat.format(entity.posX), TJValues.thousandFormat.format(entity.posY), TJValues.thousandFormat.format(entity.posZ))));
            }
        }
    }

    @Override
    public String getID() {
        return "tj:linked_entity_provider";
    }
}
