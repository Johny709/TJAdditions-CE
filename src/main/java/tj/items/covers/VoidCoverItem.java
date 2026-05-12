package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJTextures;

public class VoidCoverItem extends CoverBehavior implements CoverWithUI, ITickable {

    protected final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
    protected final IItemHandler itemHandler;

    public VoidCoverItem(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.itemHandler = this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        for (int i = 0; i < 9; i++) {
            if (compound.hasKey("slot:" + i))
                this.itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
        }
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        TJTextures.VOID_ITEM_COVER_OVERLAY.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
        TJTextures.OUTSIDE_OVERLAY_BASE.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote)
            this.openUI((EntityPlayerMP) playerIn);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(53, 27));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18, i, this.itemFilter)
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_item_cover.name"))
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void update() {
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            for (int j = 0; j < this.itemFilter.getSlots(); j++) {
                if (this.itemHandler.getStackInSlot(i).isItemEqual(this.itemFilter.getStackInSlot(j)))
                    this.itemHandler.extractItem(i, Integer.MAX_VALUE, false);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("itemFilter", this.itemFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("itemFilter"));
    }
}
