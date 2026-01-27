package tj.gui.widgets.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.util.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.ItemStackHelper;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class RecipeOutputDisplayWidget extends Widget {

    private final Int2ObjectMap<ItemStack> itemOutputIndex = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<FluidStack> fluidOutputIndex = new Int2ObjectOpenHashMap<>();
    private Supplier<List<ItemStack>> itemOutputSupplier;
    private Supplier<List<FluidStack>> fluidOutputSupplier;
    private Supplier<IItemHandlerModifiable>  itemHandlerSupplier;
    private Supplier<IMultipleTankHandler> fluidTanksSupplier;
    private List<ItemStack> itemOutputs = new ArrayList<>();
    private List<FluidStack> fluidOutputs = new ArrayList<>();
    private IItemHandlerModifiable itemHandler;
    private IMultipleTankHandler fluidTanks;
    private Size tooltipSize;
    private int lastX;
    private int lastY;

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
        int x = this.isShiftDown() ? this.lastX : mouseX;
        int y = this.isShiftDown() ? this.lastY : mouseY;
        if (this.tooltipSize == null || !this.isMouseOverElement(x, y)) return;
        int slot = 0;
        int offsetX = 3;
        int offsetY = 15;
        int screenWidth = Minecraft.getMinecraft().displayWidth;
        int screenHeight = Minecraft.getMinecraft().displayHeight;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        TJGuiTextures.TOOLTIP_BOX.draw(x, y, this.tooltipSize.getWidth() + 7, this.tooltipSize.getHeight() + 7);
        this.drawStringSized(I18n.format("machine.universal.producing"), x + 4, y + 4, 0xFFFFFF, true, 1, false);
        for (ItemStack stack : this.itemOutputs) {
            if (slot++ > 4) {
                offsetX = 3;
                offsetY += 18;
            }
            GuiTextures.SLOT.draw(x + offsetX, y + offsetY, 18, 18);
            Widget.drawItemStack(stack, x + offsetX + 1, y + offsetY + 1, null);
            if (new Rectangle(x + offsetX, y + offsetY, 18, 18).contains(mouseX, mouseY)) {
                GuiUtils.drawHoveringText(getItemToolTip(stack), mouseX, mouseY, screenWidth, screenHeight, 100, fontRenderer);
            }
            offsetX += 18;
        }
        for (FluidStack stack : this.fluidOutputs) {
            if (slot++ > 4) {
                offsetX = 3;
                offsetY += 18;
            }
            GuiTextures.FLUID_SLOT.draw(x + offsetX, y + offsetY, 18, 18);
            GlStateManager.disableBlend();
            TJGuiUtils.drawFluidForGui(stack, stack.amount, stack.amount, x + offsetX + 1, y + offsetY + 1, 17, 17);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            String s = TextFormattingUtil.formatLongToCompactString(stack.amount, 4) + "L";
            fontRenderer.drawStringWithShadow(s, (x + offsetX + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + offsetY + 14) * 2, 0xFFFFFF);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            if (new Rectangle(x + offsetX, y + offsetY, 18, 18).contains(mouseX, mouseY)) {
                String formula = FluidTooltipUtil.getFluidTooltip(stack);
                formula = formula == null || formula.isEmpty() ? "" : "\n" + formula;
                GuiUtils.drawHoveringText(Collections.singletonList(formula), mouseX, mouseY, screenWidth, screenHeight, 100, fontRenderer);
            }
            offsetX += 18;
        }
        if (!this.isShiftDown()) {
            this.lastX = mouseX;
            this.lastY = mouseY;
        }
    }

    @Override
    public void detectAndSendChanges() {
        IItemHandlerModifiable itemHandler;
        if (this.itemHandlerSupplier != null && (itemHandler = this.itemHandlerSupplier.get()) != null) {
            boolean equal = true;
            if (this.itemHandler != null && this.itemHandler.getSlots() == itemHandler.getSlots()) {
                for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                    if (this.itemHandler.getSlotLimit(i) != itemHandler.getSlotLimit(i) && !ItemHandlerHelper.canItemStacksStackRelaxed(this.itemHandler.getStackInSlot(i), itemHandler.getStackInSlot(i))) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.itemHandler = itemHandler;
                this.writeUpdateInfo(1, buffer -> {
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
                    IFluidTank tank = fluidTanks.getTankAt(i);
                    IFluidTank previousTank = this.fluidTanks.getTankAt(i);
                    if (previousTank.getCapacity() != tank.getCapacity() && !FluidStack.areFluidStackTagsEqual(previousTank.getFluid(), tank.getFluid())) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.fluidTanks = fluidTanks;
                this.writeUpdateInfo(2, buffer -> {
                    buffer.writeInt(this.fluidTanks.getTanks());
                    for (int i = 0; i < this.fluidTanks.getTanks(); i++) {
                        buffer.writeInt(this.fluidTanks.getTankAt(i).getCapacity());
                    }
                });
            }
        }
        List<ItemStack> itemStacks;
        if (this.itemOutputSupplier != null && (itemStacks = this.itemOutputSupplier.get()) != null) {
            this.itemOutputs = itemStacks;
            this.writeUpdateInfo(3, buffer -> {
                buffer.writeInt(this.itemOutputs.size());
                for (ItemStack stack : this.itemOutputs)
                        buffer.writeItemStack(stack);
                buffer.writeBoolean(this.itemHandlerSupplier != null);
                if (this.itemOutputSupplier != null) {
                    IItemHandlerModifiable itemHandlerModifiable = this.itemHandlerSupplier.get();
                    buffer.writeBoolean(itemHandlerModifiable != null);
                    if (itemHandlerModifiable != null) {
                        buffer.writeInt(itemHandlerModifiable.getSlots());
                        for (int i = 0; i < itemHandlerModifiable.getSlots(); i++)
                            buffer.writeItemStack(itemHandlerModifiable.getStackInSlot(i));
                    }
                }
            });
        }
        List<FluidStack> fluidStacks;
        if (this.fluidOutputSupplier != null && (fluidStacks = this.fluidOutputSupplier.get()) != null) {
            this.fluidOutputs = fluidStacks;
            this.writeUpdateInfo(4, buffer -> {
                buffer.writeInt(this.fluidOutputs.size());
                for (FluidStack stack : this.fluidOutputs)
                    buffer.writeCompoundTag(stack.writeToNBT(new NBTTagCompound()));
                buffer.writeBoolean(this.fluidOutputSupplier != null);
                if (this.fluidOutputSupplier != null) {
                    IMultipleTankHandler tankHandler = this.fluidTanksSupplier.get();
                    buffer.writeBoolean(tankHandler != null);
                    if (tankHandler != null) {
                        buffer.writeInt(tankHandler.getTanks());
                        for (int i = 0; i < tankHandler.getTanks(); i++) {
                            FluidStack stack = tankHandler.getTankAt(i).getFluid();
                            buffer.writeBoolean(stack != null);
                            if (stack != null)
                                buffer.writeCompoundTag(stack.writeToNBT(new NBTTagCompound()));
                        }
                    }
                }
            });
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        switch (id) {
            case 1:
                List<IItemHandler> itemHandlers = new ArrayList<>();
                int size = buffer.readInt();
                for (int i = 0; i < size; i++)
                    itemHandlers.add(new LargeItemStackHandler(1, buffer.readInt()));
                this.itemHandler = new ItemHandlerList(itemHandlers);
                break;
            case 2:
                List<IFluidTank> fluidTanks = new ArrayList<>();
                int size1 = buffer.readInt();
                for (int i = 0; i < size1; i++)
                    fluidTanks.add(new FluidTank(buffer.readInt()));
                this.fluidTanks = new FluidTankList(true, fluidTanks);
                break;
            case 3:
                this.itemOutputs.clear();
                this.itemOutputIndex.clear();
                int size2 = buffer.readInt();
                for (int i = 0; i < size2; i++) {
                    try {
                        this.itemOutputs.add(buffer.readItemStack());
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                }
                if (buffer.readBoolean() && buffer.readBoolean()) {
                    int size4 = buffer.readInt();
                    for (int i = 0; i < size4; i++) {
                        try {
                            this.itemOutputIndex.put(i, buffer.readItemStack());
                        } catch (IOException e) {
                            GTLog.logger.info(e.getMessage());
                        }
                    }
                }
                if (this.itemHandler == null) break;
                for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                    this.itemHandler.extractItem(i, Integer.MAX_VALUE, false);
                    this.itemHandler.insertItem(i, this.itemOutputIndex.get(i), false);
                }
                this.itemOutputIndex.clear();
                for (ItemStack stack : this.itemOutputs) {
                    stack = stack.copy();
                    AtomicBoolean previouslyFilled = new AtomicBoolean();
                    AtomicReference<ItemStack> inserted = new AtomicReference<>();
                    ItemStackHelper.insertIntoItemHandlerWithCallback(this.itemHandler, stack, false, (slot, itemStack) -> {
                        inserted.set(itemStack.copy());
                        previouslyFilled.set(!this.itemHandler.getStackInSlot(slot).isEmpty());
                    }, (slot, itemStack) -> {
                        ItemStack slotStack = this.itemHandler.getStackInSlot(slot);
                        inserted.get().setCount(previouslyFilled.get() ? 0 : slotStack.getCount());
                        if (slotStack.getCount() != this.itemHandler.getSlotLimit(slot))
                            this.itemOutputIndex.put(slot, inserted.get());
                    });
                }
                this.formatTooltip();
                break;
            case 4:
                this.fluidOutputs.clear();
                this.fluidOutputIndex.clear();
                int size3 = buffer.readInt();
                for (int i = 0; i < size3; i++) {
                    try {
                        this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                }
                if (buffer.readBoolean() && buffer.readBoolean()) {
                    int size4 = buffer.readInt();
                    for (int i = 0; i < size4; i++) {
                        if (buffer.readBoolean()) {
                            try {
                                this.fluidOutputIndex.put(i, FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                            } catch (IOException e) {
                                GTLog.logger.info(e.getMessage());
                            }
                        }
                    }
                }
                if (this.fluidTanks == null) break;
                for (int i = 0; i < this.fluidTanks.getTanks(); i++) {
                    this.fluidTanks.getTankAt(i).drain(Integer.MAX_VALUE, true);
                    this.fluidTanks.getTankAt(i).fill(this.fluidOutputIndex.get(i), true);
                }
                this.fluidOutputIndex.clear();
                for (FluidStack stack : this.fluidOutputs) {
                    stack = stack.copy();
                    int amount = stack.amount;
                    for (int i = 0; i < this.fluidTanks.getTanks(); i++) {
                        IFluidTank tank = this.fluidTanks.getTankAt(i);
                        boolean previouslyFilled = tank.getFluidAmount() > 0;
                        FluidStack inserted = stack.copy();
                        int filled = tank.fill(stack, true);
                        inserted.amount = previouslyFilled ? 0 : amount;
                        amount -= filled;
                        if (tank.getFluidAmount() != tank.getCapacity())
                            this.fluidOutputIndex.put(i, inserted);
                        if (amount < 1) break;
                    }
                }
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
        for (int i = 0; i < this.itemOutputs.size(); i++) {
            if (slot++ > 8) {
                slot = 0;
                widthApplied = 0;
                totalHeight += 18;
            } else totalWidth = Math.max(totalWidth, widthApplied += 18);
        }
        for (int i = 0; i < this.fluidOutputs.size(); i++) {
            if (slot++ > 8) {
                slot = 0;
                widthApplied = 0;
                totalHeight += 18;
            } else totalWidth = Math.max(totalWidth, widthApplied += 18);
        }
        this.tooltipSize = new Size(totalWidth, totalHeight);
    }

    public ItemStack getItemAt(int index) {
        return this.itemOutputIndex.get(index);
    }

    public FluidStack getFluidAt(int index) {
        return this.fluidOutputIndex.get(index);
    }
}
