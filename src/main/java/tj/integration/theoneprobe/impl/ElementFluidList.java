package tj.integration.theoneprobe.impl;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.network.NetworkTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import tj.integration.theoneprobe.TheOneProbeCompatibility;
import tj.mui.TJGuiUtils;
import tj.util.wrappers.GTFluidStackWrapper;

import java.util.Collection;

public class ElementFluidList implements IElement {

    private final Object2ObjectMap<FluidStack, GTFluidStackWrapper> fluidMap = new Object2ObjectLinkedOpenHashMap<>();
    private Collection<FluidStack> fluidStacks;

    public ElementFluidList(Collection<FluidStack> fluidStacks) {
        this.fluidStacks = fluidStacks;
    }

    public ElementFluidList(ByteBuf byteBuf) {
        final int size = byteBuf.readInt();
        for (int i = 0; i < size; i++) {
            final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(NetworkTools.readNBT(byteBuf));
            if (fluidStack == null) continue;
            this.fluidMap.computeIfAbsent(fluidStack, key -> new GTFluidStackWrapper(key, 0))
                    .increment(fluidStack.amount);
        }
    }

    @Override
    public void render(int x, int y) {
        int offsetX = 0;
        for (Object2ObjectMap.Entry<FluidStack, GTFluidStackWrapper> entry : this.fluidMap.object2ObjectEntrySet())
            TJGuiUtils.drawFluidStack(x + (18 * offsetX++), y, entry.getKey(), entry.getValue().getCountLong());
    }

    @Override
    public int getWidth() {
        return 18 * this.fluidMap.size();
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.fluidStacks.size());
        for (FluidStack fluidStack : this.fluidStacks)
            NetworkTools.writeNBT(byteBuf, fluidStack.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public int getID() {
        return TheOneProbeCompatibility.ELEMENT_FLUIDLIST;
    }
}
