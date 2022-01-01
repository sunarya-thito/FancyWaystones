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
    public String test(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (player != null) {
            try {
                ItemStack inMainHand = player.getEquipment().getItemInMainHand();
                if (inMainHand.getEnchantmentLevel(enchantment) >= level) {
                    return null;
                }
                ItemStack inOffHand = player.getEquipment().getItemInOffHand();
                if (inOffHand.getEnchantmentLevel(enchantment) >= level) {
                    return null;
                }
                return placeholder.clone()
                        .put("enchantment", ph -> enchantment.getName())
                        .put("enchantment-level", ph -> level).replace("{language.condition.enchantment}");
            } catch (Throwable ignored) {
            }
            ItemStack inHand = player.getItemInHand();
            return inHand.getEnchantmentLevel(enchantment) >= level ? null :
                    placeholder.clone()
                            .put("enchantment", ph -> enchantment.getName())
                            .put("enchantment-level", ph -> level).replace("{language.condition.enchantment}");
        }
        return placeholder.clone()
                .put("enchantment", ph -> enchantment.getName())
                .put("enchantment-level", ph -> level).replace("{language.condition.enchantment}");
    }

    @Override
    public String testNegate(Placeholder placeholder) {
        Player player = placeholder.get(Placeholder.PLAYER);
        if (player != null) {
            try {
                ItemStack inMainHand = player.getEquipment().getItemInMainHand();
                if (inMainHand.getEnchantmentLevel(enchantment) >= level) {
                    return placeholder.clone()
                            .put("enchantment", ph -> enchantment.getName())
                            .put("enchantment-level", ph -> level).replace("{language.condition.not-enchantment}");
                }
                ItemStack inOffHand = player.getEquipment().getItemInOffHand();
                if (inOffHand.getEnchantmentLevel(enchantment) >= level) {
                    return placeholder.clone()
                            .put("enchantment", ph -> enchantment.getName())
                            .put("enchantment-level", ph -> level).replace("{language.condition.not-enchantment}");
                }
                return null;
            } catch (Throwable ignored) {
            }
            ItemStack inHand = player.getItemInHand();
            return inHand.getEnchantmentLevel(enchantment) >= level ? placeholder.clone()
                    .put("enchantment", ph -> enchantment.getName())
                    .put("enchantment-level", ph -> level).replace("{language.condition.not-enchantment}") :
                    null
                    ;
        }
        return null;
    }
}
