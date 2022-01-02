package thito.fancywaystones.structure;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;
import thito.fancywaystones.XMaterial;

import java.io.Serializable;

public class StructureBlock implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private String data;
    private int raw_data;

    private transient Material bakedType;
    private transient Object bakedData;

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public int getRawData() {
        return raw_data;
    }

    public void getFrom(Block block) {
        bakedType = block.getType();
        this.type = bakedType.name();
        try {
            bakedData = block.getBlockData();
            data = ((BlockData) bakedData).getAsString();
        } catch (Throwable ignored) {
        }
        try {
            raw_data = block.getState().getData().getData();
        } catch (Throwable ignored) {
        }
    }

    public void placeOn(Block block) {
        if (!Structure.canContinue(block)) return;
        try {
            if (bakedData == null) {
                bakedData = Bukkit.createBlockData(data);
            }
            block.setBlockData((BlockData) bakedData, false);
        } catch (Throwable t) {
            try {
                if (bakedType == null) {
                    try {
                        bakedType = Material.valueOf(type);
                    } catch (Throwable x) {
                        bakedType = XMaterial.valueOf(type).parseMaterial();
                    }
                }
                BlockState state = block.getState();
                state.setData(new MaterialData(bakedType, (byte) raw_data));
                state.update(true);
            } catch (Throwable ignored) {
            }
        }
    }
}
