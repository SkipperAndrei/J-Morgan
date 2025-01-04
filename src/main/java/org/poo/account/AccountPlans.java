package org.poo.account;

import lombok.Getter;
import org.poo.plans.Plan;
import org.poo.plans.NoCommissionStrategy;
import org.poo.plans.SilverStrategy;
import org.poo.plans.StandardStrategy;


@Getter
public enum AccountPlans {

    STANDARD("standard", 0),
    STUDENT("student", 0),
    SILVER("silver", 1),
    GOLD("gold", 2);

    private final String value;
    private final int priority;
    private Plan planStrategy;

    AccountPlans(final String value, final int priority) {
        this.value = value;
        this.priority = priority;
        this.planStrategy = new StandardStrategy();
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
