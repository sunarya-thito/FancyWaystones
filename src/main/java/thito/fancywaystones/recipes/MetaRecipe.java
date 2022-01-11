package thito.fancywaystones.recipes;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.*;

import java.util.*;
import java.util.logging.Level;

public class MetaRecipe {
    private ShapedRecipe recipe;
    private Map<Character, ItemStack> ingredients;
    private String id;
    private RecipeConfiguration recipeConfiguration;

    public MetaRecipe(String id, ShapedRecipe recipe, Map<Character, ItemStack> ingredients, RecipeConfiguration recipeConfiguration) {
        this.recipe = recipe;
        this.ingredients = ingredients;
        this.recipeConfiguration = recipeConfiguration;
    }

    public RecipeConfiguration getRecipeConfiguration() {
        return recipeConfiguration;
    }

    public String getId() {
        return id;
    }

    static boolean matricesEqual(ItemStack[] matrix, ItemStack[] other) {
        if (matrix.length >= other.length) {
            for (int i = 0; i < other.length; i++) {
                if (!isSimilar(matrix[i], other[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    static boolean isSimilar(ItemStack a, ItemStack b) {
        if (a == null) {
            return b == null || b.getType() == XMaterial.AIR.parseMaterial();
        }
        if (b == null) {
            return a.getType() == XMaterial.AIR.parseMaterial();
        }
        return a.isSimilar(b);
    }

    public void printRecipe() {
        ItemStack[] matrix = new ItemStack[9];
        String[] shapes = recipe.getShape();
        int index = 0;
        for (String shape : shapes) {
            for (int i = 0; i < shape.length(); i++) {
                char c = shape.charAt(i);
                matrix[index++] = ingredients.get(c);
            }
        }
        String[] array = Arrays.stream(matrix).map(MetaRecipe::toString).toArray(String[]::new);
        int length = 0;
        for (String s : array) length = Math.max(length, s.length());
        appendEmpty(array, length);
    }

    private static void appendEmpty(String[] array, int length) {
        for (int i = 0; i < array.length; i++) {
            for (int j = array[i].length(); j < length; j++) {
                array[i] = array[i] + ' ';
            }
        }
        FancyWaystones.getPlugin().getLogger().log(Level.INFO, "["+array[0]+"]["+array[1]+"]["+array[2]+"]");
        FancyWaystones.getPlugin().getLogger().log(Level.INFO, "["+array[3]+"]["+array[4]+"]["+array[5]+"]");
        FancyWaystones.getPlugin().getLogger().log(Level.INFO, "["+array[6]+"]["+array[7]+"]["+array[8]+"]");
    }

    public static void printMatrix(ItemStack[] matrix) {
        String[] array = Arrays.stream(matrix).map(MetaRecipe::toString).toArray(String[]::new);
        int length = 0;
        for (int i = 0; i < array.length; i++) length = Math.max(length, array[i].length());
        appendEmpty(array, length);
    }

    static String toString(ItemStack itemStack) {
        if (itemStack == null) return "   ";
        return itemStack.toString();
    }

    public ShapedRecipe getRecipe() {
        return recipe;
    }

    public Map<Character, ItemStack> getIngredients() {
        return ingredients;
    }
}
