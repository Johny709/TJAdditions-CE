package tj.integration.ae2.items;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.core.Api;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.parts.misc.PartInterface;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.tile.misc.TileInterface;
import baubles.api.BaublesApi;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.tile.TileDualInterface;
import gregtech.api.gui.*;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.builder.WidgetTabBuilder;
import tj.integration.ae2.ISuperInterfaceTerminal;
import tj.integration.ae2.part.*;
import tj.integration.ae2.tile.*;
import tj.items.handlers.FilteredItemStackHandler;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.impl.*;
import tj.util.TJItemUtils;
import tj.util.references.BooleanReference;
import tj.util.references.ObjectReference;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class ItemWirelessSuperInterfaceTerminal extends ToolWirelessTerminal implements ItemUIFactory, ISuperInterfaceTerminal {

    private static final BlockPos.MutableBlockPos molecularAssemblerPos = new BlockPos.MutableBlockPos();

    @Override
    public boolean canHandle(ItemStack is) {
        return TJItems.WIRELESS_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY).isItemEqual(is);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        IGridNode gridNode = null;
        if (itemStack.getItem() instanceof ToolWirelessTerminal) {
            final String encryptionKey = ((ToolWirelessTerminal) itemStack.getItem()).getEncryptionKey(itemStack);
            if (encryptionKey.isEmpty()) {
                if (player.getEntityWorld().isRemote)
                    player.sendMessage(PlayerMessages.DeviceNotLinked.get());
                return ModularUI.defaultBuilder().widget(new LabelWidget(40, 40, PlayerMessages.DeviceNotLinked.name())).build(holder, player);
            }
            ILocatable securityStation = null;
            try {
                final long encKey = Long.parseLong(encryptionKey);
                securityStation = AEApi.instance().registries().locatable().getLocatableBy(encKey);
            } catch (NumberFormatException ignored) {}
            if (securityStation == null) {
                if (player.getEntityWorld().isRemote)
                    player.sendMessage(PlayerMessages.StationCanNotBeLocated.get());
                return ModularUI.defaultBuilder().widget(new LabelWidget(40, 40, PlayerMessages.StationCanNotBeLocated.name())).build(holder, player);
            }
            if (securityStation instanceof IActionHost) {
                gridNode = ((IActionHost) securityStation).getActionableNode();
            }
        }
        return ItemWirelessSuperInterfaceTerminal.createWirelessSuperInterfaceGUI(holder, player, gridNode, this);
    }

    @Override
    public void setItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget, LongUnaryOperator unaryOperator) {
        final ItemStack itemStack = ghostItemListWidget.getItemAt(ghostItemListWidget.getSelectedIndex());
        if (itemStack.isEmpty()) return;
        final ItemStack newStack = itemStack.copy();
        newStack.setCount((int) Math.max(1, Math.min(Integer.MAX_VALUE, unaryOperator.applyAsLong(itemStack.getCount()))));
        ghostItemListWidget.setItemAt(ghostItemListWidget.getSelectedIndex(), newStack);
    }

    @Override
    public int getItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget) {
        return ghostItemListWidget.getItemAt(ghostItemListWidget.getSelectedIndex()).getCount();
    }

    public static ModularUI createWirelessSuperInterfaceGUI(IUIHolder holder, EntityPlayer player, IGridNode gridNode, ISuperInterfaceTerminal superInterfaceTerminal) {
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
        final ModularUI.Builder builder = ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 316);
        final AEItemListWidget<IInterfaceHost> aeItemListPattern = new AEItemListWidget<>(7, 60, 162, 162, gridNode, TileInterface.class, TileDualInterface.class, TileSuperInterface.class, TileSuperDualInterface.class, TilePatternInterface.class, TileSuperUltimateInterface.class,
                PartInterface.class, PartDualInterface.class, PartSuperInterface.class, PartSuperDualInterface.class, PartPatternInterface.class, PartSuperUltimateInterface.class);
        return createPatternMultiToolGUI(builder, patternMultiTool, multiUpgradeSlots, multiPatternSlots, invTag, aeItemListPattern)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.super_interface_terminal.name"))
                .widget(new WidgetTabBuilder()
                        .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                        .addTab("gui.appliedenergistics2.Patterns", Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY), tab -> createPatternTab(tab, aeItemListPattern, multiPatternSlots))
                        .addTab("tj.multiblock.tab.config", Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY), tab -> createConfigTab(tab, gridNode, superInterfaceTerminal))
                        .addTab("tj.multiblock.tab.storage", TJItemUtils.getItemStackFromName("minecraft:chest"), tab -> createStorageTab(tab, gridNode)).build())
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

    private static void createPatternTab(List<Widget> tab, AEItemListWidget<IInterfaceHost> aeItemListWidget, FilteredItemStackHandler multiPatternSlots) {
        final BooleanReference validPatterns = new BooleanReference();
        final BooleanReference showInterfaces = new BooleanReference();
        final BooleanReference showCraftingInterfaces = new BooleanReference();
        final ObjectReference<String> searchInputs = new ObjectReference<>("");
        final ObjectReference<String> searchOutputs = new ObjectReference<>("");
        final ObjectReference<String> searchInterface = new ObjectReference<>("");
        tab.add(new TJToggleButtonWidget(-18, 88, 16, 16, TJGuiTextures.TOGGLE_INTERFACE_TERMINAL, validPatterns::isValue, validPatterns::setValue)
                .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowOnlyInvalidInterfaceOffDesc", "gui.tooltips.appliedenergistics2.ToggleShowOnlyInvalidInterfaceOnDesc")
                .setToggleTitleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowOnlyInvalidInterface", "gui.tooltips.appliedenergistics2.ToggleShowOnlyInvalidInterface"));
        tab.add(new TJToggleButtonWidget(-18, 108, 16, 16, TJGuiTextures.TOGGLE_SHOW_INTERFACES, showInterfaces::isValue, showInterfaces::setValue)
                .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowFullInterfacesOnDesc", "gui.tooltips.appliedenergistics2.ToggleShowFullInterfacesOffDesc")
                .setToggleTitleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowFullInterfaces", "gui.tooltips.appliedenergistics2.ToggleShowFullInterfaces"));
        tab.add(new TJToggleButtonWidget(-18, 128, 16, 16, TJGuiTextures.TOGGLE_CRAFTING_INTERFACES, showCraftingInterfaces::isValue, showCraftingInterfaces::setValue)
                .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleMolecularAssemblersOffDesc", "gui.tooltips.appliedenergistics2.ToggleMolecularAssemblersOnDesc")
                .setToggleTitleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleMolecularAssemblers", "gui.tooltips.appliedenergistics2.ToggleMolecularAssemblers"));
        tab.add(new NewTextFieldWidget<>(7, 16, 90, 12, true, searchInputs::getValue, (s, id) -> searchInputs.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(new NewTextFieldWidget<>(7, 30, 90, 12, true, searchOutputs::getValue, (s, id) -> searchOutputs.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldOutputs")
                .setUpdateOnTyping(true));
        tab.add(new NewTextFieldWidget<>(7, 44, 90, 12, true, searchInterface::getValue, (s, id) -> searchInterface.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldNames")
                .setUpdateOnTyping(true));
        tab.add(new ImageWidget(6, 59, 164, 164, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(aeItemListWidget.setSlotPredicate((slot, interfaceHost) -> slot / 9 <= interfaceHost.getInterfaceDuality().getInstalledUpgrades(Upgrades.PATTERN_EXPANSION))
                .setItemStackTransfer((itemStack, simulate) -> TJItemUtils.insertIntoItemHandler(multiPatternSlots, itemStack, simulate))
                .setInventorySupplier(interfaceHost -> interfaceHost.getInterfaceDuality().getPatterns())
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                .setRenderCallback(ItemWirelessSuperInterfaceTerminal::renderCallback)
                .setPredicate(interfaceHost -> {
                    if (interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) != YesNo.YES)
                        return false;
                    if (validPatterns.isValue() && isPatternValid(interfaceHost.getInterfaceDuality().getPatterns())) {
                        return false;
                    } else if (showInterfaces.isValue() && TJItemUtils.areSlotsFull(interfaceHost.getInterfaceDuality().getPatterns(), 0, Math.min(interfaceHost.getInterfaceDuality().getPatterns().getSlots(), interfaceHost.getInterfaceDuality().getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) * 9))) {
                        return false;
                    } else if (showCraftingInterfaces.isValue() && !checkAroundForMolecularAssemblers(interfaceHost)) {
                        return false;
                    } else if (!searchInputs.getValue().isEmpty() && isItemNotInPattern(interfaceHost.getInterfaceDuality().getPatterns(), searchInputs.getValue(), false)) {
                        return false;
                    } else if (!searchOutputs.getValue().isEmpty() && isItemNotInPattern(interfaceHost.getInterfaceDuality().getPatterns(), searchOutputs.getValue(), true)) {
                        return false;
                    } else return searchInterface.getValue().isEmpty() || ((ICustomNameObject) interfaceHost).getCustomInventoryName().contains(searchInterface.getValue());
                }));
    }

    private static void createConfigTab(List<Widget> tab, IGridNode gridNode, ISuperInterfaceTerminal superInterfaceTerminal) {
        final BooleanReference showInterfaces = new BooleanReference();
        final ObjectReference<String> searchName = new ObjectReference<>("");
        final AEGhostItemListWidget<IInterfaceHost> aeItemListConfig = new AEGhostItemListWidget<>(7, 36, 162, 186, gridNode, TileInterface.class, TileDualInterface.class, TileSuperInterface.class, TileSuperDualInterface.class, TileStockingInterface.class, TileStockingDualInterface.class, TileSuperUltimateInterface.class,
                PartInterface.class, PartDualInterface.class, PartSuperInterface.class, PartSuperDualInterface.class, PartStockingInterface.class, PartStockingDualInterface.class, PartSuperUltimateInterface.class);
        tab.add(new TJToggleButtonWidget(-18, 88, 16, 16, TJGuiTextures.TOGGLE_SHOW_INTERFACES, showInterfaces::isValue, showInterfaces::setValue)
                .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowFullInterfacesOnDesc", "gui.tooltips.appliedenergistics2.ToggleShowFullInterfacesOffDesc")
                .setToggleTitleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowFullInterfaces", "gui.tooltips.appliedenergistics2.ToggleShowFullInterfaces"));
        tab.add(new NewTextFieldWidget<>(7, 16, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(new ImageWidget(6, 35, 164, 188, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(aeItemListConfig.setInventorySupplier(interfaceHost -> interfaceHost.getInterfaceDuality().getConfig())
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setItemStackTransfer((itemStack, simulate) -> itemStack)
                .setScrollbar(10, 0, 12, 186, GuiTextures.SLOT)
                .setSlotPredicate((slot, interfaceHost) -> true)
                .setRenderCallback(ItemWirelessSuperInterfaceTerminal::renderCallback)
                .setPredicate(interfaceHost -> {
                    if (interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) != YesNo.YES)
                        return false;
                    if (showInterfaces.isValue() && TJItemUtils.areSlotsFull(interfaceHost.getInterfaceDuality().getConfig(), 0, interfaceHost.getInterfaceDuality().getConfig().getSlots())) {
                        return false;
                    } else return searchName.getValue().isEmpty() || TJItemUtils.isItemPresent(interfaceHost.getInterfaceDuality().getConfig(), searchName.getValue());
                }));
        tab.add(new PopUpWidget<>().setClickToDefault(false)
                .setIndexSupplier(() -> aeItemListConfig.getSelectedIndex() >= 0 ? 1 : 0)
                .addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new ImageWidget(-167, 221, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                    widgetGroup.addWidget(new LabelWidget(-160, 226, "machine.universal.stack_size"));
                    widgetGroup.addWidget(new NewTextFieldWidget<>(-160, 267, 148, 18, true, () -> String.valueOf(superInterfaceTerminal.getItemStackSize(aeItemListConfig)), (text, id) -> {
                        final ItemStack itemStack = aeItemListConfig.getItemAt(aeItemListConfig.getSelectedIndex());
                        if (itemStack.isEmpty()) return;
                        final ItemStack newStack = itemStack.copy();
                        newStack.setCount((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                        aeItemListConfig.setItemAt(aeItemListConfig.getSelectedIndex(), newStack);
                    }).setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    widgetGroup.addWidget(new ButtonWidget<>(-159, 241, 25, 20, "+1", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 1)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-129, 241, 30, 20, "+10", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 10)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-94, 241, 35, 20, "+100", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 100)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-54, 241, 40, 20, "+1000", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 1000)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-159, 291, 25, 20, "-1", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 1)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-129, 291, 30, 20, "-10", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 10)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-94, 291, 35, 20, "-100", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 100)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-54, 291, 40, 20, "-1000", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 1000)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    return false;
                }));
    }

    private static void createStorageTab(List<Widget> tab, IGridNode gridNode) {
        final BooleanReference showInterfaces = new BooleanReference();
        final ObjectReference<String> searchName = new ObjectReference<>("");
        final AEItemListWidget<IInterfaceHost> aeItemListStorage = new AEItemListWidget<>(7, 36, 162, 186, gridNode, TileInterface.class, TileDualInterface.class, TileSuperInterface.class, TileSuperDualInterface.class, TileStockingInterface.class, TileStockingDualInterface.class, TilePatternInterface.class, TileSuperUltimateInterface.class,
                PartInterface.class, PartDualInterface.class, PartSuperInterface.class, PartSuperDualInterface.class, PartStockingInterface.class, PartStockingDualInterface.class, PartPatternInterface.class, PartSuperUltimateInterface.class);
        tab.add(new TJToggleButtonWidget(-18, 88, 16, 16, TJGuiTextures.TOGGLE_SHOW_INTERFACES, showInterfaces::isValue, showInterfaces::setValue)
                .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowFullInterfacesOnDesc", "gui.tooltips.appliedenergistics2.ToggleShowFullInterfacesOffDesc")
                .setToggleTitleTooltipHoverText("gui.tooltips.appliedenergistics2.ToggleShowFullInterfaces", "gui.tooltips.appliedenergistics2.ToggleShowFullInterfaces"));
        tab.add(new NewTextFieldWidget<>(7, 16, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(new ImageWidget(6, 35, 164, 188, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(aeItemListStorage.setInventorySupplier(interfaceHost -> interfaceHost.getInterfaceDuality().getStorage())
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setItemStackTransfer((itemStack, simulate) -> itemStack)
                .setScrollbar(10, 0, 12, 186, GuiTextures.SLOT)
                .setSlotPredicate((slot, interfaceHost) -> true)
                .setRenderCallback(ItemWirelessSuperInterfaceTerminal::renderCallback)
                .setPredicate(interfaceHost -> {
                    if (interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) != YesNo.YES)
                        return false;
                    if (showInterfaces.isValue() && TJItemUtils.areSlotsFull(interfaceHost.getInterfaceDuality().getStorage(), 0, interfaceHost.getInterfaceDuality().getStorage().getSlots())) {
                        return false;
                    } else return searchName.getValue().isEmpty() || TJItemUtils.isItemPresent(interfaceHost.getInterfaceDuality().getStorage(), searchName.getValue());
                }));
    }

    private static boolean isPatternValid(IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack itemStack = itemHandler.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            final NBTTagList inputList = TJItemUtils.getCompoundFromStack(itemStack).getTagList("in", 10);
            if (inputList.isEmpty())
                return false;
            final NBTTagList outputList = TJItemUtils.getCompoundFromStack(itemStack).getTagList("out", 10);
            if (outputList.isEmpty())
                return false;
            for (int j = 0; j < inputList.tagCount(); j++) {
                final NBTTagCompound tagCompound = inputList.getCompoundTagAt(j);
                final String id = tagCompound.getString("id");
                if (id.equals("ae2fc:fluid_drop")) {
                    if (FluidRegistry.getFluid(tagCompound.getCompoundTag("tag").getString("Fluid")) == null)
                        return false;
                } else if (TJItemUtils.getItemStackFromName(id, 1, tagCompound.getShort("Damage")).isEmpty())
                    return false;
            }
            for (int j = 0; j < outputList.tagCount(); j++) {
                final NBTTagCompound tagCompound = outputList.getCompoundTagAt(j);
                final String id = tagCompound.getString("id");
                if (id.equals("ae2fc:fluid_drop")) {
                    if (FluidRegistry.getFluid(tagCompound.getCompoundTag("tag").getString("Fluid")) == null)
                        return false;
                } else if (TJItemUtils.getItemStackFromName(id, 1, tagCompound.getShort("Damage")).isEmpty())
                    return false;
            }
        }
        return true;
    }

    private static boolean checkAroundForMolecularAssemblers(IInterfaceHost host) {
        final TileEntity tileEntity = host.getTile();
        final BlockPos pos = host.getTile().getPos();
        final EnumSet<EnumFacing> facings = host.getTargets();
        for (EnumFacing facing : facings) {
            molecularAssemblerPos.setPos(pos.getX(), pos.getY(), pos.getZ());
            if (tileEntity.getWorld().getTileEntity(molecularAssemblerPos.move(facing, 1)) instanceof TileMolecularAssembler)
                return true;
        }
        return false;
    }

    private static boolean isItemNotInPattern(IItemHandler itemHandler, String name, boolean output) {
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
                    return false;
            }
        }
        return true;
    }

    private static void renderCallback(ItemStack itemStack, int x, int y) {
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

    private static ModularUI.Builder createPatternMultiToolGUI(ModularUI.Builder builder, ItemStack patternMultiTool, FilteredItemStackHandler multiUpgradeSlots, IItemHandler multiPatternSlots, NBTTagCompound invTag, AEItemListWidget<?> aeItemListWidget) {
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
