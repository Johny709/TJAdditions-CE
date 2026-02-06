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
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.ItemStackHelper;
import tj.util.TJFluidUtils;
import tj.util.references.BooleanReference;
import tj.util.references.IntegerReference;
import tj.util.references.ObjectReference;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class RecipeOutputDisplayWidget extends Widget {

    private final Int2ObjectMap<ItemStack> itemInputIndex = new Int2ObjectOpenHashMap<>(), itemOutputIndex = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<FluidStack> fluidInputIndex = new Int2ObjectOpenHashMap<>(), fluidOutputIndex = new Int2ObjectOpenHashMap<>();
    private Supplier<IItemHandlerModifiable> itemInputInventorySupplier, itemOutputInventorySupplier;
    private Supplier<IMultipleTankHandler> fluidInputTankSupplier, fluidOutputTankSupplier;
    private IItemHandlerModifiable itemInputInventory, itemOutputInventory;
    private IMultipleTankHandler fluidInputTanks, fluidOutputTanks;
    private List<ItemStack> itemOutputs = new ArrayList<>();
    private List<FluidStack> fluidOutputs = new ArrayList<>();
    private Supplier<List<ItemStack>> itemOutputSupplier;
    private Supplier<List<FluidStack>> fluidOutputSupplier;
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

    public RecipeOutputDisplayWidget setItemInputInventorySupplier(Supplier<IItemHandlerModifiable> itemInputInventorySupplier) {
        this.itemInputInventorySupplier = itemInputInventorySupplier;
        return this;
    }

    public RecipeOutputDisplayWidget setItemOutputInventorySupplier(Supplier<IItemHandlerModifiable> itemOutputInventorySupplier) {
        this.itemOutputInventorySupplier = itemOutputInventorySupplier;
        return this;
    }

    public RecipeOutputDisplayWidget setFluidInputTankSupplier(Supplier<IMultipleTankHandler> fluidInputTankSupplier) {
        this.fluidInputTankSupplier = fluidInputTankSupplier;
        return this;
    }

    public RecipeOutputDisplayWidget setFluidOutputTankSupplier(Supplier<IMultipleTankHandler> fluidOutputTankSupplier) {
        this.fluidOutputTankSupplier = fluidOutputTankSupplier;
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
        if (this.itemInputInventorySupplier != null) {
            this.itemInputInventory = this.writeItemInventoryPacket(this.itemInputInventorySupplier.get(), this.itemInputInventory, 1);
        }
        if (this.itemOutputInventorySupplier != null) {
            this.itemOutputInventory = this.writeItemInventoryPacket(this.itemOutputInventorySupplier.get(), this.itemOutputInventory, 2);
        }
        if (this.fluidInputTankSupplier != null) {
            this.fluidInputTanks = this.writeFluidTanksPacket(this.fluidInputTankSupplier.get(), this.fluidInputTanks, 3);
        }
        if (this.fluidOutputTankSupplier != null) {
            this.fluidOutputTanks = this.writeFluidTanksPacket(this.fluidOutputTankSupplier.get(), this.fluidOutputTanks, 4);
        }
        if (this.itemOutputSupplier != null) {
            List<ItemStack> itemStacks = this.itemOutputSupplier.get();
            if (itemStacks != null) {
                this.itemOutputs = itemStacks;
                this.writeUpdateInfo(5, buffer -> {
                    buffer.writeInt(this.itemOutputs.size());
                    for (ItemStack stack : this.itemOutputs)
                        buffer.writeItemStack(stack);
                    this.writeItemContentsPacket(buffer, this.itemInputInventorySupplier);
                    this.writeItemContentsPacket(buffer, this.itemOutputInventorySupplier);
                });
            }
        }
        if (this.fluidOutputSupplier != null) {
            List<FluidStack> fluidStacks = this.fluidOutputSupplier.get();
            if (fluidStacks != null) {
                this.fluidOutputs = fluidStacks;
                this.writeUpdateInfo(6, buffer -> {
                    buffer.writeInt(this.fluidOutputs.size());
                    for (FluidStack stack : this.fluidOutputs)
                        buffer.writeCompoundTag(stack.writeToNBT(new NBTTagCompound()));
                    this.writeFluidContentsPacket(buffer, this.fluidInputTankSupplier);
                    this.writeFluidContentsPacket(buffer, this.fluidOutputTankSupplier);
                });
            }
        }
    }

    private IItemHandlerModifiable writeItemInventoryPacket(IItemHandlerModifiable itemHandler, IItemHandlerModifiable lastItemHandler, int id) {
        if (itemHandler != null) {
            boolean equal = true;
            if (lastItemHandler != null && lastItemHandler.getSlots() == itemHandler.getSlots()) {
                for (int i = 0; i < lastItemHandler.getSlots(); i++) {
                    if (lastItemHandler.getSlotLimit(i) != itemHandler.getSlotLimit(i)) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.writeUpdateInfo(id, buffer -> {
                    buffer.writeInt(itemHandler.getSlots());
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        buffer.writeInt(itemHandler.getSlotLimit(i));
                    }
                });
                return itemHandler;
            }
        }
        return null;
    }

    private IMultipleTankHandler writeFluidTanksPacket(IMultipleTankHandler fluidTanks, IMultipleTankHandler lastFluidTanks, int id) {
        if (fluidTanks != null) {
            boolean equal = true;
            if (lastFluidTanks != null && lastFluidTanks.getTanks() == fluidTanks.getTanks()) {
                for (int i = 0; i < lastFluidTanks.getTanks(); i++) {
                    IFluidTank tank = fluidTanks.getTankAt(i);
                    IFluidTank previousTank = lastFluidTanks.getTankAt(i);
                    if (previousTank.getCapacity() != tank.getCapacity()) {
                        equal = false;
                        break;
                    }
                }
            } else equal = false;
            if (!equal) {
                this.writeUpdateInfo(id, buffer -> {
                    buffer.writeInt(fluidTanks.getTanks());
                    for (int i = 0; i < fluidTanks.getTanks(); i++) {
                        buffer.writeInt(fluidTanks.getTankAt(i).getCapacity());
                    }
                });
                return fluidTanks;
            }
        }
        return null;
    }

    private void writeItemContentsPacket(PacketBuffer buffer, Supplier<IItemHandlerModifiable> itemInventorySupplier) {
        buffer.writeBoolean(itemInventorySupplier != null);
        if (itemInventorySupplier != null) {
            IItemHandlerModifiable itemHandlerModifiable = itemInventorySupplier.get();
            buffer.writeBoolean(itemHandlerModifiable != null);
            if (itemHandlerModifiable != null) {
                buffer.writeInt(itemHandlerModifiable.getSlots());
                for (int i = 0; i < itemHandlerModifiable.getSlots(); i++)
                    buffer.writeItemStack(itemHandlerModifiable.getStackInSlot(i));
            }
        }
    }

    private void writeFluidContentsPacket(PacketBuffer buffer, Supplier<IMultipleTankHandler> fluidTankSupplier) {
        buffer.writeBoolean(fluidTankSupplier != null);
        if (fluidTankSupplier != null) {
            IMultipleTankHandler tankHandler = fluidTankSupplier.get();
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
    }

    private void readItemContentsPacket(PacketBuffer buffer, Int2ObjectMap<ItemStack> itemIndex) {
        if (buffer.readBoolean() && buffer.readBoolean()) {
            int size6 = buffer.readInt();
            for (int i = 0; i < size6; i++) {
                try {
                    itemIndex.put(i, buffer.readItemStack());
                } catch (IOException e) {
                    GTLog.logger.info(e.getMessage());
                }
            }
        }
    }

    private void readFluidContentsPacket(PacketBuffer buffer, Int2ObjectMap<FluidStack> fluidIndex) {
        if (buffer.readBoolean() && buffer.readBoolean()) {
            int size8 = buffer.readInt();
            for (int i = 0; i < size8; i++) {
                if (buffer.readBoolean()) {
                    try {
                        fluidIndex.put(i, FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                }
            }
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
                this.itemInputInventory = new ItemHandlerList(itemHandlers);
                break;
            case 2:
                List<IItemHandler> itemHandlers1 = new ArrayList<>();
                int size1 = buffer.readInt();
                for (int i = 0; i < size1; i++)
                    itemHandlers1.add(new LargeItemStackHandler(1, buffer.readInt()));
                this.itemOutputInventory = new ItemHandlerList(itemHandlers1);
                break;
            case 3:
                List<IFluidTank> fluidTanks = new ArrayList<>();
                int size3 = buffer.readInt();
                for (int i = 0; i < size3; i++)
                    fluidTanks.add(new FluidTank(buffer.readInt()));
                this.fluidInputTanks = new FluidTankList(true, fluidTanks);
                break;
            case 4:
                List<IFluidTank> fluidTanks1 = new ArrayList<>();
                int size4 = buffer.readInt();
                for (int i = 0; i < size4; i++)
                    fluidTanks1.add(new FluidTank(buffer.readInt()));
                this.fluidOutputTanks = new FluidTankList(true, fluidTanks1);
                break;
            case 5:
                this.itemOutputs.clear();
                this.itemInputIndex.clear();
                this.itemOutputIndex.clear();
                int size5 = buffer.readInt();
                for (int i = 0; i < size5; i++) {
                    try {
                        this.itemOutputs.add(buffer.readItemStack());
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                }
                this.readItemContentsPacket(buffer, this.itemInputIndex);
                this.readItemContentsPacket(buffer, this.itemOutputIndex);
                if (this.itemInputInventory != null) for (int i = 0; i < this.itemInputInventory.getSlots(); i++) {
                    this.itemInputInventory.extractItem(i, Integer.MAX_VALUE, false);
                    this.itemInputInventory.insertItem(i, this.itemInputIndex.get(i), false);
                }
                if (this.itemOutputInventory != null) for (int i = 0; i < this.itemOutputInventory.getSlots(); i++) {
                    this.itemOutputInventory.extractItem(i, Integer.MAX_VALUE, false);
                    this.itemOutputInventory.insertItem(i, this.itemOutputIndex.get(i), false);
                }
                this.itemInputIndex.clear();
                this.itemOutputIndex.clear();
                for (ItemStack stack : this.itemOutputs) {
                    stack = stack.copy();
                    IntegerReference previousCount = new IntegerReference();
                    BooleanReference previouslyFilled = new BooleanReference();
                    ObjectReference<ItemStack> inserted = new ObjectReference<>();
                    stack = ItemStackHelper.insertIntoItemHandlerWithCallback(this.itemInputInventory, stack, false, (slot, itemStack) -> {
                        previouslyFilled.setValue(!this.itemInputInventory.getStackInSlot(slot).isEmpty());
                        previousCount.setValue(itemStack.getCount());
                        inserted.setValue(itemStack.copy());
                    }, (slot, itemStack) -> {
                        if (itemStack.getCount() < previousCount.getValue()) {
                            inserted.getValue().setCount(previouslyFilled.isValue() ? 0 : this.itemInputInventory.getStackInSlot(slot).getCount());
                            this.itemInputIndex.put(slot, inserted.getValue());
                        }
                    });
                    ItemStackHelper.insertIntoItemHandlerWithCallback(this.itemOutputInventory, stack, false, (slot, itemStack) -> {
                        previouslyFilled.setValue(!this.itemOutputInventory.getStackInSlot(slot).isEmpty());
                        previousCount.setValue(itemStack.getCount());
                        inserted.setValue(itemStack.copy());
                    }, (slot, itemStack) -> {
                        if (itemStack.getCount() < previousCount.getValue()) {
                            inserted.getValue().setCount(previouslyFilled.isValue() ? 0 : this.itemOutputInventory.getStackInSlot(slot).getCount());
                            this.itemOutputIndex.put(slot, inserted.getValue());
                        }
                    });
                }
                this.formatTooltip();
                break;
            case 6:
                this.fluidOutputs.clear();
                this.fluidInputIndex.clear();
                this.fluidOutputIndex.clear();
                int size7 = buffer.readInt();
                for (int i = 0; i < size7; i++) {
                    try {
                        this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                }
                this.readFluidContentsPacket(buffer, this.fluidInputIndex);
                this.readFluidContentsPacket(buffer, this.fluidOutputIndex);
                if (this.fluidInputTanks != null) for (int i = 0; i < this.fluidInputTanks.getTanks(); i++) {
                    this.fluidInputTanks.getTankAt(i).drain(Integer.MAX_VALUE, true);
                    this.fluidInputTanks.getTankAt(i).fill(this.fluidInputIndex.get(i), true);
                }
                if (this.fluidOutputTanks != null) for (int i = 0; i < this.fluidOutputTanks.getTanks(); i++) {
                    this.fluidOutputTanks.getTankAt(i).drain(Integer.MAX_VALUE, true);
                    this.fluidOutputTanks.getTankAt(i).fill(this.fluidOutputIndex.get(i), true);
                }
                this.fluidInputIndex.clear();
                this.fluidOutputIndex.clear();
                for (FluidStack stack : this.fluidOutputs) {
                    stack = stack.copy();
                    IntegerReference previousCount = new IntegerReference();
                    BooleanReference previouslyFilled = new BooleanReference();
                    ObjectReference<FluidStack> inserted = new ObjectReference<>();
                    stack = TJFluidUtils.fillIntoTanksWithCallback(this.fluidInputTanks, stack, true, (slot, fluidStack) -> {
                        previouslyFilled.setValue(this.fluidInputTanks.getTankAt(slot).getFluidAmount() > 0);
                        previousCount.setValue(fluidStack.amount);
                        inserted.setValue(fluidStack.copy());
                    }, (slot, fluidStack) -> {
                        if (fluidStack.amount < previousCount.getValue()) {
                            inserted.getValue().amount = previouslyFilled.isValue() ? 0 : this.fluidInputTanks.getTankAt(slot).getFluidAmount();
                            this.fluidInputIndex.put(slot, inserted.getValue());
                        }
                    });
                    TJFluidUtils.fillIntoTanksWithCallback(this.fluidOutputTanks, stack, true, (slot, fluidStack) -> {
                        previouslyFilled.setValue(this.fluidOutputTanks.getTankAt(slot).getFluidAmount() > 0);
                        previousCount.setValue(fluidStack.amount);
                        inserted.setValue(fluidStack.copy());
                    }, (slot, fluidStack) -> {
                        if (fluidStack.amount < previousCount.getValue()) {
                            inserted.getValue().amount = previouslyFilled.isValue() ? 0 : this.fluidOutputTanks.getTankAt(slot).getFluidAmount();
                            this.fluidOutputIndex.put(slot, inserted.getValue());
                        }
                    });
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

    public ItemStack getItemInputAt(int index) {
        return this.itemInputIndex.get(index);
    }

    public ItemStack getItemOutputAt(int index) {
        return this.itemOutputIndex.get(index);
    }

    public FluidStack getFluidInputAt(int index) {
        return this.fluidInputIndex.get(index);
    }

    public FluidStack getFluidOutputAt(int index) {
        return this.fluidOutputIndex.get(index);
    }
}
