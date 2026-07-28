package tj.integration.ae2.items;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.core.Api;
import appeng.core.localization.PlayerMessages;
import appeng.fluids.parts.PartFluidStorageBus;
import appeng.items.tools.powered.ToolWirelessTerminal;
import gregtech.api.gui.*;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.integration.ae2.ISuperFluidInterfaceTerminal;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.impl.*;
import tj.util.TJItemUtils;
import tj.util.references.ObjectReference;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.LongUnaryOperator;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class ItemWirelessFluidStorageBusTerminal extends ToolWirelessTerminal implements ItemUIFactory, ISuperFluidInterfaceTerminal {

    private static final BlockPos.MutableBlockPos storageBusPos = new BlockPos.MutableBlockPos();

    @Override
    public boolean canHandle(ItemStack is) {
        return TJItems.WIRELESS_FLUID_STORAGE_BUS_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY).isItemEqual(is);
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
        return ItemWirelessFluidStorageBusTerminal.createFluidStorageBusTerminalGUI(holder, player, gridNode, this);
    }

    @Override
    public void setFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget, LongUnaryOperator unaryOperator) {
        final FluidStack fluidStack = ghostFluidListWidget.getFluidAt(ghostFluidListWidget.getSelectedIndex());
        if (fluidStack == null) return;
        final FluidStack newStack = fluidStack.copy();
        newStack.amount = (int) Math.max(1, Math.min(Integer.MAX_VALUE, unaryOperator.applyAsLong(fluidStack.amount)));
        ghostFluidListWidget.setFluidAt(ghostFluidListWidget.getSelectedIndex(), newStack);
    }

    @Override
    public int getFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget) {
        final FluidStack fluidStack = ghostFluidListWidget.getFluidAt(ghostFluidListWidget.getSelectedIndex());
        return fluidStack != null ? fluidStack.amount : 0;
    }

    public static ModularUI createFluidStorageBusTerminalGUI(IUIHolder holder, EntityPlayer player, IGridNode gridNode, ISuperFluidInterfaceTerminal superInterfaceTerminal) {
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_STORAGE_BUS_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.fluid_storage_bus_terminal.name"))
                .widget(new ImageWidget(-22, 0, 4, 55, GuiTextures.BORDERED_BACKGROUND)) // to move JEI GUI out of the way for tabs
                .widget(new WidgetTabBuilder()
                        .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                        .addTab("tj.multiblock.tab.config", Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY), tab -> createConfigTab(tab, gridNode, superInterfaceTerminal))
                        .addTab("tj.multiblock.tab.storage", TJItemUtils.getItemStackFromName("minecraft:chest"), tab -> createStorageTab(tab, gridNode))
                        .build())
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private static void createConfigTab(List<Widget> tab, IGridNode gridNode, ISuperFluidInterfaceTerminal superFluidInterfaceTerminal) {
        final ObjectReference<String> searchName = new ObjectReference<>("");
        final AEGhostFluidListWidget<PartFluidStorageBus> ghostFluidListWidget = new AEGhostFluidListWidget<>(7, 34, 162, 162, gridNode, PartFluidStorageBus.class);
        tab.add(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.popMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(new NewTextFieldWidget<>(7, 16, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(ghostFluidListWidget.setSlotPredicate((slot, storageBus) -> slot / 9 <= (storageBus.getInstalledUpgrades(Upgrades.CAPACITY) + 1))
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setRenderCallback(ItemWirelessFluidStorageBusTerminal::renderCallback)
                .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                .setFluidTankSupplier(PartFluidStorageBus::getConfig)
                .setPredicate(storageBus -> true));
        tab.add(new PopUpWidget<>().setClickToDefault(false)
                .setIndexSupplier(() -> ghostFluidListWidget.getSelectedIndex() >= 0 ? 1 : 0)
                .addPopup(widgetGroup -> true)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new ImageWidget(-167, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                    widgetGroup.addWidget(new LabelWidget(-160, 112, "machine.universal.stack_size"));
                    widgetGroup.addWidget(new NewTextFieldWidget<>(-160, 153, 148, 18, true, () -> String.valueOf(superFluidInterfaceTerminal.getFluidStackSize(ghostFluidListWidget)), (text, id) -> {
                        final FluidStack fluidStack = ghostFluidListWidget.getFluidAt(ghostFluidListWidget.getSelectedIndex());
                        if (fluidStack == null) return;
                        final FluidStack newStack = fluidStack.copy();
                        newStack.amount = (int) Math.max(0, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
                        ghostFluidListWidget.setFluidAt(ghostFluidListWidget.getSelectedIndex(), newStack);
                    }).setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    widgetGroup.addWidget(new ButtonWidget<>(-159, 127, 25, 20, "+1", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount + 1)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-129, 127, 30, 20, "+10", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount + 10)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-94, 127, 35, 20, "+100", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount + 100)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-54, 127, 40, 20, "+1000", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount + 1000)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-159, 177, 25, 20, "-1", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount - 1)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-129, 177, 30, 20, "-10", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount - 10)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-94, 177, 35, 20, "-100", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount - 100)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-54, 177, 40, 20, "-1000", data -> superFluidInterfaceTerminal.setFluidStackSize(ghostFluidListWidget, amount -> amount - 1000)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    return false;
                }));
    }

    private static void createStorageTab(List<Widget> tab, IGridNode gridNode) {
        final ObjectReference<String> searchName = new ObjectReference<>("");
        tab.add(new NewTextFieldWidget<>(7, 16, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.popMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(new AEFluidListWidget<PartFluidStorageBus>(7, 34, 162, 162, gridNode, PartFluidStorageBus.class)
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setFluidTankSupplier(ItemWirelessFluidStorageBusTerminal::getFluidInventory)
                .setRenderCallback(ItemWirelessFluidStorageBusTerminal::renderCallback)
                .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                .setSlotPredicate((s, storageBus) -> true)
                .setPredicate(storageBus -> true));
    }

    private static IFluidHandler getFluidInventory(PartFluidStorageBus storageBus) {
        final BlockPos pos = storageBus.getTile().getPos();
        storageBusPos.setPos(pos.getX(), pos.getY(), pos.getZ());
        final TileEntity tileEntity = storageBus.getTile().getWorld().getTileEntity(storageBusPos.move(storageBus.getSide().getFacing()));
        if (tileEntity != null) {
            final IFluidHandler fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, storageBus.getSide().getFacing().getOpposite());
            if (fluidHandler != null)
                return fluidHandler;
        }
        return TJValues.DUMMY_FLUID_HANDLER;
    }

    private static void renderCallback(FluidStack fluidStack, int x, int y) {
        if (fluidStack != null && fluidStack.amount > 0) {
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            GlStateManager.disableBlend();
            TJGuiUtils.drawFluidForGui(fluidStack, fluidStack.amount, fluidStack.amount, x + 1, y + 1, 18 - 1, 18 - 2);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            final String s = TextFormattingUtil.formatLongToCompactString(fluidStack.amount, 4) + "L";
            fontRenderer.drawStringWithShadow(s, (x + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + 18 - 6) * 2, 0xFFFFFF);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
    }
}
