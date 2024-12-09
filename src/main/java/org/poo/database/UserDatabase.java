package org.poo.database;

import org.poo.user.User;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public final class UserDatabase {

    private static UserDatabase instance;
    private Map<String, User> database;
    private Map<String, String> mailDatabase;

    private UserDatabase() {
        database = new LinkedHashMap<>();
        mailDatabase = new LinkedHashMap<>();
    }

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    public void addUserEntry(String userEmail, User user) {
        if (!database.containsKey(userEmail)) {
            database.put(userEmail, user);
        }
    }

    public void removeUserEntry(String userEmail) {
        database.remove(userEmail);
    }

    public User getUserEntry(String userEmail) {
        return database.get(userEmail);
    }

    public void addMailEntry(String IBAN, String userEmail) {
        mailDatabase.put(IBAN, userEmail);
    }

    public void removeMailEntry(String IBAN) {
        mailDatabase.remove(IBAN);
    }

    public String getMailEntry(String IBAN) {
        return mailDatabase.get(IBAN);
    }
}
