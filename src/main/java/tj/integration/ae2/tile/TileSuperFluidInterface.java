package tj.integration.ae2.tile;

import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.tile.TileFluidInterface;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.blocks.block.TJBlocks;
import tj.gui.TJGuiTextures;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.AEFluidTankWidget;
import tj.gui.widgets.impl.TJPhantomAEFluidSlotWidget;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;

public class TileSuperFluidInterface extends TileFluidInterface implements ITileEntityUI {

    public TileSuperFluidInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileFluidInterface.class, this, new DualitySuperFluidInterface(this.getProxy(), this), "duality");
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity) {
        this.openUI(player, tileEntity, null);
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity, EnumFacing facing) {
        TileEntityHolder holder = new TileEntityHolder(tileEntity);
        holder.setFacing(facing);
        holder.openUI((EntityPlayerMP) player);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJBlocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup();
        final DualityFluidInterface duality = this.getDualityFluidInterface();
        for (int i = 0; i < duality.getConfig().getSlots(); i++)
            widgetGroup.addWidget(new TJPhantomAEFluidSlotWidget(7 + (18 * (i % 9)), 34 + (72 * (i / 9)), 18, 18, i, duality.getConfig(), null)
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        for (int i = 0; i < duality.getTanks().getSlots(); i++)
            widgetGroup.addWidget(new AEFluidTankWidget(duality.getTanks(), i,7 + (18 * (i % 9)), 52 + (72 * (i / 9)), 18, 54)
                    .setBackgroundTexture(GuiTextures.SLOT));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(this.getItemStackRepresentation().getDisplayName()))
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
