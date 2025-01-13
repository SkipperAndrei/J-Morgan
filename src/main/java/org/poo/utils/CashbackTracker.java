package org.poo.utils;

import lombok.Setter;
import org.poo.database.CommerciantDatabase;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.poo.plans.Plan;

@Data
public class CashbackTracker {

    private DiscountTracker foodDiscount = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker clothesDiscount = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker techDiscount = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker oneHundredThreshold = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker threeHundredThreshold = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker fiveHundredThreshold = DiscountTracker.NOT_ELIGIBLE;

    // private Map<Integer, Double> spendingCommerciants = new LinkedHashMap<>();
    private Double spendingCommerciants = 0.0;
    private Map<Integer, Integer> nrOfTransCommerciants = new LinkedHashMap<>();

    public boolean checkFoodDiscount(final Integer commId) {

        if (foodDiscount.equals(DiscountTracker.CASHED_IN)) {
            return false;
        }

        Integer nrTrans = nrOfTransCommerciants.get(commId);

        if (nrTrans == 2 && foodDiscount.equals(DiscountTracker.NOT_ELIGIBLE)) {
            foodDiscount = DiscountTracker.ELIGIBLE;
            return false;
        }

        return nrTrans >= 2 && foodDiscount.equals(DiscountTracker.ELIGIBLE);

    }

    public boolean checkClothesDiscount(final Integer commId) {

        if (clothesDiscount.equals(DiscountTracker.CASHED_IN)) {
            return false;
        }

        Integer nrTrans = nrOfTransCommerciants.get(commId);

        if (nrTrans == DiscountTracker.CLOTHES_THRESHOLD.getValue()
                    && clothesDiscount.equals(DiscountTracker.NOT_ELIGIBLE)) {

            clothesDiscount = DiscountTracker.ELIGIBLE;
            return false;
        }

        return nrTrans >= DiscountTracker.CLOTHES_THRESHOLD.getValue()
                && clothesDiscount.equals(DiscountTracker.ELIGIBLE);
    }

    public boolean checkTechDiscount(final Integer commId) {
        if (techDiscount.equals(DiscountTracker.CASHED_IN)) {
            return false;
        }

        Integer nrTrans = nrOfTransCommerciants.get(commId);

        if (nrTrans == DiscountTracker.TECH_THRESHOLD.getValue()
                    && techDiscount.equals(DiscountTracker.NOT_ELIGIBLE)) {

            techDiscount = DiscountTracker.ELIGIBLE;
            return false;
        }

        return nrTrans >= DiscountTracker.TECH_THRESHOLD.getValue()
                && techDiscount.equals(DiscountTracker.ELIGIBLE);
    }

    public double checkOneHundThreshold(final double amount, final Integer commId,
                                        final Plan accPlan) {


        // Double moneySpent = spendingCommerciants.get(commId);
        Double moneySpent = spendingCommerciants;

        if (moneySpent >= DiscountTracker.ONE_HUNDRED_THRESHOLD.getValue()) {

            return accPlan.cashbackStrategy(amount,
                                            DiscountTracker.ONE_HUNDRED_THRESHOLD.getValue());

        }

        return 0;
    }

    public double checkThreeHundThreshold(final double amount, final Integer commId,
                                            final Plan accPlan) {


        // Double moneySpent = spendingCommerciants.get(commId);
        Double moneySpent = spendingCommerciants;

        if (moneySpent >= DiscountTracker.THREE_HUNDRED_THRESHOLD.getValue()) {

            return accPlan.cashbackStrategy(amount,
                                            DiscountTracker.THREE_HUNDRED_THRESHOLD.getValue());

        }

        return 0;
    }

    public double checkFiveHundThreshold(final double amount, final Integer commId,
                                          final Plan accPlan) {

        // Double moneySpent = spendingCommerciants.get(commId);
        Double moneySpent = spendingCommerciants;

        if (moneySpent >= DiscountTracker.FIVE_HUNDRED_THRESHOLD.getValue()) {

            return accPlan.cashbackStrategy(amount,
                                            DiscountTracker.FIVE_HUNDRED_THRESHOLD.getValue());

        }

        return 0;
    }

    public double calculateNrTransactionsCashback(final String commType, final double amount) {

        if (commType.equals("Food") && foodDiscount.equals(DiscountTracker.ELIGIBLE)) {
            foodDiscount = DiscountTracker.CASHED_IN;
            return amount / 50;
        }

        if (commType.equals("Clothes") && clothesDiscount.equals(DiscountTracker.ELIGIBLE)) {
            clothesDiscount = DiscountTracker.CASHED_IN;
            return amount / 20;
        }

        if (commType.equals("Tech") && techDiscount.equals(DiscountTracker.ELIGIBLE)) {
            techDiscount = DiscountTracker.CASHED_IN;
            return amount / 10;
        }

        return 0;
    }

    public double SpendingTransCashback(final double amount, final Integer commId, final Plan accPlan) {

        double cashback = checkFiveHundThreshold(amount, commId, accPlan);

        if (cashback != 0) {
            return cashback;
        }

        cashback = checkThreeHundThreshold(amount, commId, accPlan);

        if (cashback != 0) {
            return cashback;
        }

        return checkOneHundThreshold(amount, commId, accPlan);

    }

}
