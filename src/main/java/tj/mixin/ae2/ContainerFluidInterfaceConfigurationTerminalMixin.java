package tj.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IMachineSet;
import appeng.container.implementations.ContainerFluidInterfaceConfigurationTerminal;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import tj.integration.ae2.me.MachineSets;
import tj.integration.ae2.part.PartStockingDualInterface;
import tj.integration.ae2.part.PartStockingFluidInterface;
import tj.integration.ae2.part.PartSuperDualInterface;
import tj.integration.ae2.part.PartSuperFluidInterface;
import tj.integration.ae2.tile.TileStockingDualInterface;
import tj.integration.ae2.tile.TileStockingFluidInterface;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.integration.ae2.tile.TileSuperFluidInterface;

@Mixin(value = ContainerFluidInterfaceConfigurationTerminal.class, remap = false)
public abstract class ContainerFluidInterfaceConfigurationTerminalMixin {

    @Shadow
    private IGrid grid;

    @ModifyExpressionValue(method = "detectAndSendChanges", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 0))
    private IMachineSet modifyExpressionValueDetectAndSendChanges(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(TileSuperFluidInterface.class))
                .tryToAdd(this.grid.getMachines(TileSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingFluidInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingDualInterface.class));
    }

    @ModifyExpressionValue(method = "detectAndSendChanges", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 1))
    private IMachineSet modifyExpressionValueDetectAndSendChanges1(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(PartSuperFluidInterface.class))
                .tryToAdd(this.grid.getMachines(PartSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingFluidInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingDualInterface.class));
    }

    @ModifyExpressionValue(method = "regenList", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 0))
    private IMachineSet modifyExpressionValueRegenList(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(TileSuperFluidInterface.class))
                .tryToAdd(this.grid.getMachines(TileSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingFluidInterface.class))
                .tryToAdd(this.grid.getMachines(TileStockingDualInterface.class));
    }

    @ModifyExpressionValue(method = "regenList", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 1))
    private IMachineSet modifyExpressionValueRegenList1(IMachineSet original) {
        return new MachineSets()
                .tryToAdd(original)
                .tryToAdd(this.grid.getMachines(PartSuperFluidInterface.class))
                .tryToAdd(this.grid.getMachines(PartSuperDualInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingFluidInterface.class))
                .tryToAdd(this.grid.getMachines(PartStockingDualInterface.class));
    }
}
