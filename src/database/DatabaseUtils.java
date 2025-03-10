package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cette classe contient des méthodes qui manipulent des structures globales de la base de données, telles que des tables,
 * des colonnes au niveau global, ou des procédures stockées. Elle est centrée sur la gestion des tables, des requêtes SQL
 * générales, ou des opérations globales sur la base de données.
 * Cette classe regroupe des opérations qui agissent au niveau de la structure de la base de données dans son ensemble.
 */
public class DatabaseUtils {
    //NB : penser à sécuriser les méthodes qui ont accès/modifient la bdd

    /**
     * Modifie la table spécifiée pour ajouter des colonnes numériques (_num)
     * basées sur les colonnes alphanumériques existantes et les remplir avec hashtext().
     *
     * @param connection Connexion à la base de données
     * @param nomtable Nom de la table à modifier
     * @throws SQLException Si une erreur SQL survient
     */
    public static void alterTableAddNumFields(Connection connection, String nomtable) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Vérifier et ajouter les colonnes _num si nécessaire
            ColumUtils.addNumColumnsIfNeeded(connection, nomtable);

            // Mettre à jour les valeurs hashées
            updateHashedValues(statement, nomtable);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }


    //NB possibilité de changement du script car vulnérabilité aux injections sql
    private static void updateHashedValues(Statement statement, String nomtable) throws SQLException {
        String hashUpdateScript = "DO $$ \n" +
                "DECLARE \n" +
                "    column_record RECORD;\n" +
                "BEGIN\n" +
                "    FOR column_record IN \n" +
                "        SELECT column_name \n" +
                "        FROM information_schema.columns \n" +
                "        WHERE table_name = '" + nomtable + "'\n" +
                "        AND column_name NOT LIKE '%_num'\n" +
                "        AND data_type IN ('text', 'character varying')\n" + // Ajout de VARCHAR
                "    LOOP\n" +
                "        EXECUTE 'UPDATE " + nomtable + " SET ' || quote_ident(column_record.column_name) || '_num = hashtext(' || quote_ident(column_record.column_name) || '::TEXT);';\n" +
                "    END LOOP;\n" +
                "END $$;";

        // Exécuter la mise à jour des valeurs hashées
        statement.execute(hashUpdateScript);
        System.out.println("Mise à jour des valeurs hashées terminée.");
    }



    /**
     * La méthode testcall1 fait deux choses :
     * Insertion des 2 variables dans la table T_edges.
     * Appel de la procédure stockée p_corr_postgres pour calculer les corrélations.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table source
     * @param column1    Nom de la première colonne
     * @param column2    Nom de la deuxième colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void testcall1(Connection connection, String tableName, String column1, String column2) throws SQLException {
        String sql = "INSERT INTO t_edges (sname, node1, node2, corr, correlation_exists) " +
                "SELECT ?, ?, ?, CORR(" + column1 + ", " + column2 + "), " +
                "CASE WHEN CORR(" + column1 + ", " + column2 + ") <> 0 THEN TRUE ELSE FALSE END " +
                "FROM " + tableName;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, column1);
            statement.setString(3, column2);
            statement.executeUpdate();
        }
    }




    /**
     * Insère des données dans la table T_edges_ci1.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table source
     * @param column1    Nom de la première colonne
     * @param column2    Nom de la deuxième colonne
     * @param column3    Nom de la troisième colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void testcall3(Connection connection, String tableName, String column1, String column2,
                                 String column3) throws SQLException {

        String sql = "INSERT INTO T_edges_ci1 (sname, node1, node2, node3,corr12,corr13,corr23) VALUES (?, ?, ?, ?,?,?,?)";
        CallableStatement statement = connection.prepareCall(sql);
        statement.setString(1, tableName);
        statement.setString(2, column1);
        statement.setString(3, column2);
        statement.setString(4, column3);
        statement.setInt(5, calculatePearson3variables(connection,tableName,column1,column2,column3));
        statement.setInt(6, calculatePearson3variables(connection,tableName,column1,column3,column2));
        statement.setInt(7, calculatePearson3variables(connection,tableName,column2,column3,column1));
        statement.executeUpdate();
    }


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
    private static int calculatePearson3variables(Connection connection,String table, String column1, String column2, String column3) throws SQLException {
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






















//    /**
//     * Insère des données dans la table T_edges_2.
//     *
//     * @param connection Connexion à la base de données
//     * @param tableName  Nom de la table source
//     * @param column1    Nom de la première colonne
//     * @param column2    Nom de la deuxième colonne
//     * @param column3    Nom de la troisième colonne
//     * @throws SQLException Si une erreur SQL survient
//     */
//    public static void testcall2(Connection connection, String tableName, String column1, String column2, String column3) throws SQLException {
//        // Récupérer les corrélations simples de t_edges
//        Double r_yi_xj = getCorrelation(connection, tableName, column1, column2);
//        Double r_yi_xk = getCorrelation(connection, tableName, column1, column3);
//        Double r_xj_xk = getCorrelation(connection, tableName, column2, column3);
//
//        Double corrPartielle = null;
//
//        // Vérifier que les valeurs ne sont pas null pour éviter une erreur de calcul
//        if (r_yi_xj != null && r_yi_xk != null && r_xj_xk != null) {
//            // Appliquer la formule de la corrélation partielle
//            double denominator = Math.sqrt((1 - r_yi_xk * r_yi_xk) * (1 - r_xj_xk * r_xj_xk));
//            if (denominator != 0) { // Vérifier qu'on ne divise pas par zéro
//                corrPartielle = (r_yi_xj - r_yi_xk * r_xj_xk) / denominator;
//            } else {
//                corrPartielle = 0.0; // Éviter NaN en cas de division par zéro
//            }
//        } else {
//            corrPartielle = 0.0; // Si une des corrélations est manquante, on met 0
//        }
//
//        // Insérer la corrélation partielle dans t_edges_2
//        String sql = "INSERT INTO t_edges_2 (sname, node1, node2, node3, corr_part) VALUES (?, ?, ?, ?, ?)";
//        try (PreparedStatement statement = connection.prepareStatement(sql)) {
//            statement.setString(1, tableName);
//            statement.setString(2, column1);
//            statement.setString(3, column2);
//            statement.setString(4, column3);
//            statement.setDouble(5, corrPartielle); // Insérer la vraie valeur calculée
//            statement.executeUpdate();
//        }
//    }

//    private static Double getCorrelation(Connection connection, String tableName, String node1, String node2) throws SQLException {
//        String query = "SELECT corr FROM t_edges WHERE node1 = ? AND node2 = ?";
//
//        try (PreparedStatement statement = connection.prepareStatement(query)) {
//            statement.setString(1, node1);
//            statement.setString(2, node2);
//            try (ResultSet resultSet = statement.executeQuery()) {
//                if (resultSet.next()) {
//                    return resultSet.getDouble("corr");
//                }
//            }
//        }
//        return null; // Si la corrélation n'est pas trouvée, on retourne null
//    }




//    //remplace testcall3
//    /**
//     * Insère des données dans la table T_edges_ci1 avec un quadruplet de nœuds.
//     *
//     * @param connection Connexion à la base de données
//     * @param tableName  Nom de la table source
//     * @param column1    Nom du premier nœud
//     * @param column2    Nom du deuxième nœud
//     * @param column3    Nom du troisième nœud
//     * @param column4    Nom du quatrième nœud
//     * @throws SQLException Si une erreur SQL survient
//     */
//    public static void testcall4(Connection connection, String tableName, String column1, String column2, String column3, String column4) throws SQLException {
//        // Calculer les corrélations
//        double corr12 = calculateCorrelation(connection, tableName, column1, column2);
//        double corr13 = calculateCorrelation(connection, tableName, column1, column3);
//        double corr14 = calculateCorrelation(connection, tableName, column1, column4);
//        double corr23 = calculateCorrelation(connection, tableName, column2, column3);
//        double corr24 = calculateCorrelation(connection, tableName, column2, column4);
//        double corr34 = calculateCorrelation(connection, tableName, column3, column4);
//
//        // Insérer les résultats dans la table T_edges_ci1
//        insertCorrelations(connection, tableName, column1, column2, column3, column4, corr12, corr13, corr14, corr23, corr24, corr34);
//    }

//    private static double calculateCorrelation(Connection connection, String tableName, String column1, String column2) throws SQLException {
//        String sqlCall = "CALL p_corr_postgres(?, ?, ?)";
//        try (CallableStatement callStatement = connection.prepareCall(sqlCall)) {
//            callStatement.setString(1, tableName);
//            callStatement.setString(2, column1);
//            callStatement.setString(3, column2);
//            callStatement.execute();
//
//            // Supposons que la procédure retourne la corrélation dans un ResultSet
//            try (ResultSet resultSet = callStatement.getResultSet()) {
//                if (resultSet.next()) {
//                    return resultSet.getDouble(1); // Récupérer la valeur de corrélation
//                }
//            }
//        }
//        return 0.0; // Valeur par défaut si la corrélation ne peut pas être calculée
//    }

//    private static void insertCorrelations(Connection connection, String tableName, String column1, String column2, String column3, String column4, double corr12, double corr13, double corr14, double corr23, double corr24, double corr34) throws SQLException {
//        String sql = "INSERT INTO T_edges_ci1 (sname, node1, node2, node3, node4, corr12, corr13, corr14, corr23, corr24, corr34) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        try (CallableStatement statement = connection.prepareCall(sql)) {
//            statement.setString(1, tableName);
//            statement.setString(2, column1);
//            statement.setString(3, column2);
//            statement.setString(4, column3);
//            statement.setString(5, column4);
//            statement.setDouble(6, corr12);
//            statement.setDouble(7, corr13);
//            statement.setDouble(8, corr14);
//            statement.setDouble(9, corr23);
//            statement.setDouble(10, corr24);
//            statement.setDouble(11, corr34);
//            statement.executeUpdate();
//        }
//    }

}
