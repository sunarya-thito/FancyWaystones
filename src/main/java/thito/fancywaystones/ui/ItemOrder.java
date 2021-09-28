package thito.fancywaystones.ui;

public enum ItemOrder {
    ASCEND(1), DESCEND(-1);
    private int multiplier;

    ItemOrder(int multiplier) {
        this.multiplier = multiplier;
    }

    public int multiplier() {
        return multiplier;
    }
}
