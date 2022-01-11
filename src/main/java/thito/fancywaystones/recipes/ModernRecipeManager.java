package thito.fancywaystones.recipes;

import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.*;
import thito.fancywaystones.config.*;
import thito.fancywaystones.ui.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import java.util.stream.*;

public class ModernRecipeManager implements RecipeManager, Listener {
    private List<MetaRecipe> recipes = new ArrayList<>();

    static boolean exactChoiceExists;
    static {
        try {
            Class.forName("org.bukkit.inventory.RecipeChoice$ExactChoice");
            exactChoiceExists = true;
        } catch (Throwable t) {
        }
    }

    public ModernRecipeManager(FancyWaystones plugin) {
        if (!exactChoiceExists) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void registerCustomRecipes() {
        YamlConfiguration configuration = FancyWaystones.getPlugin().getRecipesYml().getConfig();
        Map<String, AtomicInteger> atomicIntegerMap = new HashMap<>();
        for (Map map : configuration.getMapList("Waystone Recipes")) {
            Section section = new MapSection(map);
            List<String> shapes = section.getList("Shape").orElse(ListSection.empty()).stream().map(String::valueOf).collect(Collectors.toList());
            if (shapes.isEmpty()) continue;
            Map<Character, ItemStack> materials = new HashMap<>();
            MapSection materialSection = section.getMap("Materials").orElse(MapSection.empty());
            for (String key : materialSection.keySet()) {
                MinecraftItem item = new MinecraftItem();
                item.load(materialSection.getMap(key).orElse(MapSection.empty()));
                materials.put(key.charAt(0), item.getItemStack(new Placeholder()));
            }
            WaystoneType type = WaystoneManager.getManager().getType(section.getString("Waystone Type").orElse(null));
            if (type == null) continue;
            World.Environment environment = section.getString("Environment").map(World.Environment::valueOf).orElse(World.Environment.NORMAL);
            WaystoneModel model = WaystoneManager.getManager().getModelMap().getOrDefault(section.getString("Model").orElse(null),
                    WaystoneManager.getManager().getDefaultModel());
            ItemStack item = WaystoneManager.getManager().createWaystoneItem(WaystoneManager.getManager().createData(type, environment, model), false);
            try {
                String id = environment.name() + "." + type.name() + "." + model.getId();
                int increment = atomicIntegerMap.computeIfAbsent(id, x -> new AtomicInteger()).getAndIncrement();
                id += "." +increment;
                ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(FancyWaystones.getPlugin(), id), item);
                shapedRecipe.shape(shapes.toArray(new String[0]));
                materials.forEach((key, i) -> {
                    if (exactChoiceExists) {
                        try {
                            shapedRecipe.setIngredient(
                                    key, (RecipeChoice) Class.forName("org.bukkit.inventory.RecipeChoice$ExactChoice")
                                            .getConstructor(ItemStack[].class)
                                            .newInstance(new Object[] {new ItemStack[] {i}})
                            );
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        shapedRecipe.setIngredient(key, i.getType());
                    }
                });
                FancyWaystones.getPlugin().getLogger()
                        .log(Level.INFO, "Registered recipe for waystone " + environment.name() + ":" + type.name());
                Bukkit.addRecipe(shapedRecipe);
                add(new MetaRecipe(environment.name() + "." +
                        type.name() + "." +
                        model.getId(), shapedRecipe, materials, RecipeConfiguration.fromConfig(section)));
            } catch (Throwable t) {
                FancyWaystones.getPlugin().getLogger().log(Level.SEVERE, "Failed to register recipe: "+environment.name()+":"+type.name()+" due to an error", t);
            }
        }

        ConfigurationSection deathBookRecipe = FancyWaystones.getPlugin().getBooksYml().getConfig().getConfigurationSection("Books.Death Book.Recipe");
        ConfigurationSection teleportationBookRecipe = FancyWaystones.getPlugin().getBooksYml().getConfig().getConfigurationSection("Books.Teleportation Book.Recipe");
        if (deathBookRecipe != null && FancyWaystones.getPlugin().getDeathBook().isEnable()) {
            List<String> shapes = deathBookRecipe.getStringList("Shape");
            Map<Character, ItemStack> materials = new HashMap<>();
            ConfigurationSection materialSection = deathBookRecipe.getConfigurationSection("Materials");
            for (String key : materialSection.getKeys(false)) {
                MinecraftItem item = new MinecraftItem();
                item.load(materialSection.getConfigurationSection(key));
                materials.put(key.charAt(0), item.getItemStack(new Placeholder()));
            }
            try {
                ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(FancyWaystones.getPlugin(), "death_book"), FancyWaystones.getPlugin().getDeathBook().createItem());
                shapedRecipe.shape(shapes.toArray(new String[0]));
                materials.forEach((key, i) -> {
                    if (exactChoiceExists) {
                        try {
                            shapedRecipe.setIngredient(
                                    key, (RecipeChoice) Class.forName("org.bukkit.inventory.RecipeChoice$ExactChoice")
                                            .getConstructor(ItemStack[].class)
                                            .newInstance(new Object[] {new ItemStack[] {i}})
                            );
                        } catch (Throwable ignored) {
                        }
                    } else {
                        shapedRecipe.setIngredient(key, i.getType());
                    }
                });
                FancyWaystones.getPlugin().getLogger()
                        .log(Level.INFO, "Registered recipe for Death Book");
                Bukkit.addRecipe(shapedRecipe);
                add(new MetaRecipe("deathbook", shapedRecipe, materials, RecipeConfiguration.fromConfig(deathBookRecipe)));
            } catch (Throwable t) {
                FancyWaystones.getPlugin().getLogger().log(Level.SEVERE, "Failed to register Death Book due to ", t);
            }
        }

        if (teleportationBookRecipe != null && FancyWaystones.getPlugin().getTeleportationBook().isEnable()) {
            List<String> shapes = teleportationBookRecipe.getStringList("Shape");
            Map<Character, ItemStack> materials = new HashMap<>();
            ConfigurationSection materialSection = teleportationBookRecipe.getConfigurationSection("Materials");
            for (String key : materialSection.getKeys(false)) {
                MinecraftItem item = new MinecraftItem();
                item.load(materialSection.getConfigurationSection(key));
                materials.put(key.charAt(0), item.getItemStack(new Placeholder()));
            }
            try {
                ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(FancyWaystones.getPlugin(), "teleportation_book"), FancyWaystones.getPlugin().getTeleportationBook().createEmptyItem());
                shapedRecipe.shape(shapes.toArray(new String[0]));
                materials.forEach((key, i) -> {
                    if (exactChoiceExists) {
                        try {
                            shapedRecipe.setIngredient(
                                    key, (RecipeChoice) Class.forName("org.bukkit.inventory.RecipeChoice$ExactChoice")
                                            .getConstructor(ItemStack[].class)
                                            .newInstance(new Object[] {new ItemStack[] {i}})
                            );
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        shapedRecipe.setIngredient(key, i.getType());
                    }
                });
                FancyWaystones.getPlugin().getLogger()
                        .log(Level.INFO, "Registered recipe for Teleportation Book");
                Bukkit.addRecipe(shapedRecipe);
                add(new MetaRecipe("teleportationbook", shapedRecipe, materials, RecipeConfiguration.fromConfig(teleportationBookRecipe)));
            } catch (Throwable t) {
                FancyWaystones.getPlugin().getLogger().log(Level.SEVERE, "Failed to register Teleportation Book due to an error", t);
            }
        }
    }

    private void add(MetaRecipe metaRecipe) {
        recipes.add(metaRecipe);
        metaRecipe.printRecipe();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void attemptCraft(CraftItemEvent e) {
        if (e.getRecipe() instanceof ShapedRecipe) {
            recipes.stream().filter(x -> x.getRecipe().getKey().equals(((ShapedRecipe) e.getRecipe()).getKey())).findAny().ifPresent(recipe -> {
                CraftingInventory inventory = e.getInventory();
                ItemStack[] matrix = inventory.getMatrix();
                ItemStack[] currentMatrix = new ItemStack[9];
                int row = 0;
                for (String shape : recipe.getRecipe().getShape()) {
                    currentMatrix[row * 3] = recipe.getIngredients().get(shape.charAt(0));
                    currentMatrix[row * 3 + 1] = recipe.getIngredients().get(shape.charAt(1));
                    currentMatrix[row * 3 + 2] = recipe.getIngredients().get(shape.charAt(2));
                    row++;
                }
                if (!MetaRecipe.matricesEqual(matrix, currentMatrix)) {
                    e.setCancelled(true);
                }
            });
        }
    }


    @Override
    public List<MetaRecipe> getRecipes() {
        return recipes;
    }

    @Override
    public void clearCustomRecipes() {
        try {
            Method method = Bukkit.class.getMethod("removeRecipe", NamespacedKey.class);
            recipes.forEach(x -> {
                try {
                    method.invoke(null, x.getRecipe().getKey());
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            });
            recipes.clear();
            return;
        } catch (Throwable t) {
        }

        try {
            Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                Recipe recipe = recipeIterator.next();
                if (recipe instanceof ShapedRecipe) {
                    if (recipes.stream().anyMatch(x -> x.getRecipe().getKey().equals(((ShapedRecipe) recipe).getKey()))) {
                        recipeIterator.remove();
                    }
                }
            }
            recipes.clear();
        } catch (Throwable ignored) {
            FancyWaystones.getPlugin().getLogger().log(Level.INFO, "Uses legacy method to remove recipe. Might take a while.");
            List<Recipe> backup = new ArrayList<>();
            Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
            while (recipeIterator.hasNext()) {
                Recipe recipe = recipeIterator.next();
                if (recipes.stream().noneMatch(x -> x.getRecipe().getResult().equals(recipe.getResult()))) {
                    backup.add(recipe);
                }
            }
            Bukkit.clearRecipes();
            backup.forEach(Bukkit::addRecipe);
        }
    }
}
