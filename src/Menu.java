/**
 * Créé par:
 * Jérémie Leroux
 * Joaquin Lee-Martinez
 */

import java.sql.*;
import oracle.jdbc.*;

public class Menu extends javax.swing.JFrame {
    //region Variables
    public static Connection conn = null;
    public String user = "leemarti";
    public String psw = "leemarti";
    public static String bd = "jdbc:oracle:thin:@mercure.clg.qc.ca:1521:orcl";
    private String Histoire;
    private javax.swing.ImageIcon Photo;
    //endregion

    //region Constructeur
    public Menu() {
        initComponents();
        try {
            oracle.jdbc.pool.OracleDataSource oracleDataSource = new oracle.jdbc.pool.OracleDataSource();
            oracleDataSource.setURL(bd);
            oracleDataSource.setUser(user);
            oracleDataSource.setPassword(psw);
            conn = oracleDataSource.getConnection();

            initListes();
            initReservationListe();
            initListeClients();
        }
        catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    //endregion

    //region Boutons Menu
    private void jButtonClientsActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(1);
    }

    private void jButtonCircuitsActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(2);
    }

    private void jButtonRéservationsActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(3);
    }

    private void jButtonMonumentsActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(4);
    }

    private void jButtonListesActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(5);
    }

    private void jButtonRechercheActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(6);
    }
    //endregion

    //region Ajouter Client
    private CallableStatement stmAjouterClient = null;

    private void jButtonAjouterClientActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jTextFieldNomClient.getText().trim().length() == 0) throw new Exception("Le nom du client ne doit pas être vide.");
            stmAjouterClient = conn.prepareCall("{call TP3.Insertion_Client(?,?)}");
            stmAjouterClient.setString(1, jTextFieldNomClient.getText());
            stmAjouterClient.setString(2, jTextFieldPrenomClient.getText());

            stmAjouterClient.executeUpdate();
            stmAjouterClient.clearParameters();
            stmAjouterClient.close();
            ClearTextBox_AjoutClient();
            initListeClients();
        } catch (SQLException e) {
            if(e instanceof SQLIntegrityConstraintViolationException)
                javax.swing.JOptionPane.showMessageDialog(null, "Ce client existe déjà.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            else e.printStackTrace();
        } catch (Exception e){
            javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        //ACTUALISER LISTE CLIENT
    }

    private void ClearTextBox_AjoutClient(){
        jTextFieldNomClient.setText("");
        jTextFieldPrenomClient.setText("");
    }
    //endregion

    //region Supprimer Client
    private CallableStatement stmSupprimerClient = null;

    private PreparedStatement stmListerClients = null;
    private ResultSet rstListerClients = null;
    private String sqlListerClients = "SELECT * FROM CLIENTS";

    private void initListeClients() throws SQLException {
        stmListerClients = conn.prepareCall(sqlListerClients, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rstListerClients = stmListerClients.executeQuery();
        rstListerClients.beforeFirst();
        if(rstListerClients.next()) loadClientsData();
        else{
            jLabelNumeroClient.setText("Aucune donnée");
            jLabelNomClient.setText("Aucune donnée");
            jLabelPrenomClient.setText("Aucune donnée");
        }
        stmListerClients.clearParameters();
    }

    private void loadClientsData(){
        try {
            jLabelNumeroClient.setText(rstListerClients.getString("NUMERO"));
            jLabelNomClient.setText(rstListerClients.getString("NOM"));
            jLabelPrenomClient.setText(rstListerClients.getString("PRENOM"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonSupprimerClientActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            stmSupprimerClient = conn.prepareCall("{call TP3.Supprimer_Client(?)}");
            stmSupprimerClient.setInt(1, Integer.parseInt(jLabelNumeroClient.getText()));
            stmSupprimerClient.executeUpdate();
            stmSupprimerClient.clearParameters();
            stmSupprimerClient.close();

            initListeClients();
        } catch (SQLException e) {
            if(e instanceof SQLIntegrityConstraintViolationException)
                javax.swing.JOptionPane.showMessageDialog(null, "Le client ne peut être supprimé car il a des réservations actives.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void jButtonPrecedentClientActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(rstListerClients.previous()) loadClientsData();
            else rstListerClients.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonSuivantClientActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(rstListerClients.next()) loadClientsData();
            else rstListerClients.last();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region Ajouter Circuit
    private CallableStatement stmAjouterCircuit = null;

    private void jButtonAjouterCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jTextFieldNomCircuit.getText().trim().length() == 0) throw new Exception("Le nom du circuit ne doit pas être vide.");
            else if(jTextFieldVilleDepart.getText().trim().length() == 0) throw new Exception("La ville de départ ne doit pas être vide.");
            else if(jTextFieldVilleFin.getText().trim().length() == 0) throw new Exception("La ville de fin ne doit pas être vide.");
            else if(jTextFieldFin.getText().trim().length() == 0) throw new Exception("Le prix ne doit pas être vide.");
            else if(jTextFieldDuree.getText().trim().length() == 0) throw new Exception("La durée ne doit pas être vide.");
            else if (jTextFieldMaxClient.getText().trim().length() == 0) throw new Exception("Le nombre maximum de client ne doit pas être vide.");
            try{Double.parseDouble(jTextFieldFin.getText());}
            catch(NumberFormatException  e){
                throw new Exception("Le nombre entré comme prix est invalide.");
            }
            try{Integer.parseInt(jTextFieldDuree.getText());}
            catch(NumberFormatException  e){
                throw new Exception("Le nombre entré comme durée est invalide.");
            }
            try{Integer.parseInt(jTextFieldMaxClient.getText());}
            catch(NumberFormatException  e){
                throw new Exception("Le nombre entré comme nombre maximum de client est invalide.");
            }

            stmAjouterCircuit = conn.prepareCall("{call TP3.Insertion_Circuit(?,?,?,?,?,?)}");
            stmAjouterCircuit.setString(1, jTextFieldNomCircuit.getText());
            stmAjouterCircuit.setString(2, jTextFieldVilleDepart.getText());
            stmAjouterCircuit.setString(3, jTextFieldVilleFin.getText());
            stmAjouterCircuit.setInt(4, Integer.parseInt(jTextFieldDuree.getText()));
            stmAjouterCircuit.setInt(5, Integer.parseInt(jTextFieldMaxClient.getText()));
            stmAjouterCircuit.setDouble(6, Double.parseDouble(jTextFieldFin.getText()));

            stmAjouterCircuit.executeUpdate();
            stmAjouterCircuit.clearParameters();
            stmAjouterCircuit.close();

            ClearTextBox_AjoutCircuit();
            initListeCircuits();
            initListes();
        } catch (SQLException e) {
            if(e instanceof SQLIntegrityConstraintViolationException)
                javax.swing.JOptionPane.showMessageDialog(null, "Le circuit existe déjà.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            else e.printStackTrace();
        } catch(Exception e){
            javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void ClearTextBox_AjoutCircuit() {
        jTextFieldNomCircuit.setText("");
        jTextFieldVilleDepart.setText("");
        jTextFieldVilleFin.setText("");
        jTextFieldDuree.setText("");
        jTextFieldMaxClient.setText("");
        jTextFieldFin.setText("");
    }

    //endregion

    //region Modifier/Supprimer Circuit
    private CallableStatement stmModifCircuit = null;

    private CallableStatement stmSuppCircuit = null;

    private PreparedStatement stmListerCircuits = null;
    private ResultSet rstListerCircuits = null;
    private String sqlListerCircuits = "SELECT * FROM CIRCUITS";

    private void initListeCircuits() throws SQLException{
        stmListerCircuits = conn.prepareCall(sqlListerCircuits, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rstListerCircuits = stmListerCircuits.executeQuery();
        rstListerCircuits.beforeFirst();
        if(rstListerCircuits.next()) loadCircuitData();
        else{
            jTextFieldNomCircuitModif.setText("");
            jTextFieldVilleDepartModif.setText("");
            jTextFieldVilleFinModif.setText("");
            jTextFieldDureeModif.setText("");
            jTextFieldMaxClientModif.setText("");
            jTextFieldPrixModif.setText("");
        }
    }

    private void loadCircuitData(){
        try {
            jTextFieldNomCircuitModif.setText(rstListerCircuits.getString("NOM"));
            jTextFieldVilleDepartModif.setText(rstListerCircuits.getString("VILLEDEBUT"));
            jTextFieldVilleFinModif.setText(rstListerCircuits.getString("VILLEFIN"));
            jTextFieldDureeModif.setText("" + rstListerCircuits.getInt("DUREE"));
            jTextFieldMaxClientModif.setText("" + rstListerCircuits.getInt("CLIENTMAX"));
            jTextFieldPrixModif.setText("" + rstListerCircuits.getDouble("PRIX"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonSupprimerCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            stmSuppCircuit = conn.prepareCall("{call TP3.Supprimer_Circuit(?)}");
            stmSuppCircuit.setString(1, jTextFieldNomCircuitModif.getText());
            stmSuppCircuit.executeUpdate();
            stmSuppCircuit.clearParameters();
            stmSuppCircuit.close();

            initListeCircuits();
            initListes();
        } catch (SQLException e) {
            if(e instanceof SQLIntegrityConstraintViolationException)
                javax.swing.JOptionPane.showMessageDialog(null, "Le circuit a des réservations active et ne peut être supprimé.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            else e.printStackTrace();
        }
    }

    private void jButtonPrecedentCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(rstListerCircuits.previous()) loadCircuitData();
            else rstListerCircuits.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonModifierCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jTextFieldVilleDepartModif.getText().trim().length() == 0) throw new Exception("La ville de départ ne doit pas être vide.");
            else if(jTextFieldVilleFinModif.getText().trim().length() == 0) throw new Exception("La ville de fin ne doit pas être vide.");
            else if(jTextFieldPrixModif.getText().trim().length() == 0) throw new Exception("Le prix ne doit pas être vide.");
            else if(jTextFieldDureeModif.getText().trim().length() == 0) throw new Exception("La durée ne doit pas être vide.");
            else if (jTextFieldMaxClientModif.getText().trim().length() == 0) throw new Exception("Le nombre maximum de client ne doit pas être vide.");
            try{Double.parseDouble(jTextFieldPrixModif.getText());}
            catch(NumberFormatException  e){
                throw new Exception("Le nombre entré comme prix est invalide.");
            }
            try{Integer.parseInt(jTextFieldDureeModif.getText());}
            catch(NumberFormatException  e){
                throw new Exception("Le nombre entré comme durée est invalide.");
            }
            try{Integer.parseInt(jTextFieldMaxClientModif.getText());}
            catch(NumberFormatException  e){
                throw new Exception("Le nombre entré comme nombre maximum de client est invalide.");
            }

            stmModifCircuit = conn.prepareCall("{call TP3.Modifier_Circuit(?,?,?,?,?,?)}");
            stmModifCircuit.setString(1, jTextFieldNomCircuitModif.getText());
            stmModifCircuit.setString(2, jTextFieldVilleDepartModif.getText());
            stmModifCircuit.setString(3, jTextFieldVilleFinModif.getText());
            stmModifCircuit.setInt(4, Integer.parseInt(jTextFieldDureeModif.getText()));
            stmModifCircuit.setInt(5, Integer.parseInt(jTextFieldMaxClientModif.getText()));
            stmModifCircuit.setDouble(6, Double.parseDouble(jTextFieldPrixModif.getText()));

            stmModifCircuit.executeUpdate();
            stmModifCircuit.clearParameters();
            stmModifCircuit.close();

            ClearTextBox_AjoutCircuit();
            initListeCircuits();
        } catch (SQLException e) {
            if(e instanceof SQLIntegrityConstraintViolationException)
                javax.swing.JOptionPane.showMessageDialog(null, "Le circuit existe déjà.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            else e.printStackTrace();
        } catch(Exception e){
            javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jButtonSuivantCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(rstListerCircuits.next()) loadCircuitData();
            else rstListerCircuits.last();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region Ajouter réservation

    private CallableStatement stmAjouterReservation = null;

    private void jButtonAjouter2ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jTextFieldNumClient.getText().trim().length() == 0) throw new Exception("Le nom du client ne doit pas être vide.");
            else if(jTextFieldNomCircuitRes.getText().trim().length() == 0) throw new Exception("Le nom du circuit ne doit pas être vide.");
            else if(jDateChooserVisite.getDate() == null) throw new Exception("La date est invalide.");
            stmAjouterReservation = conn.prepareCall("{call TP3.Insertion_Reservation(?,?,?)}");
            stmAjouterReservation.setString(1, jTextFieldNumClient.getText());
            stmAjouterReservation.setString(2, jTextFieldNomCircuitRes.getText());
            stmAjouterReservation.setDate(3, new java.sql.Date(jDateChooserVisite.getDate().getTime()));
            stmAjouterReservation.executeUpdate();
            stmAjouterReservation.clearParameters();
            stmAjouterReservation.close();

            jTextFieldNumClient.setText("");
            jTextFieldNomCircuitRes.setText("");
            jDateChooserVisite.setDate(new java.util.Date());
            initListes();
            initReservationListe();
        } catch (SQLException e) {
            String message = "";
            if(e instanceof SQLIntegrityConstraintViolationException){
                if(e.getMessage().contains("CLIENTS")) message = "Le client entré n'existe pas.";
                else if (e.getMessage().contains("CIRCUITS")) message = "Le circuit entré n'existe pas.";
                else message = "La réservation existe déjà.";
                javax.swing.JOptionPane.showMessageDialog(null, message, "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            else if(e.getMessage().contains("Le circuit à déjà atteint sa limite de client"))
                javax.swing.JOptionPane.showMessageDialog(null, "Le circuit à déjà atteint sa limite de client", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            else e.printStackTrace();
        } catch(Exception e){
            javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    //endregion

    //region Modification réservations
    private PreparedStatement stmListeReservation = null;
    private ResultSet rstListeReservation = null;
    private String sqlListeReservations = "SELECT * FROM RESERVATION WHERE ETAT='A'";

    private CallableStatement stmModifReservation = null;

    private CallableStatement stmSuppReservation = null;

    private void initReservationListe() throws SQLException {
        stmListeReservation = conn.prepareCall(sqlListeReservations,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rstListeReservation = stmListeReservation.executeQuery();
        rstListeReservation.beforeFirst();
        if(rstListeReservation.next()) loadReservationListe();
        else{
            jTextFieldNumClientModif.setText("");
            jTextFieldVilleDepartModif1.setText("");
            jDateChooserVisiteModif.setDate(null);
            jDateChooserLimiteModif.setDate(null);
            jDateChooserReservModif.setDate(null);
        }
    }

    private void loadReservationListe(){
        try {
            jTextFieldNumClientModif.setText(rstListeReservation.getString("NUMEROCLIENT"));
            jTextFieldVilleDepartModif1.setText(rstListeReservation.getString("NOMCIRCUIT"));
            jDateChooserVisiteModif.setDate(new java.util.Date(rstListeReservation.getDate("DATEVISITE").getTime()));
            jDateChooserLimiteModif.setDate(new java.util.Date(rstListeReservation.getDate("LIMITEANNULER").getTime()));
            jDateChooserReservModif.setDate(new java.util.Date(rstListeReservation.getDate("DATERESERVATION").getTime()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonPrecedentCircuit1ActionPerformed(java.awt.event.ActionEvent evt) {
        try{
            if(rstListeReservation.previous()) loadReservationListe();
            else rstListeReservation.first();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void jButtonSuivantCircuit1ActionPerformed(java.awt.event.ActionEvent evt) {
        try{
            if(rstListeReservation.next()) loadReservationListe();
            else rstListeReservation.last();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void jButtonModifierReservationActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jDateChooserVisiteModif.getDate() == null) throw new Exception("La date de visite est invalide.");
            stmModifReservation = conn.prepareCall("{call TP3.Modifier_Reservation(?,?,?)}");
            stmModifReservation.setString(1, jTextFieldNumClientModif.getText());
            stmModifReservation.setString(2, jTextFieldVilleDepartModif1.getText());
            stmModifReservation.setDate(3, new java.sql.Date(jDateChooserVisiteModif.getDate().getTime()));
            stmModifReservation.executeUpdate();
            stmModifReservation.clearParameters();
            stmModifReservation.close();

            initReservationListe();
            initListes();
        } catch (SQLException e) {
            if(e.getMessage().contains("TRIGGER_DATE_RESERVATION"))
                javax.swing.JOptionPane.showMessageDialog(null,"Impossible de réserver dans le passé.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            else e.printStackTrace();
        } catch (Exception e){
            javax.swing.JOptionPane.showMessageDialog(null,e.getMessage(), "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jButtonSupprimerCircuit1ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            float Prix = 0;
            stmSuppReservation = conn.prepareCall("{call TP3.Supprimer_Reservation(?,?,?)}");
            stmSuppReservation.setString(1, jTextFieldNumClientModif.getText());
            stmSuppReservation.setString(2, jTextFieldVilleDepartModif1.getText());
            if(jCheckBoxAnnuler.isSelected()) stmSuppReservation.setDouble(3, 0.15);
            stmSuppReservation.executeUpdate();
            stmSuppReservation.clearParameters();
            stmSuppReservation.close();

            jCheckBoxAnnuler.setSelected(false);
            initReservationListe();
            initListes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region AjouterMonu
    private CallableStatement stmAjouterMonu = null;

    private void jButtonAjouterMonuActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jTextFieldNomMonument.getText().trim().length() == 0) throw new Exception("Le nom du monument ne doit pas être vide.");
            stmAjouterMonu = conn.prepareCall("{call TP3.Ajouter_Monument(?,?,?)}");
            stmAjouterMonu.setString(1, jTextFieldNomMonument.getText());
            stmAjouterMonu.setString(2, "" + jYearChooserConstruction.getYear());
            stmAjouterMonu.setString(3, jTextAreaHistoireMonument.getText());
            stmAjouterMonu.executeUpdate();
            stmAjouterMonu.clearParameters();
            stmAjouterMonu.close();
            jTextFieldNomMonument.setText("");
            jYearChooserConstruction.setYear(Integer.parseInt(new java.text.SimpleDateFormat("yyyy").format(java.util.Calendar.getInstance().getTime())));
            jTextAreaHistoireMonument.setText("");
        } catch (SQLException e) {
            String col;
            if(e instanceof SQLIntegrityConstraintViolationException)
                javax.swing.JOptionPane.showMessageDialog(null,"Ce monument existe déjà.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            else if(e.getMessage().contains("ORA-12899")) {
                if (e.getMessage().contains("HISTOIRE")) col = "Histoire";
                else col = "Nom";
                javax.swing.JOptionPane.showMessageDialog(null, "Le nombre de caractère de la colonne " + col + " dépasse sa limite.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            } else e.printStackTrace();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,e.getMessage(), "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    //endregion

    //region AjouterMonumentCircuit
    private CallableStatement stmAjouterMonuCircuit = null;

    private void jButtonAjouterMonuCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(jTextFieldNomMonuLIENS.getText().trim().length() == 0) throw new Exception("Le nom du monument ne doit pas être vide.");
            else if(jTextFieldNomCircuitLIENS.getText().trim().length() == 0) throw new Exception("Le nom du circuit ne doit pas être vide.");

            stmAjouterMonuCircuit = conn.prepareCall("{call TP3.Ajouter_Monument_Circuit(?,?)}");
            stmAjouterMonuCircuit.setString(1, jTextFieldNomCircuitLIENS.getText());
            stmAjouterMonuCircuit.setString(2, jTextFieldNomMonuLIENS.getText());
            stmAjouterMonuCircuit.executeUpdate();
            stmAjouterMonuCircuit.clearParameters();
            stmAjouterMonuCircuit.close();

            jTextFieldNomCircuitLIENS.setText("");
            jTextFieldNomMonuLIENS.setText("");
            initListes();
        } catch(SQLException s) {
            String col = "";
            if(s instanceof SQLIntegrityConstraintViolationException){
                if(s.getMessage().contains("MONUMENTS")) col = "monument";
                else if(s.getMessage().contains("CIRCUITS")) col = "circuit";
                if(col.length() > 0)
                    javax.swing.JOptionPane.showMessageDialog(null,"Le nom du " + col + " n'existe pas.", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
                else
                    javax.swing.JOptionPane.showMessageDialog(null,"Le monument fait déjà parti du circuit", "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            else s.printStackTrace();
        }catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,e.getMessage(), "Erreur", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    //endregion

    //region Liste de monument par circuit
    private PreparedStatement stmListeMonumentParCircuit = null;
    private ResultSet rstListeMonumentParCircuit = null;
    private String sqlListeMonumentParCircuit = "SELECT * FROM MONUMENTS M INNER JOIN CIRCUITMONUMENT C ON M.NOM=C.NOMMONUMENT WHERE C.NOMCIRCUIT =?";

    private void sListeMonumentParCircuit() throws SQLException {
        stmListeMonumentParCircuit = conn.prepareCall(sqlListeMonumentParCircuit, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmListeMonumentParCircuit.setString(1, jLabelNomCircuitLISTE.getText());
        rstListeMonumentParCircuit = stmListeMonumentParCircuit.executeQuery();
        rstListeMonumentParCircuit.beforeFirst();
        if(rstListeMonumentParCircuit.next())
            loadMonumentParCircuitData();
        else{
            jLabelNomMonumentListe.setText("Aucune donnée");
            jLabelDateMonuLISTE.setText("Aucune donnée");
            Histoire = "Aucune donnée";
            changerPhoto("");
            if(jButtonPhotoHistoire.getText().equals("Photo")) jLabelHistoireMonuLISTE.setText(Histoire);
            else if (jButtonPhotoHistoire.getText().equals("Histoire")) jLabelHistoireMonuLISTE.setIcon(Photo);
        }
    }

    private void loadMonumentParCircuitData(){
        try {

            jLabelNomMonumentListe.setText(rstListeMonumentParCircuit.getString("NOM"));
            jLabelDateMonuLISTE.setText(rstListeMonumentParCircuit.getString("CONSTRUCTION"));
            Histoire = "<html>" + rstListeMonumentParCircuit.getString("HISTOIRE") + "</html>";
            changerPhoto(rstListeMonumentParCircuit.getString("IMAGE"));
            if(jButtonPhotoHistoire.getText().equals("Photo")) jLabelHistoireMonuLISTE.setText(Histoire);
            else if (jButtonPhotoHistoire.getText().equals("Histoire")) jLabelHistoireMonuLISTE.setIcon(Photo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonPhotoHistoireActionPerformed(java.awt.event.ActionEvent evt) {
        if(jButtonPhotoHistoire.getText().equals("Photo")){
            jLabelHistoireMonuLISTE.setIcon(Photo);
            jLabelHistoireMonuLISTE.setText("");
            jButtonPhotoHistoire.setText("Histoire");
        }
        else if(jButtonPhotoHistoire.getText().equals("Histoire")){
            jLabelHistoireMonuLISTE.setIcon(null);
            jLabelHistoireMonuLISTE.setText(Histoire);
            jButtonPhotoHistoire.setText("Photo");
        }
    }

    private void jButtonPrecedentListeMonuActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(rstListeMonumentParCircuit.previous()) loadMonumentParCircuitData();
            else rstListeMonumentParCircuit.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonSuivantListeMonuActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(rstListeMonumentParCircuit.next()) loadMonumentParCircuitData();
            else rstListeMonumentParCircuit.last();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region Liste Circuits {details}
    private PreparedStatement stmListeCircuits = null;
    private ResultSet rstListeCircuits = null;
    private String sqlListeCircuits = "SELECT NOM FROM CIRCUITS";
    private void sListeCircuit() throws SQLException {
        stmListeCircuits = conn.prepareCall(sqlListeCircuits,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rstListeCircuits = stmListeCircuits.executeQuery();
        rstListeCircuits.first();
        loadCircuitsData();
    }

    private void loadCircuitsData(){
        try {
            jLabelNomCircuitLISTE.setText(rstListeCircuits.getString("NOM"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonPrecedentListeCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
        if(rstListeCircuits.previous()){
            loadCircuitsData();
            loadClientParCircuitData();
            sListeMonumentParCircuit();
        }
        else rstListeCircuits.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void jButtonSuivantListeCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if(rstListeCircuits.next()){
                loadCircuitsData();
                loadClientParCircuitData();
                sListeMonumentParCircuit();
            }
            else rstListeCircuits.last();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region ListeClientParCircuit
    private CallableStatement stmListeClientParCircuit = null;

    private void sListeClientParCircuit() throws SQLException {
        stmListeClientParCircuit = conn.prepareCall("{call TP3.ListerClients(?,?)}");
        loadClientParCircuitData();
    }

    private void loadClientParCircuitData(){
        try {
            stmListeClientParCircuit.setString(1, jLabelNomCircuitLISTE.getText());
            stmListeClientParCircuit.registerOutParameter(2, OracleTypes.CURSOR);
            stmListeClientParCircuit.execute();
            jTableClientsParCircuit.setModel(buildTableModel((ResultSet)stmListeClientParCircuit.getObject(2)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region Chercher circuit
    private CallableStatement stmChercherCircuit = null;

    private void jButtonChercherCircuitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            ChercherCircuit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ChercherCircuit() throws SQLException{
        stmChercherCircuit = conn.prepareCall("{call TP3.ChercherCircuit(?,?)}");
        stmChercherCircuit.setString(1, jButtonChercherCircuit.getText());
        stmChercherCircuit.registerOutParameter(2, OracleTypes.CURSOR);
        stmChercherCircuit.execute();
        jTableClientsParCircuit1.setModel(buildTableModel((ResultSet)stmChercherCircuit.getObject(2)));
        stmChercherCircuit.clearParameters();
        stmChercherCircuit.close();
    }
    //endregion

    //region Fonctions utilitaires
    private void changerPhoto(String url){
        if(url == null) url = "Default.png";
        else if(url.trim().length() ==0) url = "Default.png";
        else if (!new java.io.File(url).exists()) url = "Default.png";
        Photo = new javax.swing.ImageIcon(
                new javax.swing.ImageIcon("Images\\" + url).getImage().getScaledInstance(
                        jLabelHistoireMonuLISTE.getWidth(), jLabelHistoireMonuLISTE.getHeight(), java.awt.Image.SCALE_SMOOTH));
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        try {
            conn.close();
        } catch (SQLException e) {

        }
    }

    public static javax.swing.table.DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        java.util.Vector<String> columnNames = new java.util.Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        java.util.Vector<java.util.Vector<Object>> data = new java.util.Vector<java.util.Vector<Object>>();
        while (rs.next()) {
            java.util.Vector<Object> vector = new java.util.Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new javax.swing.table.DefaultTableModel(data, columnNames);
    }

    private void initListes() throws SQLException{
        //region ListeCircuits
        sListeCircuit();
        //endregion
        //region ListeClientsParCircuit
        sListeClientParCircuit();
        //endregion
        //region ListeMonumentParCircuit
        sListeMonumentParCircuit();
        //endregion
    }

    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        Menu = new JImagePanel("Images\\Background.png");
        jButtonClients = new javax.swing.JButton();
        jButtonCircuits = new javax.swing.JButton();
        jButtonMonuments = new javax.swing.JButton();
        jButtonRéservations = new javax.swing.JButton();
        jButtonListes = new javax.swing.JButton();
        jButtonRecherche = new javax.swing.JButton();
        Clients = new javax.swing.JPanel();
        AjouterClient = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldNomClient = new javax.swing.JTextField();
        jTextFieldPrenomClient = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jButtonAjouterClient = new javax.swing.JButton();
        SupprimerClient = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelNomClient = new javax.swing.JLabel();
        jLabelNumeroClient = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButtonPrecedentClient = new javax.swing.JButton();
        jButtonSupprimerClient = new javax.swing.JButton();
        jButtonSuivantClient = new javax.swing.JButton();
        jLabel38 = new javax.swing.JLabel();
        jLabelPrenomClient = new javax.swing.JLabel();
        Circuits = new javax.swing.JPanel();
        AjouterCircuit = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldNomCircuit = new javax.swing.JTextField();
        jTextFieldVilleDepart = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldVilleFin = new javax.swing.JTextField();
        jTextFieldFin = new javax.swing.JTextField();
        jTextFieldDuree = new javax.swing.JTextField();
        jTextFieldMaxClient = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jButtonAjouterCircuit = new javax.swing.JButton();
        ModifierSupprimerCircuit = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldMaxClientModif = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldDureeModif = new javax.swing.JTextField();
        jTextFieldPrixModif = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jTextFieldVilleFinModif = new javax.swing.JTextField();
        jTextFieldVilleDepartModif = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jTextFieldNomCircuitModif = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jButtonPrecedentCircuit = new javax.swing.JButton();
        jButtonModifierCircuit = new javax.swing.JButton();
        jButtonSuivantCircuit = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jButtonSupprimerCircuit = new javax.swing.JButton();
        Reservations = new javax.swing.JPanel();
        AjouterReservation = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jTextFieldNumClient = new javax.swing.JTextField();
        jTextFieldNomCircuitRes = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jButtonAjouter2 = new javax.swing.JButton();
        jDateChooserVisite = new com.toedter.calendar.JDateChooser();
        ModifierSupprimerReservation = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jTextFieldVilleDepartModif1 = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jTextFieldNumClientModif = new javax.swing.JTextField();
        jCheckBoxAnnuler = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jButtonPrecedentCircuit1 = new javax.swing.JButton();
        jButtonModifierReservation = new javax.swing.JButton();
        jButtonSuivantCircuit1 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jButtonSupprimerCircuit1 = new javax.swing.JButton();
        jDateChooserVisiteModif = new com.toedter.calendar.JDateChooser();
        jDateChooserReservModif = new com.toedter.calendar.JDateChooser();
        jDateChooserLimiteModif = new com.toedter.calendar.JDateChooser();
        Monuments = new javax.swing.JPanel();
        AjouterMonument = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jTextFieldNomMonument = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaHistoireMonument = new javax.swing.JTextArea();
        jPanel10 = new javax.swing.JPanel();
        jButtonAjouterMonu = new javax.swing.JButton();
        jYearChooserConstruction = new com.toedter.calendar.JYearChooser();
        AjouterMonumentTOCircuit = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jTextFieldNomMonuLIENS = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jTextFieldNomCircuitLIENS = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jButtonAjouterMonuCircuit = new javax.swing.JButton();
        Listes = new javax.swing.JPanel();
        ListeClientParCircuit = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableClientsParCircuit = new javax.swing.JTable();
        ListeMonuments = new javax.swing.JPanel();
        jLabelNomMonumentListe = new javax.swing.JLabel();
        jLabelDateMonuLISTE = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jButtonPrecedentListeMonu = new javax.swing.JButton();
        jButtonSuivantListeMonu = new javax.swing.JButton();
        jButtonPhotoHistoire = new javax.swing.JButton();
        jLabelHistoireMonuLISTE = new javax.swing.JLabel();
        jButtonPrecedentListeCircuit = new javax.swing.JButton();
        jButtonSuivantListeCircuit = new javax.swing.JButton();
        jLabelNomCircuitLISTE = new javax.swing.JLabel();
        Recherche = new javax.swing.JPanel();
        RechercherCircuitParNom = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jTextFieldRechercheNom = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableClientsParCircuit1 = new javax.swing.JTable();
        jPanel24 = new javax.swing.JPanel();
        jButtonChercherCircuit = new javax.swing.JButton();
        PanelVide = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Agence Tourism");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane1.setAlignmentX(0.0F);
        jTabbedPane1.setAlignmentY(0.0F);
        jTabbedPane1.setDoubleBuffered(true);

        jButtonClients.setText("Clients");
        jButtonClients.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClientsActionPerformed(evt);
            }
        });

        jButtonCircuits.setText("Circuits");
        jButtonCircuits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCircuitsActionPerformed(evt);
            }
        });

        jButtonMonuments.setText("Monuments");
        jButtonMonuments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMonumentsActionPerformed(evt);
            }
        });

        jButtonRéservations.setText("Réservations");
        jButtonRéservations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRéservationsActionPerformed(evt);
            }
        });

        jButtonListes.setText("Listes");
        jButtonListes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonListesActionPerformed(evt);
            }
        });

        jButtonRecherche.setText("Recherche");
        jButtonRecherche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRechercheActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout MenuLayout = new javax.swing.GroupLayout(Menu);
        Menu.setLayout(MenuLayout);
        MenuLayout.setHorizontalGroup(
                MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MenuLayout.createSequentialGroup()
                                .addContainerGap(543, Short.MAX_VALUE)
                                .addGroup(MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jButtonClients, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonRéservations, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonMonuments, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonListes, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonRecherche, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonCircuits, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
        MenuLayout.setVerticalGroup(
                MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(MenuLayout.createSequentialGroup()
                                .addGap(91, 91, 91)
                                .addComponent(jButtonClients)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonCircuits)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonRéservations)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonMonuments)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonListes)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonRecherche)
                                .addContainerGap(91, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Menu", Menu);

        AjouterClient.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        AjouterClient.setAlignmentX(0.0F);
        AjouterClient.setAlignmentY(0.0F);
        AjouterClient.setPreferredSize(new java.awt.Dimension(348, 563));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Ajouter");

        jLabel3.setText("Nom:");

        jLabel4.setText("Prénom: ");

        jButtonAjouterClient.setText("Ajouter");
        jButtonAjouterClient.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterClient.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterClient.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonAjouterClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAjouterClientActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(119, 119, 119)
                                .addComponent(jButtonAjouterClient, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(130, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonAjouterClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout AjouterClientLayout = new javax.swing.GroupLayout(AjouterClient);
        AjouterClient.setLayout(AjouterClientLayout);
        AjouterClientLayout.setHorizontalGroup(
                AjouterClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterClientLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(AjouterClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(AjouterClientLayout.createSequentialGroup()
                                                .addGroup(AjouterClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(AjouterClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextFieldNomClient, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldPrenomClient))))
                                .addContainerGap())
                        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        AjouterClientLayout.setVerticalGroup(
                AjouterClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterClientLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(AjouterClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jTextFieldNomClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jTextFieldPrenomClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));

        SupprimerClient.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        SupprimerClient.setAlignmentX(0.0F);
        SupprimerClient.setAlignmentY(0.0F);
        SupprimerClient.setPreferredSize(new java.awt.Dimension(348, 563));
        SupprimerClient.setRequestFocusEnabled(false);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Supprimer");

        jLabel5.setText("Nom:");

        jLabel6.setText("Numéro:");

        jLabelNomClient.setText("Temporaire");

        jLabelNumeroClient.setText("Temporaire");

        jButtonPrecedentClient.setText("Précédent");
        jButtonPrecedentClient.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentClient.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentClient.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrecedentClientActionPerformed(evt);
            }
        });

        jButtonSupprimerClient.setText("Supprimer");
        jButtonSupprimerClient.setMaximumSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerClient.setMinimumSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerClient.setPreferredSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSupprimerClientActionPerformed(evt);
            }
        });

        jButtonSuivantClient.setText("Suivant");
        jButtonSuivantClient.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantClient.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantClient.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonSuivantClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSuivantClientActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButtonPrecedentClient, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                                .addComponent(jButtonSupprimerClient, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonSuivantClient, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButtonPrecedentClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonSupprimerClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonSuivantClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel38.setText("Prénom:");

        jLabelPrenomClient.setText("Temporaire");

        javax.swing.GroupLayout SupprimerClientLayout = new javax.swing.GroupLayout(SupprimerClient);
        SupprimerClient.setLayout(SupprimerClientLayout);
        SupprimerClientLayout.setHorizontalGroup(
                SupprimerClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(SupprimerClientLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(SupprimerClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(SupprimerClientLayout.createSequentialGroup()
                                                .addGroup(SupprimerClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(18, 18, 18)
                                                .addGroup(SupprimerClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabelPrenomClient, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabelNomClient, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabelNumeroClient, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addContainerGap())))
        );
        SupprimerClientLayout.setVerticalGroup(
                SupprimerClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(SupprimerClientLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(SupprimerClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(SupprimerClientLayout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel5))
                                        .addGroup(SupprimerClientLayout.createSequentialGroup()
                                                .addComponent(jLabelNumeroClient)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabelNomClient)))
                                .addGap(18, 18, 18)
                                .addGroup(SupprimerClientLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel38)
                                        .addComponent(jLabelPrenomClient))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(216, Short.MAX_VALUE))
        );

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));

        javax.swing.GroupLayout ClientsLayout = new javax.swing.GroupLayout(Clients);
        Clients.setLayout(ClientsLayout);
        ClientsLayout.setHorizontalGroup(
                ClientsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ClientsLayout.createSequentialGroup()
                                .addComponent(AjouterClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(SupprimerClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        ClientsLayout.setVerticalGroup(
                ClientsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(AjouterClient, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addComponent(SupprimerClient, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Clients", Clients);

        AjouterCircuit.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        AjouterCircuit.setAlignmentX(0.0F);
        AjouterCircuit.setAlignmentY(0.0F);
        AjouterCircuit.setPreferredSize(new java.awt.Dimension(348, 563));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Ajouter");

        jLabel8.setText("Nom:");

        jLabel9.setText("Ville de départ:");
        jLabel9.setToolTipText("");

        jLabel13.setText("Ville finale: ");
        jLabel13.setToolTipText("");

        jLabel14.setText("Prix:");
        jLabel14.setToolTipText("");

        jLabel15.setText("Durée: ");

        jLabel16.setText("Maximum client:");
        jLabel16.setToolTipText("");

        jButtonAjouterCircuit.setText("Ajouter");
        jButtonAjouterCircuit.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterCircuit.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterCircuit.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonAjouterCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAjouterCircuitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(124, 124, 124)
                                .addComponent(jButtonAjouterCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonAjouterCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout AjouterCircuitLayout = new javax.swing.GroupLayout(AjouterCircuit);
        AjouterCircuit.setLayout(AjouterCircuitLayout);
        AjouterCircuitLayout.setHorizontalGroup(
                AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(AjouterCircuitLayout.createSequentialGroup()
                                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                                        .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextFieldNomCircuit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                                        .addComponent(jTextFieldVilleDepart)
                                                        .addComponent(jTextFieldVilleFin, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldFin, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldDuree, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldMaxClient, javax.swing.GroupLayout.Alignment.TRAILING))))
                                .addContainerGap())
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        AjouterCircuitLayout.setVerticalGroup(
                AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(jTextFieldNomCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel9)
                                        .addComponent(jTextFieldVilleDepart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel13)
                                        .addComponent(jTextFieldVilleFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel14)
                                        .addComponent(jTextFieldFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel15)
                                        .addComponent(jTextFieldDuree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel16)
                                        .addComponent(jTextFieldMaxClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));

        ModifierSupprimerCircuit.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ModifierSupprimerCircuit.setAlignmentX(0.0F);
        ModifierSupprimerCircuit.setAlignmentY(0.0F);
        ModifierSupprimerCircuit.setPreferredSize(new java.awt.Dimension(348, 563));
        ModifierSupprimerCircuit.setRequestFocusEnabled(false);

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Modifier / Supprimer");

        jLabel17.setText("Maximum client:");
        jLabel17.setToolTipText("");

        jLabel18.setText("Durée: ");

        jLabel19.setText("Prix:");
        jLabel19.setToolTipText("");

        jLabel20.setText("Ville finale: ");
        jLabel20.setToolTipText("");

        jLabel21.setText("Ville de départ:");
        jLabel21.setToolTipText("");

        jLabel22.setText("Nom:");

        jTextFieldNomCircuitModif.setEditable(false);

        jPanel4.setAlignmentX(0.0F);
        jPanel4.setAlignmentY(0.0F);

        jButtonPrecedentCircuit.setText("Précédent");
        jButtonPrecedentCircuit.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentCircuit.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentCircuit.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrecedentCircuitActionPerformed(evt);
            }
        });

        jButtonModifierCircuit.setText("Modifier");
        jButtonModifierCircuit.setMaximumSize(new java.awt.Dimension(101, 26));
        jButtonModifierCircuit.setMinimumSize(new java.awt.Dimension(101, 26));
        jButtonModifierCircuit.setPreferredSize(new java.awt.Dimension(101, 26));
        jButtonModifierCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifierCircuitActionPerformed(evt);
            }
        });

        jButtonSuivantCircuit.setText("Suivant");
        jButtonSuivantCircuit.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantCircuit.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantCircuit.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonSuivantCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSuivantCircuitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButtonPrecedentCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonModifierCircuit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(17, 17, 17)
                                .addComponent(jButtonSuivantCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButtonModifierCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonPrecedentCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonSuivantCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel6.setAlignmentX(0.0F);
        jPanel6.setAlignmentY(0.0F);

        jButtonSupprimerCircuit.setText("Supprimer");
        jButtonSupprimerCircuit.setMaximumSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerCircuit.setMinimumSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerCircuit.setPreferredSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSupprimerCircuitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(115, 115, 115)
                                .addComponent(jButtonSupprimerCircuit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(128, 128, 128))
        );
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButtonSupprimerCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout ModifierSupprimerCircuitLayout = new javax.swing.GroupLayout(ModifierSupprimerCircuit);
        ModifierSupprimerCircuit.setLayout(ModifierSupprimerCircuitLayout);
        ModifierSupprimerCircuitLayout.setHorizontalGroup(
                ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ModifierSupprimerCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(ModifierSupprimerCircuitLayout.createSequentialGroup()
                                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextFieldNomCircuitModif, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldVilleDepartModif)
                                                        .addComponent(jTextFieldVilleFinModif, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldPrixModif, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldDureeModif, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldMaxClientModif, javax.swing.GroupLayout.Alignment.TRAILING))))
                                .addContainerGap())
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ModifierSupprimerCircuitLayout.setVerticalGroup(
                ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ModifierSupprimerCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel22)
                                        .addComponent(jTextFieldNomCircuitModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel21)
                                        .addComponent(jTextFieldVilleDepartModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel20)
                                        .addComponent(jTextFieldVilleFinModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel19)
                                        .addComponent(jTextFieldPrixModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel18)
                                        .addComponent(jTextFieldDureeModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel17)
                                        .addComponent(jTextFieldMaxClientModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(58, Short.MAX_VALUE))
        );

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        javax.swing.GroupLayout CircuitsLayout = new javax.swing.GroupLayout(Circuits);
        Circuits.setLayout(CircuitsLayout);
        CircuitsLayout.setHorizontalGroup(
                CircuitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(CircuitsLayout.createSequentialGroup()
                                .addComponent(AjouterCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ModifierSupprimerCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        CircuitsLayout.setVerticalGroup(
                CircuitsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(AjouterCircuit, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addComponent(ModifierSupprimerCircuit, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Circuits", Circuits);

        AjouterReservation.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        AjouterReservation.setAlignmentX(0.0F);
        AjouterReservation.setAlignmentY(0.0F);
        AjouterReservation.setPreferredSize(new java.awt.Dimension(348, 563));

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Ajouter");

        jLabel12.setText("Numéro client:");

        jLabel23.setText("Nom du circuit:");
        jLabel23.setToolTipText("");

        jLabel24.setText("Date de la visite:");
        jLabel24.setToolTipText("");

        jButtonAjouter2.setText("Ajouter");
        jButtonAjouter2.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonAjouter2.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonAjouter2.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonAjouter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAjouter2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonAjouter2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(123, 123, 123))
        );
        jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonAjouter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jDateChooserVisite.getDateEditor().setDate(new java.util.Date());

        javax.swing.GroupLayout AjouterReservationLayout = new javax.swing.GroupLayout(AjouterReservation);
        AjouterReservation.setLayout(AjouterReservationLayout);
        AjouterReservationLayout.setHorizontalGroup(
                AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterReservationLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(AjouterReservationLayout.createSequentialGroup()
                                                .addGroup(AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                                        .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextFieldNumClient, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                                        .addComponent(jTextFieldNomCircuitRes)
                                                        .addComponent(jDateChooserVisite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addContainerGap())
                        .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        AjouterReservationLayout.setVerticalGroup(
                AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterReservationLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel12)
                                        .addComponent(jTextFieldNumClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel23)
                                        .addComponent(jTextFieldNomCircuitRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel24)
                                        .addComponent(jDateChooserVisite, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(19, 19, 19)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));

        ModifierSupprimerReservation.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ModifierSupprimerReservation.setAlignmentX(0.0F);
        ModifierSupprimerReservation.setAlignmentY(0.0F);
        ModifierSupprimerReservation.setPreferredSize(new java.awt.Dimension(348, 563));
        ModifierSupprimerReservation.setRequestFocusEnabled(false);

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Supprimer");

        jLabel29.setText("A été annulée?");
        jLabel29.setToolTipText("");

        jLabel30.setText("Date limite:");

        jLabel31.setText("Date réservation:");
        jLabel31.setToolTipText("");

        jLabel32.setText("Date de la visite:");
        jLabel32.setToolTipText("");

        jTextFieldVilleDepartModif1.setEditable(false);

        jLabel33.setText("Nom du circuit:");
        jLabel33.setToolTipText("");

        jLabel34.setText("Numéro client:");

        jTextFieldNumClientModif.setEditable(false);

        jPanel8.setAlignmentX(0.0F);
        jPanel8.setAlignmentY(0.0F);
        jPanel8.setMaximumSize(new java.awt.Dimension(345, 26));
        jPanel8.setMinimumSize(new java.awt.Dimension(345, 26));

        jButtonPrecedentCircuit1.setText("Précédent");
        jButtonPrecedentCircuit1.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentCircuit1.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentCircuit1.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentCircuit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrecedentCircuit1ActionPerformed(evt);
            }
        });

        jButtonModifierReservation.setText("Modifier");
        jButtonModifierReservation.setMaximumSize(new java.awt.Dimension(101, 26));
        jButtonModifierReservation.setMinimumSize(new java.awt.Dimension(101, 26));
        jButtonModifierReservation.setPreferredSize(new java.awt.Dimension(101, 26));
        jButtonModifierReservation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifierReservationActionPerformed(evt);
            }
        });

        jButtonSuivantCircuit1.setText("Suivant");
        jButtonSuivantCircuit1.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantCircuit1.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantCircuit1.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonSuivantCircuit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSuivantCircuit1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButtonPrecedentCircuit1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonModifierReservation, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonSuivantCircuit1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(13, 13, 13))
        );
        jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButtonPrecedentCircuit1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonModifierReservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButtonSuivantCircuit1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel9.setAlignmentX(0.0F);
        jPanel9.setAlignmentY(0.0F);
        jPanel9.setMaximumSize(new java.awt.Dimension(345, 26));
        jPanel9.setMinimumSize(new java.awt.Dimension(345, 26));

        jButtonSupprimerCircuit1.setText("Supprimer");
        jButtonSupprimerCircuit1.setAlignmentY(0.0F);
        jButtonSupprimerCircuit1.setMaximumSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerCircuit1.setMinimumSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerCircuit1.setPreferredSize(new java.awt.Dimension(101, 26));
        jButtonSupprimerCircuit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSupprimerCircuit1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(119, 119, 119)
                                .addComponent(jButtonSupprimerCircuit1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonSupprimerCircuit1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jDateChooserReservModif.setEnabled(false);

        jDateChooserLimiteModif.setEnabled(false);

        javax.swing.GroupLayout ModifierSupprimerReservationLayout = new javax.swing.GroupLayout(ModifierSupprimerReservation);
        ModifierSupprimerReservation.setLayout(ModifierSupprimerReservationLayout);
        ModifierSupprimerReservationLayout.setHorizontalGroup(
                ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 344, Short.MAX_VALUE)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 344, Short.MAX_VALUE)
                        .addGroup(ModifierSupprimerReservationLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(ModifierSupprimerReservationLayout.createSequentialGroup()
                                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel33, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                                        .addComponent(jLabel34, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextFieldNumClientModif, javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jTextFieldVilleDepartModif1)))
                                        .addGroup(ModifierSupprimerReservationLayout.createSequentialGroup()
                                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel30, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(ModifierSupprimerReservationLayout.createSequentialGroup()
                                                                .addComponent(jCheckBoxAnnuler)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(jDateChooserVisiteModif, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jDateChooserReservModif, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jDateChooserLimiteModif, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addContainerGap())
        );
        ModifierSupprimerReservationLayout.setVerticalGroup(
                ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ModifierSupprimerReservationLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel34)
                                        .addComponent(jTextFieldNumClientModif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel33)
                                        .addComponent(jTextFieldVilleDepartModif1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel32)
                                        .addComponent(jDateChooserVisiteModif, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel31)
                                        .addComponent(jDateChooserReservModif, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel30)
                                        .addComponent(jDateChooserLimiteModif, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(ModifierSupprimerReservationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel29)
                                        .addComponent(jCheckBoxAnnuler, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(58, Short.MAX_VALUE))
        );

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        javax.swing.GroupLayout ReservationsLayout = new javax.swing.GroupLayout(Reservations);
        Reservations.setLayout(ReservationsLayout);
        ReservationsLayout.setHorizontalGroup(
                ReservationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ReservationsLayout.createSequentialGroup()
                                .addComponent(AjouterReservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ModifierSupprimerReservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        ReservationsLayout.setVerticalGroup(
                ReservationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(AjouterReservation, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addComponent(ModifierSupprimerReservation, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Réservations", Reservations);

        AjouterMonument.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        AjouterMonument.setAlignmentX(0.0F);
        AjouterMonument.setAlignmentY(0.0F);
        AjouterMonument.setPreferredSize(new java.awt.Dimension(348, 563));

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("Ajouter");

        jLabel26.setText("Nom:");

        jLabel27.setText("Construction:");
        jLabel27.setToolTipText("");

        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel35.setText("Histoire");
        jLabel35.setToolTipText("");

        jTextAreaHistoireMonument.setColumns(20);
        jTextAreaHistoireMonument.setRows(5);
        jTextAreaHistoireMonument.setWrapStyleWord(true);
        jTextAreaHistoireMonument.setLineWrap(true);
        jScrollPane1.setViewportView(jTextAreaHistoireMonument);

        jButtonAjouterMonu.setText("Ajouter");
        jButtonAjouterMonu.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterMonu.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterMonu.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonAjouterMonu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAjouterMonuActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
                jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonAjouterMonu, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(121, 121, 121))
        );
        jPanel10Layout.setVerticalGroup(
                jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonAjouterMonu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jYearChooserConstruction.setHorizontalAlignment(2);

        javax.swing.GroupLayout AjouterMonumentLayout = new javax.swing.GroupLayout(AjouterMonument);
        AjouterMonument.setLayout(AjouterMonumentLayout);
        AjouterMonumentLayout.setHorizontalGroup(
                AjouterMonumentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterMonumentLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(AjouterMonumentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(AjouterMonumentLayout.createSequentialGroup()
                                                .addGroup(AjouterMonumentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                                        .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(AjouterMonumentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextFieldNomMonument, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                                        .addGroup(AjouterMonumentLayout.createSequentialGroup()
                                                                .addComponent(jYearChooserConstruction, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))))
                                        .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jScrollPane1))
                                .addContainerGap())
                        .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        AjouterMonumentLayout.setVerticalGroup(
                AjouterMonumentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterMonumentLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(AjouterMonumentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel26)
                                        .addComponent(jTextFieldNomMonument, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(22, 22, 22)
                                .addGroup(AjouterMonumentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel27)
                                        .addComponent(jYearChooserConstruction, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel35)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(61, Short.MAX_VALUE))
        );

        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));

        AjouterMonumentTOCircuit.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        AjouterMonumentTOCircuit.setAlignmentX(0.0F);
        AjouterMonumentTOCircuit.setAlignmentY(0.0F);
        AjouterMonumentTOCircuit.setPreferredSize(new java.awt.Dimension(348, 563));
        AjouterMonumentTOCircuit.setRequestFocusEnabled(false);

        jLabel39.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setText("Ajouter un monument à un circuit");

        jLabel44.setText("Nom monument:");
        jLabel44.setToolTipText("");

        jLabel45.setText("Nom du circuit:");

        jButtonAjouterMonuCircuit.setText("Ajouter");
        jButtonAjouterMonuCircuit.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterMonuCircuit.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonAjouterMonuCircuit.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonAjouterMonuCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAjouterMonuCircuitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
                jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGap(121, 121, 121)
                                .addComponent(jButtonAjouterMonuCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
                jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonAjouterMonuCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout AjouterMonumentTOCircuitLayout = new javax.swing.GroupLayout(AjouterMonumentTOCircuit);
        AjouterMonumentTOCircuit.setLayout(AjouterMonumentTOCircuitLayout);
        AjouterMonumentTOCircuitLayout.setHorizontalGroup(
                AjouterMonumentTOCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterMonumentTOCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(AjouterMonumentTOCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                        .addGroup(AjouterMonumentTOCircuitLayout.createSequentialGroup()
                                                .addGroup(AjouterMonumentTOCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel44, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                                        .addComponent(jLabel45, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(AjouterMonumentTOCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextFieldNomCircuitLIENS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                                        .addComponent(jTextFieldNomMonuLIENS))))
                                .addContainerGap())
                        .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        AjouterMonumentTOCircuitLayout.setVerticalGroup(
                AjouterMonumentTOCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(AjouterMonumentTOCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(AjouterMonumentTOCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel45)
                                        .addComponent(jTextFieldNomCircuitLIENS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(AjouterMonumentTOCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel44)
                                        .addComponent(jTextFieldNomMonuLIENS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));

        javax.swing.GroupLayout MonumentsLayout = new javax.swing.GroupLayout(Monuments);
        Monuments.setLayout(MonumentsLayout);
        MonumentsLayout.setHorizontalGroup(
                MonumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(MonumentsLayout.createSequentialGroup()
                                .addComponent(AjouterMonument, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(AjouterMonumentTOCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        MonumentsLayout.setVerticalGroup(
                MonumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(AjouterMonument, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addComponent(AjouterMonumentTOCircuit, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Monuments", Monuments);

        ListeClientParCircuit.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ListeClientParCircuit.setAlignmentX(0.0F);
        ListeClientParCircuit.setAlignmentY(0.0F);
        ListeClientParCircuit.setPreferredSize(new java.awt.Dimension(348, 563));

        jLabel46.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel46.setText("Clients");

        jTableClientsParCircuit.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                        {}
                },
                new String [] {

                }
        ));
        jTableClientsParCircuit.getTableHeader().setResizingAllowed(false);
        jTableClientsParCircuit.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(jTableClientsParCircuit);

        javax.swing.GroupLayout ListeClientParCircuitLayout = new javax.swing.GroupLayout(ListeClientParCircuit);
        ListeClientParCircuit.setLayout(ListeClientParCircuitLayout);
        ListeClientParCircuitLayout.setHorizontalGroup(
                ListeClientParCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ListeClientParCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(ListeClientParCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                        .addComponent(jLabel46, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        ListeClientParCircuitLayout.setVerticalGroup(
                ListeClientParCircuitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ListeClientParCircuitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel46)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(35, Short.MAX_VALUE))
        );

        ListeMonuments.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        ListeMonuments.setAlignmentX(0.0F);
        ListeMonuments.setAlignmentY(0.0F);
        ListeMonuments.setPreferredSize(new java.awt.Dimension(348, 563));
        ListeMonuments.setRequestFocusEnabled(false);

        jLabelNomMonumentListe.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelNomMonumentListe.setText("Nom");

        jLabelDateMonuLISTE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelDateMonuLISTE.setText("Date");

        jLabel47.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel47.setText("Monuments");

        jButtonPrecedentListeMonu.setText("Précédent");
        jButtonPrecedentListeMonu.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentListeMonu.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentListeMonu.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentListeMonu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrecedentListeMonuActionPerformed(evt);
            }
        });

        jButtonSuivantListeMonu.setText("Suivant");
        jButtonSuivantListeMonu.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantListeMonu.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantListeMonu.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonSuivantListeMonu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSuivantListeMonuActionPerformed(evt);
            }
        });

        jButtonPhotoHistoire.setText("Photo");
        jButtonPhotoHistoire.setMaximumSize(new java.awt.Dimension(101, 26));
        jButtonPhotoHistoire.setMinimumSize(new java.awt.Dimension(101, 26));
        jButtonPhotoHistoire.setPreferredSize(new java.awt.Dimension(101, 26));
        jButtonPhotoHistoire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPhotoHistoireActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
                jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jButtonPrecedentListeMonu, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonPhotoHistoire, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonSuivantListeMonu, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel12Layout.setVerticalGroup(
                jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButtonPrecedentListeMonu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonSuivantListeMonu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonPhotoHistoire, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabelHistoireMonuLISTE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelHistoireMonuLISTE.setText("Histoire");
        jLabelHistoireMonuLISTE.setMaximumSize(new java.awt.Dimension(165, 319));
        jLabelHistoireMonuLISTE.setMinimumSize(new java.awt.Dimension(165, 319));
        jLabelHistoireMonuLISTE.setPreferredSize(new java.awt.Dimension(165, 319));

        javax.swing.GroupLayout ListeMonumentsLayout = new javax.swing.GroupLayout(ListeMonuments);
        ListeMonuments.setLayout(ListeMonumentsLayout);
        ListeMonumentsLayout.setHorizontalGroup(
                ListeMonumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ListeMonumentsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(ListeMonumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelDateMonuLISTE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabelNomMonumentListe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel47, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabelHistoireMonuLISTE, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                        .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ListeMonumentsLayout.setVerticalGroup(
                ListeMonumentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ListeMonumentsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel47)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelNomMonumentListe)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabelDateMonuLISTE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabelHistoireMonuLISTE, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35))
        );

        jPanel12.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));

        jButtonPrecedentListeCircuit.setText("Précédent");
        jButtonPrecedentListeCircuit.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentListeCircuit.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentListeCircuit.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonPrecedentListeCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrecedentListeCircuitActionPerformed(evt);
            }
        });

        jButtonSuivantListeCircuit.setText("Suivant");
        jButtonSuivantListeCircuit.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantListeCircuit.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonSuivantListeCircuit.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonSuivantListeCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSuivantListeCircuitActionPerformed(evt);
            }
        });

        jLabelNomCircuitLISTE.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabelNomCircuitLISTE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelNomCircuitLISTE.setText("Nom");

        javax.swing.GroupLayout ListesLayout = new javax.swing.GroupLayout(Listes);
        Listes.setLayout(ListesLayout);
        ListesLayout.setHorizontalGroup(
                ListesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ListesLayout.createSequentialGroup()
                                .addComponent(ListeClientParCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ListeMonuments, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1))
                        .addGroup(ListesLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButtonPrecedentListeCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabelNomCircuitLISTE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonSuivantListeCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        ListesLayout.setVerticalGroup(
                ListesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ListesLayout.createSequentialGroup()
                                .addGap(0, 20, Short.MAX_VALUE)
                                .addGroup(ListesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButtonPrecedentListeCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButtonSuivantListeCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabelNomCircuitLISTE))
                                .addGap(18, 18, 18)
                                .addGroup(ListesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(ListeClientParCircuit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                                        .addComponent(ListeMonuments, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Listes", Listes);

        RechercherCircuitParNom.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        RechercherCircuitParNom.setAlignmentX(0.0F);
        RechercherCircuitParNom.setAlignmentY(0.0F);
        RechercherCircuitParNom.setPreferredSize(new java.awt.Dimension(348, 563));

        jLabel36.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("Rechercher un circuit par son nom");

        jLabel37.setText("Nom:");

        jTableClientsParCircuit1.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                        {}
                },
                new String [] {

                }
        ));
        jTableClientsParCircuit1.getTableHeader().setResizingAllowed(false);
        jTableClientsParCircuit1.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(jTableClientsParCircuit1);

        jButtonChercherCircuit.setText("Chercher");
        jButtonChercherCircuit.setMaximumSize(new java.awt.Dimension(95, 26));
        jButtonChercherCircuit.setMinimumSize(new java.awt.Dimension(95, 26));
        jButtonChercherCircuit.setPreferredSize(new java.awt.Dimension(95, 26));
        jButtonChercherCircuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChercherCircuitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
                jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonChercherCircuit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(121, 121, 121))
        );
        jPanel24Layout.setVerticalGroup(
                jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButtonChercherCircuit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout RechercherCircuitParNomLayout = new javax.swing.GroupLayout(RechercherCircuitParNom);
        RechercherCircuitParNom.setLayout(RechercherCircuitParNomLayout);
        RechercherCircuitParNomLayout.setHorizontalGroup(
                RechercherCircuitParNomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(RechercherCircuitParNomLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(RechercherCircuitParNomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                        .addGroup(RechercherCircuitParNomLayout.createSequentialGroup()
                                                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextFieldRechercheNom)))
                                .addContainerGap())
                        .addComponent(jPanel24, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        RechercherCircuitParNomLayout.setVerticalGroup(
                RechercherCircuitParNomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(RechercherCircuitParNomLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(RechercherCircuitParNomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel37)
                                        .addComponent(jTextFieldRechercheNom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel24.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));

        PanelVide.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        PanelVide.setAlignmentX(0.0F);
        PanelVide.setAlignmentY(0.0F);
        PanelVide.setPreferredSize(new java.awt.Dimension(348, 563));
        PanelVide.setRequestFocusEnabled(false);

        javax.swing.GroupLayout PanelVideLayout = new javax.swing.GroupLayout(PanelVide);
        PanelVide.setLayout(PanelVideLayout);
        PanelVideLayout.setHorizontalGroup(
                PanelVideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 344, Short.MAX_VALUE)
        );
        PanelVideLayout.setVerticalGroup(
                PanelVideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 424, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout RechercheLayout = new javax.swing.GroupLayout(Recherche);
        Recherche.setLayout(RechercheLayout);
        RechercheLayout.setHorizontalGroup(
                RechercheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(RechercheLayout.createSequentialGroup()
                                .addComponent(RechercherCircuitParNom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(PanelVide, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        RechercheLayout.setVerticalGroup(
                RechercheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(RechercherCircuitParNom, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addComponent(PanelVide, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Recherche", Recherche);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1)
        );

        pack();
    }

    public void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Menu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Menu().setVisible(true);
            }
        });
    }
    //endregion

    //region Variables de l'interface
    private javax.swing.JPanel AjouterCircuit;
    private javax.swing.JPanel AjouterClient;
    private javax.swing.JPanel AjouterMonument;
    private javax.swing.JPanel AjouterMonumentTOCircuit;
    private javax.swing.JPanel AjouterReservation;
    private javax.swing.JPanel Circuits;
    private javax.swing.JPanel Clients;
    private javax.swing.JPanel ListeClientParCircuit;
    private javax.swing.JPanel ListeMonuments;
    private javax.swing.JPanel Listes;
    private javax.swing.JPanel Menu;
    private javax.swing.JPanel ModifierSupprimerCircuit;
    private javax.swing.JPanel ModifierSupprimerReservation;
    private javax.swing.JPanel Monuments;
    private javax.swing.JPanel PanelVide;
    private javax.swing.JPanel Recherche;
    private javax.swing.JPanel RechercherCircuitParNom;
    private javax.swing.JPanel Reservations;
    private javax.swing.JPanel SupprimerClient;
    private javax.swing.JButton jButtonAjouter2;
    private javax.swing.JButton jButtonAjouterCircuit;
    private javax.swing.JButton jButtonAjouterClient;
    private javax.swing.JButton jButtonAjouterMonu;
    private javax.swing.JButton jButtonAjouterMonuCircuit;
    private javax.swing.JButton jButtonChercherCircuit;
    private javax.swing.JButton jButtonCircuits;
    private javax.swing.JButton jButtonClients;
    private javax.swing.JButton jButtonListes;
    private javax.swing.JButton jButtonModifierCircuit;
    private javax.swing.JButton jButtonModifierReservation;
    private javax.swing.JButton jButtonMonuments;
    private javax.swing.JButton jButtonPhotoHistoire;
    private javax.swing.JButton jButtonPrecedentCircuit;
    private javax.swing.JButton jButtonPrecedentCircuit1;
    private javax.swing.JButton jButtonPrecedentClient;
    private javax.swing.JButton jButtonPrecedentListeCircuit;
    private javax.swing.JButton jButtonPrecedentListeMonu;
    private javax.swing.JButton jButtonRecherche;
    private javax.swing.JButton jButtonRéservations;
    private javax.swing.JButton jButtonSuivantCircuit;
    private javax.swing.JButton jButtonSuivantCircuit1;
    private javax.swing.JButton jButtonSuivantClient;
    private javax.swing.JButton jButtonSuivantListeCircuit;
    private javax.swing.JButton jButtonSuivantListeMonu;
    private javax.swing.JButton jButtonSupprimerCircuit;
    private javax.swing.JButton jButtonSupprimerCircuit1;
    private javax.swing.JButton jButtonSupprimerClient;
    private javax.swing.JCheckBox jCheckBoxAnnuler;
    private com.toedter.calendar.JDateChooser jDateChooserLimiteModif;
    private com.toedter.calendar.JDateChooser jDateChooserReservModif;
    private com.toedter.calendar.JDateChooser jDateChooserVisite;
    private com.toedter.calendar.JDateChooser jDateChooserVisiteModif;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelDateMonuLISTE;
    private javax.swing.JLabel jLabelHistoireMonuLISTE;
    private javax.swing.JLabel jLabelNomCircuitLISTE;
    private javax.swing.JLabel jLabelNomClient;
    private javax.swing.JLabel jLabelNomMonumentListe;
    private javax.swing.JLabel jLabelNumeroClient;
    private javax.swing.JLabel jLabelPrenomClient;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableClientsParCircuit;
    private javax.swing.JTable jTableClientsParCircuit1;
    private javax.swing.JTextArea jTextAreaHistoireMonument;
    private javax.swing.JTextField jTextFieldDuree;
    private javax.swing.JTextField jTextFieldDureeModif;
    private javax.swing.JTextField jTextFieldFin;
    private javax.swing.JTextField jTextFieldMaxClient;
    private javax.swing.JTextField jTextFieldMaxClientModif;
    private javax.swing.JTextField jTextFieldNomCircuit;
    private javax.swing.JTextField jTextFieldNomCircuitLIENS;
    private javax.swing.JTextField jTextFieldNomCircuitModif;
    private javax.swing.JTextField jTextFieldNomCircuitRes;
    private javax.swing.JTextField jTextFieldNomClient;
    private javax.swing.JTextField jTextFieldNomMonuLIENS;
    private javax.swing.JTextField jTextFieldNomMonument;
    private javax.swing.JTextField jTextFieldNumClient;
    private javax.swing.JTextField jTextFieldNumClientModif;
    private javax.swing.JTextField jTextFieldPrenomClient;
    private javax.swing.JTextField jTextFieldPrixModif;
    private javax.swing.JTextField jTextFieldRechercheNom;
    private javax.swing.JTextField jTextFieldVilleDepart;
    private javax.swing.JTextField jTextFieldVilleDepartModif;
    private javax.swing.JTextField jTextFieldVilleDepartModif1;
    private javax.swing.JTextField jTextFieldVilleFin;
    private javax.swing.JTextField jTextFieldVilleFinModif;
    private com.toedter.calendar.JYearChooser jYearChooserConstruction;
    //endregion
}
