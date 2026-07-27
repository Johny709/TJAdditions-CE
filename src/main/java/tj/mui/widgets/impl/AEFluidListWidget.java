package tj.mui.widgets.impl;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.render.BlockPosHighlighter;
import appeng.core.localization.PlayerMessages;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.ICustomNameObject;
import appeng.util.BlockPosUtils;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.input.Keyboard;
import tj.TJ;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.TJWidget;
import tj.util.TJItemUtils;
import tj.util.predicates.IntBiPredicate;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AEFluidListWidget<T> extends TJWidget<AEFluidListWidget<T>> implements IIngredientSlot {

    protected static final FluidStack EMPTY_FLUID = Materials.Air.getFluid(0);
    protected final Int2ObjectMap<Object> elements = new Int2ObjectLinkedOpenHashMap<>();
    protected final Class<? extends IGridHost>[] gridHosts;
    protected final IGrid grid;
    protected final int posX;
    protected TriConsumer<FluidStack, Integer, Integer> renderCallback;
    protected Function<T, IFluidHandler> fluidTankSupplier;
    protected IntBiPredicate<T> slotPredicate;
    protected Predicate<T> predicate;
    protected TextureArea scrollSliderTexture;
    protected TextureArea scrollBarTexture;
    protected Rectangle scrollSliderRec;
    protected Rectangle scrollBarRec;
    protected int scrollBarXOffset;
    protected int scrollOffset;
    protected int scrollHeight;
    protected int autoScrollY;
    protected boolean autoScroll;
    protected boolean initialized;

    @SafeVarargs
    public AEFluidListWidget(int x, int y, int width, int height, IGridNode gridNode, Class<? extends IGridHost>... gridHosts) {
        super(new Position(x, y), new Size(width, height));
        this.grid = gridNode != null ? gridNode.getGrid() : null;
        this.posX = x;
        this.gridHosts = gridHosts;
        this.setScrollbar(0, y, 18, height, GuiTextures.SLOT);
    }

    public AEFluidListWidget<T> setScrollbar(int x, int y, int width, int height, TextureArea scrollbarTexture) {
        this.scrollBarRec = new Rectangle(x, y, width, height);
        this.scrollBarTexture = scrollbarTexture;
        this.scrollBarXOffset = x;
        return this;
    }

    public AEFluidListWidget<T> setScrollSlider(int x, int y, int width, int height, TextureArea scrollSliderTexture) {
        this.scrollSliderRec = new Rectangle(x, y, width, height);
        this.scrollSliderTexture = scrollSliderTexture;
        return this;
    }

    public AEFluidListWidget<T> setFluidTankSupplier(Function<T, IFluidHandler> fluidTankSupplier) {
        this.fluidTankSupplier = fluidTankSupplier;
        return this;
    }

    public AEFluidListWidget<T> setSlotPredicate(IntBiPredicate<T> slotPredicate) {
        this.slotPredicate = slotPredicate;
        return this;
    }

    public AEFluidListWidget<T> setPredicate(Predicate<T> predicate) {
        this.predicate = predicate;
        return this;
    }

    public AEFluidListWidget<T> setRenderCallback(TriConsumer<FluidStack, Integer, Integer> renderCallback) {
        this.renderCallback = renderCallback;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY)) return;
        final Position pos = this.getPosition();
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int scrollOffset = 0;
        int slotColumn = 0;
        int slotXOffset = 0;
        for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
            if (entry.getValue() instanceof FluidStack) {
                if (slotColumn > 8) {
                    slotColumn = 0;
                    scrollOffset += 18;
                    slotXOffset = 0;
                }
                final int x = pos.getX() + slotXOffset;
                final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                final FluidStack fluidStack = (FluidStack) entry.getValue();
                if (fluidStack != null && isMouseOver(x, y, 18, 18, mouseX, mouseY)) {
                    final List<String> tooltips = new ArrayList<>();
                    if (fluidStack.amount > 0) {
                        tooltips.add(fluidStack.getLocalizedName());
                        // Add chemical formula tooltip
                        final String formula = FluidTooltipUtil.getFluidTooltip(fluidStack);
                        tooltips.add(formula == null || formula.isEmpty() ? "" : "§7" + formula);
                    }
                    tooltips.add(I18n.format("gregtech.fluid.amount", fluidStack.amount, fluidStack.amount));
                    this.drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
                }
                slotXOffset += 18;
                slotColumn++;
            } else {
                if (entry.getIntKey() > 0)
                    scrollOffset += 18;
                final int len = fontRenderer.getStringWidth(entry.getValue().toString()) + 8;
                if (isMouseOver(pos.getX(), pos.getY() + 1 + scrollOffset - (this.scrollOffset % 18), len, 16, mouseX, mouseY))
                    this.drawHoveringText(ItemStack.EMPTY, Collections.singletonList(I18n.format("gui.tooltips.appliedenergistics2.HighlightInterface")), -1, mouseX, mouseY);
                scrollOffset += 18;
                slotXOffset = 0;
                slotColumn = 0;
            }
        }
        if (this.autoScroll) {
            int scroll = mouseY - this.autoScrollY;
            if (scroll > 5 || scroll < -5) {
                scroll -= scroll < 0 ? 5 : -5;
                this.addScrollOffset(scroll);
                if (scroll > 0)
                    TJGuiTextures.AUTOSCROLL_DOWN.draw(mouseX - 8, mouseY - 8, 16, 16);
                else TJGuiTextures.AUTOSCROLL_UP.draw(mouseX - 8, mouseY - 8, 16, 16);
            } else TJGuiTextures.AUTOSCROLL.draw(mouseX - 8, mouseY - 8, 16, 16);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        if (!this.initialized) {
            this.initialized = true;
            this.scrollBarRec = new Rectangle(this.scrollBarRec.x + pos.getX() + size.getWidth(), this.scrollBarRec.y + pos.getY(), this.scrollBarRec.width, this.scrollBarRec.height);
            this.scrollSliderRec = new Rectangle(this.scrollBarRec.x + this.scrollSliderRec.x, this.scrollBarRec.y + this.scrollSliderRec.y, this.scrollSliderRec.width, this.scrollSliderRec.height);
        }
        RenderUtil.useScissor(pos.getX(), pos.getY(), size.getWidth(), size.getHeight(), () -> {
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            int scrollOffset = 0;
            int slotColumn = 0;
            int slotXOffset = 0;
            for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
                if (entry.getValue() instanceof FluidStack) {
                    if (slotColumn > 8) {
                        slotColumn = 0;
                        scrollOffset += 18;
                        slotXOffset = 0;
                    }
                    final int x = pos.getX() + slotXOffset;
                    final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                    final FluidStack fluidStack = (FluidStack) entry.getValue();
                    this.drawSlot(entry.getIntKey(), x, y, mouseX, mouseY, fluidStack);
                    slotXOffset += 18;
                    slotColumn++;
                } else {
                    if (entry.getIntKey() > 0)
                        scrollOffset += 18;
                    final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18) + 1;
                    final int len = fontRenderer.getStringWidth(entry.getValue().toString()) + 8;
                    GuiTextures.BORDERED_BACKGROUND.draw(pos.getX(), y, len, 16);
                    this.drawStringSized(entry.getValue().toString(), pos.getX() + 4, y + 4, 0xAAAAAA, true, 1, false);
                    if (this.isMouseOverElement(mouseX, mouseY) && isMouseOver(pos.getX(), y, len, 16, mouseX, mouseY))
                        drawSelectionOverlay(pos.getX(), y, len, 16);
                    scrollOffset += 18;
                    slotXOffset = 0;
                    slotColumn = 0;
                }
            }
        });
        if (this.scrollBarTexture != null)
            this.scrollBarTexture.draw(this.scrollBarRec.x, this.scrollBarRec.y, this.scrollBarRec.width, this.scrollBarRec.height);
        if (this.scrollSliderTexture == null) return;
        final double heightDiff = (double) this.scrollBarRec.height / this.scrollHeight;
        final int scrollOffset = this.scrollSliderRec.y + (int) Math.round(this.scrollOffset * heightDiff);
        final int sliderY = Math.max(this.scrollBarRec.y, Math.min(this.scrollBarRec.y + this.scrollBarRec.height - this.scrollSliderRec.height, scrollOffset));
        this.scrollSliderTexture.draw(this.scrollSliderRec.x, sliderY, this.scrollSliderRec.width, this.scrollSliderRec.height);
    }

    @SideOnly(Side.CLIENT)
    protected void drawSlot(int index, int x, int y, int mouseX, int mouseY, FluidStack fluidStack) {
        GuiTextures.SLOT.draw(x, y, 18, 18);
        if (fluidStack != null)
            this.renderCallback.accept(fluidStack, x, y);
        if (this.isMouseOverElement(mouseX, mouseY) && isMouseOver(x, y, 18, 18, mouseX, mouseY))
            drawSelectionOverlay(x + 1, y + 1, 16, 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            final Position pos = this.getPosition();
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            if (this.autoScroll) {
                this.autoScroll = false;
            } else if (button == 2) {
                this.autoScrollY = mouseY;
                this.autoScroll = true;
            }
            int scrollOffset = 0;
            int slotColumn = 0;
            int slotXOffset = 0;
            for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
                if (entry.getValue() instanceof FluidStack) {
                    if (slotColumn > 8) {
                        slotColumn = 0;
                        scrollOffset += 18;
                        slotXOffset = 0;
                    }
                    final int x = pos.getX() + slotXOffset;
                    final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                    if (isMouseOver(x, y, 18, 18, mouseX, mouseY)) {
                        final boolean shiftClick = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                        this.writeClientAction(2, buffer -> {
                            buffer.writeBoolean(true); // isSlot is true.
                            buffer.writeBoolean(ConfigHolder.newTankFilling);
                            buffer.writeBoolean(shiftClick);
                            buffer.writeInt(entry.getIntKey());
                            buffer.writeInt(button);
                        });
                        return true;
                    }
                    slotXOffset += 18;
                    slotColumn++;
                } else {
                    if (entry.getIntKey() > 0)
                        scrollOffset += 18;
                    final int len = fontRenderer.getStringWidth(entry.getValue().toString()) + 8;
                    if (isMouseOver(pos.getX(), pos.getY() + 4 + scrollOffset - (this.scrollOffset % 18), len, 16, mouseX, mouseY)) {
                        final boolean shiftClick = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                        this.playButtonClickSound();
                        this.writeClientAction(2, buffer -> {
                            buffer.writeBoolean(false); // isSlot is false.
                            buffer.writeBoolean(ConfigHolder.newTankFilling);
                            buffer.writeBoolean(shiftClick);
                            buffer.writeInt(entry.getIntKey());
                            buffer.writeInt(button);
                        });
                        return true;
                    }
                    scrollOffset += 18;
                    slotXOffset = 0;
                    slotColumn = 0;
                }
            }
        } else if (this.scrollBarRec.contains(mouseX, mouseY)) {
            final double heightDiff = (double) this.scrollBarRec.height / this.scrollHeight;
            this.scrollOffset = (int) Math.max(0, (mouseY - this.scrollBarRec.y) / heightDiff);
            this.writeClientAction(1, buffer -> buffer.writeInt(this.scrollOffset));
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if ((button == 0 || button == 1) && this.scrollBarRec.contains(mouseX, mouseY)) {
            final double heightDiff = (double) this.scrollBarRec.height / this.scrollHeight;
            this.scrollOffset = (int) Math.max(0, (mouseY - this.scrollBarRec.y) / heightDiff);
            this.writeClientAction(1, buffer -> buffer.writeInt(this.scrollOffset));
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseInWidget(mouseX, mouseY))
            this.addScrollOffset(MathHelper.clamp(wheelDelta, -1, 1) * -18);
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        try {
            if (id == 1) {
                this.scrollHeight = buffer.readInt();
            } else if (id == 2) {
                this.elements.clear();
                final int size = buffer.readInt();
                for (int i = 0; i < size; i++) {
                    final int index = buffer.readInt();
                    if (buffer.readBoolean()) {
                        this.elements.put(index, buffer.readString(Short.MAX_VALUE));
                    } else this.elements.put(index, FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
                }
            } else if (id == 3) {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
            } else if (id == 4) {
                final BlockPos pos = buffer.readBlockPos();
                BlockPosHighlighter.hilightBlock(pos, System.currentTimeMillis() + 500 * BlockPosUtils.getDistance(pos, this.gui.entityPlayer.getPosition()), this.gui.entityPlayer.dimension);
                this.gui.entityPlayer.sendStatusMessage(PlayerMessages.InterfaceHighlighted.get(pos.getX(), pos.getY(), pos.getZ()), false);
                this.gui.entityPlayer.closeScreen();
            }
        } catch (IOException e) {
            TJ.logger.info(e.getMessage());
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.scrollOffset = buffer.readInt();
        } else if (id == 2) {
            final ElementData data = new ElementData(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readInt(), buffer.readInt());
            this.actionPerformed(data, this.gui.entityPlayer.inventory.getItemStack());
        }
    }

    /**
     * @param data contains element data. Is element a slot, button pressed, and element index.
     * @param playerStack item held by player cursor.
     */
    protected void actionPerformed(ElementData data, ItemStack playerStack) {
        int index = 0;
        int scrollHeight = 0;
        final int scrollOffset = this.scrollOffset + this.getSize().getHeight() + 18;
        grid:
        for (Class<? extends IGridHost> gridHost : this.gridHosts) {
            final Iterator<IGridNode> gridNodes = this.grid.getMachines(gridHost).iterator();
            while (gridNodes.hasNext()) { // Increase scrollHeight if there are more elements remaining.
                final IGridNode gridNode = gridNodes.next();
                if (!gridNode.isActive()) continue;
                final T machine = (T) gridNode.getMachine();
                if (!this.predicate.test(machine)) continue;
                final IFluidHandler fluidHandler = this.fluidTankSupplier.apply(machine);
                if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset) {
                    if (!data.isSlot && data.index == index) {
                        this.writeUpdateInfo(4, buffer -> buffer.writeBlockPos(gridNode.getGridBlock().getLocation().getPos()));
                        break grid;
                    }
                    index++;
                }
                scrollHeight += 18;
                final IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
                for (int j = 0, slotColumn = 0; j < tankProperties.length; j++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (data.isSlot && data.index == index) {
                        playerStack = this.slotAction(fluidHandler, j, playerStack, data);
                        break grid;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset && this.slotPredicate.test(j, machine))
                        index++;
                }
                if (gridNodes.hasNext())
                    scrollHeight += 18;
            }
        }
        final ItemStack finalPlayerStack = playerStack;
        this.gui.entityPlayer.inventory.setItemStack(finalPlayerStack);
        this.writeUpdateInfo(3, buffer -> buffer.writeItemStack(finalPlayerStack));
    }

    protected ItemStack slotAction(IFluidHandler fluidHandler, int slotIndex, ItemStack playerStack, ElementData data) {
        final IFluidHandlerItem fluidHandlerItem = playerStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null)
            return playerStack;
        final AEFluidInventory fluidTank = (AEFluidInventory) fluidHandler;
        final int tankCapacity = fluidTank.getTankProperties()[0].getCapacity();
        final int size = data.shiftClick ? playerStack.getCount() : 1;
        int type = data.button;
        IAEFluidStack iaeFluidStack;
        if (!data.advanced && this.getFluidAmount(slotIndex, fluidTank) > 0)
            type = 1;
        if (type == 0) {
            for (int i = 0; i < size; i++) {
                final FluidStack fluidContained = FluidUtil.getFluidContained(playerStack);
                if (fluidContained == null)
                    return playerStack;
                final int toDrain = (int) Math.min(fluidContained.amount, tankCapacity - this.getFluidAmount(slotIndex, fluidTank));
                final FluidStack bucketFluid = this.getFluidStack(slotIndex, fluidTank);
                final FluidActionResult fluidActionResult = FluidUtil.tryEmptyContainer(playerStack, new FluidTank(bucketFluid, tankCapacity), toDrain, this.gui.entityPlayer, false);
                if (fluidActionResult == FluidActionResult.FAILURE) break;
                if (playerStack.getCount() > (data.advanced ? 1 : 0)) {
                    if (!TJItemUtils.insertInMainInventory(this.gui.entityPlayer.inventory, fluidActionResult.getResult()).isEmpty()) break;
                    playerStack.shrink(1);
                } else playerStack = fluidActionResult.getResult();
                FluidUtil.tryEmptyContainer(playerStack, new FluidTank(bucketFluid, tankCapacity), toDrain, this.gui.entityPlayer, true);
                FluidStack fluidStack = this.getFluidStack(slotIndex, fluidTank);
                if (fluidStack == null) {
                    fluidStack = new FluidStack(fluidContained.getFluid(), toDrain);
                } else fluidStack.amount += toDrain;
                iaeFluidStack = AEFluidStack.fromFluidStack(fluidStack);
                fluidTank.setFluidInSlot(slotIndex, iaeFluidStack.copy());
            }
        } else if (type == 1) {
            for (int i = 0; i < size; i++) {
                iaeFluidStack = fluidTank.getFluidInSlot(slotIndex);
                if (iaeFluidStack == null)
                    return playerStack;
                final FluidStack fluidContained = FluidUtil.getFluidContained(playerStack);
                final int bucketCapacity = fluidHandlerItem.getTankProperties()[0].getCapacity();
                final int toFill = (int) Math.min(this.getFluidAmount(slotIndex, fluidTank), bucketCapacity - (fluidContained != null ? fluidContained.amount : 0));
                final FluidStack tankFluid = this.getFluidStack(slotIndex, fluidTank);
                final IFluidHandler tank = new FluidTank(tankFluid, tankCapacity);
                final FluidActionResult fluidActionResult = FluidUtil.tryFillContainer(playerStack, tank, toFill, this.gui.entityPlayer, false);
                if (fluidActionResult == FluidActionResult.FAILURE) break;
                if (playerStack.getCount() > (data.advanced ? 1 : 0)) {
                    if (!TJItemUtils.insertInMainInventory(this.gui.entityPlayer.inventory, fluidActionResult.getResult()).isEmpty()) break;
                    playerStack.shrink(1);
                } else playerStack = fluidActionResult.getResult();
                FluidUtil.tryFillContainer(playerStack, tank, toFill, this.gui.entityPlayer, true);
                if (tank.getTankProperties()[0].getContents() == null || (iaeFluidStack.getStackSize() - toFill) < 1) {
                    iaeFluidStack = null;
                } else iaeFluidStack.setStackSize(iaeFluidStack.getStackSize() - toFill);
                fluidTank.setFluidInSlot(slotIndex, iaeFluidStack != null ? iaeFluidStack.copy() : null);
            }
        }
        return playerStack;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.elements.clear();
        int index = 0;
        int scrollHeight = 0;
        final int scrollOffset = this.scrollOffset + this.getSize().getHeight() + 18;
        for (Class<? extends IGridHost> gridHost : this.gridHosts) {
            final Iterator<IGridNode> gridNodes = this.grid.getMachines(gridHost).iterator();
            while (gridNodes.hasNext()) { // Increase scrollHeight if there are more elements remaining.
                final IGridNode gridNode = gridNodes.next();
                if (!gridNode.isActive()) continue;
                final T machine = (T) gridNode.getMachine();
                if (!this.predicate.test(machine)) continue;
                final IFluidHandler fluidHandler = this.fluidTankSupplier.apply(machine);
                if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset)
                    this.elements.put(index++, ((ICustomNameObject) gridNode.getMachine()).getCustomInventoryName());
                scrollHeight += 18;
                final IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
                for (int i = 0, slotColumn = 0; i < tankProperties.length; i++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset && this.slotPredicate.test(i, machine))
                        this.elements.put(index++, tankProperties[i].getContents());
                }
                if (gridNodes.hasNext())
                    scrollHeight += 18;
            }
        }
        if (this.scrollHeight != scrollHeight) {
            this.scrollHeight = scrollHeight;
            this.writeUpdateInfo(1, buffer -> buffer.writeInt(this.scrollHeight));
        }
        this.writeUpdateInfo(2, buffer -> {
            buffer.writeInt(this.elements.size());
            for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
                buffer.writeInt(entry.getIntKey());
                final boolean isName = entry.getValue() instanceof String;
                buffer.writeBoolean(isName);
                if (isName) {
                    buffer.writeString((String) entry.getValue());
                } else if (entry.getValue() != null) {
                    buffer.writeCompoundTag(((FluidStack) entry.getValue()).writeToNBT(new NBTTagCompound()));
                } else buffer.writeCompoundTag(EMPTY_FLUID.writeToNBT(new NBTTagCompound()));
            }
        });
    }

    private boolean isMouseInWidget(int mouseX, int mouseY) {
        final Size size = this.getSize();
        final int posY = this.getPosition().getY();
        return mouseX >= this.posX && mouseX <= this.posX + size.getWidth() && mouseY >= posY && mouseY <= posY + size.getHeight();
    }

    @Override
    public Rectangle toRectangleBox() {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        return new Rectangle(pos.getX(), Math.min(pos.getY(), this.scrollBarRec.y), size.getWidth() + this.scrollBarXOffset + this.scrollBarRec.width, Math.max(size.getHeight(), this.scrollBarRec.height));
    }

    private void addScrollOffset(int delta) {
        this.scrollOffset += delta;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.scrollHeight));
        this.writeClientAction(1, buffer -> buffer.writeInt(this.scrollOffset));
    }

    public void setFluidAt(int slotIndex, FluidStack fluidStack) {
        this.modifyFluidAt(slotIndex, fluidStack, false);
    }

    @Nullable
    public FluidStack getFluidAt(int slotIndex) {
        return this.modifyFluidAt(slotIndex, null, true);
    }

    @Nullable
    private FluidStack modifyFluidAt(int slotIndex, FluidStack fluidStack, boolean doDrain) {
        int index = 0;
        int scrollHeight = 0;
        final int scrollOffset = this.scrollOffset + this.getSize().getHeight() + 18;
        grid:
        for (Class<? extends IGridHost> gridHost : this.gridHosts) {
            final Iterator<IGridNode> gridNodes = this.grid.getMachines(gridHost).iterator();
            while (gridNodes.hasNext()) { // Increase scrollHeight if there are more elements remaining.
                final IGridNode gridNode = gridNodes.next();
                if (!gridNode.isActive()) continue;
                final T machine = (T) gridNode.getMachine();
                if (!this.predicate.test(machine)) continue;
                final IFluidHandler fluidHandler = this.fluidTankSupplier.apply(machine);
                if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset) {
                    if (slotIndex == index)
                        return fluidStack;
                    index++;
                }
                scrollHeight += 18;
                final IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
                for (int j = 0, slotColumn = 0; j < tankProperties.length; j++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (slotIndex == index) {
                        if (doDrain) {
                            fluidStack = tankProperties[j].getContents();
                        } else if (fluidHandler instanceof IAEFluidTank)
                            ((IAEFluidTank) fluidHandler).setFluidInSlot(j, AEFluidStack.fromFluidStack(fluidStack));
                        break grid;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset && this.slotPredicate.test(j, machine))
                        index++;
                }
                if (gridNodes.hasNext())
                    scrollHeight += 18;
            }
        }
        return fluidStack;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            int scrollOffset = 0;
            int slotColumn = 0;
            int slotXOffset = 0;
            for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
                if (entry.getValue() instanceof FluidStack) {
                    if (slotColumn > 8) {
                        slotColumn = 0;
                        scrollOffset += 18;
                        slotXOffset = 0;
                    }
                    final int x = this.getPosition().getX() + slotXOffset;
                    final int y = this.getPosition().getY() + scrollOffset - (this.scrollOffset % 18);
                    if (isMouseOver(x, y, 18, 18, mouseX, mouseY))
                        return entry.getValue();
                    slotXOffset += 18;
                    slotColumn++;
                } else {
                    if (entry.getIntKey() > 0)
                        scrollOffset += 18;
                    scrollOffset += 18;
                    slotXOffset = 0;
                    slotColumn = 0;
                }
            }
        }
        return null;
    }

    protected long getFluidAmount(int slotIndex, AEFluidInventory fluidInventory) {
        final IAEFluidStack iaeFluidStack = fluidInventory.getFluidInSlot(slotIndex);
        return iaeFluidStack != null ? iaeFluidStack.getStackSize() : 0;
    }

    protected FluidStack getFluidStack(int slotIndex, AEFluidInventory fluidInventory) {
        final IAEFluidStack iaeFluidStack = fluidInventory.getFluidInSlot(slotIndex);
        return iaeFluidStack != null ? iaeFluidStack.getFluidStack() : null;
    }

    public static class ElementData {

        final boolean isSlot;
        final boolean advanced;
        final boolean shiftClick;
        final int index;
        final int button;

        ElementData(boolean isSlot, boolean advanced, boolean shiftClick, int index, int button) {
            this.isSlot = isSlot;
            this.advanced = advanced;
            this.shiftClick = shiftClick;
            this.index = index;
            this.button = button;
        }
    }
}
