package thito.fancywaystones.economy;

import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.*;

public class ItemEconomyService implements EconomyService {
    private String id;
    private ItemStack itemStack;
    private String format;

    public ItemEconomyService(String id, ItemStack itemStack, String format) {
        this.id = id;
        this.itemStack = itemStack;
        this.format = format;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String formattedCurrency(int amount) {
        return format.replace("{amount}", String.valueOf(amount)).replace("{type}", capitalizeFully(itemStack.getType().name().replace('_', ' ')));
    }

    static String capitalize(String s) {
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
        return s.toUpperCase();
    }

    public static String capitalizeFully(String s) {
        String[] split = s.split(" ");
        for (int i = 0; i < split.length; i++) split[i] = capitalize(split[i]);
        return String.join(" ", split);
    }

    @Override
    public String getDisplayName(Placeholder placeholder) {
        return capitalizeFully(itemStack.getType().name());
    }

    public ItemEconomyService(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean has(Player player, int amount) {
        return balance(player) >= amount;
    }

    @Override
    public void withdraw(Player player, int amount) {
        ItemStack itemStack = this.itemStack.clone();
        itemStack.setAmount(amount);
        player.getInventory().removeItem(itemStack);
    }

    @Override
    public void deposit(Player player, int amount) {
        ItemStack itemStack = this.itemStack.clone();
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    @Override
    public double balance(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(itemStack)) {
                count += item.getAmount() * itemStack.getAmount();
            }
        }
        return count;
    }
}
