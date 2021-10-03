package thito.fancywaystones.economy;

public class Cost {
    private EconomyService service;
    private int amount;

    public Cost(EconomyService service, int amount) {
        this.service = service;
        this.amount = amount;
    }

    public EconomyService getService() {
        return service;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Cost{" +
                "service=" + service.getId() +
                ", amount=" + amount +
                '}';
    }
}
