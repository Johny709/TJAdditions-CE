package tj.integration.ae2.part;

import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.core.Api;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.misc.TileInterface;
import appeng.tile.networking.TileCableBus;
import baubles.api.BaublesApi;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.integration.ae2.tile.TilePatternInterface;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.integration.ae2.tile.TileSuperInterface;
import tj.integration.ae2.tile.TileSuperUltimateInterface;
import tj.items.handlers.FilteredItemStackHandler;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;
import tj.util.TJItemUtils;
import tj.util.references.ObjectReference;

import java.util.Optional;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PartSuperInterfaceTerminal extends PartInterfaceTerminal implements ITileEntityUI {

    public PartSuperInterfaceTerminal(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null) {
            if (!player.getEntityWorld().isRemote) {
                TileEntityHolder holder = new TileEntityHolder(tileCableBus);
                holder.setFacing(this.getSide().getFacing());
                holder.openUI((EntityPlayerMP) player);
            }
            return true;
        }
        return true;
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final ObjectReference<String> searchInputs = new ObjectReference<>("");
        final ObjectReference<String> searchOutputs = new ObjectReference<>("");
        final ObjectReference<String> searchInterface = new ObjectReference<>("");
        final ItemStack patternMultiTool = Optional.of(player.inventory.mainInventory)
                .map(inventory -> {
                    for (ItemStack stack : inventory)
                        if (stack.isItemEqual(TJItemUtils.getItemStackFromName("nae2:pattern_multiplier")))
                            return stack;
                    final IItemHandlerModifiable baubleSlots = BaublesApi.getBaublesHandler(player);
                    for (int i = 0; i < baubleSlots.getSlots(); i++)
                        if (baubleSlots.getStackInSlot(i).isItemEqual(TJItemUtils.getItemStackFromName("nae2:pattern_multiplier")))
                            return baubleSlots.getStackInSlot(i);
                    return ItemStack.EMPTY;
                }).get();
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(patternMultiTool);
        final NBTTagCompound invTag = compound.getCompoundTag("inv");
        final NBTTagCompound upgradeTag = compound.getCompoundTag("upgrades");
        final FilteredItemStackHandler multiPatternSlots = new FilteredItemStackHandler(null, 36, 64)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                        itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || itemStack.isItemEqual(TJItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")));
        multiPatternSlots.setOnContentsChangedPost((slot, itemStack) -> writePatternMultiToolToNBT(multiPatternSlots, invTag));
        final FilteredItemStackHandler multiUpgradeSlots = new FilteredItemStackHandler(null, 3, 1)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)));
        multiUpgradeSlots.setOnContentsChangedPost((slot, itemStack) -> writePatternMultiToolToNBT(multiUpgradeSlots, upgradeTag));
        final NewTextFieldWidget<?> inputsTextField = new NewTextFieldWidget<>(7, 16, 90, 12, true, searchInputs::getValue, (s, id) -> searchInputs.setValue(s));
        final NewTextFieldWidget<?> outputsTextField = new NewTextFieldWidget<>(7, 30, 90, 12, true, searchOutputs::getValue, (s, id) -> searchOutputs.setValue(s));
        final NewTextFieldWidget<?> interfaceTextField = new NewTextFieldWidget<>(7, 44, 90, 12, true, searchInterface::getValue, (s, id) -> searchInterface.setValue(s));
        final AEItemListWidget<IInterfaceHost> aeItemListWidget = new AEItemListWidget<>(7, 60, 162, 162, this.getGridNode(), TileInterface.class, TileSuperInterface.class, TileSuperDualInterface.class, TilePatternInterface.class, TileSuperUltimateInterface.class);
        final ModularUI.Builder builder = ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 316);
        return this.createPatternMultiToolGUI(builder, patternMultiTool, multiUpgradeSlots, multiPatternSlots, invTag, aeItemListWidget)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.super_interface_terminal.name"))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new ImageWidget(6, 59, 164, 164, TJGuiTextures.BLANK_SLOT))
                .widget(inputsTextField.setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                        .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                        .setUpdateOnTyping(true))
                .widget(outputsTextField.setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                        .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldOutputs")
                        .setUpdateOnTyping(true))
                .widget(interfaceTextField.setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                        .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldNames")
                        .setUpdateOnTyping(true))
                .widget(aeItemListWidget.setSlotPredicate((slot, interfaceHost) -> slot / 9 <= interfaceHost.getInterfaceDuality().getInstalledUpgrades(Upgrades.PATTERN_EXPANSION))
                        .setItemStackTransfer((itemStack, simulate) -> TJItemUtils.insertIntoItemHandler(multiPatternSlots, itemStack, simulate))
                        .setInventorySupplier(interfaceHost -> interfaceHost.getInterfaceDuality().getPatterns())
                        .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                        .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                        .setRenderCallback(this::renderCallback)
                        .setPredicate(interfaceHost -> {
                            if (interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) != YesNo.YES)
                                return false;
                            if (!searchInputs.getValue().isEmpty()) {
                                return this.isItemPresent(interfaceHost.getInterfaceDuality().getPatterns(), searchInputs.getValue(), false);
                            } else if (!searchOutputs.getValue().isEmpty()) {
                                return this.isItemPresent(interfaceHost.getInterfaceDuality().getPatterns(), searchOutputs.getValue(), true);
                            } else return searchInterface.getValue().isEmpty() || ((ICustomNameObject) interfaceHost).getCustomInventoryName().contains(searchInterface.getValue());
                        }))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 233, patternMultiTool))
                .bindOpenListener(() -> {
                    if (!patternMultiTool.isEmpty()) {
                        readPatternMultiToolNBT(multiPatternSlots, invTag.getTagList("Items", 10));
                        readPatternMultiToolNBT(multiUpgradeSlots, upgradeTag.getTagList("Items", 10));
                        if (patternMultiTool.getTagCompound() == null || patternMultiTool.getTagCompound().isEmpty()) {
                            compound.setTag("inv", invTag);
                            compound.setTag("upgrades", upgradeTag);
                            patternMultiTool.setTagCompound(compound);
                        }
                    }
                }).build(holder, player);
    }

    private boolean isItemPresent(IItemHandler itemHandler, String name, boolean output) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack itemStack = itemHandler.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            final NBTTagList tagList = TJItemUtils.getCompoundFromStack(itemStack).getTagList(output ? "out" : "in", 10);
            for (int j = 0; j < tagList.tagCount(); j++) {
                final NBTTagCompound tagCompound = tagList.getCompoundTagAt(j);
                final String id = tagCompound.getString("id");
                ItemStack stack;
                FluidStack fluid;
                if (id.equals("ae2fc:fluid_drop")) {
                    stack = ItemStack.EMPTY;
                    fluid = new FluidStack(FluidRegistry.getFluid(tagCompound.getCompoundTag("tag").getString("Fluid")), 1);
                } else {
                    stack = TJItemUtils.getItemStackFromName(id, 1, tagCompound.getShort("Damage"));
                    fluid = null;
                }
                if (stack.getDisplayName().contains(name) || fluid != null && fluid.getLocalizedName().contains(name))
                    return true;
            }
        }
        return false;
    }

    private void renderCallback(ItemStack itemStack, int x, int y) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(itemStack);
        final NBTTagCompound outputCompound = compound.getTagList("out", 10).getCompoundTagAt(0);
        final String id = outputCompound.getString("id");
        ItemStack output;
        FluidStack fluidOutput;
        long count;
        if (id.equals("ae2fc:fluid_drop")) {
            output = ItemStack.EMPTY;
            fluidOutput = new FluidStack(FluidRegistry.getFluid(outputCompound.getCompoundTag("tag").getString("Fluid")), 1);
        } else {
            output = TJItemUtils.getItemStackFromName(id, 1, outputCompound.getShort("Damage"));
            fluidOutput = null;
        }
        count = outputCompound.getLong("Cnt");
        if (count < 1)
            count = outputCompound.getInteger("Count");

        GlStateManager.disableBlend();
        if (!output.isEmpty()) {
            Widget.drawItemStack(output, x + 1, y + 1, null);
        } else if (fluidOutput != null) {
            TJGuiUtils.drawFluidForGui(fluidOutput, count, count, x + 1, y + 1, 17, 17);
        } else Widget.drawItemStack(itemStack, x + 1, y + 1, null);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = TextFormattingUtil.formatLongToCompactString(count, 4) + (fluidOutput != null ? "L" : "");
        fontRenderer.drawStringWithShadow(s, (x + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + 12) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    private ModularUI.Builder createPatternMultiToolGUI(ModularUI.Builder builder, ItemStack patternMultiTool, FilteredItemStackHandler multiUpgradeSlots, IItemHandler multiPatternSlots, NBTTagCompound invTag, AEItemListWidget<?> aeItemListWidget) {
        if (!patternMultiTool.isEmpty()) {
            final SlotScrollableWidgetGroup multiPatternSlotGroup = new SlotScrollableWidgetGroup(-118, 14, 72, 162, 4)
                    .setItemStackTransfer(aeItemListWidget::insertItem)
                    .setItemHandler(multiPatternSlots)
                    .setScrollWidth(0);
            builder.widget(new ImageWidget(-125, 0, 105, 218, GuiTextures.BORDERED_BACKGROUND))
                    .widget(new LabelWidget(-118, 4, "item.nae2.pattern_multiplier.name"))
                    .widget(new ButtonWidget<>(-118, 176, 18, 18, "*2", data -> changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m * 2, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.MULTIPLY_2.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-118, 194, 18, 18, "/2", data -> changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m / 2, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.DIVIDE_2.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-100, 176, 18, 18, "*3", data -> changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m * 3, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.MULTIPLY_3.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-100, 194, 18, 18, "/3", data -> changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m / 3, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.DIVIDE_3.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-82, 176, 18, 18, "+1", data -> changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m + 1, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.tooltips.appliedenergistics2.IncreaseByOne").setHoverTooltipText("gui.tooltips.appliedenergistics2.IncreaseByOneDesc").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-82, 194, 18, 18, "-1", data -> changePatternAmount(multiPatternSlots, multiPatternSlotGroup, m -> m - 1, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.tooltips.appliedenergistics2.DecreaseByOne").setHoverTooltipText("gui.tooltips.appliedenergistics2.DecreaseByOneDesc").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-64, 176, 36, 36, "X", data -> clearPatterns(multiPatternSlots, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("nae2.pattern_multiplier.unencode").setHoverTooltipText("nae2.pattern_multiplier.unencode.desc").setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            for (int i = 0; i < multiPatternSlots.getSlots(); i++) {
                final int index = i;
                multiPatternSlotGroup.addWidget(new AEPatternSlotWidget(multiPatternSlots, i, 18 * (i / 9), 18 * (i % 9))
                        .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                        .setActiveSupplier(() -> index / 9 <= multiUpgradeSlots.getSlotsFilled())
                        .setSlotLocationInfo(true, false)
                        .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                        .setWidgetGroup(multiPatternSlotGroup));
            }
            builder.widget(multiPatternSlotGroup);
        }
        return builder;
    }

    private static void writePatternMultiToolToNBT(IItemHandler itemHandler, NBTTagCompound compound) {
        final NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                final NBTTagCompound tagCompound = stack.serializeNBT();
                tagCompound.setInteger("Slot", i);
                tagList.appendTag(tagCompound);
            }
        }
        compound.setTag("Items", tagList);
    }

    private static void readPatternMultiToolNBT(IItemHandlerModifiable itemHandler, NBTTagList tagList) {
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound compound = tagList.getCompoundTagAt(i);
            if (compound.hasKey("Slot")) {
                final ItemStack patternStack = TJItemUtils.getItemStackFromName(compound.getString("id"), compound.getInteger("Count"), compound.getShort("Damage"));
                patternStack.setTagCompound(compound.getCompoundTag("tag"));
                itemHandler.setStackInSlot(compound.getInteger("Slot"), patternStack);
            }
        }
    }

    private static void changePatternAmount(IItemHandler patternSlots, SlotScrollableWidgetGroup patternSlotWidgets, LongUnaryOperator multiplier, Runnable callback) {
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            final ItemStack stack = patternSlots.getStackInSlot(i);
            final NBTTagCompound compound = stack.getTagCompound();
            if (stack.isEmpty() || compound == null) continue;
            final ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(stack.getItem());
            final String id = resourcelocation != null ? resourcelocation.toString() : "minecraft:air";
            final NBTTagList inputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Inputs" : "in", 10);
            final NBTTagList outputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Outputs" : "out", 10);
            final NBTTagList newInputList = new NBTTagList(), newOutputList = new NBTTagList();
            final Predicate<Boolean> setPatternInputs = simulate -> {
                for (int j = 0; j < inputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = inputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = multiplier.applyAsLong(amount);
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newInputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newInputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                for (int j = 0; j < outputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = outputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = multiplier.applyAsLong(amount);
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newOutputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newOutputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                if (!simulate) {
                    compound.setTag("in", newInputList);
                    compound.setTag("out", newOutputList);
                    if (id.equals("ae2fc:dense_encoded_pattern")) {
                        compound.setTag("Inputs", newInputList);
                        compound.setTag("Outputs", newOutputList);
                    }
                }
                return true;
            };
            if (setPatternInputs.test(true))
                setPatternInputs.test(false);
        }
        callback.run();
        patternSlotWidgets.getNativeWidgets().stream()
                .filter(widget -> widget instanceof TJSlotWidget<?>)
                .forEach(slot -> ((TJSlotWidget<?>) slot).forceUpdate());
    }

    private static void clearPatterns(IItemHandler patternSlots, Runnable callback) {
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            ItemStack pattern = patternSlots.extractItem(i, Integer.MAX_VALUE, false);
            pattern = Api.INSTANCE.definitions().materials().blankPattern().maybeStack(pattern.getCount()).orElse(ItemStack.EMPTY);
            patternSlots.insertItem(i, pattern, false);
        }
        callback.run();
    }
}
