package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.multiblockpart.TJMultiblockAbility;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.Textures;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityMachineController extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<MetaTileEntityMachineController> {

    private boolean redstonePowered = false;
    private int Id;
    private MetaTileEntity controller;
    private boolean automatic = true;
    private boolean inverted;

    public MetaTileEntityMachineController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMachineController(metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MACHINE_CONTROLLER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.machine.machine_controller.automatic.tooltip"));
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new ImageWidget(10, 40, 150, 20, GuiTextures.DISPLAY));
        widgetGroup.addWidget(new AdvancedTextWidget(15, 45, this::addDisplayText, 0xFFFFFF));
        widgetGroup.addWidget(new CycleButtonWidget(10, 60, 50, 20, this::isAutomatic, this::setAutomatic,
                "tj.machine.machine_controller.automatic.not", "tj.machine.machine_controller.automatic")
                .setTooltipHoverString("tj.machine.machine_controller.automatic.description"));
        widgetGroup.addWidget(new CycleButtonWidget(60, 60, 50, 20, this::isInverted, this::setInverted,
                "cover.machine_controller.normal", "cover.machine_controller.inverted"));
        widgetGroup.addWidget(new ClickButtonWidget(110, 60, 25, 20, "+", this::onIncrement));
        widgetGroup.addWidget(new ClickButtonWidget(135, 60, 25, 20, "-", this::onDecrement));
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 187)
                .widget(widgetGroup)
                .label(10, 5, "cover.machine_controller.name")
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(getHolder(), player);
    }

    private void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("tj.machine.machine_controller.index", this.Id +1));
    }

    @Override
    public MultiblockAbility<MetaTileEntityMachineController> getAbility() {
        return TJMultiblockAbility.REDSTONE_CONTROLLER;
    }

    @Override
    public void registerAbilities(List<MetaTileEntityMachineController> abilityList) {
        abilityList.add(this);
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return side == getFrontFacing();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            this.redstonePowered = getInputRedstoneSignal(getFrontFacing(), false) > 0;
            if (this.controller != null) {
                if (this.controller instanceof ParallelRecipeMapMultiblockController) {
                    ((ParallelRecipeMapMultiblockController) this.controller).recipeMapWorkable.setWorkingEnabled(isInverted() == getRedstonePowered(), Id);
                }
            }
        }
    }

    private void onIncrement(Widget.ClickData clickData) {
        if (this.controller instanceof ParallelRecipeMapMultiblockController)
            setID(MathHelper.clamp(Id +1, 0, ((ParallelRecipeMapMultiblockController) this.controller).recipeMapWorkable.getSize() -1));
    }

    private void onDecrement(Widget.ClickData clickData) {
        if (this.controller instanceof ParallelRecipeMapMultiblockController)
            setID(MathHelper.clamp(Id -1, 0, ((ParallelRecipeMapMultiblockController) this.controller).recipeMapWorkable.getSize() -1));
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean getRedstonePowered() {
        return redstonePowered;
    }

    public int getId() {
        return Id;
    }

    public MetaTileEntityMachineController setID(int Id) {
        this.Id = Id;
        return this;
    }

    public void setController(MetaTileEntity controller) {
        this.controller = controller;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    @Override
    public NBTTagCompound writeToNBT (NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("RedstonePowered", this.redstonePowered);
        data.setBoolean("IsAutomatic", this.automatic);
        data.setBoolean("Inverted", this.inverted);
        data.setInteger("RedstoneId", this.Id);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.redstonePowered = data.getBoolean("RedstonePowered");
        this.automatic = data.getBoolean("IsAutomatic");
        this.inverted = data.getBoolean("Inverted");
        this.Id = data.getInteger("RedstoneId");
    }
}
