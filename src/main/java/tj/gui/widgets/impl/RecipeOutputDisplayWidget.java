package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Supplier;

public class RecipeOutputDisplayWidget extends WidgetGroup {

    private Supplier<List<ItemStack>> itemOutputSupplier;
    private Supplier<List<FluidStack>> fluidOutputSupplier;
    private List<ItemStack> itemOutputs;
    private List<FluidStack> fluidOutputs;

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

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, context);

    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        List<ItemStack> itemStacks;
        if (this.itemOutputSupplier != null && (itemStacks = this.itemOutputSupplier.get()) != null) {

        }
        List<FluidStack> fluidStacks;
        if (this.fluidOutputSupplier != null && (fluidStacks = this.fluidOutputSupplier.get()) != null) {

        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
    }
}
