package hu.frontrider.momentum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mixin(PistonBlock.class)
public class PistonMixin {

    @Shadow
    public boolean isSticky;

    @Inject(at = @At("HEAD"),
            method = "isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z",
            cancellable = true)
    private static void isMovable(BlockState blockState, World world, BlockPos blockPos, Direction var3, boolean var4, Direction var5, CallbackInfoReturnable<Boolean> returnable) {
        if(blockState.getBlock() instanceof PistonMovable ){
            returnable.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"),
            method = "move(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Z",
            cancellable = true)
    private void move(World world, BlockPos originalPosition, Direction direction, boolean var4, CallbackInfoReturnable<Boolean> returnable) {
        BlockPos offsetPosition = originalPosition.offset(direction);
        if (!var4 && world.getBlockState(offsetPosition).getBlock() == Blocks.PISTON_HEAD) {
            world.setBlockState(offsetPosition, Blocks.AIR.getDefaultState(), 20);
        }

        PistonHandler pistonHandler = new PistonHandler(world, originalPosition, direction, var4);
        if (!pistonHandler.calculatePush()) {
            returnable.setReturnValue(false);
        } else {
            List<BlockPos> movedBlocks = pistonHandler.getMovedBlocks();
            List<BlockState> movedBlockStates = Lists.newArrayList();

            for (BlockPos pos : movedBlocks) {
                movedBlockStates.add(world.getBlockState(pos));
            }

            List<BlockPos> brokenBlocks = pistonHandler.getBrokenBlocks();
            int affectedBlockCount = movedBlocks.size() + brokenBlocks.size();
            BlockState[] affectedBlockStates = new BlockState[affectedBlockCount];
            Direction var12 = var4 ? direction : direction.getOpposite();
            Set<BlockPos> movedBlockSet = Sets.newHashSet(movedBlocks);

            int var14;
            BlockPos var15;
            BlockState blockState;
            for(var14 = brokenBlocks.size() - 1; var14 >= 0; --var14) {
                var15 = brokenBlocks.get(var14);
                blockState = world.getBlockState(var15);
                BlockEntity var17 = blockState.getBlock().hasBlockEntity() ? world.getBlockEntity(var15) : null;
                Block.dropStacks(blockState, world, var15, var17);
                world.setBlockState(var15, Blocks.AIR.getDefaultState(), 18);
                --affectedBlockCount;
                affectedBlockStates[affectedBlockCount] = blockState;
            }

            for(var14 = movedBlocks.size() - 1; var14 >= 0; --var14) {
                var15 = movedBlocks.get(var14);
                blockState = world.getBlockState(var15);
                var15 = var15.offset(var12);
                movedBlockSet.remove(var15);
                if(blockState.getBlock() instanceof PistonMovable && blockState.getBlock().hasBlockEntity()){
                    if(((PistonMovable)blockState).useDefault()){
                        world.setBlockState(var15, Blocks.MOVING_PISTON.getDefaultState().with(FacingBlock.field_10927, direction), 68);
             //           world.setBlockEntity(var15, PistonExtensionBlock.createBlockEntityPiston(movedBlockStates.get(var14), direction, var4, false));

                    }else{
                        world.setBlockState(var15, Blocks.MOVING_PISTON.getDefaultState().with(FacingBlock.field_10927, direction), 68);
               //         world.setBlockEntity(var15, PistonExtensionBlock.createBlockEntityPiston(movedBlockStates.get(var14), direction, var4, false));

                    }
                }else{
                    world.setBlockState(var15, Blocks.MOVING_PISTON.getDefaultState().with(FacingBlock.field_10927, direction), 68);
                    world.setBlockEntity(var15, PistonExtensionBlock.createBlockEntityPiston(movedBlockStates.get(var14), direction, var4, false));
                }

                --affectedBlockCount;
                affectedBlockStates[affectedBlockCount] = blockState;
            }

            BlockState var22;
            if (var4) {
                PistonType var20 = this.isSticky ? PistonType.STICKY : PistonType.NORMAL;
                var22 = Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.field_10927, direction).with(PistonHeadBlock.field_12224, var20);
                blockState = Blocks.MOVING_PISTON.getDefaultState().with(PistonExtensionBlock.FACING, direction).with(PistonExtensionBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.NORMAL);
                movedBlockSet.remove(offsetPosition);
                world.setBlockState(offsetPosition, blockState, 68);
                world.setBlockEntity(offsetPosition, PistonExtensionBlock.createBlockEntityPiston(var22, direction, true, true));
            }

            Iterator var21 = movedBlockSet.iterator();

            while(var21.hasNext()) {
                var15 = (BlockPos)var21.next();
                world.setBlockState(var15, Blocks.AIR.getDefaultState(), 66);
            }

            for(var14 = brokenBlocks.size() - 1; var14 >= 0; --var14) {
                var22 = affectedBlockStates[affectedBlockCount++];
                BlockPos var23 = brokenBlocks.get(var14);
                var22.method_11637(world, var23, 2);
                world.updateNeighborsAlways(var23, var22.getBlock());
            }

            for(var14 = movedBlocks.size() - 1; var14 >= 0; --var14) {
                world.updateNeighborsAlways(movedBlocks.get(var14), affectedBlockStates[affectedBlockCount++].getBlock());
            }

            if (var4) {
                world.updateNeighborsAlways(offsetPosition, Blocks.PISTON_HEAD);
            }

            returnable.setReturnValue(true);
        }
    }

}

