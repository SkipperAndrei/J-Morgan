# Project Assignment POO  - J. POO Morgan - Phase One

#### Made by : Giurgiu Andrei-Ștefan 325CA

<div align="center"><img src="https://tenor.com/en-GB/view/xrd-exrd-crypto-btc-eth-gif-23801255.gif" width="500px"></div>

## Project idea
* This project is a banking application that implements basic functionalities like users, accounts, credit cards and payments
* The application also has more complex features, like splitting payments between multiple users, freezing accounts and generating account reports 
* The project gets it's input in the form of JSON nodes, that will be parsed in the classes from the "fileio" package

## Design constrains
* Each user can have multiple accounts, which can be of two types: classic account and savings account. The difference is that the savings account has an interest rate associated, which can increase the account's funds after some time.
* The user can't generate a spending report for a saving account.
* Each account can have multiple cards, which can also be of two types : <b>classic</b> and <b>one-time pay</b>. The one-time pay card will regenerate it's card number with every transaction made.
* The user can set aliases for his/her accounts, but it can only be referenced as the receiver of a "send money" account, otherwise the command will generate an error.
* A user can set a <i><b>unique</b></i> alias for his/her accounts.
* A user can set a minimum balance for his/her accounts to avoid excessive spending
* A card can only have three statuses : <b>active</b>, <b>warning</b> and <b>frozen</b>.
* If a card becomes frozen, it can't be unfrozen.
* The card becomes "<b>warned</b>" if the difference between the account's balance and minimum balance is smaller or equal than 30.

## Functionalities
* <b>Adding an account </b>
* <b>Adding funds to an account</b>
* <b>Cash in the interest rate in a saving account</b>
* <b>Checking the status of a card</b>
* <b>Creating a new card</b>
* <b>Deleting an account or a card</b>
* <b>Making online payments using a credit card</b>
* <b>Generating the transactions for a user on all it's accounts</b>
* <b>Generating a classic report for an account</b>
* <b>Sending money online</b>
* <b>Setting an alias for an account</b>
* <b>Setting a minimum balance for an account</b>
* <b>Generating a spendings report for an account</b>
* <b>Splitting a payment between multiple users</b>

## Project Structure

* src/main/java/org.poo/
  * account/
    * Account - class that holds the account logic implementation
    * SavingAccount - class the hold the saving account logic implementation
  
  * card/
    * Card - implementation of the card logic
    * OneTimeCard - implementation of the one-time card logic

  * command/
    * AddAccount - implementation of the command "add account"
    * AddFunds - implementation of the command "add funds"
    * AddInterest - implementation of the command "add interest"
    * ChangeInterestRate - implementation of the command "change interest rate"
    * CheckCardStatus - implementation of the command "check card status"
    * Command - interface that specifies what methods must every "command" class implement : a method to execute the command and the other to generate it's output.
    * CommandConstants - enum that contains several signal codes appearing during the execution of the commands
    * CreateCard - implementation of the command "create card"
    * CreateOneTimeCard - implementation of the command "create one-time card"
    * DeleteAccount - implementation of the command "delete account"
    * FactoryCommand - utility class that instantiates a new "command" class, based on the query received as input using a switch case and command factory design pattern logic.
    * PayOnline - implementation of the command "pay online"
    * PrintTransactions - implementation of the command "print transactions"
    * PrintUsers - implementation of the command "print users"
    * Report - implementation of the command "report"
    * SendMoney - implementation of the command "send money"
    * SetAlias - implementation of the command "set alias"
    * SetMinimumBalance - implementation of the command "set minimum balance"
    * SpendingReport - implementation of the command "spending report"
    * SplitPayment - implementation of the command "split payment"

  * database/
    * ExchangeRateDatabase - class where the exchange rate graph and it's API will be implemented
    * UserDatabase - class where the user information and it's API will be stored

  * fileio/ - package that contains classes where the input will be parsed from the JSON nodes
    * CommandInput
    * CommerciantInput
    * ExchangeInput
    * ObjectInput
    * UserInput

  * main/
    * Main - class that contains the entry point of the program, the population of the database and the iteration through commands
    * Test - class used for debugging architecture and command issues

  * user/
    * User - class that contains information about a particular user
  
  * utils/
    * OutputGenerator - class responsible for generating the right output for each query requested
    * Utils - class responsible for generating iban's and card numbers for the users

## Implementation

* The application has a centralised database where all the users and their information will be kept. Since the database is global and unique, i decided to implement it using a Singleton design pattern.
* The exchange rate database is represented by a weighted oriented graph, implemented using <i>JGraphT</i>.
* Since the application is query oriented, every time a conversion between 2 nodes that are in the same component is requested, but there isn't a direct edge between them, that edge will be added at the first query that involves them
* Moreover, as the exchange rate database is also unique, there can only be one instance of it during the program, revealing the Singleton design pattern.
* For the speed of the queries, important information in the user database will be stored in several hash-maps, so that the "get" operation will have a cost of O(1).
* Each class that represents a command will have to implement the "command" interface.
* This means that each class that represents a command will implement a method that executes a command and a method that generates output for that command.
* These methods will be called in "main".
* Commands can generate output directly to stdout, or in the transactions list.
* Since commands can produce errors, the CommandConstants enum has the current present signal codes.
* The commands are instantiated using a <b> command factory design pattern </b> in the FactoryCommand class.
* Then, the commands are executed in a sequential manner.