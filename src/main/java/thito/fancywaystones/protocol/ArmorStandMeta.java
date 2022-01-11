package thito.fancywaystones.protocol;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.*;
import org.bukkit.util.*;
import thito.fancywaystones.*;

public class ArmorStandMeta {
    private static final ItemStack AIR = XMaterial.AIR.parseItem();
    private boolean customNameVisible, invisible, small, marker, arms, noBasePlate;
    private String customName;
    private ItemStack helmet, chestplate, leggings, boots, mainHand, offHand;
    private EulerAngle headPose, bodyPose, leftArmPose, rightArmPose, leftLegPose, rightLegPose;

    public boolean isSmall() {
        return small;
    }

    public ItemStack getMainHand() {
        return mainHand == null ? AIR : mainHand;
    }

    public void setMainHand(ItemStack mainHand) {
        this.mainHand = mainHand;
    }

    public ItemStack getOffHand() {
        return offHand == null ? AIR : offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public void setSmall(boolean small) {
        this.small = small;
    }

    public boolean isMarker() {
        return marker;
    }

    public void setMarker(boolean marker) {
        this.marker = marker;
    }

    public boolean isArms() {
        return arms;
    }

    public void setArms(boolean arms) {
        this.arms = arms;
    }

    public boolean isNoBasePlate() {
        return noBasePlate;
    }

    public void setNoBasePlate(boolean noBasePlate) {
        this.noBasePlate = noBasePlate;
    }

    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    public void setCustomNameVisible(boolean customNameVisible) {
        this.customNameVisible = customNameVisible;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public ItemStack getHelmet() {
        return helmet == null ? AIR :helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getChestplate() {
        return chestplate == null ? AIR : chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public ItemStack getLeggings() {
        return leggings == null ? AIR : leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getBoots() {
        return boots == null ? AIR : boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public EulerAngle getHeadPose() {
        return headPose == null ? EulerAngle.ZERO : headPose;
    }

    public void setHeadPose(EulerAngle headPose) {
        this.headPose = headPose;
    }

    public EulerAngle getBodyPose() {
        return bodyPose == null ? EulerAngle.ZERO : bodyPose;
    }

    public void setBodyPose(EulerAngle bodyPose) {
        this.bodyPose = bodyPose;
    }

    public EulerAngle getLeftArmPose() {
        return leftArmPose == null ? EulerAngle.ZERO : leftArmPose;
    }

    public void setLeftArmPose(EulerAngle leftArmPose) {
        this.leftArmPose = leftArmPose;
    }

    public EulerAngle getRightArmPose() {
        return rightArmPose == null ? EulerAngle.ZERO : rightArmPose;
    }

    public void setRightArmPose(EulerAngle rightArmPose) {
        this.rightArmPose = rightArmPose;
    }

    public EulerAngle getLeftLegPose() {
        return leftLegPose == null ? EulerAngle.ZERO : leftLegPose;
    }

    public void setLeftLegPose(EulerAngle leftLegPose) {
        this.leftLegPose = leftLegPose;
    }

    public EulerAngle getRightLegPose() {
        return rightLegPose == null ? EulerAngle.ZERO : rightLegPose;
    }

    public void setRightLegPose(EulerAngle rightLegPose) {
        this.rightLegPose = rightLegPose;
    }
}
