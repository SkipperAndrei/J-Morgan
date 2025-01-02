package org.poo.plans;

public final class StandardStrategy implements Plan {

    @Override
    public double commissionStrategy(final double sum, final String sumCurrency) {
        return sum - 2 * sum / PlanConstants.SCALING_FACTOR.getValue();
    }
}
