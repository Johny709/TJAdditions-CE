package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiUtils;

import java.util.Collections;
import java.util.function.Supplier;

public class TJLabelWidget extends Widget implements IRecipeClickArea {

    private final TextureArea labelTexture;
    private final Supplier<String> recipeUid;
    private int color = 0x404040;
    private int offsetX;
    private int tickCounter;
    private int hoverTicks;
    private boolean slideAtEnd;
    private String locale;
    private String uid;
    private ItemStack itemLabel;
    private FluidStack fluidLabel;

    public TJLabelWidget(int x, int y, int width, int height, TextureArea labelTexture) {
        this(x, y, width, height, labelTexture, null);
    }

    public TJLabelWidget(int x, int y, int width, int height, TextureArea labelTexture, Supplier<String> recipeUid) {
        super(new Position(x, y), new Size(width, height));
        this.labelTexture = labelTexture;
        this.recipeUid = recipeUid;
    }

    public TJLabelWidget setItemLabel(ItemStack itemLabel) {
        this.itemLabel = itemLabel;
        return this;
    }

    public TJLabelWidget setFluidLabel(FluidStack fluidLabel) {
        this.fluidLabel = fluidLabel;
        return this;
    }

    public TJLabelWidget setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public TJLabelWidget setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        this.tickCounter++;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        int widthApplied = 5;
        Size size = this.getSize();
        Position pos = this.getPosition();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.labelTexture.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        if (this.itemLabel != null) {
            Widget.drawItemStack(this.itemLabel, pos.getX() + 3, pos.getY() + 2, null);
            widthApplied += 16;
        }
        if (this.fluidLabel != null) {
            TJGuiUtils.drawFluidForGui(this.fluidLabel, this.fluidLabel.amount, this.fluidLabel.amount, pos.getX(), pos.getY(), 18, 18);
            widthApplied += 18;
        }
        if (this.locale != null) {
            int finalX = pos.getX() + widthApplied - this.offsetX;
            String locale = I18n.format(this.locale);
            int length = fontRenderer.getStringWidth(locale);
            length -= this.offsetX;
            if (length < 0)
                this.slideAtEnd = true;
            RenderUtil.useScissor(pos.getX() + widthApplied, pos.getY(), size.getWidth() - 25, size.getHeight(), () -> fontRenderer.drawString(locale, finalX, pos.getY() + 6, this.color));
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (this.recipeUid == null) return;
        String uid = this.recipeUid.get();
        if (uid != null && !uid.equals(this.uid)) {
            this.uid = uid;
            this.writeUpdateInfo(1, buffer -> buffer.writeString(this.uid));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1)
            this.uid = buffer.readString(Short.MAX_VALUE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (this.slideAtEnd) {
                this.slideAtEnd = false;
                this.offsetX = -(this.getSize().getWidth() - 24);
            } else if (this.tickCounter % 2 == 0 || this.tickCounter % 3 == 0)
                this.offsetX++;
            if (++this.hoverTicks > 20 && this.uid != null) {
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                String format = I18n.format("jei.tooltip.show.recipes");
                GuiUtils.drawHoveringText(Collections.singletonList(format), mouseX, mouseY, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, 256, fontRenderer);
            }
        } else {
            this.hoverTicks = 0;
            this.offsetX = 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getRecipeUid(int mouseX, int mouseY, int mouseButton) {
        return mouseButton == 0 && this.isMouseOverElement(mouseX, mouseY) ? this.uid : null;
    }
}
