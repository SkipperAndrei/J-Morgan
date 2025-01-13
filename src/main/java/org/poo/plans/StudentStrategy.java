package org.poo.plans;

public final class StudentStrategy implements Plan {

    @Override
    public double commissionStrategy(final double sum, final String sumCurrency) {
        return sum;
    }

    @Override
    public double cashbackStrategy(final double sum, final int threshold) {

        if (threshold == PlanConstants.FIRST_THRESHOLD.getValue()) {
            return sum * CashbackConstants.STD_ONE_HUNDRED_PERCENTAGE.getValue();
        }

        if (threshold == PlanConstants.SECOND_THRESHOLD.getValue()) {
            return sum * CashbackConstants.STD_THREE_HUNDRED_PERCENTAGE.getValue();
        }

        if (threshold == PlanConstants.FINAL_THRESHOLD.getValue()) {
            return sum * CashbackConstants.STD_FIVE_HUNDRED_PERCENTAGE.getValue();
        }

        return 0;
    }
}
