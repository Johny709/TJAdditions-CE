package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiTextures;
import gregicadditions.GAValues;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.EXPORT_FLUIDS;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.IMPORT_FLUIDS;

public class MetaTileEntitySuperFluidHatch extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidTank> {

    private final boolean isExport;
    private int ticks = 1;

    public MetaTileEntitySuperFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExport) {
        super(metaTileEntityId, tier);
        this.isExport = isExport;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntitySuperFluidHatch(metaTileEntityId, getTier(), isExport);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        int tier = getTier() / 3;
        int slots = Math.min(16, (tier + 1) * (tier + 1));
        int capacity = (int) Math.pow(2, tier - 1) * 1024000;
        capacity *= getTier() >= GAValues.UMV ? 8 : 1;
        int finalCapacity = getTier() >= GAValues.MAX ? Integer.MAX_VALUE : capacity;

        tooltip.add(I18n.format("gtadditions.machine.multi_fluid_hatch_universal.tooltip.1", finalCapacity));
        tooltip.add(I18n.format("gtadditions.machine.multi_fluid_hatch_universal.tooltip.2", slots));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        int tier = getTier() / 3;
        int slots = Math.min(16, (tier + 1) * (tier + 1));
        int capacity = (int) Math.pow(2, tier - 1) * 1024000;
        capacity *= getTier() >= GAValues.UMV ? 8 : 1;
        int finalCapacity = getTier() >= GAValues.MAX ? Integer.MAX_VALUE : capacity;

        return isExport ? super.createImportFluidHandler()
                : new FluidTankList(false, IntStream.range(0, slots)
                .mapToObj(tank -> new FluidTank(finalCapacity))
                .collect(Collectors.toList()));
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        int tier = getTier() / 3;
        int slots = Math.min(16, (tier + 1) * (tier + 1));
        int capacity = (int) Math.pow(2, tier - 1) * 1024000;
        capacity *= getTier() >= GAValues.UMV ? 8 : 1;
        int finalCapacity = getTier() >= GAValues.MAX ? Integer.MAX_VALUE : capacity;

        return isExport ? new FluidTankList(true, IntStream.range(0, slots)
                .mapToObj(tank -> new FluidTank(finalCapacity))
                .collect(Collectors.toList()))
        : super.createExportFluidHandler();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % ticks == 0) {
            if (isExport) {
                pushFluidsIntoNearbyHandlers(getFrontFacing());
            } else {
                pullFluidsFromNearbyHandlers(getFrontFacing());
            }
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        FluidTankList tank = isExport ? exportFluids : importFluids;
        int tier = Math.min(3, getTier() / 3);
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new ImageWidget(169, 81 + 18 * (tier - 1), 18, 18, GuiTextures.DISPLAY));
        widgetGroup.addWidget(new AdvancedTextWidget(170, 86 + 18 * (tier - 1), this::addDisplayText, 0xFFFFFF));
        widgetGroup.addWidget(new ToggleButtonWidget(169, 63 + 18 * (tier - 1), 18, 18, TJGuiTextures.UP_BUTTON, this::isIncrement, this::onIncrement)
                .setTooltipText("machine.universal.toggle.increment"));
        widgetGroup.addWidget(new ToggleButtonWidget(169, 99 + 18 * (tier - 1), 18, 18, TJGuiTextures.DOWN_BUTTON, this::isDecrement, this::onDecrement)
                .setTooltipText("machine.universal.toggle.decrement"));
        widgetGroup.addWidget(new ToggleButtonWidget(169, 121 + 18 * (tier - 1), 18, 18, TJGuiTextures.RESET_BUTTON, this::isReset, this::onReset)
                .setTooltipText("machine.universal.toggle.reset"));
        for (int i = 0; i < tank.getTanks(); i++) {
            widgetGroup.addWidget(new TankWidget(tank.getTankAt(i), 61 + (getTier() == 3 ? 18 : 0) + 18 * (i % (tier + 1)), 15 + 18 * (i / (tier + 1)), 18, 18)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setContainerClicking(!isExport, true));
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 196, 144 + 18 * (tier - 1))
                .label(7, 4, getMetaFullName())
                .bindPlayerInventory(player.inventory, 63 + 18 * (tier - 1))
                .widget(widgetGroup)
                .build(getHolder(), player);
    }

    private void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString(ticks + "t")
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("machine.universal.ticks.operation")))));
    }

    private void onIncrement(boolean b) {
        ticks = MathHelper.clamp(ticks + 1, 1, Integer.MAX_VALUE);
    }

    private boolean isIncrement() {
        return false;
    }

    private void onDecrement(boolean b) {
        ticks = MathHelper.clamp(ticks - 1, 1, Integer.MAX_VALUE);
    }

    private boolean isDecrement() {
        return false;
    }

    private void onReset(boolean b) {
        ticks = 1;
    }

    private boolean isReset() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (isExport) {
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return isExport ? EXPORT_FLUIDS : IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> list) {
        list.addAll(isExport ? exportFluids.getFluidTanks() : importFluids.getFluidTanks());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Ticks", ticks);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        ticks = data.hasKey("Ticks") ? data.getInteger("Ticks") : 1;
    }
}
