package tj.integration.ae2.part;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.helpers.IInterfaceHost;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.misc.TileInterface;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import tj.integration.ae2.tile.TilePatternInterface;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.integration.ae2.tile.TileSuperInterface;
import tj.integration.ae2.tile.TileSuperUltimateInterface;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEItemListWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.util.TJItemUtils;

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
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.super_interface_terminal.name"))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new AEItemListWidget<IInterfaceHost>(7, 34, 166, 162, this.getGridNode(), TileInterface.class, TileSuperInterface.class, TileSuperDualInterface.class, TilePatternInterface.class, TileSuperUltimateInterface.class)
                        .setPredicate(interfaceHost -> interfaceHost.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES)
                        .setInventorySupplier(interfaceHost -> interfaceHost.getInterfaceDuality().getPatterns())
                        .setRenderCallback(this::renderCallback))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    private void renderCallback(ItemStack itemStack, int x, int y) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(itemStack);
        final NBTTagList outputList = compound.getTagList("out", 10);
        final NBTTagCompound outputCompound = outputList.getCompoundTagAt(0);
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
}
