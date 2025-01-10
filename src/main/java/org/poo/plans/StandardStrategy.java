package org.poo.plans;

public final class StandardStrategy implements Plan {

    @Override
    public double commissionStrategy(final double sum, final String sumCurrency) {
        return sum + 2 * sum / PlanConstants.SCALING_FACTOR.getValue();
    }

    @Override
    public double cashbackStrategy(final double sum, final int threshold) {

        switch (threshold) {

            case 100 -> {
                return sum / PlanConstants.SCALING_FACTOR.getValue();
            }

            case 300 -> {
                return 2 * sum / PlanConstants.SCALING_FACTOR.getValue();
            }

            case 500 -> {
                return 2.5 * sum / PlanConstants.SCALING_FACTOR.getValue();
            }

            default -> {
                return 0;
            }
        }
    }
}
