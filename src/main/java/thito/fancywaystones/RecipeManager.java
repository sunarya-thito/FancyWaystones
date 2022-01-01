package thito.fancywaystones;

import thito.fancywaystones.recipes.MetaRecipe;

import java.util.List;

public interface RecipeManager {
    void registerCustomRecipes();
    void clearCustomRecipes();
    List<MetaRecipe> getRecipes();
}
