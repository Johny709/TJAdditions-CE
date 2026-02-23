package tj.blocks;

import gregtech.common.blocks.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class AbilityBlocks extends VariantBlock<AbilityBlocks.AbilityType> {

    public AbilityBlocks() {
        super(Material.IRON);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setTranslationKey("ability_block");
        this.setRegistryName("ability_block");
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(getState(AbilityType.SOLAR_COLLECTOR));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum AbilityType implements IStringSerializable {
        SOLAR_COLLECTOR("solar_collector"),
        PRIMITIVE_PUMP_CASING("primitive_pump_casing");

        private final String name;

        AbilityType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
