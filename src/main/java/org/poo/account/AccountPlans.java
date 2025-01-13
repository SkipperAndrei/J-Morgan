package org.poo.account;

import lombok.Getter;
import org.poo.plans.Plan;
import org.poo.plans.StandardStrategy;
import org.poo.plans.StudentStrategy;
import org.poo.plans.SilverStrategy;
import org.poo.plans.GoldStrategy;


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
            case "student" -> planStrategy = new StudentStrategy();
            case "silver" -> planStrategy = new SilverStrategy();
            case "gold" -> planStrategy = new GoldStrategy();
            default -> planStrategy = new StandardStrategy();
        }

        return planStrategy;
    }

}
