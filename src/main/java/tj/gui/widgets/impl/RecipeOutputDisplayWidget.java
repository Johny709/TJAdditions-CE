package tj.gui.widgets.impl;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RecipeOutputDisplayWidget extends WidgetGroup {

    private Supplier<List<ItemStack>> itemOutputSupplier;
    private Supplier<List<FluidStack>> fluidOutputSupplier;
    private List<ItemStack> itemOutputs = new ArrayList<>();
    private List<FluidStack> fluidOutputs = new ArrayList<>();
    private Size tooltipSize;

    public RecipeOutputDisplayWidget(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    public RecipeOutputDisplayWidget setItemOutputSupplier(Supplier<List<ItemStack>> itemOutputSupplier) {
        this.itemOutputSupplier = itemOutputSupplier;
        return this;
    }

    public RecipeOutputDisplayWidget setFluidOutputSupplier(Supplier<List<FluidStack>> fluidOutputSupplier) {
        this.fluidOutputSupplier = fluidOutputSupplier;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (this.tooltipSize == null || !this.isMouseOverElement(mouseX, mouseY)) return;
        int slot = 0;
        int offsetX = 3;
        int offsetY = 15;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        TJGuiTextures.TOOLTIP_BOX.draw(mouseX, mouseY, this.tooltipSize.getWidth() + 7, this.tooltipSize.getHeight() + 7);
        this.drawStringSized(I18n.format("machine.universal.producing"), mouseX + 4, mouseY + 4, 0xFFFFFF, true, 1, false);
        for (ItemStack stack : this.itemOutputs) {
            if (slot++ > 8) {
                offsetX = 3;
                offsetY += 18;
            }
            GuiTextures.SLOT.draw(mouseX + offsetX, mouseY + offsetY, 18, 18);
            Widget.drawItemStack(stack, mouseX + offsetX + 1, mouseY + offsetY + 1, null);
            offsetX += 18;
        }
        for (FluidStack stack : this.fluidOutputs) {
            if (slot++ > 8) {
                offsetX = 3;
                offsetY += 18;
            }
            GuiTextures.FLUID_SLOT.draw(mouseX + offsetX, mouseY + offsetY, 18, 18);
            GlStateManager.disableBlend();
            TJGuiUtils.drawFluidForGui(stack, stack.amount, stack.amount, mouseX + offsetX + 1, mouseY + offsetY + 1, 17, 17);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            String s = TextFormattingUtil.formatLongToCompactString(stack.amount, 4) + "L";
            fontRenderer.drawStringWithShadow(s, (mouseX + offsetX + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (mouseY + offsetY + 14) * 2, 0xFFFFFF);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            offsetX += 18;
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        List<ItemStack> itemStacks;
        if (this.itemOutputSupplier != null && (itemStacks = this.itemOutputSupplier.get()) != null) {
            boolean equal = true;
            if (this.itemOutputs != null && this.itemOutputs.size() == itemStacks.size()) {
                for (int i = 0; i < this.itemOutputs.size(); i++) {
                    if (!ItemHandlerHelper.canItemStacksStackRelaxed(this.itemOutputs.get(i), itemStacks.get(i))) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.itemOutputs = itemStacks;
                this.writeUpdateInfo(2, buffer -> {
                    buffer.writeInt(this.itemOutputs.size());
                    for (ItemStack stack : this.itemOutputs)
                        buffer.writeItemStack(stack);
                });
            }
        }
        List<FluidStack> fluidStacks;
        if (this.fluidOutputSupplier != null && (fluidStacks = this.fluidOutputSupplier.get()) != null) {
            boolean equal = true;
            if (this.fluidOutputs != null && this.fluidOutputs.size() == fluidStacks.size()) {
                for (int i = 0; i < this.fluidOutputs.size(); i++) {
                    if (!FluidStack.areFluidStackTagsEqual(this.fluidOutputs.get(i), fluidStacks.get(i))) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.fluidOutputs = fluidStacks;
                this.writeUpdateInfo(3, buffer -> {
                    buffer.writeInt(this.fluidOutputs.size());
                    for (FluidStack stack : this.fluidOutputs)
                        buffer.writeCompoundTag(stack.writeToNBT(new NBTTagCompound()));
                });
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            this.itemOutputs.clear();
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                try {
                    this.itemOutputs.add(buffer.readItemStack());
                } catch (IOException e) {
                    GTLog.logger.info(e.getMessage());
                }
            }
        } else if (id == 3) {
            this.fluidOutputs.clear();
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                try {
                    this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                } catch (IOException e) {
                    GTLog.logger.info(e.getMessage());
                }
            }
        }
        this.formatTooltip();
    }

    @SideOnly(Side.CLIENT)
    private void formatTooltip() {
        int slot = 0;
        int totalHeight = 29;
        int widthApplied = 0;
        int totalWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(I18n.format("machine.universal.producing"));
        for (ItemStack stack : this.itemOutputs) {
            if (slot++ > 8) {
                slot = 0;
                widthApplied = 0;
                totalHeight += 18;
            } else totalWidth = Math.max(totalWidth, widthApplied += 18);
        }
        for (FluidStack stack : this.fluidOutputs) {
            if (slot++ > 8) {
                slot = 0;
                widthApplied = 0;
                totalHeight += 18;
            } else totalWidth = Math.max(totalWidth, widthApplied += 18);
        }
        this.tooltipSize = new Size(totalWidth, totalHeight);
    }
}
