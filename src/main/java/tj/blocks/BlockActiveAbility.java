package tj.blocks;

import gregtech.common.blocks.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockActiveAbility extends VariantBlock<BlockActiveAbility.AbilityType> {

    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockActiveAbility() {
        super(Material.IRON);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setTranslationKey("active_ability_block");
        this.setRegistryName("active_ability_block");
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(getState(AbilityType.HEAT_VENT));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        super.createBlockState();
        return new BlockStateContainer(this, VARIANT, ACTIVE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta % VALUES.length).withProperty(ACTIVE, meta / VALUES.length >= 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) + (state.getValue(ACTIVE) ? VALUES.length : 0);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return super.damageDropped(state) - (state.getValue(ACTIVE) ? VALUES.length : 0);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum AbilityType implements IStringSerializable {
        HEAT_VENT("heat_vent");

        private final String name;

        AbilityType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
