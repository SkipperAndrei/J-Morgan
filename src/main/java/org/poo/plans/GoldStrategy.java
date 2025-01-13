package org.poo.plans;

public final class GoldStrategy implements Plan {

    @Override
    public double commissionStrategy(final double sum, final String sumCurrency) {
        return sum;
    }

    @Override
    public double cashbackStrategy(final double sum, final int threshold) {

        switch (threshold) {

            case 100 -> {
                return sum * 0.005;
            }

            case 300 -> {
                return sum * 0.0055;
            }

            case 500 -> {
                return sum * 0.007;
            }

            default -> {
                return 0;
            }
        }
    }


}
