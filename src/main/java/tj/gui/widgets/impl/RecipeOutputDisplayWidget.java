package tj.gui.widgets.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
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
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.ItemStackHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RecipeOutputDisplayWidget extends WidgetGroup {

    private Supplier<List<ItemStack>> itemOutputSupplier;
    private Supplier<List<FluidStack>> fluidOutputSupplier;
    private Supplier<IItemHandlerModifiable>  itemHandlerSupplier;
    private Supplier<IMultipleTankHandler> fluidTanksSupplier;
    private IItemHandlerModifiable itemHandler;
    private IMultipleTankHandler fluidTanks;
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

    public RecipeOutputDisplayWidget setItemHandlerSupplier(Supplier<IItemHandlerModifiable> itemHandlerSupplier) {
        this.itemHandlerSupplier = itemHandlerSupplier;
        return this;
    }

    public RecipeOutputDisplayWidget setFluidTanksSupplier(Supplier<IMultipleTankHandler> fluidTanksSupplier) {
        this.fluidTanksSupplier = fluidTanksSupplier;
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
            if (slot++ > 4) {
                offsetX = 3;
                offsetY += 18;
            }
            GuiTextures.SLOT.draw(mouseX + offsetX, mouseY + offsetY, 18, 18);
            Widget.drawItemStack(stack, mouseX + offsetX + 1, mouseY + offsetY + 1, null);
            offsetX += 18;
        }
        for (FluidStack stack : this.fluidOutputs) {
            if (slot++ > 4) {
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
        IItemHandlerModifiable itemHandler;
        if (this.itemHandlerSupplier != null && (itemHandler = this.itemHandlerSupplier.get()) != null) {
            boolean equal = true;
            if (this.itemHandler != null && this.itemHandler.getSlots() == itemHandler.getSlots()) {
                for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                    if (this.itemHandler.getSlotLimit(i) != itemHandler.getSlotLimit(i)) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.itemHandler = itemHandler;
                this.writeUpdateInfo(2, buffer -> {
                    buffer.writeInt(this.itemHandler.getSlots());
                    for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                        buffer.writeInt(this.itemHandler.getSlotLimit(i));
                    }
                });
            }
        }
        IMultipleTankHandler fluidTanks;
        if (this.fluidTanksSupplier != null && (fluidTanks = this.fluidTanksSupplier.get()) != null) {
            boolean equal = true;
            if (this.fluidTanks != null && this.fluidTanks.getTanks() == fluidTanks.getTanks()) {
                for (int i = 0; i < this.fluidTanks.getTanks(); i++) {
                    if (this.fluidTanks.getTankAt(i).getCapacity() != fluidTanks.getTankAt(i).getCapacity()) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.fluidTanks = fluidTanks;
                this.writeUpdateInfo(3, buffer -> {
                    buffer.writeInt(this.fluidTanks.getTanks());
                    for (int i = 0; i < this.fluidTanks.getTanks(); i++) {
                        buffer.writeInt(this.fluidTanks.getTankAt(i).getCapacity());
                    }
                });
            }
        }
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
                this.writeUpdateInfo(4, buffer -> {
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
                this.writeUpdateInfo(5, buffer -> {
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
        switch (id) {
            case 2:
                List<IItemHandler> itemHandlers = new ArrayList<>();
                int size = buffer.readInt();
                for (int i = 0; i < size; i++) {
                    itemHandlers.add(new LargeItemStackHandler(1, buffer.readInt()));
                }
                this.itemHandler = new ItemHandlerList(itemHandlers);
                break;
            case 3:
                List<IFluidTank> fluidTanks = new ArrayList<>();
                int size1 = buffer.readInt();
                for (int i = 0; i < size1; i++) {
                    fluidTanks.add(new FluidTank(buffer.readInt()));
                }
                this.fluidTanks = new FluidTankList(true, fluidTanks);
                break;
            case 4:
                this.itemOutputs.clear();
                int size2 = buffer.readInt();
                for (int i = 0; i < size2; i++) {
                    try {
                        this.itemOutputs.add(buffer.readItemStack());
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                }
                if (this.itemHandler == null) break;
                for (int i = 0; i < this.itemHandler.getSlots(); i++)
                    this.itemHandler.extractItem(i, Integer.MAX_VALUE, false);
                for (ItemStack stack : this.itemOutputs)
                    ItemStackHelper.insertIntoItemHandler(this.itemHandler, stack, false);
                this.formatTooltip();
                break;
            case 5:
                this.fluidOutputs.clear();
                int size3 = buffer.readInt();
                for (int i = 0; i < size3; i++) {
                    try {
                        this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                }
                if (this.fluidTanks == null) break;
                for (int i = 0; i < this.fluidTanks.getTanks(); i++)
                    this.fluidTanks.getTankAt(i).drain(Integer.MAX_VALUE, true);
                for (FluidStack stack : this.fluidOutputs)
                    this.fluidTanks.fill(stack, true);
                this.formatTooltip();
                break;
        }
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

    public ItemStack getItemAt(int index) {
        if (this.itemHandler == null || this.itemHandler.getSlots() <= index)
            return null;
        return this.itemHandler.getStackInSlot(index);
    }

    public FluidStack getFluidAt(int index) {
        if (this.fluidTanks == null || this.fluidTanks.getTanks() <= index)
            return null;
        return this.fluidTanks.getTankAt(index).getFluid();
    }
}
