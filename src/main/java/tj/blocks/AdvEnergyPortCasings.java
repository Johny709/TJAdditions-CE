package tj.blocks;

import gregicadditions.GAUtility;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityEnergyHatch;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.VariantBlock;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityEnergyHatch;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.mixin.gregicality.IGAMetaTileEntityEnergyHatchMixin;
import tj.mixin.gregtech.IMetaTileEntityEnergyHatchMixin;

import javax.annotation.Nullable;
import java.util.List;

public class AdvEnergyPortCasings extends VariantBlock<AdvEnergyPortCasings.AbilityType> implements IBlockController {

    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    private final int amps;
    private MultiblockControllerBase controller;

    public AdvEnergyPortCasings(int amps) {
        super(Material.IRON);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setTranslationKey(amps == 2 ? "energy_casing" : "energy_casing." + amps);
        this.setRegistryName(amps == 2 ? "energy_casing" : "energy_casing." + amps);
        this.setDefaultState(getState(AbilityType.ENERGY_PORT_UHV).withProperty(ACTIVE, false));
        this.amps = amps;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        super.createBlockState();
        return new BlockStateContainer(this, VARIANT, ACTIVE);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(EnumHand.MAIN_HAND);
        if (worldIn.isRemote)
            return true;
        if (stack.isEmpty()) {
            if (playerIn.isSneaking() && this.amps != 2) {
                worldIn.setBlockState(pos, TJMetaBlocks.ADV_ENERGY_PORT_CASING.getState(state.getValue(VARIANT)));
                worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), this.getEnergyHatch(state.getValue(VARIANT).getTier(), this.amps)));
                if (this.controller != null)
                    this.controller.invalidateStructure();
            } else playerIn.sendMessage(new TextComponentTranslation("tj.machine.energy_hatch.current_amps", this.amps));
            return true;
        }
        boolean isExport = false;
        IEnergyContainer energyContainer = null;
        MetaTileEntity tileEntity = GregTechAPI.META_TILE_ENTITY_REGISTRY.getObjectById(stack.getMetadata());
        if (tileEntity instanceof MetaTileEntityEnergyHatch) {
            isExport = ((IMetaTileEntityEnergyHatchMixin) tileEntity).isExport();
            energyContainer = ((IMetaTileEntityEnergyHatchMixin) tileEntity).getEnergyContainer();
        } else if (tileEntity instanceof GAMetaTileEntityEnergyHatch) {
            isExport = ((IGAMetaTileEntityEnergyHatchMixin) tileEntity).isExport();
            energyContainer = ((IGAMetaTileEntityEnergyHatchMixin) tileEntity).getEnergyContainer();
        }
        if (energyContainer == null) {
            playerIn.sendMessage(new TextComponentTranslation("tj.machine.energy_hatch.not_found"));
            return true;
        }
        if (isExport) {
            playerIn.sendMessage(new TextComponentTranslation("tj.machine.energy_hatch.not_export"));
            return true;
        }
        if (state.getValue(VARIANT).getTier() != GAUtility.getTierByVoltage(energyContainer.getInputVoltage())) {
            playerIn.sendMessage(new TextComponentTranslation("tj.machine.energy_hatch.not_tier"));
            return true;
        }
        long amps = energyContainer.getInputAmperage();
        TJMetaBlocks.ADV_ENERGY_PORT_CASINGS.forEach(block -> {
            if (amps == block.getAmps() && this.amps != amps) {
                worldIn.setBlockState(pos, block.getState(state.getValue(VARIANT)));
                playerIn.sendMessage(new TextComponentTranslation("tj.machine.energy_hatch.set", amps));
                stack.shrink(1);
                if (this.amps < 2)
                    worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), this.getEnergyHatch(state.getValue(VARIANT).getTier(), this.amps)));
                if (this.controller != null)
                    this.controller.invalidateStructure();
            }
        });
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (this.amps > 2) {
            drops.add(this.getEnergyHatch(state.getValue(VARIANT).getTier(), this.getAmps()));
            drops.add(TJMetaBlocks.ADV_ENERGY_PORT_CASING.getItemVariant(state.getValue(VARIANT)));
        } else super.getDrops(drops, world, pos, state, fortune);
    }

    private ItemStack getEnergyHatch(int tier, int amps) {
        for (MetaTileEntity metaTileEntity : GregTechAPI.META_TILE_ENTITY_REGISTRY) {
            if (metaTileEntity instanceof GAMetaTileEntityEnergyHatch && tier == ((GAMetaTileEntityEnergyHatch) metaTileEntity).getTier() && amps == ((IGAMetaTileEntityEnergyHatchMixin) metaTileEntity).getEnergyContainer().getInputAmperage()) {
                return metaTileEntity.getStackForm();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta % 6).withProperty(ACTIVE, meta / 6 >= 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) + (state.getValue(ACTIVE) ? 6 : 0);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return super.damageDropped(state) - (state.getValue(ACTIVE) ? 6 : 0);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tile.ability_casing.energy_port.description"));
    }

    public int getAmps() {
        return this.amps;
    }

    @Override
    public void setController(MultiblockControllerBase controller) {
        this.controller = controller;
    }

    public enum AbilityType implements IStringSerializable {

        ENERGY_PORT_UHV("energy_port_uhv", 9),
        ENERGY_PORT_UEV("energy_port_uev", 10),
        ENERGY_PORT_UIV("energy_port_uiv", 11),
        ENERGY_PORT_UMV("energy_port_umv", 12),
        ENERGY_PORT_UVX("energy_port_uxv", 13),
        ENERGY_PORT_MAX("energy_port_max", 14);

        private final String name;
        private final int tier;

        AbilityType(String name, int tier) {
            this.name = name;
            this.tier = tier;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public int getTier() {
            return this.tier;
        }
    }
}
