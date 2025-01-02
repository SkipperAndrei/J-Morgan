package org.poo.account;

import lombok.Getter;
import org.poo.plans.Plan;
import org.poo.plans.NoCommissionStrategy;
import org.poo.plans.SilverStrategy;
import org.poo.plans.StandardStrategy;


@Getter
public enum AccountPlans {

    STANDARD("standard"),
    STUDENT("student"),
    SILVER("silver"),
    GOLD("gold");

    private final String value;
    private Plan planStrategy;

    AccountPlans(final String value) {
        this.value = value;
    }

    /**
     * This function returns the type of plan registered to every account
     * @return The plan
     */
    public Plan getPlanStrategy() {

        switch (value) {
            case "student", "gold" -> planStrategy = new NoCommissionStrategy();
            case "silver" -> planStrategy = new SilverStrategy();
            default -> planStrategy = new StandardStrategy();
        }

        return planStrategy;
    }

}
