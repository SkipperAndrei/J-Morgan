package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.command.Command;
import org.poo.command.FactoryCommand;
import org.poo.command.payments.SplitTracker;
import org.poo.database.CommerciantDatabase;
import org.poo.database.ExchangeRateDatabase;
import org.poo.database.UserDatabase;
import org.poo.fileio.*;
import org.poo.utils.OutputGenerator;
import org.poo.user.User;
import org.poo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        var sortedFiles = Arrays.stream(Objects.requireNonNull(directory.listFiles())).
                sorted(Comparator.comparingInt(Main::fileConsumer))
                .toList();

        for (File file : sortedFiles) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(CheckerConstants.TESTS_PATH + filePath1);
        ObjectInput inputData = objectMapper.readValue(file, ObjectInput.class);

        ArrayNode output = objectMapper.createArrayNode();
        UserDatabase userDB = UserDatabase.getInstance();
        ExchangeRateDatabase exchangeDB = ExchangeRateDatabase.getInstance();
        CommerciantDatabase commDB = CommerciantDatabase.getInstance();
        OutputGenerator generator = new OutputGenerator(objectMapper, output, userDB);

        // building the user database
        for (UserInput userInp : inputData.getUsers()) {
            User usr = new User(userInp);
            userDB.addUserEntry(usr.getUserData().getEmail(), usr);
        }

        // building the exchange rate database
        for (ExchangeInput exchange : inputData.getExchangeRates()) {
            exchangeDB.addNewExchange(exchange.getFrom(), exchange.getTo(), exchange.getRate());
            exchangeDB.addNewExchange(exchange.getTo(), exchange.getFrom(), 1 / exchange.getRate());
        }

        // building the commerciant database
        for (CommerciantInput comm : inputData.getCommerciants()) {
            commDB.addCommerciant(comm);
        }

        // Iterate through the commands and execute them in a single-threaded context
        for (CommandInput command : inputData.getCommands()) {
            Command comm = FactoryCommand.extractCommand(command, exchangeDB);

            try {
                comm.executeCommand(userDB);
                comm.generateOutput(generator);
            } catch (NullPointerException ex) {
                continue;
            }

        }

        UserDatabase.getInstance().clearDatabase();
        ExchangeRateDatabase.getInstance().resetExchangeDatabase();
        CommerciantDatabase.getInstance().removeAllCommerciants();
        SplitTracker.getInstance().getListener().clear();
        Utils.resetRandom();

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }

    /**
     * Method used for extracting the test number from the file name.
     *
     * @param file the input file
     * @return the extracted numbers
     */
    public static int fileConsumer(final File file) {
        return Integer.parseInt(
                file.getName()
                        .replaceAll(CheckerConstants.DIGIT_REGEX, CheckerConstants.EMPTY_STR)
        );
    }
}
