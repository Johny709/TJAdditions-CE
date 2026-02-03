package tj.integration.appeng.tile.misc;

import appeng.helpers.DualityInterface;
import appeng.tile.misc.TileInterface;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.gui.TJGuiTextures;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;
import tj.gui.widgets.TJSlotWidget;
import tj.gui.widgets.impl.TJPhantomSlotWidget;
import tj.integration.appeng.helpers.SuperDualityInterface;


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
        WidgetGroup widgetGroup = new WidgetGroup();
        DualityInterface duality = this.getInterfaceDuality();
        for (int i = 0; i < duality.getConfig().getSlots(); i++)
            widgetGroup.addWidget(new TJPhantomSlotWidget(duality.getConfig(), i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)))
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        for (int i = 0; i < duality.getStorage().getSlots(); i++)
            widgetGroup.addWidget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setBackgroundTexture(GuiTextures.SLOT));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 292)
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
