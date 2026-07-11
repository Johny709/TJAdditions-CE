package tj.integration.ae2.part;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.AEBaseInvTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.storage.TileDrive;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEItemListWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.util.TJItemUtils;

public class PartCellTerminal extends PartInterfaceTerminal implements ITileEntityUI {

    public PartCellTerminal(ItemStack is) {
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
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.cell_terminal.name"))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new ImageWidget(6, 33, 164, 164, TJGuiTextures.BLANK_SLOT))
                .widget(new AEItemListWidget<IChestOrDrive>(7, 34, 162, 162, this.getGridNode(), TileDrive.class)
                        .setInventorySupplier(drive -> ((AEBaseInvTile) drive).getInternalInventory())
                        .setScrollSlider(1, 1, 10, 24, GuiTextures.BORDERED_BACKGROUND)
                        .setItemStackTransfer((itemStack, aBoolean) -> itemStack)
                        .setScrollbar(10, 0, 12, 162, GuiTextures.SLOT)
                        .setSlotPredicate((s, iChestOrDrive) -> true)
                        .setPredicate(iChestOrDrive -> true)
                        .setRenderCallback(this::renderCallback))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private void renderCallback(ItemStack itemStack, int x, int y) {
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
