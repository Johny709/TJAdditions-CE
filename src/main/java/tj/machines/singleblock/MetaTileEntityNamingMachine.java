package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.capability.impl.handler.INameHandler;
import tj.capability.impl.workable.NamingMachineWorkableHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;
import tj.gui.widgets.impl.RecipeOutputSlotWidget;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

import static gregtech.api.gui.GuiTextures.*;
import static tj.gui.TJGuiTextures.POWER_BUTTON;

public class MetaTileEntityNamingMachine extends TJTieredWorkableMetaTileEntity implements INameHandler {

    private final NamingMachineWorkableHandler workableHandler = new NamingMachineWorkableHandler(this);
    private final int parallel;
    private String name = "";

    public MetaTileEntityNamingMachine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.parallel = this.getTier();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityNamingMachine(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.parallel", this.parallel));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(2);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(2);
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.workableHandler.update();
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        RecipeOutputDisplayWidget displayWidget = new RecipeOutputDisplayWidget(77, 21, 21, 20)
                .setFluidOutputSupplier(this.workableHandler::getFluidOutputs)
                .setItemOutputSupplier(this.workableHandler::getItemOutputs)
                .setItemOutputInventorySupplier(this::getExportItems)
                .setFluidOutputTankSupplier(this::getExportFluids);
        return ModularUI.defaultBuilder()
                .image(-28, 0, 26, 104, GuiTextures.BORDERED_BACKGROUND)
                .image(-28, 138, 26, 26, GuiTextures.BORDERED_BACKGROUND)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(-24, 4, 18, 78, this.energyContainer::getEnergyStored, this.energyContainer::getEnergyCapacity, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setInverted(true))
                .widget(new TextFieldWidget(34, 5, 107, 18, true, this::getName, this::setName)
                        .setValidator(str -> Pattern.compile(".*").matcher(str).matches())
                        .setMaxStringLength(1024))
                .widget(new ProgressWidget(this.workableHandler::getProgressPercent, 77, 25, 21, 20, PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL))
                .widget(new SlotWidget(this.importItems, 0, 34, 26, true, true)
                        .setBackgroundTexture(SLOT))
                .widget(new SlotWidget(this.importItems, 1, 52, 26, true, true)
                        .setBackgroundTexture(SLOT))
                .widget(new SlotWidget(this.exportItems, 0, 105, 26, true, false)
                        .setBackgroundTexture(SLOT))
                .widget(new SlotWidget(this.exportItems, 1, 123, 26, true, false)
                        .setBackgroundTexture(SLOT))
                .widget(new RecipeOutputSlotWidget(0, 105, 22, 18, 18, displayWidget::getItemOutputAt, null))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, -24, 82)
                        .setBackgroundTexture(SLOT, CHARGER_OVERLAY))
                .widget(new ToggleButtonWidget(-24, 142, 18, 18, POWER_BUTTON, this.workableHandler::isWorkingEnabled, this.workableHandler::setWorkingEnabled)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(7, 62, 18, 18, BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setItemAutoOutput)
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip"))
                .widget(new ToggleButtonWidget(25, 62, 18, 18, BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setFluidAutoOutput))
                .widget(new ImageWidget(79, 42, 18, 18, INDICATOR_NO_ENERGY)
                        .setPredicate(this.workableHandler::hasNotEnoughEnergy))
                .bindPlayerInventory(player.inventory)
                .widget(displayWidget)
                .build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_ASSEMBLER_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("tagName", this.name);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.name = data.getString("tagName");
    }

    private void setName(String name) {
        this.name = name;
        this.markDirty();
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
