package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

public class SlotWidgetGroup extends Widget {

    private final TextureArea[] backgroundTextures;
    private Supplier<IItemHandler> itemHandlerSupplier;
    private IItemHandler itemHandler;
    private final int slotWidth;

    public SlotWidgetGroup(int x, int y, int width, int height, int slotWidth, TextureArea... backgroundTextures) {
        super(new Position(x, y), new Size(width, height));
        this.backgroundTextures = backgroundTextures;
        this.slotWidth = slotWidth;
    }

    public SlotWidgetGroup setItemHandler(Supplier<IItemHandler> itemHandlerSupplier) {
        this.itemHandlerSupplier = itemHandlerSupplier;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        Position pos = this.getPosition();
        int startX = pos.getX() + 1;
        int startY = pos.getY() + 1;
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            Widget.drawItemStack(this.itemHandler.getStackInSlot(i), startX + (18 * (i % this.slotWidth)), startY + (18 * (i / this.slotWidth)), null);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = this.getPosition();
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            for (TextureArea textureArea : this.backgroundTextures) {
                textureArea.draw(pos.getX() + (18 * (i % this.slotWidth)), pos.getY() + (18 * (i / this.slotWidth)), 18, 18);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isMouseOverElement(mouseX, mouseY))
            return false;
        int slotIndex = (mouseX / 18) * (mouseY / 18);
        System.out.println(slotIndex);
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        if (this.itemHandlerSupplier != null) {
            IItemHandler iItemHandler = this.itemHandlerSupplier.get();
            if (this.itemHandler == null || iItemHandler.getSlots() != this.itemHandler.getSlots()) {
                this.itemHandler = iItemHandler;
                this.writeUpdateInfo(1, buffer -> buffer.writeInt(this.itemHandler.getSlots()));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.itemHandler = new ItemStackHandler(buffer.readInt());
        }
    }
}
