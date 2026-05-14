package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.render.Textures;
import gregtech.api.util.Position;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.builder.WidgetTabBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.TJItemUtils;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregicadditions.item.GAMetaItems.*;
import static gregicadditions.item.GAMetaItems.CONVEYOR_MODULE_MAX;
import static gregicadditions.item.GAMetaItems.CONVEYOR_MODULE_UMV;
import static gregicadditions.item.GAMetaItems.CONVEYOR_MODULE_UXV;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.items.MetaItems.CONVEYOR_MODULE_UV;

public class DualCover extends CoverBehavior implements CoverWithUI, ITickable {

    protected final IItemHandler itemHandler = this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    protected final IFluidHandler fluidHandler = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    protected final LargeItemStackHandler itemFilter = new LargeItemStackHandler(16, 1);
    protected final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 16)
            .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
            .collect(Collectors.toList()));
    protected final BlockPos posOffset = this.coverHolder.getPos().offset(this.attachedSide);
    protected final Object2ObjectMap<Item, ItemStack> itemType = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<FluidStack, FluidStack> fluidType = new Object2ObjectOpenHashMap<>();
    protected final int maxItemTransferRate;
    protected final int maxFluidTransferRate;
    protected final int tier;
    protected CoverConveyor.ConveyorMode conveyorMode = CoverConveyor.ConveyorMode.EXPORT;
    protected CoverPump.PumpMode pumpMode = CoverPump.PumpMode.EXPORT;
    protected boolean isConveyorWorking;
    protected boolean isPumpWorking;
    protected boolean isItemBlacklist;
    protected boolean isFluidBlacklist;
    protected int itemTransferRate;
    protected int fluidTransferRate;

    public DualCover(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide);
        this.maxItemTransferRate = this.itemTransferRate = (int) Math.min(Integer.MAX_VALUE, 2L << tier * 2);
        this.maxFluidTransferRate = this.fluidTransferRate = (int) Math.min(Integer.MAX_VALUE, 320L << tier * 2);
        this.tier = tier;
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide) != null || this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        super.onAttached(itemStack);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        if (compound.hasKey("conveyorMode"))
            this.conveyorMode = CoverConveyor.ConveyorMode.values()[compound.getInteger("conveyorMode")];
        if (compound.hasKey("pumpMode"))
            this.pumpMode = CoverPump.PumpMode.values()[compound.getInteger("pumpMode")];
        if (compound.hasKey("itemTransferRate"))
            this.itemTransferRate = compound.getInteger("itemTransferRate");
        if (compound.hasKey("fluidTransferRate"))
            this.fluidTransferRate = compound.getInteger("fluidTransferRate");
        if (compound.hasKey("itemBlacklist"))
            this.isItemBlacklist = compound.getBoolean("itemBlacklist");
        if (compound.hasKey("fluidBlacklist"))
            this.isFluidBlacklist = compound.getBoolean("fluidBlacklist");
        if (compound.hasKey("itemWorking"))
            this.isConveyorWorking = compound.getBoolean("itemWorking");
        if (compound.hasKey("fluidWorking"))
            this.isPumpWorking = compound.getBoolean("fluidWorking");
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            if (compound.hasKey("itemSlot:" + i)) {
                this.itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("itemSlot:" + i)));
                final ItemStack stack = this.itemFilter.getStackInSlot(i);
                this.itemType.put(stack.getItem(), stack);
            }
        }
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            if (compound.hasKey("fluidSlot:" + i)) {
                this.fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluidSlot:" + i)), true);
                this.fluidType.put(this.fluidFilter.getTankAt(i).getFluid(), this.fluidFilter.getTankAt(i).getFluid());
            }
        }
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        Textures.PUMP_OVERLAY.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
        Textures.CONVEYOR_OVERLAY.renderSided(attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final MetaItem<?>.MetaValueItem[] conveyors = {null, CONVEYOR_MODULE_LV, CONVEYOR_MODULE_MV, CONVEYOR_MODULE_HV, CONVEYOR_MODULE_EV, CONVEYOR_MODULE_IV, CONVEYOR_MODULE_LUV, CONVEYOR_MODULE_ZPM, CONVEYOR_MODULE_UV, CONVEYOR_MODULE_UHV, CONVEYOR_MODULE_UEV, CONVEYOR_MODULE_UIV, CONVEYOR_MODULE_UMV, CONVEYOR_MODULE_UXV, CONVEYOR_MODULE_MAX};
        final MetaItem<?>.MetaValueItem[] pumps = {null, ELECTRIC_PUMP_LV, ELECTRIC_PUMP_MV, ELECTRIC_PUMP_HV, ELECTRIC_PUMP_EV, ELECTRIC_PUMP_IV, ELECTRIC_PUMP_LUV, ELECTRIC_PUMP_ZPM, ELECTRIC_PUMP_UV, ELECTRIC_PUMP_UHV, ELECTRIC_PUMP_UEV, ELECTRIC_PUMP_UIV, ELECTRIC_PUMP_UMV, ELECTRIC_PUMP_UXV, ELECTRIC_PUMP_MAX};
        final WidgetGroup itemWidgetGroup = new WidgetGroup(new Position(10, 95));
        final WidgetGroup fluidWidgetGroup = new WidgetGroup(new Position(10, 115));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            itemWidgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.itemFilter, item -> {
                if (!item.isEmpty())
                    this.itemType.put(item.getItem(), item);
            }, item -> this.itemType.remove(item.getItem())).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> !this.itemType.containsKey(item.getItem())));
        }
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            fluidWidgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid != null)
                    this.fluidType.put(fluid, fluid);
            }, this.fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setPutFluidsPredicate(fluid -> !this.fluidType.containsKey(fluid)));
        }
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab(String.format("metaitem.conveyor.module.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? conveyors[this.tier].getStackForm() : this.getPickItem(), tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 20, 20, "-10", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(146, 20, 20, 20, "+10", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(30, 20, 20, 20, "-1", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(126, 20, 20, 20, "+1", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(50, 20, 38, 20,"/2", data -> this.setItemTransferRate(this.itemTransferRate / 2D)));
                    tab.add(new ClickButtonWidget(88, 20, 38, 20, "*2", data -> this.setItemTransferRate(this.itemTransferRate * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 40, 156, 18, true, () -> String.valueOf(this.itemTransferRate), this::setItemTransferRate)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 65, 75, 20, CoverConveyor.ConveyorMode.class, () -> this.conveyorMode, this::setConveyorMode));
                    tab.add(itemWidgetGroup);
                    tab.add(new ToggleButtonWidget(151, 95, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isItemBlacklist, this::setItemBlacklist)
                            .setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isConveyorWorking, this::setConveyorWorking)
                            .setTooltipText("machine.universal.toggle.run.mode"));
                }).addTab(String.format("metaitem.electric.pump.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? pumps[this.tier].getStackForm() : this.getPickItem(), tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 34, 20, "-100", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(128, 20, 34, 20, "+100", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(44, 20, 22, 20, "-10", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(106, 20, 24, 20, "+10", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(66, 20, 20, 20, "-1", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(86, 20, 20, 20, "+1", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(10, 40, 76, 20, "/2", data -> this.setFluidTransferRate(this.fluidTransferRate / 2D)));
                    tab.add(new ClickButtonWidget(86, 40, 76, 20, "*2", data -> this.setFluidTransferRate(this.fluidTransferRate * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 60, 156, 18, true, () -> String.valueOf(this.fluidTransferRate), this::setFluidTransferRate)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 85, 75, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode));
                    tab.add(fluidWidgetGroup);
                    tab.add(new ToggleButtonWidget(151, 115, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isFluidBlacklist, this::setFluidBlacklist)
                            .setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isPumpWorking, this::setPumpWorking)
                            .setTooltipText("machine.universal.toggle.run.mode"));
                });
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 272)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale(String.format("metaitem.dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
                .widget(tabBuilder.build())
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190)
                .build(this, player);
    }

    @Override
    public void update() {
        if (this.isConveyorWorking && this.itemHandler != null) {
            final TileEntity tileEntity = this.coverHolder.getWorld().getTileEntity(this.posOffset);
            if (tileEntity != null) {
                final IItemHandler destItemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (destItemHandler != null) {
                    if (this.conveyorMode == CoverConveyor.ConveyorMode.IMPORT) {
                        this.transferItems(destItemHandler, this.itemHandler);
                    } else this.transferItems(this.itemHandler, destItemHandler);
                }
            }
        }
        if (this.isPumpWorking && this.fluidHandler != null) {
            final TileEntity tileEntity = this.coverHolder.getWorld().getTileEntity(this.posOffset);
            if (tileEntity != null) {
                final IFluidHandler destFluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (destFluidHandler != null) {
                    if (this.pumpMode == CoverPump.PumpMode.IMPORT) {
                        this.transferFluids(destFluidHandler, this.fluidHandler);
                    } else this.transferFluids(this.fluidHandler, destFluidHandler);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("itemTransferRate", this.itemTransferRate);
        tagCompound.setInteger("fluidTransferRate", this.fluidTransferRate);
        tagCompound.setBoolean("conveyorWorking", this.isConveyorWorking);
        tagCompound.setBoolean("pumpWorking", this.isPumpWorking);
        tagCompound.setInteger("conveyorMode", this.conveyorMode.ordinal());
        tagCompound.setInteger("pumpMode", this.pumpMode.ordinal());
        tagCompound.setTag("itemFilter", this.itemFilter.serializeNBT());
        tagCompound.setTag("fluidFilter", this.fluidFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.itemTransferRate = tagCompound.getInteger("itemTransferRate");
        this.fluidTransferRate = tagCompound.getInteger("fluidTransferRate");
        this.isConveyorWorking = tagCompound.getBoolean("conveyorWorking");
        this.isPumpWorking = tagCompound.getBoolean("pumpWorking");
        this.conveyorMode = CoverConveyor.ConveyorMode.values()[tagCompound.getInteger("conveyorMode")];
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("pumpMode")];
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("itemFilter"));
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("fluidFilter"));
        for (int i = 0; i < this.itemFilter.getSlots(); i++)
            this.itemType.put(this.itemFilter.getStackInSlot(i).getItem(), this.itemFilter.getStackInSlot(i));
        for (int i = 0; i < this.fluidFilter.getTanks(); i++)
            this.fluidType.put(this.fluidFilter.getTankAt(i).getFluid(), this.fluidFilter.getTankAt(i).getFluid());
    }

    protected void transferItems(IItemHandler itemHandler, IItemHandler destItemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty() || this.isItemBlacklist == this.itemType.containsKey(stack.getItem())) continue;
            final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
            final ItemStack otherStack = itemHandler.extractItem(i, Math.min(this.itemTransferRate, stack.getCount() - inserted), false);
            TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
        }
    }

    protected void transferFluids(IFluidHandler fluidHandler, IFluidHandler destFluidHandler) {
        final IFluidTankProperties[] tanks = fluidHandler.getTankProperties();
        for (IFluidTankProperties tank : tanks) {
            FluidStack fluidStack = tank.getContents();
            if (fluidStack == null || this.isFluidBlacklist == this.fluidType.containsKey(fluidStack)) continue;
            fluidStack = fluidHandler.drain(fluidStack, false);
            if (fluidStack == null) continue;
            fluidStack.amount = Math.min(fluidStack.amount, this.fluidTransferRate);
            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
            fluidHandler.drain(fluidStack, true);
        }
    }

    public void setItemTransferRate(double itemTransferRate) {
        this.itemTransferRate = (int) Math.max(1, Math.min(this.maxItemTransferRate, itemTransferRate));
        this.markAsDirty();
    }

    public void setItemTransferRate(String text, String id) {
        this.itemTransferRate = (int) Math.max(1, Math.min(this.maxItemTransferRate, Double.parseDouble(text)));
        this.markAsDirty();
    }

    public void setFluidTransferRate(double fluidTransferRate) {
        this.fluidTransferRate = (int) Math.max(1, Math.min(this.maxFluidTransferRate, fluidTransferRate));
        this.markAsDirty();
    }

    public void setFluidTransferRate(String text, String id) {
        this.fluidTransferRate = (int) Math.max(1, Math.min(this.maxFluidTransferRate, Double.parseDouble(text)));
        this.markAsDirty();
    }

    public void setConveyorWorking(boolean conveyorWorking) {
        this.isConveyorWorking = conveyorWorking;
        this.markAsDirty();
    }

    public void setPumpWorking(boolean pumpWorking) {
        this.isPumpWorking = pumpWorking;
        this.markAsDirty();
    }

    public void setConveyorMode(CoverConveyor.ConveyorMode conveyorMode) {
        this.conveyorMode = conveyorMode;
        this.markAsDirty();
    }

    public void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        this.markAsDirty();
    }

    public void setItemBlacklist(boolean itemBlacklist) {
        this.isItemBlacklist = itemBlacklist;
        this.markAsDirty();
    }

    public void setFluidBlacklist(boolean fluidBlacklist) {
        this.isFluidBlacklist = fluidBlacklist;
        this.markAsDirty();
    }
}
