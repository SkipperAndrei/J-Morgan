package org.poo.plans;

public final class NoCommissionStrategy implements Plan {

    @Override
    public double commissionStrategy(final double sum, final String sumCurrency) {
        return sum;
    }
}
