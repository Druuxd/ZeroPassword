package dev.zeropassword.password;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import dev.zeropassword.password.manager.FormsManager;
import io.github.cdimascio.dotenv.Dotenv;
import net.miginfocom.swing.MigLayout;
import org.bson.Document;

class AddAccountDialog extends JDialog implements ActionListener {
	private Dotenv dotenv = Dotenv.load();;
	private String mongoUser = dotenv.get("MONGO_USER");
	private String mongoPass = dotenv.get("MONGO_PASS");
	private String mongoCluster = dotenv.get("MONGO_CLUSTER");
	private String mongoDB = dotenv.get("MONGO_DB");
	private JTextField userField, urlField;
	private JPasswordField passwordField;
	private JButton saveButton;
	private String loggedInUser;
	private MainWindow mainWindow;


	public AddAccountDialog(MainWindow parent, String loggedInUser) {

		super(parent, "Add Account", true);
		this.loggedInUser = loggedInUser;
		this.mainWindow = parent;
		setSize(new Dimension(400, 500));

		setLayout(new MigLayout("fill, insets 20", "[center]","[center]"));
		JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,250:280"));
		panel.putClientProperty(FlatClientProperties.STYLE,"" +
				"arc:20;"+
				"[light]background: darken(@background, 3%);"+
				"[dark]background: lighten(@background, 3%)");
		userField = new JTextField(20);
		passwordField = new JPasswordField();
		passwordField.putClientProperty(FlatClientProperties.STYLE, ""+
				"showRevealButton: true;");
		urlField = new JTextField(20);
		userField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
		passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
		urlField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your URL");

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);

		JLabel lbTitle = new JLabel("Add Account!");
		JLabel description = new JLabel("Enter your account's details!");
		lbTitle.putClientProperty(FlatClientProperties.STYLE,""+
				"font:bold +10");
		description.putClientProperty(FlatClientProperties.STYLE, ""+
				"[light]foreground: lighten(@foreground, 30%);"+
				"[dark]foreground: darken(@foreground, 30%)");

		panel.add(lbTitle);
		panel.add(description);
		panel.add(new JLabel("Username"), "gapy 8");
		panel.add(userField);
		panel.add(new JLabel("Password"), "gapy 8");
		panel.add(passwordField);
		panel.add(new JLabel("URL"), "gapy 8");
		panel.add(urlField);
		panel.add(saveButton, "gapy 10");
		setLocationRelativeTo(parent);

		add(panel);
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveButton) {
			String username = userField.getText();
			String password = new String(passwordField.getPassword());
			String url = urlField.getText();

			String connectionString = "mongodb+srv://" + mongoUser + ":" + mongoPass + "@" + mongoCluster + "/";
			String databaseName = mongoDB;

			try (MongoClient mongoClient = MongoClients.create(connectionString)) {
				MongoDatabase database = mongoClient.getDatabase(databaseName);
				MongoCollection<Document> userCollection = database.getCollection(loggedInUser);
				Document newAccount = new Document("username", username)
						.append("password", password)
						.append("url", url);

				userCollection.insertOne(newAccount);
				JOptionPane.showMessageDialog(this, "Account saved successfully!");
				mainWindow.loadAccounts();
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}

			dispose();
		}
	}
}

public class PasswordApplication extends JFrame{

	public PasswordApplication() {
		init();
	}

	private void init() {
		ImageIcon icon = new ImageIcon("D:/Coding/GitHub/Password/zeroLogo.jpg");
		setIconImage(icon.getImage());
		setTitle("ZeroPassword Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(1000, 600));
		setLocationRelativeTo(null);
		setContentPane(new LoginWindow());
		FormsManager.getInstance().initApplication(this);
	}

	public static void main(String[] args) {
		FlatRobotoFont.install();
		FlatLaf.registerCustomDefaultsSource("zero.themes");
		UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
		FlatMacDarkLaf.setup();
		EventQueue.invokeLater(() -> new PasswordApplication().setVisible(true));
	}
}