package tj.integration.hwyla.providers;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.List;

public class FluidHandlerInfoDataProvider extends CapabilityInfoDataProvider<IFluidHandler> {

    public static final FluidHandlerInfoDataProvider INSTANCE = new FluidHandlerInfoDataProvider();

    @Override
    public void register(IWailaRegistrar registrar) {
        super.register(registrar);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.registerBodyProvider(this, TileEntity.class);
    }

    @Override
    public Capability<IFluidHandler> getCapability() {
        return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public String getConfigName() {
        return "tj.fluidhandlerinfo";
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config, IFluidHandler capability) {
        final NBTTagList tanklist = accessor.getNBTData().getTagList(this.getConfigName(), 9);
        for (int i = 0; i < tanklist.tagCount(); i++) {
            final NBTTagCompound compound = tanklist.getCompoundTagAt(i);
            final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluid"));
            final String name = fluidStack != null ? fluidStack.getLocalizedName() : "";
            final int capacity = compound.getInteger("capacity");
            final int amount = Math.min(capacity, fluidStack != null ? fluidStack.amount : 0);
            tooltip.add(SpecialChars.getRenderString("tj.progressinfo", name, String.valueOf(amount), String.valueOf(capacity),
                    " L", " L", "§b", ",###"));
        }
        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos, IFluidHandler capability) {
        final NBTTagList tankList = new NBTTagList();
        final IFluidTankProperties[] tankProperties = capability.getTankProperties();
        for (IFluidTankProperties tankProperty : tankProperties) {
            final NBTTagCompound compound = new NBTTagCompound();
            final FluidStack fluidStack = tankProperty.getContents();
            if (fluidStack != null)
                compound.setTag("fluid", fluidStack.writeToNBT(new NBTTagCompound()));
            compound.setInteger("capacity", tankProperty.getCapacity());
            tankList.appendTag(compound);
        }
        tag.setTag(this.getConfigName(), tankList);
        return tag;
    }
}
