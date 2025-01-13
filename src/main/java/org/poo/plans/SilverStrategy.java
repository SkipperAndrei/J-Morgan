package org.poo.plans;

import org.poo.database.ExchangeRateDatabase;

public final class SilverStrategy implements Plan {

    @Override
    public double commissionStrategy(final double sum, final String sumCurrency) {

        if (sumCurrency.equals("RON")) {
            if (sum < PlanConstants.SILVER_THRESHOLD.getValue()) {
                return sum;
            }
            return sum + sum / PlanConstants.SCALING_FACTOR.getValue();
        }

        double rate = ExchangeRateDatabase.getInstance().getExchangeRate(sumCurrency, "RON");
        double newSum = sum * rate;

        if (newSum < PlanConstants.SILVER_THRESHOLD.getValue()) {
            return sum;
        }
        return sum + sum / PlanConstants.SCALING_FACTOR.getValue();

    }

    @Override
    public double cashbackStrategy(final double sum, final int threshold) {

        switch (threshold) {

            case 100 -> {
                return sum * 0.003;
            }

            case 300 -> {
                return sum * 0.004;
            }

            case 500 -> {
                return sum * 0.005;
            }

            default -> {
                return 0;
            }

        }

    }
}
