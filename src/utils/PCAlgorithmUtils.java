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

    public static boolean performChiSquaredTestForTwoVariables(Connection connection, String tableName, String col1, String col2) throws SQLException {
        // 1️⃣ Créer la requête pour compter les occurrences de chaque combinaison de valeurs
        String query = "SELECT " + col1 + ", " + col2 + ", COUNT(*) as count FROM " + tableName + " GROUP BY " + col1 + ", " + col2;

        // 2️⃣ Stocker les valeurs observées
        Map<String, Integer> frequencyTable = new HashMap<>();
        int totalSamples = 0;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String key = resultSet.getString(col1) + "-" + resultSet.getString(col2);
                int count = resultSet.getInt("count");
                frequencyTable.put(key, count);
                totalSamples += count;
            }
        }

        // 3️⃣ Calculer les fréquences attendues sous l'hypothèse d'indépendance
        double chiSquare = 0;
        double expectedFrequency = (double) totalSamples / frequencyTable.size();

        for (int observed : frequencyTable.values()) {
            chiSquare += Math.pow(observed - expectedFrequency, 2) / expectedFrequency;
        }

        // 4️⃣ Seuil de significativité (p-value = 0.05)
        double chiSquareThreshold = 3.841; // df = 1, alpha = 0.05

        return chiSquare < chiSquareThreshold; // TRUE = indépendant, FALSE = corrélé
    }
    public static boolean performChiSquaredTestForThreeVariables(Connection connection, String tableName, String col1, String col2, String col3) throws SQLException {
        String query = "SELECT " + col1 + ", " + col2 + ", " + col3 + ", COUNT(*) as count FROM " + tableName + " GROUP BY " + col1 + ", " + col2 + ", " + col3;

        Map<String, Integer> frequencyTable = new HashMap<>();
        Map<String, Integer> conditionalTotals = new HashMap<>();
        int totalSamples = 0;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String key = resultSet.getString(col1) + "-" + resultSet.getString(col2) + "-" + resultSet.getString(col3);
                int count = resultSet.getInt("count");
                frequencyTable.put(key, count);
                totalSamples += count;

                // Stocker les totaux conditionnels sur col3
                String conditionalKey = resultSet.getString(col3);
                conditionalTotals.put(conditionalKey, conditionalTotals.getOrDefault(conditionalKey, 0) + count);
            }
        }

        // Calcul du Chi-carré conditionnel
        double chiSquare = 0;

        for (Map.Entry<String, Integer> entry : frequencyTable.entrySet()) {
            String key = entry.getKey();
            int observed = entry.getValue();

            String conditionalKey = key.split("-")[2]; // Extraire la valeur de col3
            double expectedFrequency = (double) conditionalTotals.get(conditionalKey) / frequencyTable.size();

            chiSquare += Math.pow(observed - expectedFrequency, 2) / expectedFrequency;
        }

        // Seuil de significativité pour df = 2 (3-1)
        double chiSquareThreshold = 5.991; // df = 2, alpha = 0.05

        return chiSquare < chiSquareThreshold; // TRUE = indépendant sous col3, FALSE = dépendant
    }

}
