package tj.integration.ae2.items;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.core.Api;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.parts.misc.PartStorageBus;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tj.TJValues;
import tj.builder.WidgetTabBuilder;
import tj.integration.ae2.ISuperInterfaceTerminal;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.impl.AEGhostItemListWidget;
import tj.mui.widgets.impl.AEItemListWidget;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.util.TJItemUtils;
import tj.util.references.ObjectReference;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.LongUnaryOperator;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class ItemWirelessStorageBusTerminal extends ToolWirelessTerminal implements ItemUIFactory, ISuperInterfaceTerminal {

    private static final BlockPos.MutableBlockPos storageBusPos = new BlockPos.MutableBlockPos();

    @Override
    public boolean canHandle(ItemStack is) {
        return TJItems.WIRELESS_STORAGE_BUS_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY).isItemEqual(is);
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
        return ItemWirelessStorageBusTerminal.createWirelessStorageBusTerminalGUI(holder, player, gridNode, this);
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
        return ghostItemListWidget.getItemAt(ghostItemListWidget.getSelectedIndex(), false).getCount();
    }

    public static ModularUI createWirelessStorageBusTerminalGUI(IUIHolder holder, EntityPlayer player, IGridNode gridNode, ISuperInterfaceTerminal superInterfaceTerminal) {
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 300)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_STORAGE_BUS_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.storage_bus_terminal.name"))
                .widget(new ImageWidget(-22, 0, 4, 55, GuiTextures.BORDERED_BACKGROUND)) // to move JEI GUI out of the way for tabs
                .widget(new WidgetTabBuilder()
                        .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                        .addTab("tj.multiblock.tab.config", Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY), tab -> createConfigTab(tab, gridNode, superInterfaceTerminal))
                        .addTab("tj.multiblock.tab.storage", TJItemUtils.getItemStackFromName("minecraft:chest"), tab -> createStorageTab(tab, gridNode))
                        .build())
                .bindPlayerInventory(player.inventory, 217)
                .build(holder, player);
    }

    private static void createConfigTab(List<Widget> tab, IGridNode gridNode, ISuperInterfaceTerminal superInterfaceTerminal) {
        final ObjectReference<String> searchName = new ObjectReference<>("");
        final AEGhostItemListWidget<PartStorageBus> aeItemListConfig = new AEGhostItemListWidget<>(7, 22, 162, 190, gridNode, PartStorageBus.class);
        tab.add(new ImageWidget(6, 21, 164, 192, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(new NewTextFieldWidget<>(7, 6, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(aeItemListConfig.setPredicate(storageBus -> searchName.getValue().isEmpty() || TJItemUtils.isItemPresent(storageBus.getInventoryByName("config"), searchName.getValue()))
                .setSlotPredicate((slot, storageBus) -> slot / 9 <= (storageBus.getInstalledUpgrades(Upgrades.CAPACITY) + 1))
                .setInventorySupplier(storageBus -> storageBus.getInventoryByName("config"))
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setItemStackTransfer((itemStack, aBoolean) -> itemStack)
                .setRenderCallback(ItemWirelessStorageBusTerminal::renderCallback)
                .setScrollbar(10, 0, 12, 190, GuiTextures.SLOT));
        tab.add(new PopUpWidget<>().setClickToDefault(false)
                .setIndexSupplier(() -> aeItemListConfig.getSelectedIndex() >= 0 ? 1 : 0)
                .addPopup(widgetGroup -> true)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new ImageWidget(-167, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                    widgetGroup.addWidget(new LabelWidget(-160, 112, "machine.universal.stack_size"));
                    widgetGroup.addWidget(new NewTextFieldWidget<>(-160, 153, 148, 18, true, () -> String.valueOf(superInterfaceTerminal.getItemStackSize(aeItemListConfig)), (text, id) -> {
                        final ItemStack itemStack = aeItemListConfig.getItemAt(aeItemListConfig.getSelectedIndex());
                        if (itemStack.isEmpty()) return;
                        final ItemStack newStack = itemStack.copy();
                        newStack.setCount((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                        aeItemListConfig.setItemAt(aeItemListConfig.getSelectedIndex(), newStack);
                    }).setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    widgetGroup.addWidget(new ButtonWidget<>(-159, 127, 25, 20, "+1", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 1)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-129, 127, 30, 20, "+10", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 10)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-94, 127, 35, 20, "+100", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 100)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-54, 127, 40, 20, "+1000", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount + 1000)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-159, 177, 25, 20, "-1", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 1)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-129, 177, 30, 20, "-10", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 10)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-94, 177, 35, 20, "-100", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 100)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    widgetGroup.addWidget(new ButtonWidget<>(-54, 177, 40, 20, "-1000", data -> superInterfaceTerminal.setItemStackSize(aeItemListConfig, amount -> amount - 1000)).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                    return false;
                }));
    }

    private static void createStorageTab(List<Widget> tab, IGridNode gridNode) {
        final ObjectReference<String> searchName = new ObjectReference<>("");
        tab.add(new NewTextFieldWidget<>(7, 6, 90, 12, true, searchName::getValue, (s, id) -> searchName.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true));
        tab.add(new ImageWidget(6, 21, 164, 192, TJGuiTextures.BLANK_SLOT) {
            @Override
            @SideOnly(Side.CLIENT)
            public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                super.drawInBackground(mouseX, mouseY, context);
            }
        });
        tab.add(new AEItemListWidget<PartStorageBus>(7, 22, 162, 190, gridNode, PartStorageBus.class)
                .setPredicate(storageBus -> searchName.getValue().isEmpty() || TJItemUtils.isItemPresent(getInventory(storageBus), searchName.getValue()))
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setItemStackTransfer((itemStack, aBoolean) -> itemStack)
                .setInventorySupplier(ItemWirelessStorageBusTerminal::getInventory)
                .setScrollbar(10, 0, 12, 190, GuiTextures.SLOT)
                .setRenderCallback(ItemWirelessStorageBusTerminal::renderCallback)
                .setSlotPredicate((s, storageBus) -> true));
    }

    private static IItemHandler getInventory(PartStorageBus storageBus) {
        final BlockPos pos = storageBus.getTile().getPos();
        storageBusPos.setPos(pos.getX(), pos.getY(), pos.getZ());
        final TileEntity tileEntity = storageBus.getTile().getWorld().getTileEntity(storageBusPos.move(storageBus.getSide().getFacing()));
        if (tileEntity != null) {
            final IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, storageBus.getSide().getFacing().getOpposite());
            if (itemHandler != null)
                return itemHandler;
        }
        return TJValues.DUMMY_ITEM_HANDLER;
    }

    private static void renderCallback(ItemStack itemStack, int x, int y) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(itemStack);
        final NBTTagCompound outputCompound = compound.getCompoundTag("#0");
        final String id = outputCompound.getString("id");
        ItemStack output;
        FluidStack fluidOutput = null;
        long count;
        if (id.isEmpty()) {
            output = ItemStack.EMPTY;
            if (outputCompound.hasKey("FluidName"))
                fluidOutput = new FluidStack(FluidRegistry.getFluid(outputCompound.getString("FluidName")), 1);
        } else output = TJItemUtils.getItemStackFromName(id, 1, outputCompound.getShort("Damage"));
        count = outputCompound.getLong("Cnt");
        if (count < 1)
            count = outputCompound.getInteger("Count");
        if (output.isEmpty() && fluidOutput == null) {
            final NBTTagCompound partition = compound.getCompoundTag("list").getTagList("Items", 10).getCompoundTagAt(0);
            final String partitionId = partition.getString("id");
            if (partitionId.equals("appliedenergistics2:dummy_fluid_item")) {
                fluidOutput = new FluidStack(FluidRegistry.getFluid(partition.getCompoundTag("tag").getString("FluidName")), 1);
                count = -1;
            } else {
                output = TJItemUtils.getItemStackFromName(partitionId, 1, partition.getShort("Damage"));
                if (!output.isEmpty())
                    count = -1;
            }
        }
        GlStateManager.disableBlend();
        if (!output.isEmpty()) {
            Widget.drawItemStack(output, x + 1, y + 1, null);
        } else if (fluidOutput != null) {
            TJGuiUtils.drawFluidForGui(fluidOutput, Math.max(1, count), Math.max(1, count), x + 1, y + 1, 17, 17);
        } else Widget.drawItemStack(itemStack, x + 1, y + 1, null);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = count < 0 ? "§eP" : TextFormattingUtil.formatLongToCompactString(count, 4) + (fluidOutput != null ? "L" : "");
        fontRenderer.drawStringWithShadow(s, (x + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + 12) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }
}
