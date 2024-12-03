package org.poo.database;

import org.poo.user.User;
import java.util.HashMap;
import java.util.Map;

public final class UserDatabase {

    private static UserDatabase instance;
    private Map<String, User> database;

    private UserDatabase() {
        database = new HashMap<>();
    }

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    public void addEntry(String userEmail, User user) {
        if (!database.containsKey(userEmail)) {
            database.put(userEmail, user);
        }
    }

    public void removeEntry(String userEmail) {
        database.remove(userEmail);
    }

    public User getEntry(String userEmail) {
        return database.get(userEmail);
    }
}
