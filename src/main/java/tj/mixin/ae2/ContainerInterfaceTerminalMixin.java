package tj.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IMachineSet;
import appeng.container.implementations.ContainerInterfaceTerminal;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import tj.integration.ae2.me.MachineSets;
import tj.integration.ae2.part.PartPatternInterface;
import tj.integration.ae2.part.PartSuperDualInterface;
import tj.integration.ae2.part.PartSuperInterface;
import tj.integration.ae2.tile.TilePatternInterface;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.integration.ae2.tile.TileSuperInterface;

@Mixin(value = ContainerInterfaceTerminal.class, remap = false)
public abstract class ContainerInterfaceTerminalMixin {

    @Shadow
    private IGrid grid;

    @ModifyExpressionValue(method = "detectAndSendChanges", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 0))
    private IMachineSet modifyExpressionValueDetectAndSendChanges(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(TileSuperInterface.class))
                .tryToAdd(this.grid.getMachines(TileSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(TilePatternInterface.class));
    }

    @ModifyExpressionValue(method = "detectAndSendChanges", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 1))
    private IMachineSet modifyExpressionValueDetectAndSendChanges1(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(PartSuperInterface.class))
                .tryToAdd(this.grid.getMachines(PartSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(PartPatternInterface.class));
    }
    
    @ModifyExpressionValue(method = "regenList", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 0))
    private IMachineSet modifyExpressionValueRegenList(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(TileSuperInterface.class))
                .tryToAdd(this.grid.getMachines(TileSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(TilePatternInterface.class));
    }

    @ModifyExpressionValue(method = "regenList", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 1))
    private IMachineSet modifyExpressionValueRegenList1(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(PartSuperInterface.class))
                .tryToAdd(this.grid.getMachines(PartSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(PartPatternInterface.class));
    }
}
