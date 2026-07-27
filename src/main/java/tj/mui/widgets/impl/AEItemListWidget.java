package tj.mui.widgets.impl;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.client.render.BlockPosHighlighter;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.ICustomNameObject;
import appeng.util.BlockPosUtils;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.input.Keyboard;
import tj.TJ;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.TJWidget;
import tj.util.TJItemUtils;
import tj.util.predicates.IntBiPredicate;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class AEItemListWidget<T> extends TJWidget<AEItemListWidget<T>> implements IIngredientSlot {

    protected final Int2ObjectMap<Object> elements = new Int2ObjectLinkedOpenHashMap<>();
    protected final Class<? extends IGridHost>[] gridHosts;
    protected final IGrid grid;
    protected final int posX;
    protected BiFunction<ItemStack, Boolean, ItemStack> itemStackTransfer;
    protected TriConsumer<ItemStack, Integer, Integer> renderCallback;
    protected Function<T, IItemHandler> inventorySupplier;
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
    public AEItemListWidget(int x, int y, int width, int height, IGridNode gridNode, Class<? extends IGridHost>... gridHosts) {
        super(new Position(x, y), new Size(width, height));
        this.grid = gridNode != null ? gridNode.getGrid() : null;
        this.posX = x;
        this.gridHosts = gridHosts;
        this.setScrollbar(0, y, 18, height, GuiTextures.SLOT);
    }

    public AEItemListWidget<T> setScrollbar(int x, int y, int width, int height, TextureArea scrollbarTexture) {
        this.scrollBarRec = new Rectangle(x, y, width, height);
        this.scrollBarTexture = scrollbarTexture;
        this.scrollBarXOffset = x;
        return this;
    }

    public AEItemListWidget<T> setScrollSlider(int x, int y, int width, int height, TextureArea scrollSliderTexture) {
        this.scrollSliderRec = new Rectangle(x, y, width, height);
        this.scrollSliderTexture = scrollSliderTexture;
        return this;
    }

    public AEItemListWidget<T> setInventorySupplier(Function<T, IItemHandler> inventorySupplier) {
        this.inventorySupplier = inventorySupplier;
        return this;
    }

    public AEItemListWidget<T> setSlotPredicate(IntBiPredicate<T> slotPredicate) {
        this.slotPredicate = slotPredicate;
        return this;
    }

    public AEItemListWidget<T> setPredicate(Predicate<T> predicate) {
        this.predicate = predicate;
        return this;
    }

    public AEItemListWidget<T> setRenderCallback(TriConsumer<ItemStack, Integer, Integer> renderCallback) {
        this.renderCallback = renderCallback;
        return this;
    }

    public AEItemListWidget<T> setItemStackTransfer(BiFunction<ItemStack, Boolean, ItemStack> itemStackTransfer) {
        this.itemStackTransfer = itemStackTransfer;
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
            if (entry.getValue() instanceof ItemStack) {
                if (slotColumn > 8) {
                    slotColumn = 0;
                    scrollOffset += 18;
                    slotXOffset = 0;
                }
                final int x = pos.getX() + slotXOffset;
                final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                final ItemStack itemStack = (ItemStack) entry.getValue();
                if (!itemStack.isEmpty() && isMouseOver(x, y, 18, 18, mouseX, mouseY)) {
                    final List<String> tooltip = getItemToolTip(itemStack);
                    final String itemStoredText = I18n.format("gregtech.item_list.item_stored", itemStack.getCount());
                    tooltip.add(TextFormatting.GRAY + itemStoredText);
                    this.drawHoveringText(itemStack, tooltip, -1, mouseX, mouseY);
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
                if (entry.getValue() instanceof ItemStack) {
                    if (slotColumn > 8) {
                        slotColumn = 0;
                        scrollOffset += 18;
                        slotXOffset = 0;
                    }
                    final int x = pos.getX() + slotXOffset;
                    final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                    final ItemStack itemStack = (ItemStack) entry.getValue();
                    this.drawSlot(entry.getIntKey(), x, y, mouseX, mouseY, itemStack);
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
    protected void drawSlot(int index, int x, int y, int mouseX, int mouseY, ItemStack itemStack) {
        GuiTextures.SLOT.draw(x, y, 18, 18);
        if (!itemStack.isEmpty())
            this.renderCallback.accept(itemStack, x, y);
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
                if (entry.getValue() instanceof ItemStack) {
                    if (slotColumn > 8) {
                        slotColumn = 0;
                        scrollOffset += 18;
                        slotXOffset = 0;
                    }
                    final int x = pos.getX() + slotXOffset;
                    final int y = pos.getY() + scrollOffset - (this.scrollOffset % 18);
                    if (isMouseOver(x, y, 18, 18, mouseX, mouseY)) {
                        final boolean shiftClick = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                        this.writeClientAction(shiftClick ? 3 : 2, buffer -> {
                            buffer.writeBoolean(true); // isSlot is true.
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
                        this.playButtonClickSound();
                        this.writeClientAction(2, buffer -> {
                            buffer.writeBoolean(false); // isSlot is false.
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
                    } else this.elements.put(index, buffer.readItemStack());
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
            final ElementData data = new ElementData(buffer.readBoolean(), buffer.readInt(), buffer.readInt());
            this.actionPerformed(data, this.gui.entityPlayer.inventory.getItemStack(), null);
        } else if (id == 3) {
            final ElementData data = new ElementData(buffer.readBoolean(), buffer.readInt(), buffer.readInt());
            this.actionPerformed(data, this.gui.entityPlayer.inventory.getItemStack(), this.itemStackTransfer);
        }
    }

    public ItemStack insertItem(ItemStack stack) {
        final int scrollOffset = this.scrollOffset + this.getSize().getHeight() + 18;
        int scrollHeight = 0;
        for (Class<? extends IGridHost> gridHost : this.gridHosts) {
            final Iterator<IGridNode> gridNodes = this.grid.getMachines(gridHost).iterator();
            while (gridNodes.hasNext()) { // Increase scrollHeight if there are more elements remaining.
                final IGridNode gridNode = gridNodes.next();
                if (!gridNode.isActive()) continue;
                final T machine = (T) gridNode.getMachine();
                if (!this.predicate.test(machine)) continue;
                final IItemHandler inventory = this.inventorySupplier.apply(machine);
                scrollHeight += 18;
                for (int j = 0, slotColumn = 0; j < inventory.getSlots(); j++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset && this.slotPredicate.test(j, machine)) {
                        stack = inventory.insertItem(j, stack, false);
                        if (stack.isEmpty())
                            return stack;
                    }
                }
                if (gridNodes.hasNext())
                    scrollHeight += 18;
            }
        }
        return stack;
    }

    /**
     * @param data contains element data. Is element a slot, button pressed, and element index.
     * @param playerStack item held by player cursor.
     * @param itemStackTransfer Inserts item into element slot from player cursor if null. Inserts into external inventory and then player inventory if provided itemStackTransfer function.
     */
    protected void actionPerformed(ElementData data, ItemStack playerStack, BiFunction<ItemStack, Boolean, ItemStack> itemStackTransfer) {
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
                final IItemHandler inventory = this.inventorySupplier.apply(machine);
                if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset) {
                    if (!data.isSlot && data.index == index) {
                        this.writeUpdateInfo(4, buffer -> buffer.writeBlockPos(gridNode.getGridBlock().getLocation().getPos()));
                        break grid;
                    }
                    index++;
                }
                scrollHeight += 18;
                for (int j = 0, slotColumn = 0; j < inventory.getSlots(); j++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (data.isSlot && data.index == index) {
                        playerStack = this.slotAction(inventory, j, playerStack, data, itemStackTransfer);
                        break grid;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset && this.slotPredicate.test(j, machine))
                        index++;
                }
                if (gridNodes.hasNext())
                    scrollHeight += 18;
            }
        }
        if (itemStackTransfer != null) return;
        final ItemStack finalPlayerStack = playerStack;
        this.gui.entityPlayer.inventory.setItemStack(finalPlayerStack);
        this.writeUpdateInfo(3, buffer -> buffer.writeItemStack(finalPlayerStack));
    }

    protected ItemStack slotAction(IItemHandler itemHandler, int slotIndex, ItemStack playerStack, ElementData data, BiFunction<ItemStack, Boolean, ItemStack> itemStackTransfer) {
        if (itemStackTransfer == null) {
            final ItemStack output = itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, false);
            if (output.isEmpty() || itemHandler.insertItem(slotIndex, playerStack, true).isEmpty()) {
                playerStack = itemHandler.insertItem(slotIndex, playerStack, false);
                if (playerStack.isEmpty())
                    playerStack = output;
            } else itemHandler.insertItem(slotIndex, output, false);
        } else if (itemHandler.insertItem(slotIndex, itemStackTransfer.apply(itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, true), true), true).isEmpty()) {
            itemHandler.insertItem(slotIndex, itemStackTransfer.apply(itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, false), false), false);
        } else itemHandler.insertItem(slotIndex, TJItemUtils.insertInMainInventory(this.gui.entityPlayer.inventory, itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, false)), false);
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
                final IItemHandler inventory = this.inventorySupplier.apply(machine);
                if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset)
                    this.elements.put(index++, ((ICustomNameObject) gridNode.getMachine()).getCustomInventoryName());
                scrollHeight += 18;
                for (int i = 0, slotColumn = 0; i < inventory.getSlots(); i++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset && this.slotPredicate.test(i, machine))
                        this.elements.put(index++, inventory.getStackInSlot(i));
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
                } else buffer.writeItemStack((ItemStack) entry.getValue());
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

    public void setItemAt(int slotIndex, @Nonnull ItemStack itemStack) {
        this.modifyItemAt(slotIndex, itemStack, true);
    }

    @Nonnull
    public ItemStack getItemAt(int slotIndex) {
        return this.modifyItemAt(slotIndex, ItemStack.EMPTY, false);
    }

    @Nonnull
    private ItemStack modifyItemAt(int slotIndex, @Nonnull ItemStack itemStack, boolean insert) {
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
                final IItemHandler inventory = this.inventorySupplier.apply(machine);
                if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset) {
                    if (slotIndex == index)
                        return itemStack;
                    index++;
                }
                scrollHeight += 18;
                for (int j = 0, slotColumn = 0; j < inventory.getSlots(); j++, slotColumn++) {
                    if (slotColumn > 8) {
                        scrollHeight += 18;
                        slotColumn = 0;
                    }
                    if (slotIndex == index) {
                        if (insert) {
                            itemStack = inventory.insertItem(j, itemStack, false);
                        } else itemStack = inventory.extractItem(j, Integer.MAX_VALUE, false);
                        break grid;
                    }
                    if (scrollHeight >= this.scrollOffset && scrollHeight <= scrollOffset && this.slotPredicate.test(j, machine))
                        index++;
                }
                if (gridNodes.hasNext())
                    scrollHeight += 18;
            }
        }
        return itemStack;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            int scrollOffset = 0;
            int slotColumn = 0;
            int slotXOffset = 0;
            for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
                if (entry.getValue() instanceof ItemStack) {
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

    public static class ElementData {

        final boolean isSlot;
        final int index;
        final int button;

        ElementData(boolean isSlot, int index, int button) {
            this.isSlot = isSlot;
            this.index = index;
            this.button = button;
        }
    }
}
