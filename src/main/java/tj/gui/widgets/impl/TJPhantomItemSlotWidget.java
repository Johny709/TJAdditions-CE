package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TJPhantomItemSlotWidget extends Widget implements IGhostIngredientTarget, IIngredientSlot {

    private final Consumer<ItemStack> onExtracted;
    private final Consumer<ItemStack> onItemUpdate;
    private final IItemHandlerModifiable itemHandler;
    private final int slotIndex;
    private TextureArea[] backgroundTextures;
    private BooleanSupplier putItemsPredicate;
    private BooleanSupplier takeItemsPredicate;

    @Nonnull
    private ItemStack itemStack = ItemStack.EMPTY;

    public TJPhantomItemSlotWidget(int x, int y, int width, int height, int slotIndex, IItemHandlerModifiable itemHandler) {
        this(x, y, width, height, slotIndex, itemHandler, null, null);
    }

    public TJPhantomItemSlotWidget(int x, int y, int width, int height, int slotIndex, IItemHandlerModifiable itemHandler, Consumer<ItemStack> onItemUpdate) {
        this(x, y, width, height, slotIndex, itemHandler, onItemUpdate, null);
    }

    public TJPhantomItemSlotWidget(int x, int y, int width, int height, int slotIndex, IItemHandlerModifiable itemHandler, Consumer<ItemStack> onItemUpdate, Consumer<ItemStack> onExtracted) {
        super(new Position(x, y), new Size(width, height));
        this.slotIndex = slotIndex;
        this.itemHandler = itemHandler;
        this.onItemUpdate = onItemUpdate;
        this.onExtracted = onExtracted;
    }

    /**
     * Set condition for item to be placed into slot either by dragging from JEI or placing item held by mouse into slot.
     * Will always be true if supplier not set or null.
     */
    public TJPhantomItemSlotWidget setPutItemsPredicate(BooleanSupplier putItemsPredicate) {
        this.putItemsPredicate = putItemsPredicate;
        return this;
    }

    /**
     * Set condition for item to be removed from slot. Will always be true if supplier not set or null.
     */
    public TJPhantomItemSlotWidget setTakeItemsPredicate(BooleanSupplier takeItemsPredicate) {
        this.takeItemsPredicate = takeItemsPredicate;
        return this;
    }

    public TJPhantomItemSlotWidget setBackgroundTextures(TextureArea... backgroundTextures) {
        this.backgroundTextures = backgroundTextures;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.itemStack.isEmpty() || !this.isMouseOverElement(mouseX, mouseY)) return;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final int screenWidth = Minecraft.getMinecraft().displayWidth;
        final int screenHeight = Minecraft.getMinecraft().displayHeight;
        GuiUtils.drawHoveringText(getItemToolTip(this.itemStack), mouseX, mouseY, screenWidth, screenHeight, 100, fontRenderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Position pos = this.getPosition();
        if (this.backgroundTextures != null) for (TextureArea textureArea : this.backgroundTextures) {
            textureArea.draw(pos.getX(), pos.getY(), this.getSize().getWidth(), this.getSize().getHeight());
        }
        if (!this.itemStack.isEmpty())
            Widget.drawItemStack(this.itemStack, pos.getX() + 1, pos.getY() + 1, null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (button == 1 && (this.takeItemsPredicate == null || this.takeItemsPredicate.getAsBoolean())) { // Right-Click
                this.writeClientAction(2, buffer -> {});
                this.itemStack = ItemStack.EMPTY;
                return true;
            } else if (button == 0 && (this.putItemsPredicate == null || this.putItemsPredicate.getAsBoolean())) { // Left-Click
                final ItemStack stack = this.gui.entityPlayer.inventory.getItemStack();
                if (!stack.isEmpty()) {
                    this.writeClientAction(1, buffer -> buffer.writeItemStack(stack));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            try {
                this.itemStack = buffer.readItemStack();
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack stack = this.itemHandler.getStackInSlot(this.slotIndex);
        if (!stack.isEmpty()) {
            this.itemStack = stack;
            this.writeUpdateInfo(1, buffer -> buffer.writeItemStack(this.itemStack));
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            try {
                this.itemStack = buffer.readItemStack();
                this.itemHandler.extractItem(this.slotIndex, Integer.MIN_VALUE, false);
                this.itemHandler.insertItem(this.slotIndex, this.itemStack, false);
                if (this.onItemUpdate != null)
                    this.onItemUpdate.accept(this.itemStack);
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        } else if (id == 2) {
            this.itemStack = ItemStack.EMPTY;
            final ItemStack extracted = this.itemHandler.extractItem(this.slotIndex, Integer.MIN_VALUE, false);
            if (this.onItemUpdate != null)
                this.onItemUpdate.accept(this.itemStack);
            if (this.onExtracted != null)
                this.onExtracted.accept(extracted);
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object o) {
        return !(o instanceof ItemStack) ? Collections.emptyList() : Collections.singletonList(new IGhostIngredientHandler.Target<Object>() {
            @Override
            public Rectangle getArea() {
                return toRectangleBox();
            }

            @Override
            public void accept(Object o) {
                if (o instanceof ItemStack) {
                    itemStack = ((ItemStack) o).copy();
                    if (putItemsPredicate == null || putItemsPredicate.getAsBoolean())
                        writeClientAction(1, buffer -> buffer.writeItemStack(itemStack));
                }
            }
        });
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) ? this.itemStack : null;
    }
}
