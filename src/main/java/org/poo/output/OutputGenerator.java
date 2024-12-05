package org.poo.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.database.UserDatabase;
import org.poo.user.User;
import lombok.Data;

@Data
public final class OutputGenerator {

    private ObjectMapper mapper;
    private ArrayNode output;
    private UserDatabase userDatabase;

    public OutputGenerator(ObjectMapper mapper, ArrayNode output, UserDatabase userDatabase) {
        this.mapper = mapper;
        this.output = output;
        this.userDatabase = userDatabase;
    }

    public void addUsers(final int timestamp) {

        ObjectNode usersNode = mapper.createObjectNode();
        usersNode.put("command", "printUsers");
        ArrayNode users = mapper.createArrayNode();

        for (User user : userDatabase.getDatabase().values()) {
            users.add(user.userToJson(mapper));
        }
        usersNode.set("output", users);
        usersNode.put("timestamp", timestamp);
        output.add(usersNode);
    }

    public void deleteAccount(final int timestamp, final boolean error) {
        ObjectNode deleteNode = mapper.createObjectNode();
        deleteNode.put("command", "deleteAccount");

        ObjectNode infoNode = mapper.createObjectNode();

        if (error) {
            infoNode.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
        } else {
            infoNode.put("success", "Account deleted");
        }

        infoNode.put("timestamp", timestamp);
        deleteNode.set("output", infoNode);
        deleteNode.put("timestamp", timestamp);

        output.add(deleteNode);
    }

}
