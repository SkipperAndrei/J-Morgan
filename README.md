# Project Assignment POO  - J. POO Morgan - Phase One

#### Made by : Giurgiu Andrei-Ștefan 325CA

<div align="center"><img src="https://tenor.com/view/xrd-exrd-crypto-btc-eth-gif-23801255.gif" width="500px"></div>

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
* Every split payment initiated must be accepted or declined.
* The user has the same service plan on all accounts.
* The business account has the service plan of his owner.
* The spending threshold cashback is repeatable.
* The Nr. of transactions discounts can be cashed in once per account.

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
* <b>Generating a spending report for an account</b>
* <b>Splitting a payment between multiple users</b>
* <b>Possibility to get discounts and cashback from the commerciants</b>
* <b>Having a service plan that can affect payments and account withdrawals</b>
* <b>Creating a shared account used across a business</b>
* <b>The shared account supports an employee hierarchy</b>

## Project Structure

* src/main/java/org.poo/
  * account/
    * Account - class that holds the account logic implementation
    * SavingAccount - class that holds the saving account logic implementation
    * BusinessAccount - class where the business account logic is implemented
    * AccountPlans - enum that holds service plan information and the commission and cashback strategy
  
  * card/
    * Card - implementation of the card logic
    * OneTimeCard - implementation of the one-time card logic

  * command/
    * account/ 
      * AddAccount - implementation of the command "add account"
      * AddFunds - implementation of the command "add funds"
      * AddInterest - implementation of the command "add interest"
      * CashWithdrawal - implementation of the command "cash withdrawal"
      * ChangeInterestRate - implementation of the command "change interest rate"
      * DeleteAccount - implementation of the command "delete account"
      * SetAlias - implementation of the command "set alias"
      * SetMinimumBalance - implementation of the command "set minimum balance"
      * UpgradePlan - implementation of the command "upgrade plan"
      * WithdrawSavings - implementation of the command "withdraw savings"
    * business/
      * AddNewBusinessAssociate - implementation of the command "add new business associate"
      * BusinessReport - implementation of the command "business report"
      * ChangeDepositLimit - implementation of the command "change deposit limit"
      * ChangeSpendingLimit - implementation of the command "change spending limit"
    * cards/
      * CheckCardStatus - implementation of the command "check card status"
      * CreateCard - implementation of the command "create card"
      * CreateOneTimeCard - implementation of the command "create one-time card"
      * DeleteCard - implementation of the command "delete card"
    * payments/
      * AcceptSplitPayment - implementation of the command "accept split payment"
      * PayOnline - implementation of the command "pay online"
      * RejectSplitPayment - implementation of the command "reject split payment"
      * SendMoney - implementation of the command "send money"
      * SplitPayment - implementation of the command "split payment"
      * SplitTracker - utility class that will hold the queued split payments and is responsible for handling accept and reject requests
    * statistics/
      * PrintTransactions - implementation of the command "print transactions"
      * PrintUsers - implementation of the command "print users"
      * Report - implementation of the command "report"
      * SpendingReport - implementation of the command "spending report"
    * Command - interface that specifies what methods must every "command" class implement : a method to execute the command and the other to generate it's output.
    * CommandConstants - enum that contains several signal codes appearing during the execution of the commands
    * FactoryCommand - utility class that instantiates a new "command" class, based on the query received as input using a switch case and command factory design pattern logic.

  * database/
    * ExchangeRateDatabase - class where the exchange rate graph and it's API will be implemented
    * UserDatabase - class where the user information and it's API will be stored
    * CommerciantDatabase - class where the commerciant information and it's API will be stored

  * fileio/ - package that contains classes where the input will be parsed from the JSON nodes
    * CommandInput
    * CommerciantInput
    * ExchangeInput
    * ObjectInput
    * UserInput

  * main/
    * Main - class that contains the entry point of the program, the population of the database and the iteration through commands
    * Test - class used for debugging architecture and command issues

  * plans/
    * CashbackConstants - enum that contains several double values used in the cashback feature
    * Plan - interface that will be implemented by the plan strategies
    * StandardStrategy - class that implements the standard commission and cashback strategy
    * StudentStrategy - class that implements the student commission and cashback strategy
    * SilverStrategy - class that implements the silver commission and cashback strategy
    * GoldStrategy - class that implements the gold commission and cashback strategy
    * PlanConstants - enum that contains integer values used in the commission and cashback strategies
  
  * user/
    * User - class that contains information about a particular user
  
  * utils/
    * CashbackTracker - utility class that holds information about possible discounts or cashback
    * DiscountTracker - enum that contains integer values used in the CashbackTracker functions
    * OutputGenerator - class responsible for generating the right output for each query requested
    * Utils - class responsible for generating iban's and card numbers for the users

## Implementation

### Databases
* The application has a centralised database where all the users and their information will be kept. Since the database is global and unique, i decided to implement it using a Singleton design pattern.
* The exchange rate database is represented by a weighted oriented graph, implemented using <i>JGraphT</i>.
* The commerciant database is also unique and is implemented using a <b> Singleton design pattern</b>
* Since the application is query oriented, every time a conversion between 2 nodes that are in the same component is requested, but there isn't a direct edge between them, that edge will be added at the first query that involves them
* Moreover, as the exchange rate database is also unique, there can only be one instance of it during the program, revealing the Singleton design pattern.
* For the speed of the queries, important information in the user database will be stored in several hash-maps, so that the "get" operation will have a cost of O(1).

### Commands
* Each class that represents a command will have to implement the "command" interface.
* This means that each class that represents a command will implement a method that executes a command and a method that generates output for that command.
* These methods will be called in "main".
* Commands can generate output directly to stdout, or in the transactions list.
* Since commands can produce errors, the CommandConstants enum has the current present signal codes.
* The commands are instantiated using a <b> factory design pattern </b> in the FactoryCommand class.
* Then, the commands are executed in a sequential manner, and using the <b> command design pattern </b>.
* The split payment, which requires an accept/reject from every account involved, is implemented using an additional class, SplitTracker. In this class, which is also a <b>Singleton</b>, all the pending Split Payments are placed in an ArrayList, awaiting for response.
* When a split payment, was fully accepted, it is removed from the ArrayList, and it is executed and output is generated.

### Service plans
* Each user is assigned a service plan, that can be upgraded.
* Commissions and cashback received from commerciants is implemented using the <b> strategy design pattern </b>.
* The classes from the plans package that implement the Plan interface are responsible for the correct strategies.

### Cashback and discounts
* For this feature, I created a utility class called CashbackTracker.
* For the discounts, I used three signal codes : <b> NOT_ELIGIBLE </b>, <b> ELIGIBLE </b> and <b> CASHED_IN </b>.
* The need for these codes relies on the fact that an account can cash in the discounts only once.
* The discount codes are updated when the methods <b> checkFoodDiscount, checkClothesDiscount </b> and <b> checkTechDiscount </b> are called in the <b> handleCommerciantPayment </b> method in the Account class.
* The cashback is calculated by calling the <b> calculateNrTransactionsCashback </b> and <b> spendingTransCashback </b> methods in <b> handleCommerciantPayment </b>

### Business account
* The BusinessAccount class has more complex functionalities, than the other type of accounts in this application.
* This is because, unlike the other two types of accounts, this one is shared, so additional checks must be enforced.
* This type of account has two inner classes. 
* The first one, <b>EmployeeInfo</b>, stores the name, email, role, how much he/she spent and deposited and also the transactions.
* The last part of information is relevant for business reports.
* The second inner class, <b>CommerciantInfo</b>, stores the name of the commerciant, the amount paid by the employees and the employees that made transactions to them.
* This class is only used for generating the commerciant type of the business report.
