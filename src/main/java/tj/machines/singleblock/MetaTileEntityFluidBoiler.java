package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemFuelInfo;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;

import java.util.*;
import java.util.function.Consumer;

import static tj.machines.singleblock.BoilerType.BRONZE;
import static tj.machines.singleblock.BoilerType.STEEL;
import static gregtech.api.gui.widgets.ProgressWidget.MoveType.VERTICAL;
import static gregtech.api.unification.material.Materials.Creosote;

public class MetaTileEntityFluidBoiler extends MetaTileEntityCoalBoiler {

    private final IFluidTank fuelTank;

    public MetaTileEntityFluidBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId, boilerType);
        this.fuelTank = new FilteredFluidHandler(16000).setFillPredicate(fluid ->
                new FluidStack(FluidRegistry.LAVA, 1).isFluidEqual(fluid) ||
                Objects.requireNonNull(Creosote.getFluid(1)).isFluidEqual(fluid));
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityFluidBoiler(metaTileEntityId, boilerType);
    }

    @Override
    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new TankWidget(fuelTank, 112, 15, 15, 60)
                .setBackgroundTexture(boilerType.getProgressBar()));
        widget.accept(new ProgressWidget(this::getBurnPercent, 130, 58, 18, 18) {
            private int displayBurnTimeInSeconds;

            @Override
            public void drawInForeground(int mouseX, int mouseY) {
                if(isMouseOverElement(mouseX, mouseY)) {
                    List<String> hoverList = Arrays.asList(I18n.format("machine.boiler.display.burning", this.displayBurnTimeInSeconds),
                            I18n.format("machine.boiler.display.burning.info.bucket"));
                    drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
                }
            }

            @Override
            public void detectAndSendChanges() {
                super.detectAndSendChanges();
                int displayBurnTimeInSeconds = getProgress() / 20;
                writeUpdateInfo(1, buffer -> buffer.writeInt(displayBurnTimeInSeconds));
            }

            @Override
            public void readUpdateInfo(int id, PacketBuffer buffer) {
                super.readUpdateInfo(id, buffer);
                if (id == 1) {
                    this.displayBurnTimeInSeconds = buffer.readInt();
                }
            }
        }.setProgressBar(boilerType.getFuelEmpty(), boilerType.getFuelFull(), VERTICAL));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, waterTank, fuelTank);
    }

    @Override
    protected boolean canBurn() {
        if (burnTime > 0) {
            burnTime = MathHelper.clamp(burnTime - boilerType.getTicks(), 0, maxBurnTime);
            return true;
        }
        if (fuelTank.getFluid() == null) {
            return false;
        }
        ItemStack stack = FluidUtil.getFilledBucket(fuelTank.getFluid());
        int consumeFuelAmount = Math.min(1000, fuelTank.getFluidAmount());
        float consumePercent = (float) consumeFuelAmount / 1000;
        int burnValue = (int) (getBurnValue(stack) * consumePercent);
        if (burnValue > 0) {
            stack.shrink(1);
            this.fuelTank.drain(consumeFuelAmount, true);
            this.burnTime = burnValue;
            this.maxBurnTime = burnValue;
            return true;
        }
        return false;
    }

    private int getBurnValue(ItemStack stack) {
        return ((stack.getTranslationKey().equals("item.forge.bucketFilled") ? 6000 : TileEntityFurnace.getItemBurnTime(stack)) * BASE_MODIFIER) /
                (boilerType == BRONZE ? 1
                        : boilerType == STEEL ? 2
                        : 4);
    }

    @Override
    public String getFuelName() {
        return fuelTank.getFluid() != null ? fuelTank.getFluid().getUnlocalizedName() : "";
    }

    @Override
    public int getFuelRemaining() {
        return fuelTank.getFluid() != null ? fuelTank.getFluidAmount() : 0;
    }

    @Override
    public int getFuelCapacity() {
        return fuelTank.getFluid() != null ? fuelTank.getCapacity() : 0;
    }

    @Override
    public int getFuelBurnTime() {
        if (fuelTank.getFluid() == null)
            return 0;
        ItemStack stack = FluidUtil.getFilledBucket(fuelTank.getFluid());
        return getBurnValue(stack) * getFuelRemaining() / 1000;
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        FluidStack stack = fuelTank.getFluid();
        if (stack == null)
            return Collections.emptyList();
        return Collections.singletonList(new ItemFuelInfo(FluidUtil.getFilledBucket(stack), getFuelRemaining(), getFuelCapacity(), getFuelMinConsumed(), (long) getFuelBurnTime()));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.LAVA_BOILER_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive());
    }
}
