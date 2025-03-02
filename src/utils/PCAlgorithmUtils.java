package utils;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PCAlgorithmUtils {

    // Méthode pour effectuer le test du Chi-carré pour 2 variables
    public static boolean performChiSquaredTestForTwoVariables(Connection connection, String tableName, String var1, String var2) throws SQLException {
        String query = "SELECT " + var1 + ", " + var2 + " FROM " + tableName;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // Construire le tableau de contingence
            Map<String, Map<String, Integer>> contingencyTable = new HashMap<>();

            while (resultSet.next()) {
                String value1 = resultSet.getString(var1);
                String value2 = resultSet.getString(var2);

                contingencyTable.putIfAbsent(value1, new HashMap<>());
                Map<String, Integer> subTable = contingencyTable.get(value1);
                subTable.put(value2, subTable.getOrDefault(value2, 0) + 1);
            }

            // Convertir le tableau de contingence en tableau 2D
            int rows = contingencyTable.size();
            int cols = contingencyTable.values().stream().mapToInt(Map::size).max().orElse(0);
            long[][] table = new long[rows][cols];

            int rowIndex = 0;
            for (Map<String, Integer> subTable : contingencyTable.values()) {
                int colIndex = 0;
                for (Map.Entry<String, Integer> entry : subTable.entrySet()) {
                    table[rowIndex][colIndex++] = entry.getValue();
                }
                rowIndex++;
            }

            // Effectuer le test du Chi-carré
            ChiSquareTest chiSquareTest = new ChiSquareTest();
            double chiSquare = chiSquareTest.chiSquare(table);
            int degreesOfFreedom = (rows - 1) * (cols - 1);
            double pValue = 1.0 - new ChiSquaredDistribution(degreesOfFreedom).cumulativeProbability(chiSquare);

            // Retourner vrai si les variables sont indépendantes (p-value > seuil)
            return pValue > 0.05;
        }
    }

    // Méthode pour effectuer le test du Chi-carré pour 3 variables
    public static boolean performChiSquaredTestForThreeVariables(Connection connection, String tableName, String var1, String var2, String var3) throws SQLException {
        String query = "SELECT " + var1 + ", " + var2 + ", " + var3 + " FROM " + tableName;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // Construire le tableau de contingence
            Map<String, Map<String, Map<String, Integer>>> contingencyTable = new HashMap<>();

            while (resultSet.next()) {
                String value1 = resultSet.getString(var1);
                String value2 = resultSet.getString(var2);
                String value3 = resultSet.getString(var3);

                contingencyTable.putIfAbsent(value1, new HashMap<>());
                Map<String, Map<String, Integer>> subTable1 = contingencyTable.get(value1);
                subTable1.putIfAbsent(value2, new HashMap<>());
                Map<String, Integer> subTable2 = subTable1.get(value2);
                subTable2.put(value3, subTable2.getOrDefault(value3, 0) + 1);
            }

            // Convertir le tableau de contingence en tableau 3D
            int dim1 = contingencyTable.size();
            int dim2 = contingencyTable.values().stream().mapToInt(Map::size).max().orElse(0);
            int dim3 = contingencyTable.values().stream().flatMap(map -> map.values().stream()).mapToInt(Map::size).max().orElse(0);
            long[][][] table = new long[dim1][dim2][dim3];

            int index1 = 0;
            for (Map<String, Map<String, Integer>> subTable1 : contingencyTable.values()) {
                int index2 = 0;
                for (Map<String, Integer> subTable2 : subTable1.values()) {
                    int index3 = 0;
                    for (Map.Entry<String, Integer> entry : subTable2.entrySet()) {
                        table[index1][index2][index3++] = entry.getValue();
                    }
                    index2++;
                }
                index1++;
            }

            // Effectuer le test du Chi-carré
            ChiSquareTest chiSquareTest = new ChiSquareTest();
            double chiSquare = 0.0;
            int degreesOfFreedom = 0;

            for (int i = 0; i < dim1; i++) {
                for (int j = 0; j < dim2; j++) {
                    long[][] subTable = new long[dim3][2];
                    for (int k = 0; k < dim3; k++) {
                        subTable[k][0] = table[i][j][k];
                        subTable[k][1] = table[i][j][k];
                    }
                    chiSquare += chiSquareTest.chiSquare(subTable);
                    degreesOfFreedom += (subTable.length - 1) * (subTable[0].length - 1);
                }
            }

            double pValue = 1.0 - new ChiSquaredDistribution(degreesOfFreedom).cumulativeProbability(chiSquare);

            // Retourner vrai si les variables sont indépendantes (p-value > seuil)
            return pValue > 0.05;
        }
    }
}
