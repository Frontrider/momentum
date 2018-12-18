package hu.frontrider.momentum;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Implement it on your blocks that need to be pushable. NOT THE TILE ENTITY!
 */
public interface PistonMovable {

    /**
     * return true, if you want to use the default that will just copy the nbt over to the new location
     * @return weather or not you want to rely on the default push implementation
     */
    default boolean useDefault() {
        return true;
    }

    /**
     * if you return false on useDefault, than you have to implement this method to move the block yourself.
     *
     * @param pos the current position of the block
     * @param direction the direction where the block is being pushed
     * @param world the current world
     * */
    default void DoMove(BlockPos pos, Direction direction, World world) {

    }
}
