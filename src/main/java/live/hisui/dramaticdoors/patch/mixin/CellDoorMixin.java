package live.hisui.dramaticdoors.patch.mixin;

import com.lazrproductions.cuffed.blocks.CellDoor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author hisui
 * compat patch for Cuffed x DramaticDoors
 * DD makes all DoorBlock implement SimpleWaterloggedBlock
 * CellDoor doesn't call super in createBlockStateDefinition so it never receives the WATERLOGGED property
 * this fixes that problem + its consequences
 */
@Mixin(CellDoor.class)
public abstract class CellDoorMixin extends DoorBlock {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private CellDoorMixin(Properties properties, BlockSetType type) {
        super(properties, type);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void dramaticdoors_patch$addWaterloggedProperty(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WATERLOGGED);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Lnet/minecraft/world/level/block/state/properties/BlockSetType;)V",
        at = @At("TAIL")
    )
    private void dramaticdoors_patch$defaultNotWaterlogged(Properties properties, BlockSetType type, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void dramaticdoors_patch$waterlogOnPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state != null) {
            boolean inWater = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
            cir.setReturnValue(state.setValue(WATERLOGGED, inWater));
        }
    }
}
