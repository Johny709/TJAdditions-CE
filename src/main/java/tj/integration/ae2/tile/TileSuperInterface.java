package tj.integration.ae2.tile;

import appeng.helpers.DualityInterface;
import appeng.tile.misc.TileInterface;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.gui.TJGuiTextures;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.integration.ae2.helpers.SuperDualityInterface;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;


public class TileSuperInterface extends TileInterface implements ITileEntityUI {

    public TileSuperInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new SuperDualityInterface(this.getProxy(), this), "duality");
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
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup();
        final DualityInterface duality = this.getInterfaceDuality();
        for (int i = 0; i < duality.getConfig().getSlots(); i++)
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
        for (int i = 0; i < duality.getStorage().getSlots(); i++)
            widgetGroup.addWidget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setBackgroundTexture(GuiTextures.SLOT));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(new ItemStack(this.getBlockType())).setLocale(String.format("tile.%s.name", this.getBlockType().getRegistryName().getPath())))
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
