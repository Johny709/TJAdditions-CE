package tj.items.covers;

import gregicadditions.GAValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.common.covers.CoverPump;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.mui.widgets.impl.TJTankWidget;
import tj.mui.widgets.impl.ButtonPopUpWidget;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.textures.TJSimpleOverlayRenderer;
import tj.util.EnderWorldData;

import java.awt.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregtech.api.gui.GuiTextures.*;
import static tj.TJValues.DUMMY_TANK;
import static tj.mui.TJGuiTextures.FLUID_FILTER;
import static tj.textures.TJTextures.PORTAL_OVERLAY;

public class EnderFluidCover extends AbstractEnderCover<FluidTank> {

    private final IFluidHandler fluidTank;
    private final Object2ObjectMap<FluidStack, FluidStack> fluidType = new Object2ObjectOpenHashMap<>();
    private final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 9)
            .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
            .collect(Collectors.toList()));
    private final int capacity;
    private final int tier;
    private boolean isFilterBlacklist = true;

    public EnderFluidCover(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide);
        this.tier = tier;
        this.fluidTank = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        this.capacity = (int) Math.min(Math.pow(4, tier) * 1000, Integer.MAX_VALUE);
        this.maxTransferRate = (int) Math.min(Math.pow(4, tier) * 16, Integer.MAX_VALUE);
        this.transferRate = this.maxTransferRate;
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    protected TJSimpleOverlayRenderer getOverlay() {
        return PORTAL_OVERLAY;
    }

    @Override
    protected int getPortalColor() {
        return 0x00a6ff;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    protected Object2ObjectMap<String, EnderCoverProfile<FluidTank>> getPlayerMap() {
        return EnderWorldData.getINSTANCE().getFluidTankPlayerMap();
    }

    @Override
    public FluidTank createHandler() {
        return new FluidTank(this.capacity);
    }

    @Override
    protected String getName() {
        return "metaitem.ender_fluid_cover_" + GAValues.VN[this.tier].toLowerCase() + ".name";
    }

    @Override
    protected void addChannelText(ITextComponent keyEntry, String key, FluidTank value) {
        final FluidStack fluid = value.getFluid();
        final boolean empty = fluid == null;
        final String name = !empty ? fluid.getUnlocalizedName() : "metaitem.fluid_cell.empty";
        final int capacity = value.getCapacity();
        final int amount = !empty ? fluid.amount : 0;
        keyEntry.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation(name)
                .appendText("\n")
                .appendSibling(new TextComponentTranslation("gregtech.fluid.amount", amount, capacity))));
    }

    @Override
    protected void addToPopUpWidget(PopUpWidget<?> buttonPopUpWidget) {
        ((ButtonPopUpWidget<?>) buttonPopUpWidget).addPopup(112, 61, 60, 78, new TJToggleButtonWidget(151, 161, 18, 18)
                .setHoverTooltipText("cover.pump.fluid_filter.title")
                .setToggleTexture(TOGGLE_BUTTON_BACK)
                .setBackgroundTextures(FLUID_FILTER)
                .useToggleTexture(true), widgetGroup -> {
            widgetGroup.addWidget(new ImageWidget(0, 0, 60, 78, BORDERED_BACKGROUND));
            widgetGroup.addWidget(new TJToggleButtonWidget(3, 57, 18, 18, BUTTON_BLACKLIST, this::isFilterBlacklist, this::setFilterBlacklist)
                    .setToggleTitleTooltipHoverText("cover.filter.blacklist.disabled",
                            "cover.filter.blacklist.enabled"));
            for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
                widgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 3) + 3, 18 * (i / 3) + 3, 18, 18, i, this.fluidFilter,
                        fluid -> {
                    if (fluid != null)
                        this.fluidType.put(fluid, fluid);
                }, this.fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                        .setPutFluidsPredicate(fluid -> !this.fluidType.containsKey(fluid)));
            }
            return false;
        }).setClickArea(new Rectangle(346, 107, 60, 78));
    }

    @Override
    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new TJTankWidget(this::getFluidTank, 7, 38, 18, 18)
                .setBackgroundTexture(FLUID_SLOT)
                .setContainerClicking(true, true));
    }

    private void setFilterBlacklist(boolean isFilterBlacklist) {
        this.isFilterBlacklist = isFilterBlacklist;
        this.markAsDirty();
    }

    private boolean isFilterBlacklist() {
        return this.isFilterBlacklist;
    }

    private IFluidTank getFluidTank() {
        return this.handler != null ? this.handler : DUMMY_TANK;
    }

    @Override
    public void update() {
        if (this.isWorkingEnabled && this.handler != null) {
            if (this.pumpMode == CoverPump.PumpMode.EXPORT) {
                final FluidStack enderStack = this.getFluidTank().drain(this.transferRate, false);
                if (enderStack != null && this.fluidTank.fill(enderStack, false) > 0) {
                    if (this.fluidType.containsKey(enderStack) == this.isFilterBlacklist) return;
                    this.getFluidTank().drain(this.fluidTank.fill(enderStack, true), true);
                }
            } else {
                FluidStack fluidStack = this.fluidTank.drain(this.transferRate, false);
                if (fluidStack != null && this.getFluidTank().fill(fluidStack, false) > 0) {
                    if (this.fluidType.containsKey(fluidStack) == this.isFilterBlacklist) return;
                    this.fluidTank.drain(this.getFluidTank().fill(fluidStack, true), true);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("fluidFilter", this.fluidFilter.serializeNBT());
        data.setBoolean("FilterBlacklist", this.isFilterBlacklist);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.fluidFilter.deserializeNBT(data.getCompoundTag("fluidFilter"));
        this.isFilterBlacklist = data.getBoolean("FilterBlacklist");
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final FluidStack fluidStack = this.fluidFilter.getTankAt(i).getFluid();
            if (fluidStack == null) continue;
            this.fluidType.put(fluidStack, fluidStack);
        }
    }
}
