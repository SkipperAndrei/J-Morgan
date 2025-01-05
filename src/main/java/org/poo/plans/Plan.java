package org.poo.plans;

public interface Plan {

    /**
     * This function implements commission strategies based on the service plan of the account
     * @param sum The sum that needs to be transferred
     * @param sumCurrency The currency that sum is calculated
     * @return The sum that will be transferred after applying commission strategy logic
     */
    double commissionStrategy(double sum, String sumCurrency);

    /**
     * This function implements cashback strategies based on the service plan of the account
     * @param sum The sum of the current transaction
     * @param threshold The threshold that was surpassed
     * @return The sum with the cashback
     */
    double cashbackStrategy(double sum, int threshold);

}
