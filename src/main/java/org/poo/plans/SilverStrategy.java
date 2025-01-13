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

        if (threshold == PlanConstants.FIRST_THRESHOLD.getValue()) {
            return sum * CashbackConstants.SILVER_ONE_HUNDRED_PERCENTAGE.getValue();
        }

        if (threshold == PlanConstants.SECOND_THRESHOLD.getValue()) {
            return sum * CashbackConstants.SILVER_THREE_HUNDRED_PERCENTAGE.getValue();
        }

        if (threshold == PlanConstants.FINAL_THRESHOLD.getValue()) {
            return sum * CashbackConstants.SILVER_FIVE_HUNDRED_PERCENTAGE.getValue();
        }

        return 0;

    }
}
