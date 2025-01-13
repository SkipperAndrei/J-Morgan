package org.poo.database;

import org.poo.fileio.CommerciantInput;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

@Data
public class CommerciantDatabase {

    private static CommerciantDatabase instance;
    private Map<String, Integer> commNameToId;
    private Map<String, Integer> commIbanToId;
    private Map<Integer, CommerciantInput> commerciantDb;

    private CommerciantDatabase() {
        commerciantDb = new LinkedHashMap<>();
        commNameToId = new LinkedHashMap<>();
        commIbanToId = new LinkedHashMap<>();
    }

    public static CommerciantDatabase getInstance() {
        if (instance == null) {
            instance = new CommerciantDatabase();
        }

        return instance;
    }

    public void addCommerciant(final CommerciantInput commerciant) {
        commerciantDb.put(commerciant.getId(), commerciant);
        commNameToId.put(commerciant.getCommerciant(), commerciant.getId());
        commIbanToId.put(commerciant.getAccount(), commerciant.getId());
    }

    public void removeCommerciant(final Integer id) {

        try {
            CommerciantInput commerciantInput = commerciantDb.get(id);
            commerciantDb.remove(id);
            commNameToId.remove(commerciantInput.getCommerciant());
            commIbanToId.remove(commerciantInput.getAccount());
        } catch (NullPointerException e) {
            return;
        }
    }

    public CommerciantInput getCommerciant(final Integer id) {
        return commerciantDb.get(id);
    }

    // public String

    public void removeAllCommerciants() {
        commerciantDb.clear();
    }
}
