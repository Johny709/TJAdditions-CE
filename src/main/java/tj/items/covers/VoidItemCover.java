package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.integration.ae2.ITickSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;
import tj.util.map.Strategies;

import java.util.regex.Pattern;

public class VoidItemCover extends CoverBehavior implements CoverWithUI, ITickable, IWorkable, ITickSetter {

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
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(this);
        return TJModularUI.builder(this, player)
                .setWidthHeight(200, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_item_cover.name"))
                .widget(new TJToggleButtonWidget(174, 105, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON, () -> false, this::onReset)
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 162, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isWorking, this::setWorkingEnabled)
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "macine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 82, 162, 18, true, () -> String.valueOf(this.tickTime),
                        (text, id) -> this.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 82, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
                    if (!value && panelWidgetGroup.isPanelOpen()) {
                        panelWidgetGroup.closePanel();
                    } else if (value) {
                        panelWidgetGroup.openPanel();
                    }
                }).setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11)) // infinity bimetal gear
                        .setHoverTooltipText("tj.machine.universal.ticks.operation_configure")
                        .useToggleTexture(true))
                .widget(panelWidgetGroup)
                .widget(widgetGroup)
                .build();
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

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        return null;
    }

    private void onReset(boolean reset) {
        this.tickTime = 1;
        this.markAsDirty();
    }

    public void setTickTime(String text, String id) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    @Override
    public int getProgress() {
        return (int) this.coverHolder.getOffsetTimer() % this.tickTime;
    }

    @Override
    public int getMaxProgress() {
        return this.tickTime;
    }

    @Override
    public boolean isActive() {
        return this.isWorking;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorking;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.isWorking = b;
        this.markAsDirty();
    }
}
