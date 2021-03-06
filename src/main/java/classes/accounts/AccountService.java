package classes.accounts;

import classes.MySqlCon;
import  main.java.classes.Audit;
import classes.MyException;
import java.sql.*;
import java.util.ArrayList;

public class AccountService {

    public static ArrayList<Account> accounts = new ArrayList<>();

    public static void getAccounts() throws Exception {
        Audit.write("getAccounts");

        MySqlCon mySqlCon = new MySqlCon();
        Connection connection = mySqlCon.Connection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from account, current where idaccount = idcurrent ");
        while (resultSet.next()){
            Account account = new Current(resultSet.getInt("idaccount"),
                    resultSet.getString("iban"),
                    resultSet.getDouble("balance"),
                    resultSet.getString("createDate"),
                    resultSet.getInt("idclient"));
            accounts.add(account);
        }

        resultSet = statement.executeQuery("select * " +
                                               "from account, deposit " +
                                               "where idaccount = iddeposit");
        while (resultSet.next()){
            Account account = new Deposit(resultSet.getInt("idaccount"),
                    resultSet.getString("iban"),
                    resultSet.getDouble("balance"),
                    resultSet.getInt("idclient"),
                    resultSet.getString("createDate"),
                    resultSet.getInt("period"),
                    resultSet.getDouble("gain"));
            accounts.add(account);
        }
    }
    public static void displayAccounts ()
    {
        Audit.write("DisplayAccounts");
        System.out.println("List of accounts: \n");
        for ( Account account : accounts)
        {
            System.out.println(account.toString());
        }
    }


    public static Account getAccountByClientId (Integer id)
    {
        for ( Account account : accounts)
        {
            if ( account.getIdClient().equals(id) ) {
                return account;
            }
        }
        return null;
    }
    public static Account getAccountById (int id)
    {
        for ( Account account : accounts)
        {
            if ( account.getID() == id) {
                return account;
            }
        }
        return  null;
    }
    public static Account getAccountByIban(String IBAN)
    {
        for ( Account account : accounts)
        {
            if ( account.getIBAN().equals(IBAN)) {
                return account;
            }
        }
        return  null;
    }

    public static void withdraw(double amount, Integer ID) throws MyException, SQLException {
        for ( Account account : accounts)
        {
            if ( account.getID()==ID ) {
                if(amount > account.getBalance())
                {
                    throw new MyException("Insufficient funds");
                }
                ((Current) account).withdraw(amount);
                updateBalance(account.getBalance(), ID);
            }
        }
    }
    public static void deposit(double amount, Integer ID) throws MyException, SQLException {
        for ( Account account : accounts)
        {
            if ( account.getID()==ID ) {
                account.deposit(amount);
                updateBalance(account.getBalance(), ID);
            }
        }
    }

    public static void updateBalance(Double balance, int id) throws SQLException {

        MySqlCon mySqlCon = new MySqlCon();
        Connection connection = mySqlCon.Connection();
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE banckingapp.account SET balance = ? WHERE idaccount = ?;");
        preparedStatement.setDouble(1,balance);
        preparedStatement.setInt(2,id);
        preparedStatement.execute();}


    public static ArrayList<Account> addAccount (Account account) throws SQLException {
        Audit.write("AddAccount");
        accounts.add(account);

        MySqlCon mySqlCon = new MySqlCon();
        Connection connection = mySqlCon.Connection();
        PreparedStatement preparedStatement;

        preparedStatement = connection.prepareStatement("INSERT INTO account values (?,?,?,?,?)");
        preparedStatement.setInt(1,   account.getID());
        preparedStatement.setString(2,account.getIBAN());
        preparedStatement.setDouble(3,account.getBalance());
        preparedStatement.setDate(4,new Date(account.getCreateDate().getTimeInMillis()));
        preparedStatement.setInt(5,account.getIdClient());
        preparedStatement.execute();

        if(account instanceof Current)
        {
            preparedStatement = connection.prepareStatement("INSERT INTO current values (?)");
            preparedStatement.setInt(1,   account.getID());
            preparedStatement.execute();
        }
        else
        {
            account = (Deposit) account;
            System.out.println(account);
            preparedStatement = connection.prepareStatement("INSERT INTO deposit values (?, ?, ?)");
            preparedStatement.setInt(1,   account.getID());
            preparedStatement.setInt(2,   ((Deposit) account).getPeriod());
            preparedStatement.setDouble(3, ((Deposit) account).getGain());
            preparedStatement.execute();
        }


        return accounts;
    }

    public static ArrayList<Account> deleteAccount (Account account) throws SQLException {
        Audit.write("DeleteAccount");
        accounts.remove(account);
        MySqlCon mySqlCon = new MySqlCon();
        Connection connection = mySqlCon.Connection();
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM banckingapp.account WHERE idaccount = ?;");
        preparedStatement.setInt(1,account.getID());
        preparedStatement.execute();
        return accounts;
    }

}
