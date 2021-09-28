package thito.fancywaystones.recipes;

import org.bukkit.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.*;

import java.util.*;

public class MetaRecipe {
    private ShapedRecipe recipe;
    private Map<Character, ItemStack> ingredients;
    private String id;

    public MetaRecipe(String id, ShapedRecipe recipe, Map<Character, ItemStack> ingredients) {
        this.recipe = recipe;
        this.ingredients = ingredients;
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
            return a == null || a.getType() == XMaterial.AIR.parseMaterial();
        }
        return a.isSimilar(b);
    }

    public void printRecipe() {
        ItemStack[] matrix = new ItemStack[9];
        String[] shapes = recipe.getShape();
        int index = 0;
        for (int j = 0; j < shapes.length; j++) {
            String shape = shapes[j];
            for (int i = 0; i < shape.length(); i++) {
                char c = shape.charAt(i);
                matrix[index++] = ingredients.get(c);
            }
        }
        String[] array = Arrays.stream(matrix).map(MetaRecipe::toString).toArray(String[]::new);
        int length = 0;
        for (int i = 0; i < array.length; i++) length = Math.max(length, array[i].length());
        for (int i = 0; i < array.length; i++) {
            for (int j = array[i].length(); j < length; j++) {
                array[i] = array[i] + ' ';
            }
        }
        System.out.println(
                "\n["+array[0]+"]["+array[1]+"]["+array[2]+"]\n" +
                "["+array[3]+"]["+array[4]+"]["+array[5]+"]\n" +
                "["+array[6]+"]["+array[7]+"]["+array[8]+"]"
        );
    }

    public static void printMatrix(ItemStack[] matrix) {
        String[] array = Arrays.stream(matrix).map(MetaRecipe::toString).toArray(String[]::new);
        int length = 0;
        for (int i = 0; i < array.length; i++) length = Math.max(length, array[i].length());
        for (int i = 0; i < array.length; i++) {
            for (int j = array[i].length(); j < length; j++) {
                array[i] = array[i] + ' ';
            }
        }
        System.out.println(
                "\n["+array[0]+"]["+array[1]+"]["+array[2]+"]\n" +
                        "["+array[3]+"]["+array[4]+"]["+array[5]+"]\n" +
                        "["+array[6]+"]["+array[7]+"]["+array[8]+"]"
        );
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
