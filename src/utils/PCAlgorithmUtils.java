package utils;

import java.sql.*;
import java.util.*;

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
            Integer totalForConditionalKey = conditionalTotals.getOrDefault(conditionalKey, 0); // 🔥 FIX ici
            if (totalForConditionalKey == 0) continue; // Évite la division par 0

            double expectedFrequency = (double) totalForConditionalKey / frequencyTable.size();
            // peut etre celle la est plus correcte double expectedFrequency = (double) totalForConditionalKey / totalSamples;
            chiSquare += Math.pow(observed - expectedFrequency, 2) / expectedFrequency;
        }

        // Seuil de significativité pour df = 2 (3-1)
        double chiSquareThreshold = 5.991; // df = 2, alpha = 0.05

        return chiSquare < chiSquareThreshold; // TRUE = indépendant sous col3, FALSE = dépendant


    }

    public static boolean performChiSquaredTestForFourVariables(Connection connection, String tableName, String col1, String col2, String col3, String col4) throws SQLException {
        // Requête SQL pour obtenir les données groupées par les quatre colonnes
        String query = "SELECT " + col1 + ", " + col2 + ", " + col3 + ", " + col4 + ", COUNT(*) as count FROM " + tableName + " GROUP BY " + col1 + ", " + col2 + ", " + col3 + ", " + col4;

        Map<String, Integer> frequencyTable = new HashMap<>();
        Map<String, Integer> conditionalTotals = new HashMap<>();
        int totalSamples = 0;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String key = resultSet.getString(col1) + "-" + resultSet.getString(col2) + "-" + resultSet.getString(col3) + "-" + resultSet.getString(col4);
                int count = resultSet.getInt("count");
                frequencyTable.put(key, count);
                totalSamples += count;

                // Stocker les totaux conditionnels sur col4
                String conditionalKey = resultSet.getString(col4);
                conditionalTotals.put(conditionalKey, conditionalTotals.getOrDefault(conditionalKey, 0) + count);
            }
        }

        // Calcul du Chi-carré conditionnel pour 4 variables
        double chiSquare = 0;

        for (Map.Entry<String, Integer> entry : frequencyTable.entrySet()) {
            String key = entry.getKey();
            int observed = entry.getValue();

            String conditionalKey = key.split("-")[3]; // Extraire la valeur de col4
            Integer totalForConditionalKey = conditionalTotals.getOrDefault(conditionalKey, 0); // 🔥 FIX ici
            if (totalForConditionalKey == 0) continue; // Évite la division par 0

            double expectedFrequency = (double) totalForConditionalKey / frequencyTable.size();
            // Alternative plus correcte pour expectedFrequency : (double) totalForConditionalKey / totalSamples;
            chiSquare += Math.pow(observed - expectedFrequency, 2) / expectedFrequency;
        }

        // Seuil de significativité pour df = 3 (4-1)
        double chiSquareThreshold = 7.815; // df = 3, alpha = 0.05

        return chiSquare < chiSquareThreshold; // TRUE = indépendant sous col4, FALSE = dépendant
    }



    //Pearson
    /**
     * Fonction qui calcule la corrélation partielle avec la formule de Pearson pour les variables continues.
     * @param connection
     * @param table
     * @param column1
     * @param column2
     * @param column3
     * @return
     * @throws SQLException
     */
    public static int calculatePearson3variables(Connection connection, String table, String column1, String column2, String column3) throws SQLException {
        // Étape 1: Récupérer les données des trois colonnes
        double[][] values = fetchDataFromDatabase(connection, column1, column2, column3, table);
        double[] x = values[0];
        double[] y = values[1];
        double[] z = values[2];

        // Étape 2: Calculer les corrélations entre les paires de variables
        double r_xy = pearsonCorrelation(x, y); // Corrélation entre column1 et column2
        double r_xz = pearsonCorrelation(x, z); // Corrélation entre column1 et column3
        double r_yz = pearsonCorrelation(y, z); // Corrélation entre column2 et column3

        // Étape 3: Calculer la corrélation partielle
        return calculatePartialCorrelation(r_xy, r_xz, r_yz);
    }

    /**
     * Méthode pour calculer la corrélation de Pearson entre deux tableaux de valeurs.
     * @param x
     * @param y
     * @return
     */
    private static double pearsonCorrelation(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = (n * sumXY) - (sumX * sumY);
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return numerator / denominator;
    }

    // Méthode pour calculer la corrélation partielle
    private static int calculatePartialCorrelation(double r_xy, double r_xz, double r_yz) {
        double corrPartielle = (r_xy - r_xz * r_yz) / Math.sqrt((1 - r_xz * r_xz) * (1 - r_yz * r_yz));
        return (int)(corrPartielle * 1000); // Multiplier par 1000 pour éviter la perte de précision
    }

    /**
     * Méthode pour récupérer les données des colonnes entrées en paramètres depuis une table de la base de données.
     * @param connection
     * @param column1
     * @param column2
     * @param column3
     * @param table_name
     * @return
     * @throws SQLException
     */
    private static double[][] fetchDataFromDatabase(Connection connection, String column1, String column2, String column3, String table_name) throws SQLException {
        // Liste des tables autorisées protection contre les injections SQL
        List<String> allowedTables = Arrays.asList("aka_name", "title_akas", "title_basics","title_crew","title_episode",
                "title_principals","title_ratings");

        // Vérification du nom de la table
        if (!allowedTables.contains(table_name)) {
            throw new IllegalArgumentException("Ce nom de table n'appartient pas aux tables IMDB");
        }

        // Construire dynamiquement la requête SQL
        String query = "SELECT " + column1 + ", " + column2 + ", " + column3 + " FROM " + table_name;

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        List<Double> values1 = new ArrayList<>();
        List<Double> values2 = new ArrayList<>();
        List<Double> values3 = new ArrayList<>();

        while (rs.next()) {
            values1.add(rs.getDouble(column1));
            values2.add(rs.getDouble(column2));
            values3.add(rs.getDouble(column3));
        }

        // Convertir les listes en arrays pour faciliter les calculs
        double[] x = values1.stream().mapToDouble(d -> d).toArray();
        double[] y = values2.stream().mapToDouble(d -> d).toArray();
        double[] z = values3.stream().mapToDouble(d -> d).toArray();

        return new double[][] {x, y, z};
    }
}