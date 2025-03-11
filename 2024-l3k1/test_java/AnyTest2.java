package test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.util.Arrays;

public class AnyTest2 {
    public static void main(String[] args) {
        String tableName="test_table";
        String jdbcUrl1 = "jdbc:postgresql://localhost:5432/imdb";
        String username1 = "postgres";
        String password1 = "";
       
        int i=0;
        
        

        try 
        		{
        	Connection connection=DriverManager.getConnection(jdbcUrl1, username1, password1);
        		String[] tokens = getColumnNames1(connection, "test_table");
        		System.out.println(tokens[1]);
        		System.out.println("Starting ....\n");
            int count = 3;
           Combiner<String> combiner = new Combiner<String>(count, tokens);
            String[] result = new String[count];
            System.out.println("Starting 2 ....\n");
            System.out.println("Result ...."+ result[0]);

            alterTableAddNumFields(connection);
            while (combiner.searchNext(result))
            {
            	  System.out.println("Starting 3 ....\n");
            	i=i+1;
            System.out.println("i...."+i);
                System.out.println(Arrays.toString(result));
                String column1 = result[0];
                String column2 = result[1];
                System.out.println("Inserting ....\n");
                System.out.println(result[0]);
                System.out.println(result[1]);
              testcall1(connection,tableName,column1,column2);
                System.out.println("Calculating ....\n");
                System.out.println(result[0]);
                System.out.println(result[1]);
               //count=3;
               String column3=result[2];
               System.out.println(result[2]);
              testcall3(connection,tableName,column1,column2,column3);
                
            }
            
            
          //  count = 3;
        //  String[] result1 = new String[count];
         //               while (combiner.searchNext(result1))
         //   {
        //    	i=i+1;
        //    System.out.println("i...."+i);
        //        System.out.println(Arrays.toString(result1));
        //        String column1 = result1[0];
        //        String column2 = result1[1];
        //        String column3 = result1[2];
        //        System.out.println("Inserting ....\n");
        //        System.out.println(result1[0]);
        //        System.out.println(result1[1]);
                
         //       System.out.println("Calculating ....\n");
         //       System.out.println(result[0]);
         //       System.out.println(result[1]);
                
           // }

            
            
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
    

    
    public static String[] getColumnNames1(Connection connection, String tableName) throws SQLException {
        String[] columnNames = null;
        try {
            Statement statement = connection.createStatement();
            Statement statementCount = connection.createStatement();
            System.out.println("Get column names ....\n");
            // Adjust SQL for PostgreSQL
            String sql = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "' AND data_type = 'integer'";
            String sqlcount = "SELECT count(*) FROM information_schema.columns WHERE table_name = '" + tableName + "' AND data_type = 'integer'";

            ResultSet resultSet = statement.executeQuery(sql);
            ResultSet resultSetCount = statementCount.executeQuery(sqlcount);

            int columnCount = 0;
            if (resultSetCount.next()) {
                columnCount = resultSetCount.getInt(1);
            }

            columnNames = new String[columnCount];
            System.out.println(columnCount);

            System.out.println("Get column names 1....\n");
            int i = 0;
            while (resultSet.next()) {
                columnNames[i] = resultSet.getString("column_name");
                System.out.println(columnNames[i]);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error\n");
        }
        return columnNames;
    }

    
    public static class Combiner<T> {
        protected int count;
        protected T[] array;
        protected int[] indexes;

        public Combiner(int count, T[] array) {
            super();
            this.count = count;
            this.array = array;
            indexes = new int[count];
            for (int i = 0; i < count; i++)
                indexes[i] = i;
        }

        public boolean searchNext(T[] result) {
            if (indexes == null)
                return false;

            int resultIndex = 0;
            for (int index : indexes)
                result[resultIndex++] = array[index];

            int indexesRank = count - 1;
            int arrayRank = array.length - 1;
            while (indexes[indexesRank] == arrayRank) {
                if (indexesRank == 0) {
                    indexes = null;
                    return true;
                }
                indexesRank--;
                arrayRank--;
            }

            int restartIndex = indexes[indexesRank] + 1;
            while (indexesRank < count)
                indexes[indexesRank++] = restartIndex++;

            return true;
        }
    }

	
	
public static void testcall1(Connection connection, String tableName, String column1, String column2) throws SQLException {
    String sql = "INSERT INTO T_edges (sname, node1, node2, corr) VALUES (?, ?, ?, ?)";
    CallableStatement statement = connection.prepareCall(sql);
        statement.setString(1, tableName);
        statement.setString(2, column1);
        statement.setString(3, column2);
        statement.setInt(4, 0);
        statement.executeUpdate();
    }




public static void testcall2(Connection connection, String tableName, String column1, String column2,String column3) throws SQLException {
String sql = "INSERT INTO T_edges_2 (sname, node1, node2, node3,corr_part) VALUES (?, ?, ?, ?,?)";
CallableStatement statement = connection.prepareCall(sql);
    statement.setString(1, tableName);
    statement.setString(2, column1);
    statement.setString(3, column2);
    statement.setString(4, column3);
    statement.setInt(5, 0);
    statement.executeUpdate();
}

public static void testcall3(Connection connection, String tableName, String column1, String column2,String column3) throws SQLException {
String sql = "INSERT INTO T_edges_ci1 (sname, node1, node2, node3,corr12,corr13,corr23) VALUES (?, ?, ?, ?,?,?,?)";
CallableStatement statement = connection.prepareCall(sql);
    statement.setString(1, tableName);
    statement.setString(2, column1);
    statement.setString(3, column2);
    statement.setString(4, column3);
    statement.setInt(5, 0);
    statement.setInt(6, 0);
    statement.setInt(7, 0);
    statement.executeUpdate();
}

 private static void alterTableAddNumFields(Connection connection) throws SQLException {
        // Iterate over the columns in the temp_data table
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet columns = metadata.getColumns(null, null, "t_s_structure_new22", null);
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String dataType = columns.getString("TYPE_NAME");

            // Check if the data type is not a number
            if (!dataType.equals("INTEGER")) {
                // Create a dynamic SQL statement to add the _num field
                String alterStatement = "ALTER TABLE t_s_structure_new22 ADD " + columnName + "_num INTEGER";
                System.out.println(alterStatement);
                Statement statement = connection.createStatement();
                statement.executeUpdate(alterStatement);
                statement.close();
            }
        }
        columns.close();
    }


}	
	



