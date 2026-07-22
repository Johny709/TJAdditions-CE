package tj.integration.ae2.items;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.Api;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.parts.PartFluidInterface;
import appeng.fluids.tile.TileFluidInterface;
import appeng.tile.storage.TileDrive;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.tile.TileDualInterface;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.WidgetTabBuilder;
import tj.integration.ae2.part.*;
import tj.integration.ae2.tile.*;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.impl.AEFluidListWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class ItemPartSuperFluidInterfaceTerminal extends Item implements IPartItem<IPart> {

    public ItemPartSuperFluidInterfaceTerminal() {}

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack itemStack) {
        return new PartSuperFluidInterfaceTerminal(itemStack);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, facing, player, hand, worldIn);
    }

    public static ModularUI createSuperFluidInterfaceTerminalGUI(IUIHolder holder, EntityPlayer player, IGridNode gridNode) {
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_FLUID_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.cell_terminal.name"))
                .widget(new WidgetTabBuilder()
                        .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                        .addTab("tj.multiblock.tab.config", Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY), tab -> createConfigTab(tab, gridNode))
                        .addTab("tj.multiblock.tab.storage", TJItemUtils.getItemStackFromName("minecraft:chest"), tab -> createStorageTab(tab, gridNode))
                        .build())
                .widget(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT))
                .widget(new AEFluidListWidget<IFluidInterfaceHost>(7, 34, 162, 162, gridNode, TileFluidInterface.class, TileDualInterface.class, TileSuperFluidInterface.class, TileSuperDualInterface.class, TileStockingFluidInterface.class, TileStockingDualInterface.class, TileSuperUltimateInterface.class,
                        PartFluidInterface.class, PartDualInterface.class, PartSuperFluidInterface.class, PartSuperDualInterface.class, PartStockingFluidInterface.class, PartStockingDualInterface.class, PartSuperUltimateInterface.class)
                        .setFluidTankSupplier(iFluidInterfaceHost -> iFluidInterfaceHost.getDualityFluidInterface().getTanks())
                        .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                        .setSlotPredicate((s, iFluidInterfaceHost) -> true)
                        .setRenderCallback(ItemPartSuperFluidInterfaceTerminal::renderCallback)
                        .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                        .setPredicate(iFluidInterfaceHost -> true))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private static void createConfigTab(List<Widget> tab, IGridNode gridNode) {
        tab.add(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT));
        tab.add(new AEFluidListWidget<IFluidInterfaceHost>(7, 34, 162, 162, gridNode, TileFluidInterface.class, TileDualInterface.class, TileSuperFluidInterface.class, TileSuperDualInterface.class, TileStockingFluidInterface.class, TileStockingDualInterface.class, TileSuperUltimateInterface.class,
                PartFluidInterface.class, PartDualInterface.class, PartSuperFluidInterface.class, PartSuperDualInterface.class, PartStockingFluidInterface.class, PartStockingDualInterface.class, PartSuperUltimateInterface.class)
                .setFluidTankSupplier(iFluidInterfaceHost -> iFluidInterfaceHost.getDualityFluidInterface().getConfig())
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setSlotPredicate((s, iFluidInterfaceHost) -> true)
                .setRenderCallback(ItemPartSuperFluidInterfaceTerminal::renderCallback)
                .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                .setPredicate(iFluidInterfaceHost -> true));
    }

    private static void createStorageTab(List<Widget> tab, IGridNode gridNode) {
        tab.add(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT));
        tab.add(new AEFluidListWidget<IFluidInterfaceHost>(7, 34, 162, 162, gridNode, TileDrive.class)
                .setFluidTankSupplier(iFluidInterfaceHost -> iFluidInterfaceHost.getDualityFluidInterface().getTanks())
                .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                .setSlotPredicate((s, iFluidInterfaceHost) -> true)
                .setRenderCallback(ItemPartSuperFluidInterfaceTerminal::renderCallback)
                .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                .setPredicate(iFluidInterfaceHost -> true));
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
