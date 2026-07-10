package tj.integration.ae2.part;

import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.misc.TileInterface;
import appeng.tile.networking.TileCableBus;
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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
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
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.util.TJItemUtils;
import tj.util.references.ObjectReference;

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
        final NewTextFieldWidget<?> inputsTextField = new NewTextFieldWidget<>(7, 16, 90, 12, true, searchInputs::getValue, (s, id) -> searchInputs.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldInputs")
                .setUpdateOnTyping(true);
        final NewTextFieldWidget<?> outputsTextField = new NewTextFieldWidget<>(7, 30, 90, 12, true, searchOutputs::getValue, (s, id) -> searchOutputs.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldOutputs")
                .setUpdateOnTyping(true);
        final NewTextFieldWidget<?> interfaceTextField = new NewTextFieldWidget<>(7, 44, 90, 12, true, searchInterface::getValue, (s, id) -> searchInterface.setValue(s))
                .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                .setTooltipText("gui.tooltips.appliedenergistics2.SearchFieldNames")
                .setUpdateOnTyping(true);
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 316)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.super_interface_terminal.name"))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new ImageWidget(6, 59, 164, 164, TJGuiTextures.BLANK_SLOT))
                .widget(inputsTextField)
                .widget(outputsTextField)
                .widget(interfaceTextField)
                .widget(new AEItemListWidget<IInterfaceHost>(7, 60, 162, 162, this.getGridNode(), TileInterface.class, TileSuperInterface.class, TileSuperDualInterface.class, TilePatternInterface.class, TileSuperUltimateInterface.class)
                        .setSlotPredicate((slot, interfaceHost) -> slot / 9 <= interfaceHost.getInterfaceDuality().getInstalledUpgrades(Upgrades.PATTERN_EXPANSION))
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
                        })).bindPlayerInventory(player.inventory, 233)
                .build(holder, player);
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
}
