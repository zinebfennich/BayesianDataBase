package database;

import utils.PCAlgorithmUtils;

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
     * Insère des données dans la table T_edges_2 en utilisant Khi² ou la corrélation partielle selon les types de variables.
     *
     * @param connection Connexion à la base de données
     * @param tableName  Nom de la table source
     * @param column1    Nom de la première colonne
     * @param column2    Nom de la deuxième colonne
     * @param column3    Nom de la troisième colonne
     * @throws SQLException Si une erreur SQL survient
     */
    public static void testcall2(Connection connection, String tableName, String column1, String column2, String column3) throws SQLException {
        String sql = "INSERT INTO T_edges_2 (sname, node1, node2, node3, corr_part) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, column1);
            statement.setString(3, column2);
            statement.setString(4, column3);

            double corrValue = 0; // Valeur par défaut

            // Vérifier si les colonnes sont hachées ou numériques
            boolean isHashed1 = !ColumUtils.columnIsNumeric(connection, tableName, column1);
            boolean isHashed2 = !ColumUtils.columnIsNumeric(connection, tableName, column2);
            boolean isHashed3 = !ColumUtils.columnIsNumeric(connection, tableName, column3);

            boolean isNumeric1 = ColumUtils.columnIsNumeric(connection, tableName, column1);
            boolean isNumeric2 = ColumUtils.columnIsNumeric(connection, tableName, column2);
            boolean isNumeric3 = ColumUtils.columnIsNumeric(connection, tableName, column3);

            // 🔹 Cas 1 : Si au moins une variable est hachée, utiliser Corrélation Partielle (CorrPart)
            if (isHashed1 || isHashed2 || isHashed3) {
                corrValue = PCAlgorithmUtils.calculatePearson3variables(connection, tableName, column1, column2, column3);
            }
            // 🔹 Cas 2 : Si toutes les variables sont numériques, utiliser Khi² après binning
            else if (isNumeric1 && isNumeric2 && isNumeric3) {
                // Discrétisation des variables en plages de valeurs (binning)
                String binnedColumn1 = ColumUtils.createBinnedColumn(connection, tableName, column1);
                String binnedColumn2 = ColumUtils.createBinnedColumn(connection, tableName, column2);
                String binnedColumn3 = ColumUtils.createBinnedColumn(connection, tableName, column3);

                // Exécuter le test du Khi² sur les colonnes discrétisées
                corrValue = PCAlgorithmUtils.performChiSquaredTestForThreeVariables(connection, tableName, binnedColumn1, binnedColumn2, binnedColumn3);
            }

            // Insérer la valeur de corrélation calculée
            statement.setDouble(5, corrValue);
            statement.executeUpdate();

            // Mettre à jour le flag si `corr_part < 30`
            updateFlagTEdges2(connection, tableName, column1, column2, column3, corrValue);
        }
    }

    /**
     * Met à jour le flag `correlation_exists` dans `T_edges_2` si `corr_part < 30`.
     */
    private static void updateFlagTEdges2(Connection connection, String tableName, String col1, String col2, String col3, double corrValue) throws SQLException {
        String sql = "UPDATE T_edges_2 SET correlation_exists = ? WHERE sname = ? AND node1 = ? AND node2 = ? AND node3 = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, corrValue < 30); // True si inférieur à 30%
            statement.setString(2, tableName);
            statement.setString(3, col1);
            statement.setString(4, col2);
            statement.setString(5, col3);
            statement.executeUpdate();
        }
    }




    public static void testcall3(Connection connection, String tableName, String column1, String column2,
                                 String column3) throws SQLException {

        String sql = "INSERT INTO T_edges_ci1 (sname, node1, node2, node3,corr12,corr13,corr23) VALUES (?, ?, ?, ?,?,?,?)";
        CallableStatement statement = connection.prepareCall(sql);
        statement.setString(1, tableName);
        statement.setString(2, column1);
        statement.setString(3, column2);
        statement.setString(4, column3);
        if (!ColumUtils.columnIsNumeric(connection,tableName,column1) || !ColumUtils.columnIsNumeric(connection,tableName,column2) || !ColumUtils.columnIsNumeric(connection,tableName,column3)) {
            statement.setInt(5, PCAlgorithmUtils.calculatePearson3variables(connection,tableName,column1,column2,column3));
            statement.setInt(6, PCAlgorithmUtils.calculatePearson3variables(connection,tableName,column1,column3,column2));
            statement.setInt(7, PCAlgorithmUtils.calculatePearson3variables(connection,tableName,column2,column3,column1));
        }else{
            float chiSquaredValue = PCAlgorithmUtils.performChiSquaredTestForThreeVariables(connection, "my_table", "col1", "col2", "col3");

            if (chiSquaredValue < 5.991) {
                System.out.println("Les variables sont indépendantes sous col3 (Khi² = " + chiSquaredValue + ")");
            } else {
                System.out.println("Les variables sont corrélées sous col3 (Khi² = " + chiSquaredValue + ")");
            }

        }


        //si discrètes khi 2
//        // **Ajoute la condition ici** pour ignorer `id`
//        if (!colonne1.equals("id") && !colonne2.equals("id") && !colonne3.equals("id")) {
//            boolean isIndependent = false;
//            try {
//                isIndependent = PCAlgorithmUtils.performChiSquaredTestForThreeVariables(
//                        connection, tableName, colonne1, colonne2, colonne3);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

            statement.executeUpdate();
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
