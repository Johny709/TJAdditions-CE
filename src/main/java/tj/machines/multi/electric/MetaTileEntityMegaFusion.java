package tj.machines.multi.electric;

import gregicadditions.GAConfig;
import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.fusion.GACryostatCasing;
import gregicadditions.item.fusion.GADivertorCasing;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.item.fusion.GAVacuumCasing;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.render.ICubeRenderer;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJValues;
import tj.blocks.AdvEnergyPortCasings;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.capability.*;
import tj.capability.impl.handler.IFusionHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.gui.TJGuiTextures;
import tj.textures.TJOrientedOverlayRenderer;
import tj.textures.TJTextures;
import tj.util.TJFluidUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.advance.MetaTileEntityAdvFusionReactor.*;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MetaTileEntityMegaFusion extends TJRecipeMapMultiblockController implements IHeatInfo, IProgressBar, IFusionHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();
    private IEnergyContainer energyContainer;
    private Recipe recipe;
    private boolean initialized;
    private long energyToStart;
    private long heat;
    private long maxHeat;
    private int parallels;
    private int coilTier;
    private int vacuumTier;
    private int divertorTier;

    public MetaTileEntityMegaFusion(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.ADV_FUSION_RECIPES, false, false);
        this.recipeLogic.setActiveConsumer(this::replaceEnergyPortsAsActive);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMegaFusion(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.mega_fusion.description"));
        tooltip.add(I18n.format("tj.multiblock.mega_fusion.coil_duration", TJValues.thousandTwoPlaceFormat.format(GAConfig.multis.advFusion.coilDurationDiscount)));
        tooltip.add(I18n.format("tj.multiblock.mega_fusion.coolant_increase", TJValues.thousandTwoPlaceFormat.format(GAConfig.multis.advFusion.vacuumCoolantIncrease)));
        tooltip.add(I18n.format("tj.multiblock.mega_fusion.energy_decrease", TJValues.thousandTwoPlaceFormat.format(GAConfig.multis.advFusion.vacuumEnergyDecrease)));
        tooltip.add(I18n.format("tj.multiblock.mega_fusion.output_increase", TJValues.thousandTwoPlaceFormat.format(GAConfig.multis.advFusion.divertorOutputIncrease)));
    }

    @Override
    protected BasicRecipeLogic<IFusionHandler> createRecipeLogic() {
        return new MegaFusionRecipeLogic(this);
    }

    @Override
    public boolean checkRecipe(Recipe recipe) {
        long energyCapacity = this.energyContainer.getEnergyCapacity();
        this.parallels = (int) (energyCapacity / (long) recipe.getProperty("eu_to_start"));
        this.recipe = recipe;
        this.maxHeat = energyCapacity;
        long energyToStart = recipe.getProperty("eu_to_start");
        int tier = recipe.getProperty("coil_tier");
        return this.energyToStart >= energyToStart && this.tier >= tier && this.heat >= energyToStart;
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {}

    @Override
    public void postOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        int recipeTier = recipe.getProperty("coil_tier");
        int coilTierDifference = this.coilTier - recipeTier;
        int vacuumTierDifference = this.vacuumTier - recipeTier;
        overclockManager.setDuration((int) Math.max(1.0, overclockManager.getDuration() * (1 - GAConfig.multis.advFusion.coilDurationDiscount * coilTierDifference)));
        overclockManager.setEUt((long) Math.max(1, overclockManager.getEUt() * (1 - vacuumTierDifference * GAConfig.multis.advFusion.vacuumEnergyDecrease)));
        overclockManager.setEUt(overclockManager.getEUt() * overclockManager.getParallel());
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        long inputEnergyStored = this.inputEnergyContainer.getEnergyStored();
        if (inputEnergyStored > 0) {
            long energyAdded = this.energyContainer.addEnergy(inputEnergyStored);
            if (energyAdded > 0)
                this.inputEnergyContainer.removeEnergy(energyAdded);
        }

        if (this.heat > this.maxHeat)
            this.heat = this.maxHeat;

        if (!this.recipeLogic.isActive() || !this.recipeLogic.isWorkingEnabled()) {
            this.heat -= Math.min(this.heat, 10000L * this.getParallel());
        }

        if (this.recipe != null && this.recipeLogic.isWorkingEnabled()) {
            long remainingHeat = this.maxHeat - this.heat;
            long energyToRemove = Math.min(remainingHeat, this.inputEnergyContainer.getInputAmperage() * this.inputEnergyContainer.getInputVoltage());
            this.heat += Math.abs(this.energyContainer.removeEnergy(energyToRemove));
        }
        if (this.getOffsetTimer() > 20 && !this.initialized) {
            this.initialized = true;
            this.writeCustomData(10, buffer -> buffer.writeInt(this.tier));
            this.markDirty();
        }
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addEnergyStoredLine(this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity(), 2)
                .customLine(text -> {
                    text.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.industrial_fusion_reactor.heat", this.heat)));
                    if (this.recipe != null) {
                        long energyToStart = this.recipe.getProperty("eu_to_start");
                        text.addTextComponent(new TextComponentTranslation("tj.multiblock.industrial_fusion_reactor.required_heat", TJValues.thousandFormat.format(energyToStart))
                                .setStyle(new Style().setColor(this.heat >= energyToStart ? TextFormatting.GREEN : TextFormatting.RED)));
                    }
                });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        List<String[]> pattern = new ArrayList<>();
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~", "~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~", "~~~~~~~~~cccc~~~~~~~~~~~~~~~~~~~~~cccc~~~~~~~~~", "~~~~~~~~~~cc~~~~~~~~~~~~~~~~~~~~~~~cc~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~cc~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~cc~~~~~~", "~~~~~cccc~~~~~~~~~~CCCCCCCCC~~~~~~~~~~cccc~~~~~", "~~~~~~cc~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~cc~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~CCCCCCCCC~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~cc~~~~~~~~~~~~~~~~~~~~~~~cc~~~~~~~~~~", "~~~~~~~~~cccc~~~~~~~~~~~~~~~~~~~~~cccc~~~~~~~~~", "~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~", "~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~BBBBBBBBB~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~BBBBBBBBBBBBBBBBB~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~BBBBBB~~~ccc~~~BBBBBB~~~~~~~~~~~~~", "~~~~~~~~~~cBBBB~~~~~~~~c~~~~~~~~BBBBc~~~~~~~~~~", "~~~~~~~~~cBBB~~~~~~~~~~~~~~~~~~~~~BBBc~~~~~~~~~", "~~~~~~~~~BBBcc~~~~~~~~~~~~~~~~~~~ccBBB~~~~~~~~~", "~~~~~~~~~BBcc~~~~~~~~~~~~~~~~~~~~~ccBB~~~~~~~~~", "~~~~~~~~BB~c~~~~~~~~~~~~~~~~~~~~~~~c~BB~~~~~~~~", "~~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~~", "~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~", "~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~", "~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~", "~~~~~~~BB~~~~~~~~~~CCCCCCCCC~~~~~~~~~~BB~~~~~~~", "~~~~~~BB~~~~~~~~~~CcccccccccC~~~~~~~~~~BB~~~~~~", "~~~~~~BB~~~~~~~~~~CcccccccccC~~~~~~~~~~BB~~~~~~", "~~~~~~BB~~~~~~~~~~CcccccccccC~~~~~~~~~~BB~~~~~~", "~~~~~cBBc~~~~~~~~~CcccccccccC~~~~~~~~~cBBc~~~~~", "~~~~ccBBcc~~~~~~~~CcccccccccC~~~~~~~~ccBBcc~~~~", "~~~~~cBBc~~~~~~~~~CcccccccccC~~~~~~~~~cBBc~~~~~", "~~~~~~BB~~~~~~~~~~CcccccccccC~~~~~~~~~~BB~~~~~~", "~~~~~~BB~~~~~~~~~~CcccccccccC~~~~~~~~~~BB~~~~~~", "~~~~~~BB~~~~~~~~~~CcccccccccC~~~~~~~~~~BB~~~~~~", "~~~~~~~BB~~~~~~~~~~CCCCCCCCC~~~~~~~~~~BB~~~~~~~", "~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~", "~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~", "~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~", "~~~~~~~~BB~~~~~~~~~~~~~~~~~~~~~~~~~~~BB~~~~~~~~", "~~~~~~~~BB~c~~~~~~~~~~~~~~~~~~~~~~~c~BB~~~~~~~~", "~~~~~~~~~BBcc~~~~~~~~~~~~~~~~~~~~~ccBB~~~~~~~~~", "~~~~~~~~~BBBcc~~~~~~~~~~~~~~~~~~~ccBBB~~~~~~~~~", "~~~~~~~~~cBBB~~~~~~~~~~~~~~~~~~~~~BBBc~~~~~~~~~", "~~~~~~~~~~cBBBB~~~~~~~~c~~~~~~~~BBBBc~~~~~~~~~~", "~~~~~~~~~~~~~BBBBBB~~~ccc~~~BBBBBB~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~BBBBBBBBBBBBBBBBB~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~BBBBBBBBB~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~s~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~eeeBBBeee~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~eeeeBBBBBBBBBeeee~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~eeBBBBBBBBBBBBBBBBBee~~~~~~~~~~~~~", "~~~~~~~~~~ceeBBBBBBBBBBBBBBBBBBBBBeec~~~~~~~~~~", "~~~~~~~~~cBBBBBBBBB~~~ccc~~~BBBBBBBBBc~~~~~~~~~", "~~~~~~~~cBBBBBB~~~~~~~~c~~~~~~~~BBBBBBc~~~~~~~~", "~~~~~~~~eBBBBc~~~~~~~~~~~~~~~~~~~cBBBBe~~~~~~~~", "~~~~~~~~eBBBcc~~~~~~~~~~~~~~~~~~~ccBBBe~~~~~~~~", "~~~~~~~eBBBcc~~~~~~~~~~~~~~~~~~~~~ccBBBe~~~~~~~", "~~~~~~~eBBB~~~~~~~~~~~~~~~~~~~~~~~~~BBBe~~~~~~~", "~~~~~~eBBB~~~~~~~~~~~~~~~~~~~~~~~~~~~BBBe~~~~~~", "~~~~~~eBBB~~~~~~~~~~~~~~~~~~~~~~~~~~~BBBe~~~~~~", "~~~~~~eBBB~~~~~~~~~CCCCCCCCC~~~~~~~~~BBBe~~~~~~", "~~~~~~eBBB~~~~~~~~CcccccccccC~~~~~~~~BBBe~~~~~~", "~~~~~eBBB~~~~~~~~CcccccccccccC~~~~~~~~BBBe~~~~~", "~~~~~eBBB~~~~~~~~CcccccccccccC~~~~~~~~BBBe~~~~~", "~~~~~eBBB~~~~~~~~CcccccccccccC~~~~~~~~BBBe~~~~~", "~~~~cBBBBc~~~~~~~CcccccccccccC~~~~~~~cBBBBc~~~~", "~~~ccBBBBcc~~~~~~CcccccccccccC~~~~~~ccBBBBcc~~~", "~~~~cBBBBc~~~~~~~CcccccccccccC~~~~~~~cBBBBc~~~~", "~~~~~eBBB~~~~~~~~CcccccccccccC~~~~~~~~BBBe~~~~~", "~~~~~eBBB~~~~~~~~CcccccccccccC~~~~~~~~BBBe~~~~~", "~~~~~eBBB~~~~~~~~CcccccccccccC~~~~~~~~BBBe~~~~~", "~~~~~~eBBB~~~~~~~~CcccccccccC~~~~~~~~BBBe~~~~~~", "~~~~~~eBBB~~~~~~~~~CCCCCCCCC~~~~~~~~~BBBe~~~~~~", "~~~~~~eBBB~~~~~~~~~~~~~~~~~~~~~~~~~~~BBBe~~~~~~", "~~~~~~eBBB~~~~~~~~~~~~~~~~~~~~~~~~~~~BBBe~~~~~~", "~~~~~~~eBBB~~~~~~~~~~~~~~~~~~~~~~~~~BBBe~~~~~~~", "~~~~~~~eBBBcc~~~~~~~~~~~~~~~~~~~~~ccBBBe~~~~~~~", "~~~~~~~~eBBBcc~~~~~~~~~~~~~~~~~~~ccBBBe~~~~~~~~", "~~~~~~~~eBBBBc~~~~~~~~~~~~~~~~~~~cBBBBe~~~~~~~~", "~~~~~~~~cBBBBBB~~~~~~~~c~~~~~~~~BBBBBBc~~~~~~~~", "~~~~~~~~~cBBBBBBBBB~~~ccc~~~BBBBBBBBBc~~~~~~~~~", "~~~~~~~~~~ceeBBBBBBBBBBBBBBBBBBBBBeec~~~~~~~~~~", "~~~~~~~~~~~~~eeBBBBBBBBBBBBBBBBBee~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~eeeeBBBBBBBBBeeee~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~eeeBBBeee~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~BBBBBBBBB~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~BBBBBBBBBBBBBBBBB~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~BBBBBBBBBBBBBBBBBBBBB~~~~~~~~~~~~~", "~~~~~~~~~~~BBBBBBBBBBBBBBBBBBBBBBBBB~~~~~~~~~~~", "~~~~~~~~~cBBBBBBBBBBBBBBBBBBBBBBBBBBBc~~~~~~~~~", "~~~~~~~~cBBBBBBBBBBBBBBBBBBBBBBBBBBBBBc~~~~~~~~", "~~~~~~~~BBBBBBBBBBB~~~ccc~~~BBBBBBBBBBB~~~~~~~~", "~~~~~~~BBBBBBBB~~~~~~~~c~~~~~~~~BBBBBBBB~~~~~~~", "~~~~~~~BBBBBBcc~~~~~~~~~~~~~~~~~ccBBBBBB~~~~~~~", "~~~~~~BBBBBBcc~~~~~~~~~~~~~~~~~~~ccBBBBBB~~~~~~", "~~~~~~BBBBBBc~~~~~~~~~~~~~~~~~~~~~cBBBBBB~~~~~~", "~~~~~BBBBBB~~~~~~~~~~~~~~~~~~~~~~~~~BBBBBB~~~~~", "~~~~~BBBBBB~~~~~~~~CCCCCCCCC~~~~~~~~BBBBBB~~~~~", "~~~~~BBBBBB~~~~~~~CcccccccccC~~~~~~~BBBBBB~~~~~", "~~~~~BBBBBB~~~~~~CcccccccccccC~~~~~~BBBBBB~~~~~", "~~~~BBBBBB~~~~~~CcccccccccccccC~~~~~~BBBBBB~~~~", "~~~~BBBBBB~~~~~~CcccccccccccccC~~~~~~BBBBBB~~~~", "~~~~BBBBBB~~~~~~CcccccccccccccC~~~~~~BBBBBB~~~~", "~~~cBBBBBBc~~~~~CcccccccccccccC~~~~~cBBBBBBc~~~", "~~ccBBBBBBcc~~~~CcccccccccccccC~~~~ccBBBBBBcc~~", "~~~cBBBBBBc~~~~~CcccccccccccccC~~~~~cBBBBBBc~~~", "~~~~BBBBBB~~~~~~CcccccccccccccC~~~~~~BBBBBB~~~~", "~~~~BBBBBB~~~~~~CcccccccccccccC~~~~~~BBBBBB~~~~", "~~~~BBBBBB~~~~~~CcccccccccccccC~~~~~~BBBBBB~~~~", "~~~~~BBBBBB~~~~~~CcccccccccccC~~~~~~BBBBBB~~~~~", "~~~~~BBBBBB~~~~~~~CcccccccccC~~~~~~~BBBBBB~~~~~", "~~~~~BBBBBB~~~~~~~~CCCCCCCCC~~~~~~~~BBBBBB~~~~~", "~~~~~BBBBBB~~~~~~~~~~~~~~~~~~~~~~~~~BBBBBB~~~~~", "~~~~~~BBBBBBc~~~~~~~~~~~~~~~~~~~~~cBBBBBB~~~~~~", "~~~~~~BBBBBBcc~~~~~~~~~~~~~~~~~~~ccBBBBBB~~~~~~", "~~~~~~~BBBBBBcc~~~~~~~~~~~~~~~~~ccBBBBBB~~~~~~~", "~~~~~~~BBBBBBBB~~~~~~~~c~~~~~~~~BBBBBBBB~~~~~~~", "~~~~~~~~BBBBBBBBBBB~~~ccc~~~BBBBBBBBBBB~~~~~~~~", "~~~~~~~~cBBBBBBBBBBBBBBBBBBBBBBBBBBBBBc~~~~~~~~", "~~~~~~~~~cBBBBBBBBBBBBBBBBBBBBBBBBBBBc~~~~~~~~~", "~~~~~~~~~~~BBBBBBBBBBBBBBBBBBBBBBBBB~~~~~~~~~~~", "~~~~~~~~~~~~~BBBBBBBBBBBBBBBBBBBBB~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~BBBBBBBBBBBBBBBBB~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~BBBBBBBBB~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~ccc~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~cV#########################Vc~~~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~RV###########VVVVVVVVV###########VR~~~~~~", "~~~~~RV########VVVV~~~ccc~~~VVVV########VR~~~~~", "~~~~~RV######VV~~~~~~~~c~~~~~~~~VV######VR~~~~~", "~~~~RV######Vcc~~~~~~~~~~~~~~~~~ccV######VR~~~~", "~~~~RV######Vc~~~~~~~~~~~~~~~~~~~cV######VR~~~~", "~~~RV######V~~~~~~~CCCCCCCCC~~~~~~~V######VR~~~", "~~~RV######V~~~~~~CcccccccccC~~~~~~V######VR~~~", "~~~RV######V~~~~~CcccccccccccC~~~~~V######VR~~~", "~~~RV######V~~~~CcccccccccccccC~~~~V######VR~~~", "~~RV######V~~~~CcccccccccccccccC~~~~V######VR~~", "~~RV######V~~~~CcccccccccccccccC~~~~V######VR~~", "~~RV######V~~~~CcccccccccccccccC~~~~V######VR~~", "~~cV######Vc~~~CcccccccccccccccC~~~cV######Vc~~", "~ccV######Vcc~~CcccccccccccccccC~~ccV######Vcc~", "~~cV######Vc~~~CcccccccccccccccC~~~cV######Vc~~", "~~RV######V~~~~CcccccccccccccccC~~~~V######VR~~", "~~RV######V~~~~CcccccccccccccccC~~~~V######VR~~", "~~RV######V~~~~CcccccccccccccccC~~~~V######VR~~", "~~~RV######V~~~~CcccccccccccccC~~~~V######VR~~~", "~~~RV######V~~~~~CcccccccccccC~~~~~V######VR~~~", "~~~RV######V~~~~~~CcccccccccC~~~~~~V######VR~~~", "~~~RV######V~~~~~~~CCCCCCCCC~~~~~~~V######VR~~~", "~~~~RV######Vc~~~~~~~~~~~~~~~~~~~cV######VR~~~~", "~~~~RV######Vcc~~~~~~~~~~~~~~~~~ccV######VR~~~~", "~~~~~RV######VV~~~~~~~~c~~~~~~~~VV######VR~~~~~", "~~~~~RV########VVVV~~~ccc~~~VVVV########VR~~~~~", "~~~~~~RV###########VVVVVVVVV###########VR~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~~~cV#########################Vc~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~RV#########################VR~~~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~RV###############################VR~~~~~~", "~~~~~RV#################################VR~~~~~", "~~~~RV#############VVVVVVVVV#############VR~~~~", "~~~~RV#########VVVV~~~ccc~~~VVVV#########VR~~~~", "~~~RV########VVc~~~~~~~c~~~~~~~cVV########VR~~~", "~~~RV########Vcc~~~~~~~c~~~~~~~ccV########VR~~~", "~~RV########Vcccc~~CCCCcCCCC~~ccccV########VR~~", "~~RV########V~~ccccccccccccccccc~~V########VR~~", "~~RV########V~~~ccccccccccccccc~~~V########VR~~", "~~RV########V~~~ccccccccccccccc~~~V########VR~~", "~RV########V~~~CcccccccccccccccC~~~V########VR~", "~RV########V~~~CcccccccccccccccC~~~V########VR~", "~RV########V~~~CcccccccccccccccC~~~V########VR~", "~cV########Vc~~CcccccccccccccccC~~cV########Vc~", "ccV########VcccccccccccccccccccccccV########Vcc", "~cV########Vc~~CcccccccccccccccC~~cV########Vc~", "~RV########V~~~CcccccccccccccccC~~~V########VR~", "~RV########V~~~CcccccccccccccccC~~~V########VR~", "~RV########V~~~CcccccccccccccccC~~~V########VR~", "~~RV########V~~~ccccccccccccccc~~~V########VR~~", "~~RV########V~~~ccccccccccccccc~~~V########VR~~", "~~RV########V~~ccccccccccccccccc~~V########VR~~", "~~RV########Vcccc~~CCCCcCCCC~~ccccV########VR~~", "~~~RV########Vcc~~~~~~~c~~~~~~~ccV########VR~~~", "~~~RV########VVc~~~~~~~c~~~~~~~cVV########VR~~~", "~~~~RV#########VVVV~~~ccc~~~VVVV#########VR~~~~", "~~~~RV#############VVVVVVVVV#############VR~~~~", "~~~~~RV#################################VR~~~~~", "~~~~~~RV###############################VR~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~~~RV#########################VR~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~~~~~c~~~~~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~RV#########################VR~~~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~cV###############################Vc~~~~~~", "~~~~~RV#################################VR~~~~~", "~~~~RV###################################VR~~~~", "~~~RV#####################################VR~~~", "~~~RV##############VVVVVVVVV##############VR~~~", "~~RV###########VVVV~~~ccc~~~VVVV###########VR~~", "~~RV##########Vcc~~~~~ccc~~~~~ccV##########VR~~", "~RV##########Vcccc~ccccccccc~ccccV##########VR~", "~RV##########VcccccccccccccccccccV##########VR~", "~RV##########V~ccccccccccccccccc~V##########VR~", "~RV##########V~~ccccccccccccccc~~V##########VR~", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "cV##########VcccccccccccccccccccccV##########Vc", "cV##########VcccccccccccccccccccccV##########Vc", "cV##########VcccccccccccccccccccccV##########Vc", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "~RV##########V~~ccccccccccccccc~~V##########VR~", "~RV##########V~ccccccccccccccccc~V##########VR~", "~RV##########VcccccccccccccccccccV##########VR~", "~RV##########Vcccc~ccccccccc~ccccV##########VR~", "~~RV##########Vcc~~~~~ccc~~~~~ccV##########VR~~", "~~RV###########VVVV~~~ccc~~~VVVV###########VR~~", "~~~RV##############VVVVVVVVV##############VR~~~", "~~~RV#####################################VR~~~", "~~~~RV###################################VR~~~~", "~~~~~RV#################################VR~~~~~", "~~~~~~cV###############################Vc~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~~~RV#########################VR~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~"});
        pattern.add(new String[]{"~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~RV#########################VR~~~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~cV###############################Vc~~~~~~", "~~~~~RV#################################VR~~~~~", "~~~~RV###################################VR~~~~", "~~~RV#####################################VR~~~", "~~~RV##############VVVVVVVVV##############VR~~~", "~~RV###########VVVV~~~ccc~~~VVVV###########VR~~", "~~RV##########Vcc~~~~~ccc~~~~~ccV##########VR~~", "~RV##########Vcccc~ccccccccc~ccccV##########VR~", "~RV##########VcccccccccccccccccccV##########VR~", "~RV##########V~ccccccccccccccccc~V##########VR~", "~RV##########V~~ccccccccccccccc~~V##########VR~", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "cV##########VcccccccccccccccccccccV##########Vc", "cV##########VcccccccccccccccccccccV##########Vc", "cV##########VcccccccccccccccccccccV##########Vc", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "RV##########V~~ccccccccccccccccc~~V##########VR", "~RV##########V~~ccccccccccccccc~~V##########VR~", "~RV##########V~ccccccccccccccccc~V##########VR~", "~RV##########VcccccccccccccccccccV##########VR~", "~RV##########Vcccc~ccccccccc~ccccV##########VR~", "~~RV##########Vcc~~~~~ccc~~~~~ccV##########VR~~", "~~RV###########VVVV~~~ccc~~~VVVV###########VR~~", "~~~RV##############VVVVVVVVV##############VR~~~", "~~~RV#####################################VR~~~", "~~~~RV###################################VR~~~~", "~~~~~RV#################################VR~~~~~", "~~~~~~cV###############################Vc~~~~~~", "~~~~~~~cV#############################Vc~~~~~~~", "~~~~~~~~cV###########################Vc~~~~~~~~", "~~~~~~~~~RV#########################VR~~~~~~~~~", "~~~~~~~~~~RVV#####################VVR~~~~~~~~~~", "~~~~~~~~~~~RRVV#################VVRR~~~~~~~~~~~", "~~~~~~~~~~~~~RRVVVV#########VVVVRR~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~RRRRVVVVVVVVVRRRR~~~~~~~~~~~~~~~", "~~~~~~~~~~~~~~~~~~~RRRcccRRR~~~~~~~~~~~~~~~~~~~"});
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(LEFT, FRONT, DOWN);
        pattern.forEach(factoryPattern::aisle);
        for (int i = 0; i < pattern.size(); i++) {
            String[] aisle = pattern.get(i);
            String[] aisle2 = new String[aisle.length];
            for (int j = 0; j < aisle.length; j++) {
                aisle2[j] = aisle[j].replace("s", "S").replace("B", "D").replace("e", "H");
            }
            pattern.set(i, aisle2);
        }
        Collections.reverse(pattern);
        pattern.forEach(factoryPattern::aisle);
        return factoryPattern.where('S', this.selfPredicate())
                .where('C', statePredicate(GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.ADV_FUSION_CASING)))
                .where('B', statePredicate(GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.FUSION_BLANKET)))
                .where('e', statePredicate(GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.FUSION_BLANKET)))
                .where('H', divertorPredicate().or(abilityPartPredicate(ALLOWED_ABILITIES)).or(this.energyPortPredicate()))
                .where('s', coilPredicate())
                .where('c', coilPredicate())
                .where('V', vacuumPredicate())
                .where('R', cryostatPredicate())
                .where('D', divertorPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    public Predicate<BlockWorldState> energyPortPredicate() {
        return (blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            Block block = blockState.getBlock();
            if (block instanceof AdvEnergyPortCasings) {
                AdvEnergyPortCasings abilityCasings = (AdvEnergyPortCasings) block;
                abilityCasings.setController(this);
                AdvEnergyPortCasings.AbilityType tieredCasingType = abilityCasings.getState(blockState);
                List<AdvEnergyPortCasings.AbilityType> currentCasing = blockWorldState.getMatchContext().getOrCreate("EnergyPort", ArrayList::new);
                Set<BlockPos> activeStates = blockWorldState.getMatchContext().getOrCreate("activeStates", HashSet::new);
                LongList amps = blockWorldState.getMatchContext().getOrCreate("EnergyAmps", LongArrayList::new);
                currentCasing.add(tieredCasingType);
                amps.add(abilityCasings.getAmps());
                activeStates.add(blockWorldState.getPos());
                return currentCasing.get(0).getName().equals(tieredCasingType.getName());
            }
            return false;
        };
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        LongList energyPortAmps = context.getOrDefault("EnergyAmps", new LongArrayList());
        List<AdvEnergyPortCasings.AbilityType> energyPorts = context.getOrDefault("EnergyPort", new ArrayList<>());
        this.activeStates.addAll(context.getOrDefault("activeStates", new HashSet<>()));
        this.divertorTier = context.getOrDefault("Divertor", GADivertorCasing.CasingType.DIVERTOR_1).getTier();
        this.coilTier = context.getOrDefault("Coil", GAFusionCasing.CasingType.ADV_FUSION_COIL_1).ordinal() - 3;
        this.vacuumTier = context.getOrDefault("Vacuum", GAVacuumCasing.CasingType.VACUUM_1).getTier();
        int cryostat = context.getOrDefault("Cryostat", GACryostatCasing.CasingType.CRYOSTAT_1).getTier();
        int fusionTier = Math.min(this.divertorTier, Math.min(this.coilTier, Math.min(this.vacuumTier, cryostat)));
        this.tier = fusionTier + GAValues.UV;
        this.maxVoltage = 8L << this.tier * 2;
        long energyCapacity = 0;
        for (int i = 0; i < energyPortAmps.size(); i++) {
            energyCapacity += (long) (100000000 * energyPortAmps.get(i) * Math.pow(2, energyPorts.get(i).getTier() - GAValues.UHV));
        }
        for (IEnergyContainer container : this.getAbilities(INPUT_ENERGY)) {
            energyCapacity += (long) (100000000 * container.getInputAmperage() * Math.pow(2, GAUtility.getTierByVoltage(container.getInputVoltage()) - GAValues.UHV));
        }
        this.energyToStart = 1_600_000_000L << fusionTier - 1;
        this.energyContainer = new EnergyContainerHandler(this, energyCapacity, GAValues.V[this.tier], 0, 0, 0);
        this.initialized = false;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.activeStates.clear();
        this.divertorTier = 0;
        this.coilTier = 0;
        this.vacuumTier = 0;
        this.energyToStart = 0;
    }

    @Override
    public TJOrientedOverlayRenderer getFrontalOverlay() {
        return TJTextures.TJ_FUSION_REACTOR_OVERLAY;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        switch (this.tier) {
            case 1: return this.recipeLogic.isActive() ? TJTextures.ADV_FUSION_PORT_UHV_ACTIVE : TJTextures.ADV_FUSION_PORT_UHV;
            case 2: return this.recipeLogic.isActive() ? TJTextures.ADV_FUSION_PORT_UEV_ACTIVE : TJTextures.ADV_FUSION_PORT_UEV;
            case 3: return this.recipeLogic.isActive() ? TJTextures.ADV_FUSION_PORT_UIV_ACTIVE : TJTextures.ADV_FUSION_PORT_UIV;
            case 4: return this.recipeLogic.isActive() ? TJTextures.ADV_FUSION_PORT_UMV_ACTIVE : TJTextures.ADV_FUSION_PORT_UMV;
            case 5: return this.recipeLogic.isActive() ? TJTextures.ADV_FUSION_PORT_UXV_ACTIVE : TJTextures.ADV_FUSION_PORT_UXV;
            case 6: return this.recipeLogic.isActive() ? TJTextures.ADV_FUSION_PORT_MAX_ACTIVE : TJTextures.ADV_FUSION_PORT_MAX;
            default: return ClientHandler.FUSION_TEXTURE;
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.writeActiveBlockPacket(buf, this.recipeLogic.isActive());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.readActiveBlockPacket(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 10) {
            this.tier = buf.readInt();
            this.scheduleRenderUpdate();
        } else if (dataId == 128) {
            this.readActiveBlockPacket(buf);
        }
    }

    private void writeActiveBlockPacket(PacketBuffer buffer, boolean isActive) {
        buffer.writeBoolean(isActive);
        buffer.writeInt(this.activeStates.size());
        for (BlockPos pos : this.activeStates) {
            buffer.writeBlockPos(pos);
        }
    }

    private void readActiveBlockPacket(PacketBuffer buffer) {
        boolean isActive = buffer.readBoolean();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buffer.readBlockPos();
            IBlockState state = this.getWorld().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof AdvEnergyPortCasings) {
                state = state.withProperty(AdvEnergyPortCasings.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            }
        }
    }

    public void replaceEnergyPortsAsActive(boolean isActive) {
        this.writeCustomData(128, buffer -> this.writeActiveBlockPacket(buffer, isActive));
        if (!isActive)
            this.recipe = null;
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceEnergyPortsAsActive(false);
            this.markDirty();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
         super.writeToNBT(data);
         data.setLong("heat", this.heat);
         data.setLong("maxHeat", this.maxHeat);
         return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.heat = data.getLong("heat");
        this.maxHeat = data.getLong("maxHeat");
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == TJCapabilities.CAPABILITY_HEAT)
            return TJCapabilities.CAPABILITY_HEAT.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::heat).setMaxProgress(this::maxHeat)
                .setBarTexture(TJGuiTextures.BAR_RED)
                .setLocale("tj.multiblock.bars.heat"));
    }

    @Override
    public long heat() {
        return this.heat;
    }

    @Override
    public long maxHeat() {
        return this.maxHeat;
    }

    @Override
    public int getParallel() {
        return this.parallels;
    }

    @Override
    public IItemHandlerModifiable getInputBus(int index) {
        return this.getImportItemInventory();
    }

    @Override
    public int getDiverterTier() {
        return this.divertorTier;
    }

    @Override
    public int getVacuumTier() {
        return this.vacuumTier;
    }

    private static class MegaFusionRecipeLogic extends BasicRecipeLogic<IFusionHandler> {

        public MegaFusionRecipeLogic(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        @Override
        protected int checkFluidInputsAmount(int parallels, Recipe recipe) {
            int vacuumTierDifference = this.handler.getVacuumTier() - (int) recipe.getProperty("coil_tier");
            int size = ((IGTRecipe) recipe).getMergedFluidInputs().size();
            for (int i = 0; i < size; i++) {
                FluidStack fluid = ((IGTRecipe) recipe).getMergedFluidInputs().get(i);
                int amount = i == size - 1 ? (int) (fluid.amount * (1 + vacuumTierDifference * GAConfig.multis.advFusion.vacuumCoolantIncrease)) : fluid.amount;
                if (amount > 0) {
                    parallels = Math.min(parallels, TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), fluid, amount * parallels, false) / amount);
                    if (parallels < 1) return 0;
                } else if (!TJFluidUtils.findFluidFromTanks(this.handler.getImportFluidTank(), fluid))
                    return 0;
            }
            return parallels;
        }

        @Override
        protected void consumeFluidInputs(int parallels, Recipe recipe) {
            int vacuumTierDifference = this.handler.getVacuumTier() - (int) recipe.getProperty("coil_tier");
            for (int i = 0; i < ((IGTRecipe) recipe).getMergedFluidInputs().size(); i++) {
                FluidStack fluid = ((IGTRecipe) recipe).getMergedFluidInputs().get(i).copy();
                if (recipe.getFluidInputs().size() == 3)
                    fluid.amount = i == 1 ? (int) (fluid.amount * (1 + vacuumTierDifference * GAConfig.multis.advFusion.vacuumCoolantIncrease)) : fluid.amount;
                fluid.amount *= parallels;
                TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), fluid, fluid.amount, true);
                this.getFluidInputs().add(fluid);
            }
        }

        @Override
        protected void addFluidOutputs(int parallels, Recipe recipe) {
            int recipeTier = recipe.getProperty("coil_tier");
            int divertorTierDifference = this.handler.getDiverterTier() - recipeTier;
            int vacuumTierDifference = this.handler.getVacuumTier() - recipeTier;
            for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
                FluidStack fluid = recipe.getFluidOutputs().get(i).copy();
                if (i == 0) {
                    fluid.amount = (int) (fluid.amount * (1 + divertorTierDifference * GAConfig.multis.advFusion.divertorOutputIncrease));
                } else if (i == recipe.getFluidOutputs().size() - 1) {
                    fluid.amount = (int) (fluid.amount * (1 + vacuumTierDifference * GAConfig.multis.advFusion.vacuumCoolantIncrease));
                }
                fluid.amount *= parallels;
                this.getFluidOutputs().add(fluid);
            }
        }
    }
}
