package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJValues;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public class MetaTileEntityCreativeEnergyHatch extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer> {

    private final EnergyContainerHandler energyContainer = new EnergyContainerHandler(this, Long.MAX_VALUE, 0, 0, 0, 0) {

        @Override
        public long changeEnergy(long energyToAdd) {
            return energyToAdd;
        }

        @Override
        public long getEnergyStored() {
            return energyStored;
        }

        @Override
        public long getInputVoltage() {
            return inputVoltage;
        }

        @Override
        public long getInputAmperage() {
            return inputAmps;
        }
    };

    private long energyStored = Long.MAX_VALUE;
    private long inputVoltage = Integer.MAX_VALUE;
    private long inputAmps = 2;

    public MetaTileEntityCreativeEnergyHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GAValues.MAX);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCreativeEnergyHatch(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("cover.creative.only"));
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 196, 170)
                .widget(new TJLabelWidget(7, -19, 180, 19, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new NewTextFieldWidget<>(10, 14, 87, 18, () -> String.valueOf(this.energyStored), (text, id) -> this.energyStored = (long) Math.min(Long.MAX_VALUE, Math.max(0, Double.parseDouble(text))))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setBackgroundText("metaitem.creative_energy_cover.set.energy_rate")
                        .setTooltipText("metaitem.creative_energy_cover.set.energy_rate")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(20)
                        .enableBackground(true))
                .widget(new NewTextFieldWidget<>(10, 34, 87, 18, () -> String.valueOf(this.inputVoltage), (text, id) -> this.inputVoltage = (long) Math.min(Long.MAX_VALUE, Math.max(0, Double.parseDouble(text))))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setBackgroundText("metaitem.creative_energy_cover.set.voltage")
                        .setTooltipText("metaitem.creative_energy_cover.set.voltage")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(20)
                        .enableBackground(true))
                .widget(new NewTextFieldWidget<>(10, 54,87, 18, () -> String.valueOf(this.inputAmps), (text, id) -> this.inputAmps = Math.min(4294967295L, Math.max(0, Long.parseLong(text))))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setBackgroundText("metaitem.creative_energy_cover.set.amps")
                        .setTooltipText("metaitem.creative_energy_cover.set.amps")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11)
                        .enableBackground(true))
                .widget(new TJProgressBarWidget(97, 7, 72, 72, this.energyContainer::getEnergyStored, this.energyContainer::getEnergyCapacity, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setInverted(true))
                .bindPlayerInventory(player.inventory, 86)
                .build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            final int oldBaseColor = renderState.baseColour;
            final int oldAlphaOverride = renderState.alphaOverride;

            renderState.baseColour = TJValues.VC[this.getTier() - 2] << 8; // TODO get better MAX color overlay. use UMV color overlay for the time being
            renderState.alphaOverride = 0xFF;

            for (EnumFacing facing : EnumFacing.VALUES)
                TJTextures.SUPER_HATCH_OVERLAY.renderSided(facing, renderState, translation, pipeline);

            renderState.baseColour = oldBaseColor;
            renderState.alphaOverride = oldAlphaOverride;
        }
        Textures.ENERGY_IN_MULTI.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(List<IEnergyContainer> list) {
        list.add(this.energyContainer);
    }
}
