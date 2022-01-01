package thito.fancywaystones.model;

import org.bukkit.inventory.ItemStack;
import thito.fancywaystones.Placeholder;
import thito.fancywaystones.Util;
import thito.fancywaystones.WaystoneData;
import thito.fancywaystones.config.ListSection;
import thito.fancywaystones.config.MapSection;
import thito.fancywaystones.config.Section;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemModel {
    private final List<String> modelList;
    private final List<String> typeList;
    private final List<String> environmentList;
    private final Section item;
    public ItemModel(Map<?, ?> map) {
        Section section = new MapSection(map);
        modelList = section.getList("model").orElse(ListSection.empty()).stream().map(String::valueOf).collect(Collectors.toList());
        typeList = section.getList("type").orElse(ListSection.empty()).stream().map(String::valueOf).collect(Collectors.toList());
        environmentList = section.getList("environment").orElse(ListSection.empty()).stream().map(String::valueOf).collect(Collectors.toList());
        item = section.getMap("item").orElse(null);
    }

    public boolean accept(WaystoneData data) {
        return (modelList.isEmpty() || modelList.contains(data.getModel().getId())) &&
                (typeList.isEmpty() || typeList.contains(data.getType().name())) &&
                (environmentList.isEmpty() || environmentList.contains(data.getEnvironment().name()));
    }

    public List<String> getModelList() {
        return modelList;
    }

    public List<String> getTypeList() {
        return typeList;
    }

    public List<String> getEnvironmentList() {
        return environmentList;
    }

    public ItemStack getItemStack(Placeholder placeholder) {
        return Util.deserializeItemStack(new MapSection((MapSection)item), placeholder);
    }

}
