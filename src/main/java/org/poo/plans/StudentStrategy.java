package org.poo.plans;

public final class StudentStrategy implements Plan {

    @Override
    public double commissionStrategy(final double sum, final String sumCurrency) {
        return sum;
    }

    @Override
    public double cashbackStrategy(final double sum, final int threshold) {

        switch (threshold) {

            case 100 -> {
                return sum * 0.001;
            }

            case 300 -> {
                return sum * 0.002;
            }

            case 500 -> {
                return  sum * 0.0025;
            }

            default -> {
                return 0;
            }
        }
    }
}
