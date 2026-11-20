package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tj.integration.ae2.IFluidConfigSetter;
import tj.integration.ae2.ITickSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.textures.TJTextures;
import tj.util.TJItemUtils;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CreativeFluidCover extends CoverBehavior implements CoverWithUI, ITickable, IWorkable, IFluidConfigSetter, ITickSetter {

    private final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
            .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
            .collect(Collectors.toList()));
    private final IFluidHandler fluidHandler;
    private boolean isWorking;
    private int ticks = 1;

    public CreativeFluidCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.fluidHandler = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        TJTextures.COVER_CREATIVE_FLUID.renderSided(attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote) {
            this.openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 25));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 25, 54, 54);
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, this.fluidFilter, null)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(this);
        return TJModularUI.builder(this, player)
                .setWidthHeight(200, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("cover.creative_fluid.title"))
                .widget(new TJToggleButtonWidget(174, 105, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON, () -> false, this::onReset)
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 163, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "machine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 82, 162, 18, true, () -> String.valueOf(this.ticks), (text, id) ->
                        this.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 82, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
                    if (!value && panelWidgetGroup.isPanelOpen()) {
                        panelWidgetGroup.closePanel();
                    } else if (value) {
                        panelWidgetGroup.openPanel();
                    }
                }).setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11)) // infinity bimetal gear
                        .setHoverTooltipText("tj.machine.universal.ticks.operation_configure")
                        .useToggleTexture(true))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.createFluidConfigPanel(this, selectionWidgetGroup))
                .widget(panelWidgetGroup)
                .build();
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        if (compound.hasKey("speed"))
            this.ticks = compound.getInteger("speed");
        if (compound.hasKey("power"))
            this.isWorking = compound.getBoolean("power");
        for (int i = 0; i < 9; i++) {
            if (compound.hasKey("slot:" + i))
                this.fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)), true);
        }
    }

    @Override
    public void update() {
        if (this.isWorking && this.coverHolder.getOffsetTimer() % this.ticks == 0) {
            for (int index = 0; index < 9; index++) {
                final FluidStack fluid = this.fluidFilter.getTankAt(index).getFluid();
                if (fluid != null) {
                    this.fluidHandler.fill(fluid, true);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("fluidFilter", this.fluidFilter.serializeNBT());
        data.setInteger("Speed", this.ticks);
        data.setBoolean("IsWorking", this.isWorking);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.fluidFilter.deserializeNBT(data.getCompoundTag("fluidFilter"));
        this.isWorking = data.getBoolean("IsWorking");
        if (data.hasKey("Speed"))
            this.ticks = data.getInteger("Speed");
    }

    @Override
    public void setFluidCount(long count, int index) {
        if (index < 0 || index >= this.fluidFilter.getTanks()) return;
        final FluidStack fluidStack = this.fluidFilter.getTankAt(index).getFluid();
        if (fluidStack == null) return;
        fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, count);
        this.markAsDirty();
    }

    @Override
    public int getFluidCount(int index) {
        return index >= 0 && index < this.fluidFilter.getTanks() ? this.fluidFilter.getTankAt(index).getFluidAmount() : 0;
    }

    private void onReset(boolean reset) {
        this.ticks = 1;
        this.markAsDirty();
    }

    @Override
    public void setTickTime(long tickTime) {
        this.ticks = (int) Math.max(1, Math.min(Integer.MAX_VALUE, tickTime));
        this.markAsDirty();
    }

    @Override
    public int getTickTime() {
        return this.ticks;
    }

    @Override
    public int getProgress() {
        return (int) this.coverHolder.getOffsetTimer() % this.ticks;
    }

    @Override
    public int getMaxProgress() {
        return this.ticks;
    }

    @Override
    public boolean isActive() {
        return this.isWorking;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorking;
    }

    @Override
    public void setWorkingEnabled(boolean isWorking) {
        this.isWorking =  isWorking;
        this.markAsDirty();
    }
}
