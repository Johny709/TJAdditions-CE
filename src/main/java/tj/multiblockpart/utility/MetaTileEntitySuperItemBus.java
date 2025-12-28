package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.impl.GhostCircuitWidget;
import tj.items.handlers.LargeItemStackHandler;
import gregicadditions.GAValues;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.EXPORT_ITEMS;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.IMPORT_ITEMS;

public class MetaTileEntitySuperItemBus extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final IItemHandlerModifiable ghostCircuitSlot = new ItemStackHandler(1);
    private final boolean isExport;
    private int ticks = 5;

    public MetaTileEntitySuperItemBus(ResourceLocation metaTileEntityId, int tier, boolean isExport) {
        super(metaTileEntityId, tier);
        this.isExport = isExport;
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntitySuperItemBus(this.metaTileEntityId, this.getTier(), this.isExport);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        int slots = Math.min(100,(this.getTier() + 1) * (this.getTier() + 1));
        int size = this.getTier() < GAValues.UMV ? 1024
                : this.getTier() < GAValues.MAX ? 16384
                : Integer.MAX_VALUE;

        tooltip.add(I18n.format("machine.universal.stack", size));
        tooltip.add(I18n.format("machine.universal.slots", slots));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        int slots = Math.min(100,(this.getTier() + 1) * (this.getTier() + 1));
        int size = this.getTier() < GAValues.UMV ? 1024
                : this.getTier() < GAValues.MAX ? 16384
                : Integer.MAX_VALUE;

        return this.isExport ? super.createImportItemHandler()
                : new LargeItemStackHandler(slots, size);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        int slots = Math.min(100,(this.getTier() + 1) * (this.getTier() + 1));
        int size = this.getTier() < GAValues.UMV ? 1024
                : this.getTier() < GAValues.MAX ? 16384
                : Integer.MAX_VALUE;

        return isExport ? new LargeItemStackHandler(slots, size)
                : super.createExportItemHandler();
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote && this.getOffsetTimer() % this.ticks == 0) {
            if (this.isExport) {
                this.pushItemsIntoNearbyHandlers(getFrontFacing());
            } else {
                this.pullItemsFromNearbyHandlers(getFrontFacing());
            }
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        IItemHandlerModifiable bus = this.isExport ? this.exportItems : this.importItems;
        int tier = Math.min(3, this.getTier() / 3);
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new ImageWidget(169, 72 * tier, 18, 18, GuiTextures.DISPLAY));
        widgetGroup.addWidget(new AdvancedTextWidget(170, 5 + 72 * tier, this::addDisplayText, 0xFFFFFF));
        widgetGroup.addWidget(new ToggleButtonWidget(169, -18 + 72 * tier, 18, 18, TJGuiTextures.UP_BUTTON, () -> false, this::onIncrement)
                .setTooltipText("machine.universal.toggle.increment"));
        widgetGroup.addWidget(new ToggleButtonWidget(169, 18 + 72 * tier, 18, 18, TJGuiTextures.DOWN_BUTTON, () -> false, this::onDecrement)
                .setTooltipText("machine.universal.toggle.decrement"));
        widgetGroup.addWidget(new GhostCircuitWidget(this.ghostCircuitSlot, 169, 40 + 72 * tier));
        for (int i = 0; i < bus.getSlots(); i++) {
            widgetGroup.addWidget(new SlotWidget(bus, i, 7 + 18 * (i % 10), 14 + 18 * (i / 10), true, !this.isExport)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 196, 63 + 72 * tier)
                .label(7, 4, this.getMetaFullName())
                .bindPlayerInventory(player.inventory, -18 + 72 * tier)
                .widget(widgetGroup)
                .build(getHolder(), player);
    }

    private void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString(this.ticks + "t")
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("machine.universal.ticks.operation")))));
    }

    private void onIncrement(boolean b) {
        this.ticks = MathHelper.clamp(this.ticks + 1, 1, Integer.MAX_VALUE);
        this.markDirty();
    }

    private void onDecrement(boolean b) {
        ticks = MathHelper.clamp(ticks - 1, 1, Integer.MAX_VALUE);
        this.markDirty();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.isExport) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.ITEM_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
        else {
            Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return this.isExport ? EXPORT_ITEMS : IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> list) {
        if (!this.isExport)
            list.add(this.ghostCircuitSlot);
        list.add(this.isExport ? this.exportItems : this.importItems);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Ticks", this.ticks);
        data.setTag("GhostCircuit", this.ghostCircuitSlot.getStackInSlot(0).serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.ticks = data.hasKey("Ticks") ? data.getInteger("Ticks") : 5;
        if (data.hasKey("GhostCircuit"))
            this.ghostCircuitSlot.setStackInSlot(0, new ItemStack(data.getCompoundTag("GhostCircuit")));
    }
}
