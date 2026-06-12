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
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.Position;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.mui.widgets.impl.TJSlotWidget;
import tj.mui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.mui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.TJMetaItems;
import tj.items.handlers.FilteredItemStackHandler;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.TJItemUtils;
import tj.util.map.Strategies;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;

public class DualCover extends CoverBehavior implements CoverWithUI, ITickable {

    protected final IItemHandler itemHandler = this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    protected final IFluidHandler fluidHandler = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    protected final FilteredItemStackHandler itemFilterSlot = new FilteredItemStackHandler(this.coverHolder, 1, Integer.MAX_VALUE)
            .setItemStackPredicate((slot, itemStack) -> ITEM_FILTER.isItemEqual(itemStack) || ORE_DICTIONARY_FILTER.isItemEqual(itemStack))
            .setOnContentsChangedPre((slot, itemStack, insert) -> {
                if (!insert) return;
                this.itemFilterType = ITEM_FILTER.isItemEqual(itemStack) ? FilterType.NORMAL : ORE_DICTIONARY_FILTER.isItemEqual(itemStack) ? FilterType.ORE_DICT : FilterType.NONE;
            }).setOnContentsChangedPost((slot, itemStack) -> {
                if (!itemStack.isEmpty()) return;
                this.itemFilterType = FilterType.NONE;
            });
    protected final FilteredItemStackHandler fluidFilterSlot = new FilteredItemStackHandler(this.coverHolder, 1, 1)
            .setItemStackPredicate((slot, itemStack) -> FLUID_FILTER.isItemEqual(itemStack))
            .setOnContentsChangedPre((slot, itemStack, insert) -> {
                if (!insert) return;
                this.fluidFilterType = FLUID_FILTER.isItemEqual(itemStack) ? FilterType.NORMAL : FilterType.NONE;
            }).setOnContentsChangedPost((slot, itemStack) -> {
                if (!itemStack.isEmpty()) return;
                this.fluidFilterType = FilterType.NONE;
            });
    protected final LargeItemStackHandler itemFilter = new LargeItemStackHandler(16, Integer.MAX_VALUE);
    protected final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 16)
            .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
            .collect(Collectors.toList()));
    protected final OreDictionaryItemFilter oreDictionaryItemFilter = new OreDictionaryItemFilter() {
        @Override
        public void initUI(Consumer<Widget> widgetGroup) {
            widgetGroup.accept(new LabelWidget(10, 90, "cover.ore_dictionary_filter.title1"));
            widgetGroup.accept(new LabelWidget(10, 100, "cover.ore_dictionary_filter.title2"));
            widgetGroup.accept(new TextFieldWidget(10, 115, 100, 12, true, () -> this.oreDictionaryFilter, this::setOreDictionaryFilter)
                    .setMaxStringLength(64)
                    .setValidator(str -> Pattern.compile("\\*?[a-zA-Z0-9_]*\\*?").matcher(str).matches()));
        }
    };
    protected final BlockPos posSideOf = this.coverHolder.getPos().offset(this.attachedSide);
    protected final EnumFacing sideOf = this.attachedSide.getOpposite();
    protected final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
    protected final Object2ObjectMap<FluidStack, FluidStack> fluidType = new Object2ObjectOpenHashMap<>();
    protected final int maxItemTransferRate;
    protected final int maxFluidTransferRate;
    protected final int tier;
    protected CoverConveyor.ConveyorMode conveyorMode = CoverConveyor.ConveyorMode.EXPORT;
    protected CoverPump.PumpMode pumpMode = CoverPump.PumpMode.EXPORT;
    protected FilterType itemFilterType = FilterType.NONE;
    protected FilterType fluidFilterType = FilterType.NONE;
    protected boolean isConveyorWorking;
    protected boolean isPumpWorking;
    protected boolean isItemBlacklist;
    protected boolean isFluidBlacklist;
    protected int itemTransferRate;
    protected int fluidTransferRate;
    protected int itemTicks = 20;
    protected int fluidTicks = 20;

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
        if (compound.hasKey("itemTicks"))
            this.itemTicks = compound.getInteger("itemTicks");
        if (compound.hasKey("fluidTicks"))
            this.fluidTicks = compound.getInteger("fluidTicks");
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
        if (compound.hasKey("itemFilterSlot"))
            this.itemFilterSlot.insertItem(0, new ItemStack(compound.getCompoundTag("itemFilterSlot")), false);
        if (compound.hasKey("fluidFilterSlot"))
            this.fluidFilterSlot.insertItem(0, new ItemStack(compound.getCompoundTag("fluidFilterSlot")), false);
        if (compound.hasKey("oreDictFilter"))
            this.oreDictionaryItemFilter.readFromNBT(compound.getCompoundTag("oreDictFilter"));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            if (compound.hasKey("itemSlot:" + i)) {
                this.itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("itemSlot:" + i)));
                final ItemStack stack = this.itemFilter.getStackInSlot(i);
                this.itemType.put(stack, stack);
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
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab(String.format("metaitem.conveyor.module.%s.name", GAValues.VN[this.tier].toLowerCase()), TJMetaItems.CONVEYORS[this.tier].getStackForm(), this::createConveyorTab)
                .addTab(String.format("metaitem.electric.pump.%s.name", GAValues.VN[this.tier].toLowerCase()), TJMetaItems.PUMPS[this.tier].getStackForm(), this::createPumpTab);
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 272)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale(String.format("metaitem.dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
                .widget(tabBuilder.build())
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190)
                .build(this, player);
    }

    private void createConveyorTab(List<Widget> widgetGroup) {
        final WidgetGroup itemWidgetGroup = new WidgetGroup(new Position(10, 95));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            itemWidgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.itemFilter, item -> {
                if (!item.isEmpty())
                    this.itemType.put(item, item);
            }, this.itemType::remove).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> !this.itemType.containsKey(item)));
        }
        final PopUpWidget<?> itemFilterPopup = new PopUpWidget<>()
                .setClickToDefault(false)
                .setIndexSupplier(() -> {
                    final ItemStack itemStack = this.itemFilterSlot.getStackInSlot(0);
                    return ITEM_FILTER.isItemEqual(itemStack) ? 1 : 0;
                }).addPopup(widgetGroup1 -> true)
                .addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(itemWidgetGroup);
                    return false;
                }).addPopup(widgetGroup1 -> {
                    this.oreDictionaryItemFilter.initUI(widgetGroup1::addWidget);
                    return false;
                });
        widgetGroup.add(new LabelWidget(7, 5, "cover.conveyor.title", GAValues.VN[this.tier]));
        widgetGroup.add(new ClickButtonWidget(7, 20, 23, 20, "-10", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 100 : 10))));
        widgetGroup.add(new ClickButtonWidget(146, 20, 23, 20, "+10", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 100 : 10))));
        widgetGroup.add(new ClickButtonWidget(30, 20, 20, 20, "-1", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 5 : 1))));
        widgetGroup.add(new ClickButtonWidget(126, 20, 20, 20, "+1", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 5 : 1))));
        widgetGroup.add(new ClickButtonWidget(50, 20, 38, 20,"/2", data -> this.setItemTransferRate(this.itemTransferRate / 2D)));
        widgetGroup.add(new ClickButtonWidget(88, 20, 38, 20, "*2", data -> this.setItemTransferRate(this.itemTransferRate * 2)));
        widgetGroup.add(new NewTextFieldWidget<>(7, 40, 162, 18, true, () -> String.valueOf(this.itemTransferRate), this::setItemTransferRate)
                .setTooltipText("tj.machine.universal.item_throughput").setTooltipFormat(() -> new String[]{String.valueOf(this.itemTransferRate)})
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true));
        widgetGroup.add(new CycleButtonWidget(7, 65, 78, 20, CoverConveyor.ConveyorMode.class, () -> this.conveyorMode, this::setConveyorMode));
        widgetGroup.add(new ImageWidget(-28, 127, 26, 44, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new TJSlotWidget<>(this.itemFilterSlot, 0, -24, 131)
                .setActiveBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
        widgetGroup.add(itemFilterPopup);
        widgetGroup.add(new ToggleButtonWidget(-24, 149, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isItemBlacklist, this::setItemBlacklist)
                .setTooltipText("cover.filter.blacklist"));
        widgetGroup.add(new NewTextFieldWidget<>(92, 133, 76, 18, true, () -> String.valueOf(this.itemTicks), this::setItemTicks)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("machine.universal.ticks.operation")
                .setUpdateOnTyping(true));
        widgetGroup.add(new ClickButtonWidget(92, 151, 38, 18, "/2", data -> this.setItemTicks(String.valueOf((long) this.itemTicks / 2), "")));
        widgetGroup.add(new ClickButtonWidget(130, 151, 38, 18, "*2", data -> this.setItemTicks(String.valueOf((long) this.itemTicks * 2), "")));
        widgetGroup.add(new ImageWidget(-28, 244, 26, 26, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new ToggleButtonWidget(-24, 248, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isConveyorWorking, this::setConveyorWorking)
                .setTooltipText("machine.universal.toggle.run.mode"));
    }

    private void createPumpTab(List<Widget> widgetGroup) {
        final WidgetGroup fluidWidgetGroup = new WidgetGroup(new Position(10, 115));
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            fluidWidgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid != null)
                    this.fluidType.put(fluid, fluid);
            }, this.fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setPutFluidsPredicate(fluid -> !this.fluidType.containsKey(fluid)));
        }
        final PopUpWidget<?> fluidFilterPopup = new PopUpWidget<>()
                .setClickToDefault(false)
                .setIndexSupplier(() -> {
                    final ItemStack itemStack = this.fluidFilterSlot.getStackInSlot(0);
                    return FLUID_FILTER.isItemEqual(itemStack) ? 1 : 0;
                })
                .addPopup(widgetGroup1 -> true)
                .addPopup(widgetGroup1 -> {
                    widgetGroup1.addWidget(fluidWidgetGroup);
                    return false;
                });
        widgetGroup.add(new LabelWidget(7, 5, "cover.pump.title", GAValues.VN[this.tier]));
        widgetGroup.add(new ClickButtonWidget(7, 20, 37, 20, "-100", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 500 : 100))));
        widgetGroup.add(new ClickButtonWidget(132, 20, 37, 20, "+100", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 500 : 100))));
        widgetGroup.add(new ClickButtonWidget(44, 20, 24, 20, "-10", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 50 : 10))));
        widgetGroup.add(new ClickButtonWidget(108, 20, 24, 20, "+10", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 50 : 10))));
        widgetGroup.add(new ClickButtonWidget(68, 20, 20, 20, "-1", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 5 : 1))));
        widgetGroup.add(new ClickButtonWidget(88, 20, 20, 20, "+1", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 5 : 1))));
        widgetGroup.add(new ClickButtonWidget(7, 40, 81, 20, "/2", data -> this.setFluidTransferRate(this.fluidTransferRate / 2D)));
        widgetGroup.add(new ClickButtonWidget(88, 40, 81, 20, "*2", data -> this.setFluidTransferRate(this.fluidTransferRate * 2)));
        widgetGroup.add(new NewTextFieldWidget<>(7, 60, 162, 18, true, () -> String.valueOf(this.fluidTransferRate), this::setFluidTransferRate)
                .setTooltipText("tj.machine.universal.fluid_throughput").setTooltipFormat(() -> new String[]{String.valueOf(this.fluidTransferRate)})
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true));
        widgetGroup.add(new CycleButtonWidget(7, 85, 78, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode));
        widgetGroup.add(new ImageWidget(-28, 147, 26, 44, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new TJSlotWidget<>(this.fluidFilterSlot, 0, -24, 151)
                .setActiveBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
        widgetGroup.add(fluidFilterPopup);
        widgetGroup.add(new ToggleButtonWidget(-24, 169, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isFluidBlacklist, this::setFluidBlacklist)
                .setTooltipText("cover.filter.blacklist"));
        widgetGroup.add(new NewTextFieldWidget<>(92, 151, 76, 18, true, () -> String.valueOf(this.fluidTicks), this::setFluidTicks)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("machine.universal.ticks.operation")
                .setUpdateOnTyping(true));
        widgetGroup.add(new ClickButtonWidget(92, 169, 38, 18, "/2", data -> this.setFluidTicks(String.valueOf((long) this.fluidTicks / 2), "")));
        widgetGroup.add(new ClickButtonWidget(130, 169, 38, 18, "*2", data -> this.setFluidTicks(String.valueOf((long) this.fluidTicks * 2), "")));
        widgetGroup.add(new ImageWidget(-28, 244, 26, 26, GuiTextures.BORDERED_BACKGROUND));
        widgetGroup.add(new ToggleButtonWidget(-24, 248, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isPumpWorking, this::setPumpWorking)
                .setTooltipText("machine.universal.toggle.run.mode"));
    }

    @Override
    public void update() {
        if (this.isConveyorWorking && this.itemHandler != null && this.coverHolder.getOffsetTimer() % this.itemTicks == 0) {
            final TileEntity tileEntity = this.coverHolder.getWorld().getTileEntity(this.posSideOf);
            if (tileEntity != null) {
                final IItemHandler destItemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.sideOf);
                if (destItemHandler != null) {
                    if (this.conveyorMode == CoverConveyor.ConveyorMode.IMPORT) {
                        this.transferItems(destItemHandler, this.itemHandler);
                    } else this.transferItems(this.itemHandler, destItemHandler);
                }
            }
        }
        if (this.isPumpWorking && this.fluidHandler != null && this.coverHolder.getOffsetTimer() % this.fluidTicks == 0) {
            final TileEntity tileEntity = this.coverHolder.getWorld().getTileEntity(this.posSideOf);
            if (tileEntity != null) {
                final IFluidHandler destFluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.sideOf);
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
        tagCompound.setInteger("itemTicks", this.itemTicks);
        tagCompound.setInteger("fluidTicks", this.fluidTicks);
        tagCompound.setInteger("itemTransferRate", this.itemTransferRate);
        tagCompound.setInteger("fluidTransferRate", this.fluidTransferRate);
        tagCompound.setInteger("conveyorMode", this.conveyorMode.ordinal());
        tagCompound.setInteger("pumpMode", this.pumpMode.ordinal());
        tagCompound.setInteger("itemFilterType", this.itemFilterType.ordinal());
        tagCompound.setInteger("fluidFilterType", this.fluidFilterType.ordinal());
        tagCompound.setBoolean("conveyorWorking", this.isConveyorWorking);
        tagCompound.setBoolean("pumpWorking", this.isPumpWorking);
        tagCompound.setBoolean("itemBlacklist", this.isItemBlacklist);
        tagCompound.setBoolean("fluidBlacklist", this.isFluidBlacklist);
        tagCompound.setTag("itemFilter", this.itemFilter.serializeNBT());
        tagCompound.setTag("fluidFilter", this.fluidFilter.serializeNBT());
        tagCompound.setTag("itemFilterSlot", this.itemFilterSlot.serializeNBT());
        tagCompound.setTag("fluidFilterSlot", this.fluidFilterSlot.serializeNBT());
        final NBTTagCompound compound = new NBTTagCompound();
        this.oreDictionaryItemFilter.writeToNBT(compound);
        tagCompound.setTag("oreDictFilter", compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.itemTicks = Math.max(1, tagCompound.getInteger("itemTicks"));
        this.fluidTicks = Math.max(1, tagCompound.getInteger("fluidTicks"));
        this.itemTransferRate = tagCompound.getInteger("itemTransferRate");
        this.fluidTransferRate = tagCompound.getInteger("fluidTransferRate");
        this.conveyorMode = CoverConveyor.ConveyorMode.values()[tagCompound.getInteger("conveyorMode")];
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("pumpMode")];
        this.itemFilterType = FilterType.values()[tagCompound.getInteger("itemFilterType")];
        this.fluidFilterType = FilterType.values()[tagCompound.getInteger("fluidFilterType")];
        this.isConveyorWorking = tagCompound.getBoolean("conveyorWorking");
        this.isPumpWorking = tagCompound.getBoolean("pumpWorking");
        this.isItemBlacklist = tagCompound.getBoolean("itemBlacklist");
        this.isFluidBlacklist = tagCompound.getBoolean("fluidBlacklist");
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("itemFilter"));
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("fluidFilter"));
        this.itemFilterSlot.deserializeNBT(tagCompound.getCompoundTag("itemFilterSlot"));
        this.fluidFilterSlot.deserializeNBT(tagCompound.getCompoundTag("fluidFilterSlot"));
        this.oreDictionaryItemFilter.readFromNBT(tagCompound.getCompoundTag("oreDictFilter"));
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            final ItemStack stack = this.itemFilter.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            this.itemType.put(stack, stack);
        }
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final FluidStack stack = this.fluidFilter.getTankAt(i).getFluid();
            if (stack == null) continue;
            this.fluidType.put(stack, stack);
        }
    }

    protected void transferItems(IItemHandler itemHandler, IItemHandler destItemHandler) {
        int itemTransferRate = this.itemTransferRate;
        switch (this.itemFilterType) {
            case NORMAL:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && this.isItemBlacklist == (this.itemType.get(stack) == null)) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = Math.min(itemTransferRate, stack.getCount() - inserted);
                        if (extract < 1) continue;
                        itemTransferRate -= extract;
                        final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
                break;
            case ORE_DICT:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && this.oreDictionaryItemFilter.matchItemStack(stack) != null) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = Math.min(itemTransferRate, stack.getCount() - inserted);
                        if (extract < 1) continue;
                        itemTransferRate -= extract;
                        final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
                break;
            default:
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = Math.min(this.itemTransferRate, stack.getCount() - inserted);
                        if (extract < 1) continue;
                        itemTransferRate -= extract;
                        final ItemStack otherStack = itemHandler.extractItem(i, extract, false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
        }
    }

    protected void transferFluids(IFluidHandler fluidHandler, IFluidHandler destFluidHandler) {
        final IFluidTankProperties[] tanks = fluidHandler.getTankProperties();
        if (this.fluidFilterType == FilterType.NORMAL) {
            for (IFluidTankProperties tank : tanks) {
                FluidStack fluidStack = tank.getContents();
                if (fluidStack != null && this.isFluidBlacklist == (this.fluidType.get(fluidStack) == null)) {
                    fluidStack = fluidHandler.drain(fluidStack, false);
                    if (fluidStack == null) continue;
                    fluidStack.amount = Math.min(fluidStack.amount, this.fluidTransferRate);
                    fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                    fluidHandler.drain(fluidStack, true);
                }
            }
        } else for (IFluidTankProperties tank : tanks) {
            FluidStack fluidStack = tank.getContents();
            if (fluidStack != null) {
                fluidStack = fluidHandler.drain(fluidStack, false);
                if (fluidStack == null) continue;
                fluidStack.amount = Math.min(fluidStack.amount, this.fluidTransferRate);
                fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                fluidHandler.drain(fluidStack, true);
            }
        }
    }

    public void setItemTicks(String text, String id) {
        this.itemTicks = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    public void setFluidTicks(String text, String id) {
        this.fluidTicks = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
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

    public enum FilterType {
        NONE,
        NORMAL,
        SMART,
        ORE_DICT
    }
}
