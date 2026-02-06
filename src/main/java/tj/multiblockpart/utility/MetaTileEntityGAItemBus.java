package tj.multiblockpart.utility;

import gregicadditions.client.ClientHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.util.Position;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityItemBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.impl.SlotScrollableWidgetGroup;
import tj.gui.widgets.TJLabelWidget;

public class MetaTileEntityGAItemBus extends MetaTileEntityItemBus {

    private final boolean isOutput;
    private ICubeRenderer hatchTexture = null;

    public MetaTileEntityGAItemBus(ResourceLocation metaTileEntityId, int tier, boolean isOutput) {
        super(metaTileEntityId, tier, isOutput);
        this.isOutput = isOutput;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityGAItemBus(this.metaTileEntityId, this.getTier(), this.isOutput);
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        int startX = Math.max(7, 79 - (9 * (this.getTier() - 1)));
        IItemHandlerModifiable itemHandler = this.isOutput ? this.getExportItems() : this.getImportItems();
        WidgetGroup slotGroup = new WidgetGroup(new Position(0, 7));
        SlotScrollableWidgetGroup slotScrollGroup = new SlotScrollableWidgetGroup(0, 7, 193, 180, 10)
                .setScrollWidth(5);
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            SlotWidget slotWidget = new SlotWidget(itemHandler, i, startX + (18 * (i % Math.min(10, this.getTier() + 1))), 18 * (i / Math.min(10, this.getTier() + 1)))
                    .setBackgroundTexture(GuiTextures.SLOT);
            if (this.getTier() > 9)
                slotScrollGroup.addWidget(slotWidget);
            else slotGroup.addWidget(slotWidget);
        }
        return ModularUI.builder(GuiTextures.BACKGROUND, 196, 137 + Math.min(144, 18 * (this.getTier() - 1)))
                .widget(new TJLabelWidget(7, -18, 178, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(this.getTier() > 9 ? slotScrollGroup : slotGroup)
                .bindPlayerInventory(player.inventory, 55 + Math.min(144, 18 * (this.getTier() - 1)))
                .build(this.getHolder(), player);
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            this.hatchTexture = controller.getBaseTexture(this);
        }
        if (controller == null && this.hatchTexture != null) {
            return this.hatchTexture;
        }
        if (controller == null) {
            this.setPaintingColor(DEFAULT_PAINTING_COLOR);
            return ClientHandler.VOLTAGE_CASINGS[getTier()];
        }
        this.setPaintingColor(0xFFFFFF);
        return controller.getBaseTexture(this);
    }
}
