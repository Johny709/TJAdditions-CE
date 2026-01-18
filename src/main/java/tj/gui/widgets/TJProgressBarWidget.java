package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.gui.TJGuiUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class TJProgressBarWidget extends Widget {

    private final DoubleSupplier progressSupplier;
    private final DoubleSupplier maxProgressSupplier;
    private final boolean isFluid;
    private final ProgressWidget.MoveType moveType;

    private boolean inverted;
    private double progress;
    private double maxProgress;
    private int color;
    private TextureArea backgroundTexture;
    private TextureArea barTexture;
    private String locale;
    private Supplier<Object[]> paramSupplier;
    private Supplier<FluidStack> fluidStackSupplier;
    private Object[] params;
    private FluidStack fluid;

    public TJProgressBarWidget(int x, int y, int width, int height, DoubleSupplier progressSupplier, DoubleSupplier maxProgressSupplier, ProgressWidget.MoveType moveType) {
        this(x, y, width, height, progressSupplier, maxProgressSupplier, false, moveType);
    }

    public TJProgressBarWidget(int x, int y, int width, int height, DoubleSupplier progressSupplier, DoubleSupplier maxProgressSupplier, boolean isFluid) {
        this(x, y, width, height, progressSupplier, maxProgressSupplier, isFluid, ProgressWidget.MoveType.HORIZONTAL);
    }

    public TJProgressBarWidget(int x, int y, int width, int height, DoubleSupplier progressSupplier, DoubleSupplier maxProgressSupplier, boolean isFluid, ProgressWidget.MoveType moveType) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.maxProgressSupplier = maxProgressSupplier;
        this.isFluid = isFluid;
        this.moveType = moveType;
    }

    public TJProgressBarWidget setFluid(Supplier<FluidStack> fluidStackSupplier) {
        this.fluidStackSupplier = fluidStackSupplier;
        return this;
    }

    public TJProgressBarWidget setLocale(String locale, Supplier<Object[]> paramSupplier) {
        this.locale = locale;
        this.paramSupplier = paramSupplier;
        return this;
    }

    public TJProgressBarWidget setTexture(TextureArea backgroundTextures) {
        this.backgroundTexture = backgroundTextures;
        return this;
    }

    public TJProgressBarWidget setBarTexture(TextureArea barTexture) {
        this.barTexture = barTexture;
        return this;
    }

    public TJProgressBarWidget setColor(int color) {
        this.color = color;
        return this;
    }

    public TJProgressBarWidget setInverted(boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY) && this.locale != null) {
            Object[] format;
            if (this.isFluid && this.params != null && this.params.length > 0)
                format = ArrayUtils.addAll(this.params, TJValues.thousandFormat.format(this.progress), TJValues.thousandFormat.format(this.maxProgress), (int) (100 * (this.progress / this.maxProgress)));
            else format = ArrayUtils.toArray(TJValues.thousandFormat.format(this.progress), TJValues.thousandFormat.format(this.maxProgress), (int) (100 * (this.progress / this.maxProgress)));
            List<String> hoverList = Arrays.asList(I18n.format(this.locale, format).split("/n"));
            this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Size size = this.getSize();
        Position pos = this.getPosition();
        int width = this.moveType == ProgressWidget.MoveType.HORIZONTAL ? (int) ((size.getWidth() - 2) * (this.progress / this.maxProgress)) : size.getWidth() - 2;
        int height = this.moveType == ProgressWidget.MoveType.VERTICAL ? (int) ((size.getHeight() - 2) * (this.progress / this.maxProgress)) : size.getHeight() - 2;
        int x = this.inverted ? pos.getX() + size.getWidth() - 1: pos.getX() + 1;
        int y = this.inverted ? pos.getY() + size.getHeight() - 1: pos.getY() + 1;
        if (this.backgroundTexture != null)
            this.backgroundTexture.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        if (!this.isFluid) {
            if (this.barTexture != null)
                this.barTexture.draw(x, y, this.inverted ? -width : width, this.inverted ? -height : height);
            else Widget.drawSolidRect(x, y, this.inverted ? -width : width, this.inverted ? -height : height, this.color);
        } else if (this.fluid != null) {
            GlStateManager.disableBlend();
            TJGuiUtils.drawFluidForGui(this.fluid, (long) this.progress, (long) this.maxProgress, x, y, this.inverted ? -(width + 1) : width + 1, this.inverted ? -height : height);
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void detectAndSendChanges() {
        double progress, maxProgress;
        if (this.progressSupplier != null && (progress = this.progressSupplier.getAsDouble()) != this.progress) {
            this.progress = progress;
            this.writeUpdateInfo(1, buffer -> buffer.writeDouble(this.progress));
        }
        if (this.maxProgressSupplier != null && (maxProgress = this.maxProgressSupplier.getAsDouble()) != this.maxProgress) {
            this.maxProgress = maxProgress;
            this.writeUpdateInfo(2, buffer -> buffer.writeDouble(this.maxProgress));
        }
        Object[] params;
        if (this.paramSupplier != null && (params = this.paramSupplier.get()) != null && params.length > 0 && (this.params == null || !Arrays.equals(this.params, params))) {
            this.params = params;
            this.writeUpdateInfo(3, buffer -> {
                buffer.writeInt(this.params.length);
                for (Object param : this.params)
                    buffer.writeString(param != null ? (String) param : "");
            });
        }
        FluidStack fluidStack;
        if (this.fluidStackSupplier != null && (fluidStack = this.fluidStackSupplier.get()) != null && (this.fluid == null || !this.fluid.isFluidStackIdentical(fluidStack))) {
            this.fluid = fluidStack;
            this.writeUpdateInfo(4, buffer -> buffer.writeCompoundTag(this.fluid.writeToNBT(new NBTTagCompound())));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        switch (id) {
            case 1:
                this.progress = buffer.readDouble();
                break;
            case 2:
                this.maxProgress = buffer.readDouble();
                break;
            case 3:
                this.params = new Object[buffer.readInt()];
                for (int i = 0; i < this.params.length; i++) {
                    this.params[i] = I18n.format(buffer.readString(Short.MAX_VALUE));
                }
                break;
            case 4:
                try {
                    this.fluid = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
                } catch (IOException e) {
                    GTLog.logger.info(e.getMessage());
                }
        }
    }
}
