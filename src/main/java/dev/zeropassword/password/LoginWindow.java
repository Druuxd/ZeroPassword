package dev.zeropassword.password;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import javax.swing.ImageIcon;
import java.awt.event.KeyEvent;

import com.formdev.flatlaf.FlatClientProperties;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import dev.zeropassword.password.manager.FormsManager;

import io.github.cdimascio.dotenv.Dotenv;
import net.miginfocom.swing.MigLayout;
import org.bson.Document;

public class LoginWindow extends JPanel implements ActionListener {
    private Dotenv dotenv = Dotenv.load();
    private String mongoUser = dotenv.get("MONGO_USER");
    private String mongoPass = dotenv.get("MONGO_PASS");
    private String mongoCluster = dotenv.get("MONGO_CLUSTER");
    private String mongoDB = dotenv.get("MONGO_DB");
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chRememberMe;
    private JButton cmdLogin;

    public LoginWindow() {
        init();
        txtUsername.addKeyListener(new EnterKeyListener());
        txtPassword.addKeyListener(new EnterKeyListener());
    }

    private void init(){

        setLayout(new MigLayout("fill, insets 20", "[center]","[center]"));
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        chRememberMe = new JCheckBox("Remember me");
        cmdLogin = new JButton("Login");
        cmdLogin.addActionListener(this);
        JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,250:280"));
        panel.putClientProperty(FlatClientProperties.STYLE,"" +
                "arc:20;"+
                "[light]background: darken(@background, 3%);"+
                "[dark]background: lighten(@background, 3%)");

        txtPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "showRevealButton: true;");

        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");


        JLabel lbTitle = new JLabel("Welcome back!");
        JLabel description = new JLabel("Please sign in to access your account!");
        lbTitle.putClientProperty(FlatClientProperties.STYLE,""+
                "font:bold +10");
        description.putClientProperty(FlatClientProperties.STYLE, ""+
                "[light]foreground: lighten(@foreground, 30%);"+
                "[dark]foreground: darken(@foreground, 30%)");

        panel.add(lbTitle);
        panel.add(description);
        panel.add(new JLabel("Username"), "gapy 8");
        panel.add(txtUsername);
        panel.add(new JLabel("Password"), "gapy 8");
        panel.add(txtPassword);
        panel.add(chRememberMe, "grow 0");
        panel.add(cmdLogin, "gapy 10");
        panel.add(createSignupLable(), "gapy 10");
        add(panel);
    }

    private Component createSignupLable(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.putClientProperty(FlatClientProperties.STYLE,""+
                "background:null");
        JButton cmdRegister = new JButton("<html><a href=\"#\">Signup</a></html>");
        cmdRegister.putClientProperty(FlatClientProperties.STYLE,""+
                "border:3,3,3,3");
        cmdRegister.setContentAreaFilled(false);
        cmdRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmdRegister.addActionListener(e -> {
            FormsManager.getInstance().showForm(new Register());
        });
        JLabel label = new JLabel("Don't have an account?");
        label.putClientProperty(FlatClientProperties.STYLE,""+
                "[light]foreground: lighten(@foreground, 30%);"+
                "[dark]foreground: darken(@foreground, 30%)");

        panel.add(label);
        panel.add(cmdRegister);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cmdLogin) {
            loginActionPerformed();
        } else if (e.getSource() == chRememberMe) {
            System.out.println("merge");
        }
    }

    private void loginActionPerformed() {
        String enteredUsername = txtUsername.getText().toLowerCase();
        String enteredPassword = new String(txtPassword.getPassword());

        String connectionString = "mongodb+srv://" + mongoUser + ":" + mongoPass + "@" + mongoCluster + "/";
        String databaseName = mongoDB;

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection("users");
            Document query = new Document("username", enteredUsername);
            Document user = collection.find(query).first();

            if (user != null) {
                String storedPassword = user.getString("password");
                if (enteredPassword.equals(storedPassword)) {
                    JOptionPane.showMessageDialog(this, "Logged-in!");
                    Window window = SwingUtilities.getWindowAncestor(this);
                    if (window instanceof JFrame) {
                        window.dispose();
                    }
                    String loggedInUser = enteredUsername;
                    new MainWindow("ZeroPassword", loggedInUser);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid password!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class EnterKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginActionPerformed();
                }
            }
        }
    }