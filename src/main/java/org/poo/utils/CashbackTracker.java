package org.poo.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.poo.plans.Plan;

@Data
public final class CashbackTracker {

    private DiscountTracker foodDiscount = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker clothesDiscount = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker techDiscount = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker oneHundredThreshold = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker threeHundredThreshold = DiscountTracker.NOT_ELIGIBLE;
    private DiscountTracker fiveHundredThreshold = DiscountTracker.NOT_ELIGIBLE;

    private Double spendingCommerciants = 0.0;
    private Map<Integer, Integer> nrOfTransCommerciants = new LinkedHashMap<>();

    public void checkFoodDiscount(final Integer commId) {

        if (foodDiscount.equals(DiscountTracker.CASHED_IN)) {
            return;
        }

        Integer nrTrans = nrOfTransCommerciants.get(commId);

        if (nrTrans == 2 && foodDiscount.equals(DiscountTracker.NOT_ELIGIBLE)) {
            foodDiscount = DiscountTracker.ELIGIBLE;
        }

    }

    public void checkClothesDiscount(final Integer commId) {

        if (clothesDiscount.equals(DiscountTracker.CASHED_IN)) {
            return;
        }

        Integer nrTrans = nrOfTransCommerciants.get(commId);

        if (nrTrans == DiscountTracker.CLOTHES_THRESHOLD.getValue()
                    && clothesDiscount.equals(DiscountTracker.NOT_ELIGIBLE)) {

            clothesDiscount = DiscountTracker.ELIGIBLE;
        }

    }

    public void checkTechDiscount(final Integer commId) {
        if (techDiscount.equals(DiscountTracker.CASHED_IN)) {
            return;
        }

        Integer nrTrans = nrOfTransCommerciants.get(commId);

        if (nrTrans == DiscountTracker.TECH_THRESHOLD.getValue()
                    && techDiscount.equals(DiscountTracker.NOT_ELIGIBLE)) {

            techDiscount = DiscountTracker.ELIGIBLE;
        }

    }

    public double checkOneHundThreshold(final double amount, final Plan accPlan) {

        Double moneySpent = spendingCommerciants;

        if (moneySpent >= DiscountTracker.ONE_HUNDRED_THRESHOLD.getValue()) {

            return accPlan.cashbackStrategy(amount,
                                            DiscountTracker.ONE_HUNDRED_THRESHOLD.getValue());

        }

        return 0;
    }

    public double checkThreeHundThreshold(final double amount, final Plan accPlan) {

        Double moneySpent = spendingCommerciants;

        if (moneySpent >= DiscountTracker.THREE_HUNDRED_THRESHOLD.getValue()) {

            return accPlan.cashbackStrategy(amount,
                                            DiscountTracker.THREE_HUNDRED_THRESHOLD.getValue());

        }

        return 0;
    }

    public double checkFiveHundThreshold(final double amount, final Plan accPlan) {

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
            return amount * 0.02;
        }

        if (commType.equals("Clothes") && clothesDiscount.equals(DiscountTracker.ELIGIBLE)) {
            clothesDiscount = DiscountTracker.CASHED_IN;
            return amount * 0.05;
        }

        if (commType.equals("Tech") && techDiscount.equals(DiscountTracker.ELIGIBLE)) {
            techDiscount = DiscountTracker.CASHED_IN;
            return amount * 0.1;
        }

        return 0;
    }

    public double SpendingTransCashback(final double amount, final Plan accPlan) {

        double cashback = checkFiveHundThreshold(amount, accPlan);

        if (cashback != 0) {
            return cashback;
        }

        cashback = checkThreeHundThreshold(amount, accPlan);

        if (cashback != 0) {
            return cashback;
        }

        return checkOneHundThreshold(amount, accPlan);

    }

}
