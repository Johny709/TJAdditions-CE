package tj.integration.ae2.part;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractPartDisplay;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;
import tj.TJ;
import tj.integration.ae2.ISuperFluidInterfaceTerminal;
import tj.integration.ae2.items.ItemWirelessSuperFluidInterfaceTerminal;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEGhostFluidListWidget;

import javax.annotation.Nonnull;
import java.util.function.LongUnaryOperator;

public class PartSuperFluidInterfaceTerminal extends AbstractPartDisplay implements ITileEntityUI, ISuperFluidInterfaceTerminal {

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_terminal_off");

    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public PartSuperFluidInterfaceTerminal(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null && !player.getEntityWorld().isRemote) {
            TileEntityHolder holder = new TileEntityHolder(tileCableBus);
            holder.setFacing(this.getSide().getFacing());
            holder.openUI((EntityPlayerMP) player);
        }
        return true;
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return ItemWirelessSuperFluidInterfaceTerminal.createSuperFluidInterfaceTerminalGUI(holder, player, this.getGridNode(), this);
    }

    @Override
    public void setFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget, LongUnaryOperator unaryOperator) {
        final FluidStack fluidStack = ghostFluidListWidget.getFluidAt(ghostFluidListWidget.getSelectedIndex());
        if (fluidStack == null) return;
        final FluidStack newStack = fluidStack.copy();
        newStack.amount = (int) Math.max(1, Math.min(Integer.MAX_VALUE, unaryOperator.applyAsLong(fluidStack.amount)));
        ghostFluidListWidget.setFluidAt(ghostFluidListWidget.getSelectedIndex(), newStack);
    }

    @Override
    public int getFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget) {
        final FluidStack fluidStack = ghostFluidListWidget.getFluidAt(ghostFluidListWidget.getSelectedIndex());
        return fluidStack != null ? fluidStack.amount : 0;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
