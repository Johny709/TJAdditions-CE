package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.impl.NewTextFieldWidget;
import tj.gui.widgets.impl.TJLabelWidget;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.textures.TJTextures;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoidFluidCover extends CoverBehavior implements CoverWithUI, ITickable {

    protected Object2ObjectMap<FluidStack, FluidStack> fluidType = new Object2ObjectOpenHashMap<>();
    protected final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
            .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
            .collect(Collectors.toList()));
    protected final IFluidHandler fluidHandler = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide);
    protected boolean isWorking;
    protected int tickTime = 20;

    public VoidFluidCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        if (compound.hasKey("tickTime"))
            this.tickTime = compound.getInteger("tickTime");
        if (compound.hasKey("isWorking"))
            this.isWorking = compound.getBoolean("isWorking");
        for (int i = 0; i < 9; i++) {
            if (compound.hasKey("slot:" + i)) {
                this.fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("slot:" + i)), true);
                this.fluidType.put(this.fluidFilter.getTankAt(i).getFluid(), this.fluidFilter.getTankAt(i).getFluid());
            }
        }
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        TJTextures.VOID_FLUID_COVER_OVERLAY.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
        TJTextures.OUTSIDE_OVERLAY_BASE.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote)
            this.openUI((EntityPlayerMP) playerIn);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(53, 27));
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomFluidSlotWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid == null) return;
                fluid.amount = Integer.MAX_VALUE;
                this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
                this.fluidFilter.getTankAt(index).fill(fluid, true);
                this.fluidType.put(fluid, fluid);
            }, this.fluidType::remove).setPutFluidsPredicate(fluid -> !this.fluidType.containsKey(fluid))
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_fluid_cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, () -> String.valueOf(this.tickTime), this::setTickTime)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ClickButtonWidget(7, 7, 18, 18, "/2", data -> this.setTickTime(String.valueOf((long) this.tickTime / 2), "")))
                .widget(new ClickButtonWidget(151, 7, 18, 18, "*2", data -> this.setTickTime(String.valueOf((long) this.tickTime * 2), "")))
                .widget(new ToggleButtonWidget(151, 85, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, () -> this.isWorking, this::setWorking)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void update() {
        for (IFluidTankProperties fluidTankProperties : this.fluidHandler.getTankProperties()) {
            final FluidStack fluidStack = fluidTankProperties.getContents();
            if (fluidStack == null) continue;
            final FluidStack filterStack = this.fluidType.get(fluidStack);
            if (filterStack != null)
                this.fluidHandler.drain(filterStack, true);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("tickTime", this.tickTime);
        tagCompound.setBoolean("isWorking", this.isWorking);
        tagCompound.setTag("fluidFilter", this.fluidFilter.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.tickTime = Math.max(1, tagCompound.getInteger("tickTime"));
        this.isWorking = tagCompound.getBoolean("isWorking");
        this.fluidFilter.deserializeNBT(tagCompound.getCompoundTag("fluidFilter"));
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final FluidStack stack = this.fluidFilter.getTankAt(i).getFluid();
            if (stack == null) continue;
            this.fluidType.put(stack, stack);
        }
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
        this.markAsDirty();
    }

    public void setTickTime(String text, String id) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }
}
