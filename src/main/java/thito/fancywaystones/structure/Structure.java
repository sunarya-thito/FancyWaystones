package thito.fancywaystones.structure;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import thito.fancywaystones.Util;

import java.io.Serializable;
import java.util.function.BiPredicate;

public class Structure implements Serializable {
    private static final long serialVersionUID = 1L;
    private int width, height, length;
    private StructureBlock[] cube;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public void fromSelection(Selection selection) {
        Location pos1 = selection.getPos1();
        Location pos2 = selection.getPos2();
        width = pos2.getBlockX() - pos1.getBlockX() + 1;
        height = pos2.getBlockY() - pos1.getBlockY() + 1;
        length = pos2.getBlockZ() - pos1.getBlockZ() + 1;
        cube = new StructureBlock[width * height * length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = x * height * length + y * length + z;
                    StructureBlock structureBlock = cube[index] = new StructureBlock();
                    structureBlock.getFrom(pos1.clone().add(x, y, z).getBlock());
                }
            }
        }
    }

    public void placeAt(Location pos1, BiPredicate<Location, StructureBlock> structureBlockPredicate) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = 1; y < height; y++) {
                    int index = x * height * length + y * length + z;
                    StructureBlock structureBlock = cube[index];
                    Location location = pos1.clone().add(x, y - 1, z);
                    if (structureBlockPredicate.test(location, structureBlock)) {
                        Util.submitSync(() -> {
                            structureBlock.placeOn(location.getBlock());
                        });
                    }
                }
                StructureBlock structureBlock = cube[x * height * length + z]; // bottom x z
                checkBelow(structureBlock, pos1.getWorld(), pos1.getBlockX() + x, pos1.getBlockY() - 1, pos1.getBlockZ() + z);
            }
        }
    }

    private void checkBelow(StructureBlock structureBlock, World world, int x, int y, int z) {
        if (y < 0) return;
        Util.submitSync(() -> {
            Block block = world.getBlockAt(x, y, z);
            if (canContinue(block)) {
                structureBlock.placeOn(block);
                checkBelow(structureBlock, world, x, y - 1, z);
            }
        });
    }

    public static boolean canContinue(Block block) {
        if (block.isEmpty()) return true;
        Material type = block.getType();
        if (type.name().contains("WATER") || type.name().contains("LAVA") || type.name().contains("GRASS")) return true;
        return type.name().contains("LEAVES");
    }
}
