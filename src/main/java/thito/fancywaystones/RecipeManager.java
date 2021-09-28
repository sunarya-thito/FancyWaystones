package thito.fancywaystones;

import org.bukkit.inventory.*;

import java.util.*;

public interface RecipeManager {
    void registerCustomRecipes();
    void clearCustomRecipes();
    List<? extends Recipe> getRecipes();
}
