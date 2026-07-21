package tj.integration.ae2.items;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.AEBaseInvTile;
import appeng.tile.storage.TileDrive;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.impl.AEItemListWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;

public class ItemWirelessCellTerminal extends ToolWirelessTerminal implements ItemUIFactory {

    @Override
    public boolean canHandle(ItemStack is) {
        return TJItems.WIRELESS_CELL_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY).isItemEqual(is);
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
        return ItemWirelessCellTerminal.createWirelessCellTerminal(holder, player, gridNode);
    }

    public static ModularUI createWirelessCellTerminal(IUIHolder holder, EntityPlayer player, IGridNode gridNode) {
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.cell_terminal.name"))
                .widget(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT))
                .widget(new AEItemListWidget<IChestOrDrive>(7, 34, 162, 162, gridNode, TileDrive.class)
                        .setInventorySupplier(drive -> ((AEBaseInvTile) drive).getInternalInventory())
                        .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                        .setItemStackTransfer((itemStack, aBoolean) -> itemStack)
                        .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                        .setSlotPredicate((s, iChestOrDrive) -> true)
                        .setPredicate(iChestOrDrive -> true)
                        .setRenderCallback(ItemWirelessCellTerminal::renderCallback))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
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
