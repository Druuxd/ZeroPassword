package dev.zeropassword.password;

import com.formdev.flatlaf.FlatClientProperties;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.zeropassword.password.manager.FormsManager;
import io.github.cdimascio.dotenv.Dotenv;
import net.miginfocom.swing.MigLayout;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;

public class Register extends JPanel {

    private Dotenv dotenv = Dotenv.load();
    private String mongoUser = dotenv.get("MONGO_USER");
    private String mongoPass = dotenv.get("MONGO_PASS");
    private String mongoCluster = dotenv.get("MONGO_CLUSTER");
    private String mongoDB = dotenv.get("MONGO_DB");
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    private JButton cmdRegister;
    public Register() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill,insets 20", "[center]", "[center]"));
        setSize(new Dimension(460,460));
        txtFirstName = new JTextField();
        txtLastName = new JTextField();
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtConfirmPassword = new JPasswordField();
        cmdRegister = new JButton("Sign Up");

        cmdRegister.addActionListener(e -> {
            if(e.getSource() == cmdRegister){
                signUpActionPerformed();
                FormsManager.getInstance().showForm(new LoginWindow());
            }
        });

        JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "[fill,360]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:20;" +
                "[light]background:darken(@background,3%);" +
                "[dark]background:lighten(@background,3%)");

        txtFirstName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "First name");
        txtLastName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Last name");
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username or email");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Re-enter your password");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "showRevealButton:true");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "showRevealButton:true");

        cmdRegister.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:darken(@background,10%);" +
                "[dark]background:lighten(@background,10%);" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0");

        JLabel lbTitle = new JLabel("Welcome to your Password Manger");
        JLabel description = new JLabel("Please enter your desired information");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +10");
        description.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]foreground:lighten(@foreground,30%);" +
                "[dark]foreground:darken(@foreground,30%)");


        panel.add(lbTitle);
        panel.add(description);
        panel.add(new JLabel("Full Name"), "gapy 10");
        panel.add(txtFirstName, "split 2");
        panel.add(txtLastName);
        panel.add(new JSeparator(), "gapy 5 5");
        panel.add(new JLabel("Username or Email"));
        panel.add(txtUsername);
        panel.add(new JLabel("Password"), "gapy 8");
        panel.add(txtPassword);
        panel.add(new JLabel("Confirm Password"), "gapy 0");
        panel.add(txtConfirmPassword);
        panel.add(cmdRegister, "gapy 20");
        panel.add(createLoginLabel(), "gapy 10");
        add(panel);
    }

    private Component createLoginLabel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null");
        JButton cmdLogin = new JButton("<html><a href=\"#\">Sign in here</a></html>");
        cmdLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "border:3,3,3,3");
        cmdLogin.setContentAreaFilled(false);
        cmdLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmdLogin.addActionListener(e -> {
            FormsManager.getInstance().showForm(new LoginWindow());
        });
        JLabel label = new JLabel("Already have an account ?");
        label.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]foreground:lighten(@foreground,30%);" +
                "[dark]foreground:darken(@foreground,30%)");
        panel.add(label);
        panel.add(cmdLogin);
        return panel;
    }

    private void signUpActionPerformed() {
        String enteredUsername = txtUsername.getText().toLowerCase();
        String enteredPassword = new String(txtPassword.getPassword());

        if (enteredUsername.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters long.");
            return;
        }

        // Check password length
        if (enteredPassword.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long.");
            return;
        }

        String connectionString = "mongodb+srv://" + mongoUser + ":" + mongoPass + "@" + mongoCluster + "/";
        String databaseName = mongoDB;

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection("users");
            Document existingUser = collection.find(new Document("username", enteredUsername)).first();
            if (existingUser != null) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.");
            } else {
                Document newUser = new Document("username", enteredUsername)
                        .append("password", enteredPassword);
                collection.insertOne(newUser);
                JOptionPane.showMessageDialog(this, "User signed up successfully!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
