package tj.machines.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import tj.TJConfig;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.textures.TJTextures;
import gregicadditions.item.components.MotorCasing;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static gregicadditions.GAMaterials.TungstenTitaniumCarbide;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.motorPredicate;
import static gregtech.api.unification.material.Materials.DrillingFluid;

public class MetaTileEntityWorldDestroyer extends MetaTileEntityEliteLargeMiner {

    private int chunkArea = 1, chunkAreaLimit;

    public MetaTileEntityWorldDestroyer(ResourceLocation metaTileEntityId, Type type) {
        super(metaTileEntityId, type);
        isWorkingEnabled = false;
    }

    @Override
    public IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(TungstenTitaniumCarbide).getDefaultState();
    }

    @Override
    public IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.TUNGSTEN_TITANIUM_CARBIDE_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.TUNGSTEN_TITANIUM_CARBIDE;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("F###F", "F###F", "PQQQP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("#####", "#####", "QPPPQ", "#CCC#", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("#####", "#####", "QPMPQ", "#CPC#", "#FFF#", "#FFF#", "#FFF#", "##F##", "##F##", "##F##")
                .aisle("#####", "#####", "CPPPQ", "#CSC#", "#####", "#####", "#####", "#####", "#####", "#####")
                .aisle("F###F", "F###F", "PQQQP", "#####", "#####", "#####", "#####", "#####", "#####", "#####")
                .setAmountAtLeast('L', 3)
                .where('S', selfPredicate())
                .where('L', statePredicate(getCasingState()))
                .where('C', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', statePredicate(getCasingState()))
                .where('M', motorPredicate())
                .where('Q', statePredicate(getCasingState()).or(abilityPartPredicate(MultiblockAbility.EXPORT_ITEMS)))
                .where('F', statePredicate(getFrameState()))
                .where('#', blockWorldState -> true)
                .build();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityWorldDestroyer(metaTileEntityId, getType());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.elite_large_miner.description", type.chunk, type.chunk, type.fortuneString));
        tooltip.add(I18n.format("gtadditions.machine.miner.fluid_usage", type.drillingFluidConsumePerTick, I18n.format(DrillingFluid.getFluid(0).getUnlocalizedName())));
        tooltip.add(I18n.format("tj.multiblock.elite_large_miner.chunk.info"));
        tooltip.add(I18n.format("tj.multiblock.elite_large_miner.filter.warning"));
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (playerIn.getHeldItemMainhand().isItemEqual(MetaItems.HARD_HAMMER.getStackForm())) {
            if (!getWorld().isRemote) {
                if (!playerIn.isSneaking()) {
                    if (chunkArea < chunkAreaLimit) {
                        chunkArea++;
                        playerIn.sendMessage(new TextComponentTranslation("tj.multiblock.elite_large_miner.chunk.increase")
                                .appendSibling(new TextComponentString(" " + chunkArea + "x" + chunkArea).setStyle(new Style().setColor(TextFormatting.YELLOW))));
                    } else {
                        playerIn.sendMessage(new TextComponentTranslation("tj.multiblock.elite_large_miner.chunk.max")
                                .appendSibling(new TextComponentString(" " + chunkArea + "x" + chunkArea).setStyle(new Style().setColor(TextFormatting.YELLOW))));
                    }
                } else {
                    if (chunkArea > 1) {
                        chunkArea--;
                        playerIn.sendMessage(new TextComponentTranslation("tj.multiblock.elite_large_miner.chunk.decrease")
                                .appendSibling(new TextComponentString(" " + chunkArea + "x" + chunkArea).setStyle(new Style().setColor(TextFormatting.YELLOW))));
                    } else {
                        playerIn.sendMessage(new TextComponentTranslation("tj.multiblock.elite_large_miner.chunk.min")
                                .appendSibling(new TextComponentString(" " + chunkArea + "x" + chunkArea).setStyle(new Style().setColor(TextFormatting.YELLOW))));
                    }
                }
                done = false;
                isWorkingEnabled = false;
                if (!chunks.isEmpty()) {
                    currentChunk.set(0);
                    x.set(chunks.get(currentChunk.intValue()).getPos().getXStart());
                    z.set(chunks.get(currentChunk.intValue()).getPos().getZStart());
                    y.set(maxY.get());
                    chunks.clear();
                }
            }
            return true;
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        chunkAreaLimit = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier() * TJConfig.worldDestroyerMiner.worldDestroyerChunkMultiplier;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        chunkAreaLimit = 0;
        chunkArea = 1;

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("ChunkLimit", chunkAreaLimit);
        data.setInteger("ChunkMultiplier", chunkArea);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        chunkAreaLimit = data.getInteger("ChunkLimit");
        chunkArea = data.getInteger("ChunkMultiplier");
    }
}
