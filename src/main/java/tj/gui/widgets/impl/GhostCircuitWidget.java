package tj.gui.widgets.impl;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.Size;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

public class GhostCircuitWidget extends SlotWidget {

    private final int posX;

    public GhostCircuitWidget(IItemHandler itemHandler, int x, int y) {
        super(itemHandler, 0, x, y, false, false);
        this.setBackgroundTexture(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY);
        this.posX = x;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.writeClientAction(2, buffer -> buffer.writeVarInt(button));
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseInWidget(mouseX, mouseY)) {
            this.writeClientAction(1, buffer -> buffer.writeVarInt(wheelDelta));
            return true;
        }
        return false;
    }

    private boolean isMouseInWidget(int mouseX, int mouseY) {
        Size size = this.getSize();
        int posY = this.getPosition().getY();
        return mouseX >= this.posX && mouseX <= this.posX + size.getWidth() && mouseY >= posY && mouseY <= posY + size.getHeight();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.setCircuit(buffer.readVarInt());
        } else if (id == 2) {
            switch (buffer.readVarInt()) {
                case 0: this.setCircuit(1); break;
                case 1: this.setCircuit(-1); break;
                case 2: this.slotReference.putStack(ItemStack.EMPTY);
            }
        }
    }

    private void setCircuit(int num) {
        ItemStack stack = this.slotReference.getStack();
        if (stack.isEmpty()) {
            ItemStack circuitStack = MetaItems.INTEGRATED_CIRCUIT.getStackForm();
            IntCircuitIngredient.setCircuitConfiguration(circuitStack, num > 0 ? 0 : 32);
            this.slotReference.putStack(circuitStack);
        } else if (MetaItems.INTEGRATED_CIRCUIT.isItemEqual(stack)) {
            int config = IntCircuitIngredient.getCircuitConfiguration(stack) + num;
            if (config > 32 || config < 0)
                this.slotReference.putStack(ItemStack.EMPTY);
            else IntCircuitIngredient.setCircuitConfiguration(stack, config);
        }
    }
}
