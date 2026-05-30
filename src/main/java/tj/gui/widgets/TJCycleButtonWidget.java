package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextureArea;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TJCycleButtonWidget<T extends Enum<T>> extends ButtonWidget<TJCycleButtonWidget<T>> {

    private final Supplier<Enum<T>> cycleSupplier;
    private final Consumer<T> onCycle;
    private final EnumSet<T> cycles;
    private TextureArea cycleTexture;
    private String[] cycleHoverTooltipText;
    private int index;

    public TJCycleButtonWidget(int x, int y, int width, int height, EnumSet<T> cycles, Supplier<Enum<T>> cycleSupplier, Consumer<T> onCycle) {
        super(x, y, width, height);
        this.cycleSupplier = cycleSupplier;
        this.onCycle = onCycle;
        this.cycles = cycles;
    }

    public TJCycleButtonWidget<T> setCycleTexture(TextureArea cycleTexture) {
        this.cycleTexture = cycleTexture;
        return this;
    }

    /**
     * Text that will be displayed upon hovering over this button.
     * This will attempt to translate the texts if they're a lang string.
     * @param cycleHoverTooltipText array of texts to display.
     */
    public TJCycleButtonWidget<T> setCycleHoverTooltipText(String... cycleHoverTooltipText) {
        this.cycleHoverTooltipText = cycleHoverTooltipText;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (this.cycleHoverTooltipText != null && this.isMouseOverElement(mouseX, mouseY)) {
            final List<String> hoverList = Collections.singletonList(I18n.format(this.cycleHoverTooltipText[this.index]));
            this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, context);
        if (this.cycleTexture != null) {
            final double offsetY = 1.0 / this.cycles.size();
            this.cycleTexture.drawSubArea(this.getPosition().getX(), this.getPosition().getY(), this.getSize().getWidth(), this.getSize().getHeight(), 0.0, offsetY * this.index, 1.0, offsetY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            final int lastIndex = this.index;
            if (button == 0) {  // Left-Click
                this.index++;
            } else if (button == 1) { // Right-Click
                this.index--;
            }
            if (this.index >= this.cycles.size())
                this.index = 0;
            if (this.index < 0)
                this.index = this.cycles.size() - 1;
            if (lastIndex != this.index) {
                this.writeClientAction(1, buffer -> buffer.writeInt(this.index));
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 4) {
            this.index = buffer.readInt();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.index = buffer.readInt();
            if (this.onCycle != null) {
                this.onCycle.accept(this.cycles.stream()
                        .filter(c -> c.ordinal() == this.index)
                        .findFirst()
                        .orElse(null));
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.cycleSupplier != null) {
            final int index = this.cycleSupplier.get().ordinal();
            if (index != this.index) {
                this.index = index;
                this.writeUpdateInfo(4, buffer -> buffer.writeInt(this.index));
            }
        }
    }
}
