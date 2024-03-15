# ZeroPassword

**ZeroPassword** is a password manager application built using Java and MongoDB. It allows users to securely store, manage, and access their passwords.

## Features

* User registration and login
* Secure password storage with MongoDB
* Account management (adding, viewing, copying, and deleting accounts)
* Dark mode compatibility

## Installation

1. Clone this repository:

```
git clone https://github.com/Druuxd/ZeroPassword
```

2. Install dependencies:

**JDK 21**: https://www.oracle.com/ro/java/technologies/downloads/

**MongoDB Java driver**: https://github.com/mongodb/mongo-java-driver

**FlatLaf**: https://github.com/topics/flatlaf

**Dotenv-Java**: https://github.com/cdimascio/dotenv-java

3. Create a .env file in the project root and set the following environment variables:

```
MONGO_USER=your_mongodb_user
MONGO_PASS=your_mongodb_password
MONGO_CLUSTER=your_mongodb_cluster_url
MONGO_DB=your_mongodb_database_name
```
4. Run the application:
```
cd PasswordApplication
javac PasswordApplication.java
java PasswordApplication
```

## Usage
1. Launch ZeroPassword.
   
2. Register a new user or log in with existing credentials.
   
3. To add a new account:
 * Click the "Add Account" button.
 * Enter website/application name, username, and password.
   
4. Manage Accounts:
 * Right-click on an existing account to copy the username/password or delete the account.

## Credits

This project was developed by Darius Andrei.

## Contributing
   
Pull requests are welcome!
