package thito.fancywaystones.structure;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import thito.fancywaystones.config.ListSection;
import thito.fancywaystones.config.Section;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StructureSpawnCondition {
    private boolean notAtSpawnProtection, spawnFromAbove, ignoreTrees, placeOnLowestBlockPossible, onWater, onlyNewChunks;
    private double spawnPercentage;
    private int chunkXDistance, chunkZDistance, minXOffset, maxXOffset, minZOffset, maxZOffset;
    private List<String> dimensionList;
    private List<String> biomesList;
    private List<String> worldList;

    public void loadFromConfig(Section section) {
        onWater = section.getBoolean("On Water").orElse(false);
        notAtSpawnProtection = section.getBoolean("Not At Spawn Protection").orElse(false);
        spawnFromAbove = section.getBoolean("Spawn From Above").orElse(false);
        onlyNewChunks = section.getBoolean("Only New Chunks").orElse(false);
        ignoreTrees = section.getBoolean("Ignore Trees").orElse(false);
        placeOnLowestBlockPossible = section.getBoolean("Place On The Lowest Block Possible").orElse(false);
        spawnPercentage = section.getDouble("Spawn Chance Percentage").orElse(0d);
        chunkXDistance = section.getInteger("X Chunk Distance").orElse(1);
        chunkZDistance = section.getInteger("Z Chunk Distance").orElse(1);
        minXOffset = section.getInteger("X Offset Range.Min").orElse(0);
        maxXOffset = section.getInteger("X Offset Range.Max").orElse(0);
        minZOffset = section.getInteger("Z Offset Range.Min").orElse(0);
        maxZOffset = section.getInteger("Z Offset Range.Max").orElse(0);
        worldList = section.getList("Worlds").orElse(new ListSection()).stream().map(String::valueOf).collect(Collectors.toList());
        dimensionList = section.getList("Dimensions").orElse(new ListSection()).stream().map(String::valueOf).collect(Collectors.toList());
        biomesList = section.getList("Biomes").orElse(new ListSection()).stream().map(String::valueOf).collect(Collectors.toList());
    }

    public boolean isOnlyNewChunks() {
        return onlyNewChunks;
    }

    public void setOnlyNewChunks(boolean onlyNewChunks) {
        this.onlyNewChunks = onlyNewChunks;
    }

    public void setMinXOffset(int minXOffset) {
        this.minXOffset = minXOffset;
    }

    public void setMaxXOffset(int maxXOffset) {
        this.maxXOffset = maxXOffset;
    }

    public void setMaxZOffset(int maxZOffset) {
        this.maxZOffset = maxZOffset;
    }

    public void setMinZOffset(int minZOffset) {
        this.minZOffset = minZOffset;
    }

    public void setOnWater(boolean onWater) {
        this.onWater = onWater;
    }

    public boolean isOnWater() {
        return onWater;
    }

    public int getMaxXOffset() {
        return maxXOffset;
    }

    public int getMaxZOffset() {
        return maxZOffset;
    }

    public int getMinXOffset() {
        return minXOffset;
    }

    public int getMinZOffset() {
        return minZOffset;
    }

    public List<String> getBiomesList() {
        return biomesList;
    }

    public void setBiomesList(List<String> biomesList) {
        this.biomesList = biomesList;
    }

    public boolean isNotAtSpawnProtection() {
        return notAtSpawnProtection;
    }

    public void setNotAtSpawnProtection(boolean notAtSpawnProtection) {
        this.notAtSpawnProtection = notAtSpawnProtection;
    }

    public boolean isSpawnFromAbove() {
        return spawnFromAbove;
    }

    public void setSpawnFromAbove(boolean spawnFromAbove) {
        this.spawnFromAbove = spawnFromAbove;
    }

    public boolean isIgnoreTrees() {
        return ignoreTrees;
    }

    public void setIgnoreTrees(boolean ignoreTrees) {
        this.ignoreTrees = ignoreTrees;
    }

    public boolean isPlaceOnLowestBlockPossible() {
        return placeOnLowestBlockPossible;
    }

    public void setPlaceOnLowestBlockPossible(boolean placeOnLowestBlockPossible) {
        this.placeOnLowestBlockPossible = placeOnLowestBlockPossible;
    }

    public double getSpawnPercentage() {
        return spawnPercentage;
    }

    public void setSpawnPercentage(double spawnPercentage) {
        this.spawnPercentage = spawnPercentage;
    }

    public int getChunkXDistance() {
        return chunkXDistance;
    }

    public void setChunkXDistance(int chunkXDistance) {
        this.chunkXDistance = chunkXDistance;
    }

    public int getChunkZDistance() {
        return chunkZDistance;
    }

    public void setChunkZDistance(int chunkZDistance) {
        this.chunkZDistance = chunkZDistance;
    }

    public List<String> getDimensionList() {
        return dimensionList;
    }

    public void setDimensionList(List<String> dimensionList) {
        this.dimensionList = dimensionList;
    }

    public List<String> getWorldList() {
        return worldList;
    }

    public void setWorldList(List<String> worldList) {
        this.worldList = worldList;
    }

    private boolean noMinY;
    public Location canSpawn(Structure structure, Chunk chunk) {
        World world = chunk.getWorld();
        if (worldList != null && !worldList.isEmpty() && !worldList.contains(world.getName())) return null;
        int x = chunk.getX();
        int z = chunk.getZ();
        int structureHeight = structure.getHeight();
        int structureWidth = structure.getWidth();
        int structureLength = structure.getLength();
        int worldMinY = 0;
        if (!noMinY) {
            try {
                worldMinY = world.getMinHeight();
            } catch (Throwable t) {
                noMinY = true;
            }
        }
        if (notAtSpawnProtection) {
            Location spawnLocation = world.getSpawnLocation();
            int spawnProtectionDistance = Bukkit.getSpawnRadius();
            int coordX = (x + 1) * 16;
            int coordZ = (z + 1) * 16;
            if (Math.sqrt(Math.pow(coordX - spawnLocation.getX(), 2) + Math.pow(coordZ - spawnLocation.getZ(), 2)) <= spawnProtectionDistance) {
                return null;
            }
        }
        if (spawnPercentage >= 0) {
            Random random = new Random();
            double percentage = random.nextDouble() * 100;
            if (percentage > spawnPercentage) {
                return null;
            }
        }
        if (dimensionList != null && !dimensionList.isEmpty() && !dimensionList.contains(world.getEnvironment().name())) {
            return null;
        }
        if (chunkXDistance == 0 || x % chunkXDistance != 0) {
            return null;
        }
        if (chunkZDistance == 0 || z % chunkZDistance != 0) {
            return null;
        }
        Location picked = null;
        try {
            Random random = new Random();
            int startX = random.nextInt(maxXOffset -minXOffset) + minXOffset;
            int startZ = random.nextInt(maxZOffset - minZOffset) + minZOffset;
            if (spawnFromAbove) {
                for (int a = startX; a < startZ + structureWidth; a++) {
                    for (int b = startZ; b < startZ + structureLength; b++) {
                        for (int c = world.getMaxHeight() - structureHeight - 1; c >= worldMinY; c--) {
                            Location check = checkLocation(chunk, picked, a, b, c);
                            if (check != null) {
                                picked = check.clone().add(0, 1, 0);
                                break;
                            }
                        }
                    }
                }
            } else {
                for (int a = startX; a < startZ + structureWidth; a++) {
                    for (int b = startZ; b < startZ + structureLength; b++) {
                        for (int c = worldMinY + 1; c < world.getMaxHeight() - structureHeight; c++) {
                            Location check = checkLocation(chunk, picked, a, b, c);
                            if (check != null) {
                                picked = check;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IllegalStateException ignored) {
            return null;
        }
        return picked;
    }

    private Location checkLocation(Chunk chunk, Location picked, int a, int b, int c) {
        Block block = chunk.getBlock(a, c, b);
        if (biomesList != null && !biomesList.isEmpty() && !biomesList.contains(block.getBiome().name())) {
            throw new IllegalStateException();
        }

        if (block.isEmpty()) {
            return null;
        } else {
            if (ignoreTrees && block.getType().name().contains("LEAVES")) {
                return null;
            }
            if (!onWater && block.getType().name().equals("WATER")) {
                throw new IllegalStateException();
            }
        }
        Location location = block.getLocation();
        if (picked == null || (
                spawnFromAbove ?
                        placeOnLowestBlockPossible && location.getBlockY() < picked.getBlockY()
                        :
                        !placeOnLowestBlockPossible && location.getBlockY() > picked.getBlockY()
                )) {
            return location;
        }
        return null;
//        return block.getLocation();
    }
}
