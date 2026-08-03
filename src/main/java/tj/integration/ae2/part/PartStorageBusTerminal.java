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
import tj.TJ;
import tj.integration.ae2.ISuperInterfaceTerminal;
import tj.integration.ae2.items.ItemWirelessStorageBusTerminal;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEGhostItemListWidget;

import javax.annotation.Nonnull;
import java.util.function.LongUnaryOperator;


public class PartStorageBusTerminal extends AbstractPartDisplay implements ITileEntityUI, ISuperInterfaceTerminal {

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(TJ.MODID, "part/me.part.storage_bus_terminal_off");

    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(TJ.MODID, "part/me.part.storage_bus_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public PartStorageBusTerminal(ItemStack is) {
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
        return ItemWirelessStorageBusTerminal.createWirelessStorageBusTerminalGUI(holder, player, this.getGridNode(), this);
    }

    @Override
    public void setItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget, LongUnaryOperator unaryOperator) {
        final ItemStack itemStack = ghostItemListWidget.getItemAt(ghostItemListWidget.getSelectedIndex());
        if (itemStack.isEmpty()) return;
        final ItemStack newStack = itemStack.copy();
        newStack.setCount((int) Math.max(1, Math.min(Integer.MAX_VALUE, unaryOperator.applyAsLong(itemStack.getCount()))));
        ghostItemListWidget.setItemAt(ghostItemListWidget.getSelectedIndex(), newStack);
    }

    @Override
    public int getItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget) {
        return ghostItemListWidget.getItemAt(ghostItemListWidget.getSelectedIndex(), true).getCount();
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
