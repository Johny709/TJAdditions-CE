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
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJTextures;
import tj.util.map.Strategies;

import java.util.regex.Pattern;

public class VoidItemCover extends CoverBehavior implements CoverWithUI, ITickable {

    protected final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
    protected final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
    protected final IItemHandler itemHandler = this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide);
    protected boolean isWorking;
    protected int tickTime = 20;

    public VoidItemCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        if (compound.hasKey("tickTime"))
            this.tickTime = compound.getInteger("tickTime");
        if (compound.hasKey("isWorking"))
            this.isWorking = compound.getBoolean("isWorking");
        for (int i = 0; i < 9; i++) {
            if (compound.hasKey("slot:" + i)) {
                this.itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
                this.itemType.put(this.itemFilter.getStackInSlot(i), this.itemFilter.getStackInSlot(i));
            }
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
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(63, 27));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, this.itemFilter, item -> {
                if (item.isEmpty()) return;
                this.itemType.put(item, item);
            }, this.itemType::remove).setPutItemsPredicate(item -> !this.itemType.containsKey(item))
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_item_cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, () -> String.valueOf(this.tickTime), this::setTickTime)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ClickButtonWidget(7, 7, 18, 18, "/2", data -> this.setTickTime(String.valueOf((long) this.tickTime / 2), "")))
                .widget(new ClickButtonWidget(151, 7, 18, 18, "*2", data -> this.setTickTime(String.valueOf((long) this.tickTime * 2), "")))
                .widget(new ToggleButtonWidget(151, 85, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isWorking, this::setWorking)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void update() {
        if (this.isWorking && this.coverHolder.getOffsetTimer() % this.tickTime == 0) {
            for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                final ItemStack stack = this.itemHandler.getStackInSlot(i);
                if (stack.isEmpty() || !this.itemType.containsKey(stack)) continue;
                this.itemHandler.extractItem(i, Integer.MAX_VALUE, false);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("tickTime", this.tickTime);
        tagCompound.setBoolean("isWorking", this.isWorking);
        tagCompound.setTag("itemFilter", this.itemFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.tickTime = Math.max(1, tagCompound.getInteger("tickTime"));
        this.isWorking = tagCompound.getBoolean("isWorking");
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("itemFilter"));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            final ItemStack stack = this.itemFilter.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            this.itemType.put(stack, stack);
        }
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
        this.markAsDirty();
    }

    public void setTickTime(String text, String id) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }
}
