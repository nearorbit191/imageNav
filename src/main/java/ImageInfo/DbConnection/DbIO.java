package ImageInfo.DbConnection;

import java.sql.*;

public class DbIO {
    Connection connection;
    public Statement statement;

    void connectToDatabase(String pathToDatabase) throws SQLException {
        pathToDatabase=Character.toString(pathToDatabase.charAt(pathToDatabase.length()-1)).equals("/")?pathToDatabase:pathToDatabase+"/";
        connection= DriverManager.getConnection("jdbc:sqlite:"+pathToDatabase+"nice.db");
        statement = connection.createStatement();
        statement.setQueryTimeout(30);
    }

    public void executeUpdateStatement(String sql) throws SQLException {
        statement.executeUpdate(sql);
    }

    public ResultSet executeQueryStatement(String sql) throws SQLException {
        return statement.executeQuery(sql);
    }

}
