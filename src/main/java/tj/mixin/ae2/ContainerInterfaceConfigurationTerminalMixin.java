package tj.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IMachineSet;
import appeng.container.implementations.ContainerInterfaceConfigurationTerminal;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import tj.integration.ae2.me.MachineSets;
import tj.integration.ae2.part.PartStockingDualInterface;
import tj.integration.ae2.part.PartStockingInterface;
import tj.integration.ae2.part.PartSuperDualInterface;
import tj.integration.ae2.part.PartSuperInterface;
import tj.integration.ae2.tile.TileStockingDualInterface;
import tj.integration.ae2.tile.TileStockingInterface;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.integration.ae2.tile.TileSuperInterface;

@Mixin(value = ContainerInterfaceConfigurationTerminal.class, remap = false)
public abstract class ContainerInterfaceConfigurationTerminalMixin {

    @Shadow
    private IGrid grid;

    @ModifyExpressionValue(method = "detectAndSendChanges", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 0))
    private IMachineSet modifyExpressionValueDetectAndSendChanges(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(TileSuperInterface.class))
                .tryToAdd(this.grid.getMachines(TileSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingDualInterface.class));
    }

    @ModifyExpressionValue(method = "detectAndSendChanges", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 1))
    private IMachineSet modifyExpressionValueDetectAndSendChanges1(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(PartSuperInterface.class))
                .tryToAdd(this.grid.getMachines(PartSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingDualInterface.class));
    }

    @ModifyExpressionValue(method = "regenList", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 0))
    private IMachineSet modifyExpressionValueRegenList(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(TileSuperInterface.class))
                .tryToAdd(this.grid.getMachines(TileSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingDualInterface.class));
    }

    @ModifyExpressionValue(method = "regenList", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 1))
    private IMachineSet modifyExpressionValueRegenList1(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(PartSuperInterface.class))
                .tryToAdd(this.grid.getMachines(PartSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingDualInterface.class));
    }
}
