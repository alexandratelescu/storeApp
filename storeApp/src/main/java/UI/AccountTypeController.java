package UI;

import Domain.*;
import Repository.*;
import Service.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class AccountTypeController {

    @FXML
    private SplitMenuButton accountTypeButton;

    @FXML
    private Label resultLabel;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField newemailField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField newpasswordField;

    @FXML
    private Button submitButton;

    @FXML
    private Button submitButton2;


    public adminService adminService;
    public userService userService;

    @FXML
    public void initialize() throws IOException, SQLException {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream("Properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Error loading properties file: " + e.getMessage());
            return;
        }
        String repositoryType1 = properties.getProperty("AdminRepository", "").toLowerCase();
        String repositoryType2 = properties.getProperty("UserRepository", "").toLowerCase();
        String adminFile = properties.getProperty("AdminFile", "");
        String userFile = properties.getProperty("UserFile", "");

        Repo<Admin> adminRepository;
        Repo<User> userRepository;

        switch (repositoryType1) {
            case "binary":
                adminRepository = new BinaryFileRepo<>(adminFile);
                break;
            case "text":
                adminRepository = new TextFileRepo<>(adminFile) {
                    @Override
                    public Admin createEntityFromCsv(String csvLine) {
                        return null;
                    }

                    @Override
                    protected String toCsv(Admin entity) {
                        return "";
                    }
                };
                break;
            case "memory":
                adminRepository = new Repo<>();
                break;
            case "sql":
                adminRepository = new SQLRepository<>(Admin.class);
                break;
            default:
                System.out.println("Invalid repository type for admins: " + repositoryType1);
                return;
        }

        switch (repositoryType2) {
            case "binary":
                userRepository = new BinaryFileRepo<>(userFile);
                break;
            case "text":
                userRepository = new TextFileRepo<>(userFile) {
                    @Override
                    public User createEntityFromCsv(String csvLine) {
                        return null;
                    }

                    @Override
                    protected String toCsv(User entity) {
                        return "";
                    }
                };
                break;
            case "memory":
                userRepository = new Repo<>();
                break;
            case "sql":
                userRepository = new SQLRepository<>(User.class);
                break;
            default:
                System.out.println("Invalid repository type for users: " + repositoryType2);
                return;
        }
        this.adminService = new adminService(adminRepository);
        this.userService = new userService(userRepository);

    }

    @FXML
    private void selectUser() {
        User user = new User("User123","LastN","em","pass");

        GlobalAccount.setAccount(user);

        accountTypeButton.setText("User");
        resultLabel.setText("Selected account type: User");
        resultLabel.setTextFill(javafx.scene.paint.Color.BLACK);
    }

    @FXML
    private void selectAdmin() {

        Admin admin = new Admin("Admin123","LastN","em","pass");


        GlobalAccount.setAccount(admin);


        accountTypeButton.setText("Admin");
        resultLabel.setText("Selected account type: Admin");
        resultLabel.setTextFill(javafx.scene.paint.Color.BLACK);
    }

    @FXML
    private void logIn() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (password.isEmpty() || email.isEmpty()) {
            resultLabel.setText("Email and password are required.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        if (!EmailValidator.isValidEmail(email)) {
            resultLabel.setText("Invalid email format.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        Admin admin = adminService.findByEmailAndPassword(email, password);
        User user = userService.findByEmailAndPassword(email, password);
        if (admin != null) {
            GlobalAccount.setAccount(admin);
            try {

                Stage stage = (Stage) emailField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(StoreApplication.class.getResource("admin-dashboard.fxml"));
                VBox root = fxmlLoader.load();
                Scene adminScene = new Scene(root,750, 550);


                AdminDashboardController controller = fxmlLoader.getController();
                controller.setAdmin(admin);


                stage.setScene(adminScene);
            } catch (IOException e) {
                e.printStackTrace();
                resultLabel.setText("Failed to load admin dashboard.");
                resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            } catch (RepoException e) {
                throw new RuntimeException(e);
            }
        } else if (user != null) {
            GlobalAccount.setAccount(user);
            try {

                Stage stage = (Stage) emailField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(StoreApplication.class.getResource("user-dashboard.fxml"));
                VBox root = fxmlLoader.load();
                Scene userScene = new Scene(root,750, 550);


                UserDashboardController controller = fxmlLoader.getController();
                controller.setUser(user);


                stage.setScene(userScene);
            } catch (IOException e) {
                e.printStackTrace();
                resultLabel.setText("Failed to load user dashboard.");
                resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            } catch (RepoException e) {
                throw new RuntimeException(e);
            }
        } else {
            resultLabel.setText("Authentication failed. Invalid email or password.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void showLogInFields() {
        Object account = GlobalAccount.getAccount();


        if (account != null) {

            firstNameField.setVisible(false);
            firstNameField.setManaged(false);
            lastNameField.setVisible(false);
            lastNameField.setManaged(false);

            emailField.setVisible(true);
            emailField.setManaged(true);
            newemailField.setVisible(false);
            newemailField.setManaged(false);

            passwordField.setVisible(true);
            passwordField.setManaged(true);
            newpasswordField.setVisible(false);
            newpasswordField.setManaged(false);

            submitButton.setVisible(true);
            submitButton.setManaged(true);
            submitButton2.setVisible(false);
            submitButton2.setManaged(false);
        } else {
            resultLabel.setText("Please select an account type before loging in.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void signUp() throws RepoException, IOException {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = newemailField.getText().trim();
        String password = newpasswordField.getText().trim();


        if (password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            resultLabel.setText("First name, last name, email, and password are required.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }


        if (!EmailValidator.isValidEmail(email)) {
            resultLabel.setText("Invalid email format.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }


        if (GlobalAccount.getAccount() instanceof Admin && !email.endsWith("@store.ro")) {
            resultLabel.setText("Admin email must end with @store.ro.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }


        Object account = GlobalAccount.getAccount();
        if (account == null) {
            resultLabel.setText("Please select a valid account type before signing up.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }


        if (account.getClass() == User.class) {
            List<User> existingUsers = (List<User>) userService.getAllUsers();
            for (User user : existingUsers) {
                if (user.getEmail().equals(email)) {
                    resultLabel.setText("There is already an account with this email.");
                    resultLabel.setTextFill(javafx.scene.paint.Color.RED);
                    return;
                }
            }

            User newUser = new User(firstName, lastName, email, password);
            userService.addUser(newUser);
            GlobalAccount.setAccount(newUser);

            try {
                Stage stage = (Stage) emailField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(StoreApplication.class.getResource("user-dashboard.fxml"));
                VBox root = fxmlLoader.load();
                Scene userScene = new Scene(root, 750, 550);

                UserDashboardController controller = fxmlLoader.getController();
                controller.setUser(newUser);
                stage.setScene(userScene);
            } catch (IOException e) {
                e.printStackTrace();
                resultLabel.setText("Failed to load user dashboard.");
                resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            } catch (RepoException e) {
                throw new RuntimeException(e);
            }

        } else if (account.getClass() == Admin.class) {
            List<Admin> existingAdmins = (List<Admin>) adminService.getAllAdmins();
            for (Admin admin : existingAdmins) {
                if (admin.getEmail().equals(email)) {
                    resultLabel.setText("There is already an admin with this email.");
                    resultLabel.setTextFill(javafx.scene.paint.Color.RED);
                    return;
                }
            }

            Admin newAdmin = new Admin(firstName, lastName, email, password);
            adminService.addAdmin(newAdmin);
            GlobalAccount.setAccount(newAdmin);

            try {
                Stage stage = (Stage) emailField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(StoreApplication.class.getResource("admin-dashboard.fxml"));
                VBox root = fxmlLoader.load();
                Scene adminScene = new Scene(root, 750, 550);

                AdminDashboardController controller = fxmlLoader.getController();
                controller.setAdmin(newAdmin);
                stage.setScene(adminScene);
            } catch (IOException e) {
                e.printStackTrace();
                resultLabel.setText("Failed to load admin dashboard.");
                resultLabel.setTextFill(javafx.scene.paint.Color.RED);
            } catch (RepoException e) {
                throw new RuntimeException(e);
            }

        } else {
            resultLabel.setText("Invalid account type for sign up.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void showSignUpFields() {
        Object account = GlobalAccount.getAccount();

        if (account != null) {
            firstNameField.setVisible(true);
            firstNameField.setManaged(true);

            lastNameField.setVisible(true);
            lastNameField.setManaged(true);

            newemailField.setVisible(true);
            newemailField.setManaged(true);
            emailField.setVisible(false);
            emailField.setManaged(false);

            newpasswordField.setVisible(true);
            newpasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);

            submitButton2.setVisible(true);
            submitButton2.setManaged(true);
            submitButton.setVisible(false);
            submitButton.setManaged(false);
        } else if (account == null) {
            resultLabel.setText("Please select a valid account type before signing up.");
            resultLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

}

