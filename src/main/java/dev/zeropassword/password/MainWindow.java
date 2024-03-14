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

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.*;



public class MainWindow extends JFrame implements ActionListener {

	private Dotenv dotenv = Dotenv.load();
	private String mongoUser = dotenv.get("MONGO_USER");
	private String mongoPass = dotenv.get("MONGO_PASS");
	private String mongoCluster = dotenv.get("MONGO_CLUSTER");
	private String mongoDB = dotenv.get("MONGO_DB");
	private JButton addButton, logoutButton, deleteButton;
	private String loggedInUser;
	private DefaultListModel<String> accountsModel;
	private JList<String> accountsList;
	private JPopupMenu popupMenu;
	private JMenuItem copyUsernameItem;
	private JMenuItem copyPasswordItem;

	private JMenuItem removeAccountItem;
	public MainWindow(String title, String loggedInUser) {
		super(title);
		this.loggedInUser = loggedInUser;

		ImageIcon icon = new ImageIcon("zeroLogo.jpg");
		setIconImage(icon.getImage());

		setLayout(new MigLayout("fill, insets 20", "[center]","[center]"));
		JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,550:580"));
		panel.putClientProperty(FlatClientProperties.STYLE,"" +
				"arc:20;"+
				"[light]background: darken(@background, 3%);"+
				"[dark]background: lighten(@background, 3%)");


		addButton = new JButton("Add Account");
		addButton.putClientProperty(FlatClientProperties.STYLE, "" +
				"[light]background:darken(@background,10%);" +
				"[dark]background:lighten(@background,10%);" +
				"borderWidth:0;" +
				"focusWidth:0;" +
				"innerFocusWidth:0");
		addButton.addActionListener(this);

		logoutButton = new JButton("Log-out");
		logoutButton.putClientProperty(FlatClientProperties.STYLE, "" +
				"[light]background:darken(@background,10%);" +
				"[dark]background:lighten(@background,10%);" +
				"borderWidth:0;" +
				"focusWidth:0;" +
				"innerFocusWidth:0");
		logoutButton.addActionListener(this);

		deleteButton = new JButton("Delete Account");
		deleteButton.putClientProperty(FlatClientProperties.STYLE, "" +
				"[light]background:darken(@background,10%);" +
				"[dark]background:lighten(@background,10%);" +
				"borderWidth:0;" +
				"focusWidth:0;" +
				"innerFocusWidth:0");
		deleteButton.addActionListener(this);

		accountsModel = new DefaultListModel<>();
		accountsList = new JList<>(accountsModel);
		accountsList.putClientProperty(FlatClientProperties.STYLE,""+
				"font:bold +5");

		JScrollPane scrollPane = new JScrollPane(accountsList);
		scrollPane.putClientProperty(FlatClientProperties.STYLE,
				"background: #2b2b2b; " +
				"foreground: #ffffff; " +
				"border: none; " +
				"border-radius: 10;" +
				"width:50%");
		panel.add(scrollPane);
		panel.add(addButton, "gapy 10");
		panel.add(deleteButton);
		panel.add(logoutButton);


		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 500);
		setLocationRelativeTo(null);
		setVisible(true);



		popupMenu = new JPopupMenu();
		copyUsernameItem = new JMenuItem("Copy Username");
		copyPasswordItem = new JMenuItem("Copy Password");
		removeAccountItem = new JMenuItem("Remove Account");

		copyUsernameItem.addActionListener(e -> {
			int selectedIndex = accountsList.getSelectedIndex();
			if (selectedIndex != -1) {
				String username = getUsernameAtIndex(selectedIndex);
				copyToClipboard(username);
				JOptionPane.showMessageDialog(this, "Username copied to clipboard!");
			}
		});

		copyPasswordItem.addActionListener(e -> {
			int selectedIndex = accountsList.getSelectedIndex();
			if (selectedIndex != -1) {
				String accountInfo = accountsModel.getElementAt(selectedIndex);
				String[] parts = accountInfo.split(", ");
				String username = parts[0].substring(parts[0].indexOf(":") + 2);

				String password = getPasswordFromDatabase(username);

				if (password != null) {
					copyToClipboard(password);
					JOptionPane.showMessageDialog(this, "Password copied to clipboard!");
				} else {
					JOptionPane.showMessageDialog(this, "Password not found!");
				}
			}
		});

		removeAccountItem.addActionListener(e -> {
			int selectedIndex = accountsList.getSelectedIndex();
			if (selectedIndex != -1) {
				int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this account?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					removeAccountAtIndex(selectedIndex);
				}
			}
		});

		popupMenu.add(copyUsernameItem);
		popupMenu.add(copyPasswordItem);
		popupMenu.add(removeAccountItem);

		accountsList.setComponentPopupMenu(popupMenu);

		accountsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int index = accountsList.locationToIndex(e.getPoint());
					accountsList.setSelectedIndex(index);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int index = accountsList.locationToIndex(e.getPoint());
					accountsList.setSelectedIndex(index);
					if (!e.isConsumed()) {
						popupMenu.show(accountsList, e.getX(), e.getY());
					}
				}
			}
		});
		add(panel);
		loadAccounts();
	}

	private String getPasswordFromDatabase(String username) {
		String password = null;

		String connectionString = "mongodb+srv://" + mongoUser + ":" + mongoPass + "@" + mongoCluster + "/";
		String databaseName = mongoDB;

		try (MongoClient mongoClient = MongoClients.create(connectionString)) {
			MongoDatabase database = mongoClient.getDatabase(databaseName);
			MongoCollection<Document> userCollection = database.getCollection(loggedInUser);
			Document user = userCollection.find(new Document("username", username)).first();
			if (user != null) {
				password = user.getString("password");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		return password;
	}

	private String getUsernameAtIndex(int index) {
		String accountInfo = accountsModel.getElementAt(index);
		String[] parts = accountInfo.split(", ");
		String username = parts[0].substring(parts[0].indexOf(":") + 2);
		return username;
	}

	private void copyToClipboard(String text) {
		StringSelection stringSelection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	private void removeAccountAtIndex(int index) {
		String accountInfo = accountsModel.getElementAt(index);
		String[] parts = accountInfo.split(", ");
		String username = parts[0].substring(parts[0].indexOf(":") + 2);

		String connectionString = "mongodb+srv://" + mongoUser + ":" + mongoPass + "@" + mongoCluster + "/";
		String databaseName = mongoDB;

		try (MongoClient mongoClient = MongoClients.create(connectionString)) {
			MongoDatabase database = mongoClient.getDatabase(databaseName);
			MongoCollection<Document> userCollection = database.getCollection(loggedInUser);
			userCollection.deleteOne(new Document("username", username));

			JOptionPane.showMessageDialog(this, "Account removed successfully!");
			loadAccounts();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	void loadAccounts() {
		String connectionString = "mongodb+srv://" + mongoUser + ":" + mongoPass + "@" + mongoCluster + "/";
		String databaseName = mongoDB;

		try (MongoClient mongoClient = MongoClients.create(connectionString)) {
			MongoDatabase database = mongoClient.getDatabase(databaseName);
			MongoCollection<Document> userCollection = database.getCollection(loggedInUser);
			List<Document> accounts = userCollection.find().into(new ArrayList<>());

			accountsModel.clear();

			for (Document account : accounts) {
				String username = account.getString("username");
				String password = "**********";
				String url = account.getString("url");

				String accountInfo = "Username: " + username + ", Password: " + password + ", URL: " + url;

				accountsModel.addElement(accountInfo);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {
			new AddAccountDialog(this, loggedInUser);
		} else if (e.getSource() == deleteButton) {
			int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete your account?", "Confirm Deletion",
					JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				deleteAccount(loggedInUser);
			}
		} else if (e.getSource() == logoutButton) {
			int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout",
					JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				dispose();
			}
		}
	}

	private void deleteAccount(String username) {
		String connectionString = "mongodb+srv://" + mongoUser + ":" + mongoPass + "@" + mongoCluster + "/";
		String databaseName = mongoDB;

		try (MongoClient mongoClient = MongoClients.create(connectionString)) {
			MongoDatabase database = mongoClient.getDatabase(databaseName);

			database.getCollection(loggedInUser).drop();

			MongoCollection<Document> usersCollection = database.getCollection("users");
			usersCollection.deleteOne(new Document("username", loggedInUser));

			JOptionPane.showMessageDialog(this, "Account deleted successfully!");

			dispose();
			FormsManager.getInstance().showForm(new LoginWindow());;
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}