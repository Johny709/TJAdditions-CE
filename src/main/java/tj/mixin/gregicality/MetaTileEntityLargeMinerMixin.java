package tj.mixin.gregicality;

import gregicadditions.machines.multi.miner.MetaTileEntityLargeMiner;
import gregicadditions.machines.multi.miner.Miner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(value = MetaTileEntityLargeMiner.class, remap = false)
public abstract class MetaTileEntityLargeMinerMixin extends GAMultiblockWithDisplayBaseMixin {

    @Shadow
    private AtomicLong x;

    @Shadow
    private AtomicLong y;

    @Shadow
    private AtomicLong z;

    @Shadow
    private AtomicInteger currentChunk;

    @Shadow
    private List<Chunk> chunks;

    @Shadow
    @Final
    public Miner.Type type;

    @Shadow
    private boolean silktouch;

    @Shadow
    private boolean done;

    public MetaTileEntityLargeMinerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    public abstract long getNbBlock();

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        if (this.x.get() == Long.MAX_VALUE) {
            builder.addTextComponent(new TextComponentString("X: Not Active"))
                    .addTextComponent(new TextComponentString("Y: Not Active"))
                    .addTextComponent(new TextComponentString("Z: Not Active"));
        } else builder.addTextComponent(new TextComponentString(String.format("X: %d", this.x.get())))
                .addTextComponent(new TextComponentString(String.format("Y: %d", this.y.get())))
                .addTextComponent(new TextComponentString(String.format("Z: %d", this.z.get())));
        builder.addTranslationLine("gregtech.multiblock.large_miner.chunk", this.currentChunk.get())
                .addTranslationLine("gregtech.multiblock.large_miner.nb_chunk", this.chunks.size())
                .addTranslationLine("gregtech.multiblock.large_miner.block_per_tick", this.getNbBlock());
        if (this.type != Miner.Type.CREATIVE) {
            builder.addTranslationLine("gregtech.multiblock.large_miner.silktouch", this.silktouch)
                    .addTranslationLine("gregtech.multiblock.large_miner.mode");
        }
        if (this.done)
            builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_miner.done", this.getNbBlock())
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
    }
}
