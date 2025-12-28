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
import gregicadditions.recipes.impl.AdvFusionRecipeBuilder;
import gregicadditions.utils.GALog;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
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
import tj.builder.handlers.IFusionProvider;
import tj.builder.multicontrollers.MultiblockDisplayBuilder;
import tj.builder.multicontrollers.TJLargeSimpleRecipeMapMultiblockControllerBase;
import tj.capability.IHeatInfo;
import tj.capability.TJCapabilities;
import tj.textures.TJTextures;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.advance.MetaTileEntityAdvFusionReactor.*;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MetaTileEntityMegaFusion extends TJLargeSimpleRecipeMapMultiblockControllerBase implements IFusionProvider, IHeatInfo {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final Set<BlockPos> activeStates = new HashSet<>();
    private IEnergyContainer inputEnergyContainers;
    private Recipe recipe;
    private boolean initialized;
    private long energyToStart;
    private long heat;
    private long maxHeat;
    private int parallels;
    private int tier;
    private int coilTier;
    private int vacuumTier;
    private int divertorTier;

    public MetaTileEntityMegaFusion(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GARecipeMaps.ADV_FUSION_RECIPES, 100, 100, 100, 16);
        this.recipeMapWorkable = new MegaFusionRecipeLogic(this, 100, 100, 100, 1);
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
        TooltipHelper.shiftText(tooltip, tip -> super.addInformation(stack, player, tip, advanced));
    }

    @Override
    public boolean checkRecipe(Recipe recipe, boolean consumeIfSuccess) {
        long energyToStart = recipe.getProperty("eu_to_start");
        int tier = recipe.getProperty("coil_tier");
        return this.tier >= tier && this.heat >= energyToStart;
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        long inputEnergyStored = this.inputEnergyContainers.getEnergyStored();
        if (inputEnergyStored > 0) {
            long energyAdded = this.energyContainer.addEnergy(inputEnergyStored);
            if (energyAdded > 0)
                this.inputEnergyContainers.removeEnergy(energyAdded);
        }

        if (this.heat > this.maxHeat)
            this.heat = this.maxHeat;

        if (!this.recipeMapWorkable.isActive() || !this.recipeMapWorkable.isWorkingEnabled()) {
            this.heat -= Math.min(this.heat, 10000L * this.getParallels());
        }

        if (this.recipe != null && this.recipeMapWorkable.isWorkingEnabled()) {
            long remainingHeat = this.maxHeat - this.heat;
            long energyToRemove = Math.min(remainingHeat, this.inputEnergyContainers.getInputAmperage() * this.inputEnergyContainers.getInputVoltage());
            this.heat += Math.abs(this.energyContainer.removeEnergy(energyToRemove));
        }
        if (this.getOffsetTimer() > 20 && !this.initialized) {
            this.initialized = true;
            this.writeCustomData(10, buffer -> buffer.writeInt(this.tier));
            this.markDirty();
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (this.isStructureFormed()) {
            MultiblockDisplayBuilder.start(textList)
                    .voltageIn(this.energyContainer)
                    .energyStored(this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity())
                    .custom(text -> {
                        text.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.industrial_fusion_reactor.message", this.getParallels())));
                        text.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.industrial_fusion_reactor.heat", this.heat)));
                        if (this.recipe != null) {
                            long energyToStart = this.recipe.getProperty("eu_to_start");
                            text.add(new TextComponentTranslation("tj.multiblock.industrial_fusion_reactor.required_heat", TJValues.thousandFormat.format(energyToStart))
                                    .setStyle(new Style().setColor(this.heat >= energyToStart ? TextFormatting.GREEN : TextFormatting.RED)));
                        }
                        if (this.recipeMapWorkable.isHasNotEnoughEnergy()) {
                            text.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
                        }
                    })
                    .isWorking(this.recipeMapWorkable.isWorkingEnabled(), this.recipeMapWorkable.isActive(), this.recipeMapWorkable.getProgress(), this.recipeMapWorkable.getMaxProgress());
        }
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
                LongList amps = blockWorldState.getMatchContext().getOrCreate("EnergyAmps", LongArrayList::new);
                currentCasing.add(tieredCasingType);
                amps.add(abilityCasings.getAmps());
                if (currentCasing.get(0).getName().equals(tieredCasingType.getName()) && blockWorldState.getWorld() != null) {
                    this.activeStates.add(blockWorldState.getPos());
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public int getParallels() {
        return this.parallels;
    }

    @Override
    public long getEnergyToStart() {
        return this.energyToStart;
    }

    @Override
    public void setRecipe(long heat, Recipe recipe) {
        if (recipe != null) {
            long energyCapacity = this.energyContainer.getEnergyCapacity();
            this.parallels = (int) (energyCapacity / (long) recipe.getProperty("eu_to_start"));
            this.recipe = recipe;
            this.maxHeat = energyCapacity;
        }
    }

    @Override
    public void replaceEnergyPortsAsActive(boolean active) {
        this.activeStates.forEach(pos -> {
            IBlockState state = this.getWorld().getBlockState(pos);
            if (state.getBlock() instanceof AdvEnergyPortCasings) {
                state = state.withProperty(AdvEnergyPortCasings.ACTIVE, active);
                this.getWorld().setBlockState(pos, state);
            }
        });
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceEnergyPortsAsActive(false);
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        LongList energyPortAmps = context.getOrDefault("EnergyAmps", new LongArrayList());
        List<AdvEnergyPortCasings.AbilityType> energyPorts = context.getOrDefault("EnergyPort", new ArrayList<>());
        this.divertorTier = context.getOrDefault("Divertor", GADivertorCasing.CasingType.DIVERTOR_1).getTier();
        this.coilTier = context.getOrDefault("Coil", GAFusionCasing.CasingType.ADV_FUSION_COIL_1).ordinal() - 3;
        this.vacuumTier = context.getOrDefault("Vacuum", GAVacuumCasing.CasingType.VACUUM_1).getTier();
        int cryostat = context.getOrDefault("Cryostat", GACryostatCasing.CasingType.CRYOSTAT_1).getTier();
        this.tier = Math.min(this.divertorTier, Math.min(this.coilTier, Math.min(this.vacuumTier, cryostat)));
        this.maxVoltage = (long) (Math.pow(4, this.tier + GAValues.UV) * 8);
        long energyCapacity = 0;
        for (int i = 0; i < energyPortAmps.size(); i++) {
            energyCapacity += (long) (100000000 * energyPortAmps.get(i) * Math.pow(2, energyPorts.get(i).getTier() - GAValues.UHV));
        }
        for (IEnergyContainer container : this.getAbilities(INPUT_ENERGY)) {
            energyCapacity += (long) (100000000 * container.getInputAmperage() * Math.pow(2, GAUtility.getTierByVoltage(container.getInputVoltage()) - GAValues.UHV));
        }
        this.energyToStart = (long) (Math.pow(2, this.tier - 1) * 1_600_000_000);
        this.inputEnergyContainers = new EnergyContainerList(this.getAbilities(INPUT_ENERGY));
        this.inputFluidInventory = new FluidTankList(true, this.getAbilities(IMPORT_FLUIDS));
        this.outputFluidInventory = new FluidTankList(true, this.getAbilities(EXPORT_FLUIDS));
        this.energyContainer = new EnergyContainerHandler(this, energyCapacity, GAValues.V[this.tier + GAValues.UV], 0, 0, 0);
        this.initialized = false;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.FUSION_REACTOR_OVERLAY;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        switch (this.tier) {
            case 1: return this.recipeMapWorkable.isActive() ? TJTextures.ADV_FUSION_PORT_UHV_ACTIVE : TJTextures.ADV_FUSION_PORT_UHV;
            case 2: return this.recipeMapWorkable.isActive() ? TJTextures.ADV_FUSION_PORT_UEV_ACTIVE : TJTextures.ADV_FUSION_PORT_UEV;
            case 3: return this.recipeMapWorkable.isActive() ? TJTextures.ADV_FUSION_PORT_UIV_ACTIVE : TJTextures.ADV_FUSION_PORT_UIV;
            case 4: return this.recipeMapWorkable.isActive() ? TJTextures.ADV_FUSION_PORT_UMV_ACTIVE : TJTextures.ADV_FUSION_PORT_UMV;
            case 5: return this.recipeMapWorkable.isActive() ? TJTextures.ADV_FUSION_PORT_UXV_ACTIVE : TJTextures.ADV_FUSION_PORT_UXV;
            case 6: return this.recipeMapWorkable.isActive() ? TJTextures.ADV_FUSION_PORT_MAX_ACTIVE : TJTextures.ADV_FUSION_PORT_MAX;
            default: return ClientHandler.FUSION_TEXTURE;
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 10) {
            this.tier = buf.readInt();
            this.scheduleRenderUpdate();
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
    public long heat() {
        return this.heat;
    }

    @Override
    public long maxHeat() {
        return this.maxHeat;
    }

    private class MegaFusionRecipeLogic extends LargeSimpleMultiblockRecipeLogic {
        private final int EUtPercentage;
        private final int durationPercentage;
        private final IFusionProvider fusionReactor;

        public MegaFusionRecipeLogic(RecipeMapMultiblockController tileEntity, int EUtPercentage, int durationPercentage, int chancePercentage, int stack) {
            super(tileEntity, EUtPercentage, durationPercentage, chancePercentage, stack);
            this.fusionReactor = (IFusionProvider) tileEntity;
            this.EUtPercentage = EUtPercentage;
            this.durationPercentage = durationPercentage;
            this.recipeMap = tileEntity.recipeMap;
            this.allowOverclocking = true;
        }

        @Override
        protected void completeRecipe() {
            super.completeRecipe();
            this.fusionReactor.setRecipe(0L, null);
        }

        @Override
        protected Recipe createRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, Recipe matchingRecipe) {
            int EUt = matchingRecipe.getEUt();
            int duration = matchingRecipe.getDuration();
            int minMultiplier = Integer.MAX_VALUE;
            long recipeEnergy = matchingRecipe.getProperty("eu_to_start");

            this.fusionReactor.setRecipe(recipeEnergy, matchingRecipe);
            RecipeBuilder<?> newRecipe = this.recipeMap.recipeBuilder();
            int recipeTier = matchingRecipe.getIntegerProperty("coil_tier");
            int coilTierDifference = coilTier - recipeTier;
            int vacuumTierDifference = vacuumTier - recipeTier;
            int divertorTierDifference = divertorTier - recipeTier;
            newRecipe.fluidInputs(matchingRecipe.getFluidInputs().get(0), matchingRecipe.getFluidInputs().get(1));
            FluidStack newOutput = matchingRecipe.getFluidOutputs().get(0).copy();
            newOutput.amount = (int) (newOutput.amount * (1 + divertorTierDifference * GAConfig.multis.advFusion.divertorOutputIncrease));
            newRecipe.fluidOutputs(newOutput);
            newRecipe.duration(duration);
            newRecipe.EUt(EUt);

            if (matchingRecipe.getFluidInputs().size() == 3) {

                FluidStack newFluid = matchingRecipe.getFluidInputs().get(2).copy();
                newFluid.amount = (int) (newFluid.amount * (1 + vacuumTierDifference * GAConfig.multis.advFusion.vacuumCoolantIncrease));
                newRecipe.fluidInputs(newFluid);

                newOutput = matchingRecipe.getFluidOutputs().get(1).copy();
                newOutput.amount = (int) (newOutput.amount * (1 + divertorTierDifference * GAConfig.multis.advFusion.vacuumCoolantIncrease));
                newRecipe.fluidOutputs(newOutput);
            }
            Recipe modifiedRecipe = newRecipe.build().getResult();
            Map<String, Integer> countFluid = new HashMap<>();
            if (!modifiedRecipe.getFluidInputs().isEmpty()) {

                this.findFluid(countFluid, fluidInputs);
                minMultiplier = Math.min(minMultiplier, this.getMinRatioFluid(countFluid, modifiedRecipe, this.fusionReactor.getParallels() * this.fusionReactor.getBatchMode().getAmount()));
            }

            if (minMultiplier == Integer.MAX_VALUE) {
                GALog.logger.error("Cannot calculate ratio of items for large multiblocks");
                return null;
            }

            List<FluidStack> newFluidInputs = new ArrayList<>();
            List<FluidStack> outputF = new ArrayList<>();
            multiplyInputsAndOutputs(newFluidInputs, outputF, modifiedRecipe, minMultiplier);

            newRecipe = this.recipeMap.recipeBuilder();
            ((AdvFusionRecipeBuilder) newRecipe).euStart(recipeEnergy)
                    .euReturn(matchingRecipe.getIntegerProperty("eu_return"))
                    .coilTier(recipeTier)
                    .fluidInputs(newFluidInputs)
                    .fluidOutputs(outputF)
                    .EUt((int) Math.max(1, (EUt * (1 - vacuumTierDifference * GAConfig.multis.advFusion.vacuumEnergyDecrease) * this.EUtPercentage * minMultiplier / 100.0) / this.fusionReactor.getBatchMode().getAmount()))
                    .duration((int) Math.max(1, (duration * (1 - GAConfig.multis.advFusion.coilDurationDiscount * coilTierDifference) * (this.durationPercentage / 100.0)) * this.fusionReactor.getBatchMode().getAmount()));

            return newRecipe.build().getResult();
        }

        private void multiplyInputsAndOutputs(List<FluidStack> newFluidInputs, List<FluidStack> outputF, Recipe recipe, int multiplier) {
            for (FluidStack fluidS : recipe.getFluidInputs()) {
                FluidStack newFluid = new FluidStack(fluidS.getFluid(), fluidS.amount * multiplier);
                newFluidInputs.add(newFluid);
            }
            for (FluidStack fluid : recipe.getFluidOutputs()) {
                int fluidNum = fluid.amount * multiplier;
                FluidStack fluidCopy = fluid.copy();
                fluidCopy.amount = fluidNum;
                outputF.add(fluidCopy);
            }
        }

        @Override
        protected void setActive(boolean active) {
            this.fusionReactor.replaceEnergyPortsAsActive(active);
            super.setActive(active);
        }
    }
}
