package tj.gui.widgets.impl;

import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.util.TriConsumer;
import tj.gui.widgets.TJSlotWidget;

import java.io.IOException;


public class SlotDisplayWidget extends TJSlotWidget<SlotDisplayWidget> {

    private TriConsumer<Integer, Integer, ItemStack> onPressed;

    public SlotDisplayWidget(IItemHandler itemHandler, int slotIndex, int x, int y) {
        super(itemHandler, slotIndex, x, y);
        this.setTakeItemsPredicate((stack) -> false)
                .setPutItemsPredicate((stack) -> false);
    }

    public SlotDisplayWidget onPressedConsumer(TriConsumer<Integer, Integer, ItemStack> onPressed) {
        this.onPressed = onPressed;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.writeClientAction(1, buffer -> {
                buffer.writeInt(button);
                buffer.writeInt(this.index());
                buffer.writeItemStack(this.getItemHandler().getStackInSlot(this.index()));
            });
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            try {
                int button = buffer.readInt();
                int index = buffer.readInt();
                ItemStack stack = buffer.readItemStack();
                if (this.onPressed != null)
                    this.onPressed.accept(button, index, stack);
            } catch (IOException e) {
                GTLog.logger.error(e);
            }
        }
    }
}
