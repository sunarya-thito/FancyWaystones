package thito.fancywaystones.condition.handler;

import thito.fancywaystones.*;
import thito.fancywaystones.condition.*;

import java.util.*;

public class RandomConditionHandler implements ConditionHandler {
    private int chance;
    private Random random = new Random();

    public RandomConditionHandler(int chance) {
        this.chance = chance;
    }

    @Override
    public boolean test(Placeholder placeholder) {
        return random.nextInt(100) <= chance;
    }
}
