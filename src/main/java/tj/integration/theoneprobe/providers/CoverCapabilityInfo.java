package tj.integration.theoneprobe.providers;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public abstract class CoverCapabilityInfo<T> extends CapabilityInfoProvider<T> {

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        if (!blockState.getBlock().hasTileEntity(blockState)) return;
        final EnumFacing sideHit = data.getSideHit();
        final TileEntity tileEntity = world.getTileEntity(data.getPos());
        if (!(tileEntity instanceof MetaTileEntityHolder)) return;
        final Capability<T> capability = getCapability();
        final MetaTileEntityHolder holder = (MetaTileEntityHolder) tileEntity;
        final T resultCapability = holder.getMetaTileEntity().getCoverCapability(capability, sideHit);
        if (resultCapability != null && allowDisplaying(resultCapability)) {
            this.addProbeInfo(resultCapability, probeInfo, tileEntity, sideHit);
        }
    }
}
