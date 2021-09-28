package thito.fancywaystones.condition.handler;

import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

public class EnchantedConditionHandler implements ConditionHandler {
    private Enchantment enchantment;
    private int level;

    public EnchantedConditionHandler(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    @Override
    public boolean test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (player != null) {
            try {
                ItemStack inMainHand = player.getEquipment().getItemInMainHand();
                if (inMainHand.getEnchantmentLevel(enchantment) >= level) {
                    return true;
                }
                ItemStack inOffHand = player.getEquipment().getItemInOffHand();
                if (inOffHand.getEnchantmentLevel(enchantment) >= level) {
                    return true;
                }
                return false;
            } catch (Throwable t) {
            }
            ItemStack inHand = player.getItemInHand();
            return inHand.getEnchantmentLevel(enchantment) >= level;
        }
        return false;
    }
}
