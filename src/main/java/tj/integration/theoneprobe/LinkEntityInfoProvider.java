package tj.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.LinkEntity;
import tj.capability.TJCapabilities;

public class LinkEntityInfoProvider extends CapabilityInfoProvider<LinkEntity> {

    @Override
    protected Capability<LinkEntity> getCapability() {
        return TJCapabilities.CAPABILITY_LINK_ENTITY;
    }

    @Override
    protected void addProbeInfo(LinkEntity capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        int pageIndex = capability.getPageIndex();
        int pageSize = capability.getPageSize();
        int size = capability.getPosSize();

        IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.text(TextStyleClass.INFO + "§b(" +(pageIndex + 1) + "/" + size + ")");

        for (int i = pageIndex; i < pageIndex + pageSize && i < size; i++) {
            WorldServer world = capability.isInterDimensional() ? DimensionManager.getWorld(capability.getDimension(i)) : (WorldServer) capability.world();
            DimensionType worldType = world.provider.getDimensionType();
            int worldID = world.provider.getDimension();
            Entity entity = capability.getEntity(i);
            if (entity != null) {

                IProbeInfo nameInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                nameInfo.text(TextStyleClass.INFO + "§b[" + (i + 1) + "]§r ");

                IProbeInfo entityInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                entityInfo.text(TextStyleClass.INFO + (entity.hasCustomName() ? entity.getCustomNameTag() : entity.getName()));

                IProbeInfo posInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                posInfo.text(TextStyleClass.INFO + I18n.translateToLocalFormatted("machine.universal.linked.dimension", worldType.getName(), worldID));
                posInfo.text(TextStyleClass.INFO + I18n.translateToLocalFormatted("machine.universal.linked.pos", (int) entity.posX, (int) entity.posY, (int) entity.posZ));
            }
        }
    }

    @Override
    public String getID() {
        return "tj:linked_entity_provider";
    }
}
