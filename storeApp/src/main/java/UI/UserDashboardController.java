package UI;

import Domain.GlobalAccount;
import Domain.Order;
import Domain.Product;
import Domain.User;
import Repository.*;
import Service.orderService;
import Service.productService;
import Service.userService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class UserDashboardController {

    private User loggedInUser;

    @FXML
    private Label userDetailsLabel;

    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, Integer> productIDColumn;
    @FXML
    private TableColumn<Product, String> productCategoryColumn;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, Integer> productPriceColumn;

    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> orderIDColumn;
    @FXML
    private TableColumn<Order, String> orderProductsColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, String> orderAddressColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;

    @FXML
    private TextField orderProductsField;
    @FXML
    private TextField orderDateField;
    @FXML
    private TextField orderAdressField;
    @FXML
    private TextField orderIDField;
    @FXML
    private Button deleteAccountButton;

    private productService productService;
    private orderService orderService;
    private userService userService;

    @FXML
    private VBox addOrderSection;
    @FXML
    private VBox cancelOrderSection;

    private ObservableList<Product> productObservableList;
    private ObservableList<Order> orderObservableList;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public void setUser(User user) throws RepoException {
        this.loggedInUser = user;
        updateUserDetails();
    }

    private void updateUserDetails() throws RepoException {
        if (loggedInUser != null) {
            userDetailsLabel.setText("Welcome, " + loggedInUser.getFirstName() + " " + loggedInUser.getLastName()+"!");
            userDetailsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            orderObservableList = FXCollections.observableArrayList(orderService.getOrdersByUserId(loggedInUser.getId()));

            orderIDColumn.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

            orderProductsColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getProducts().stream()
                                    .map(produs -> String.valueOf(produs.getId()))
                                    .collect(Collectors.joining(", "))
                    )
            );

            orderDateColumn.setCellValueFactory(cellData -> {
                Date date = cellData.getValue().getDate();
                String formattedDate = sdf.format(date);
                return new SimpleStringProperty(formattedDate);
            });

            orderAddressColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getAddress())
            );

            orderStatusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStatus())
            );

            if (!ordersTable.getColumns().contains(orderIDColumn)) {
                ordersTable.getColumns().addAll(orderIDColumn, orderProductsColumn, orderDateColumn, orderAddressColumn, orderStatusColumn);
            }
            ordersTable.setItems(orderObservableList);
            ordersTable.refresh();

            productObservableList = FXCollections.observableArrayList(productService.getAllProducts());

            productIDColumn.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getId()).asObject()
            );

            productCategoryColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getCategory())
            );

            productNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getName())
            );

            productPriceColumn.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getPrice()).asObject()
            );

            if (!productsTable.getColumns().contains(productIDColumn)) {
                productsTable.getColumns().addAll(productIDColumn, productCategoryColumn, productNameColumn, productPriceColumn);
            }
            productsTable.setItems(productObservableList);
            productsTable.refresh();

        } else {
            userDetailsLabel.setText("No user details available.");
        }
    }


    @FXML
    public void initialize() throws IOException, SQLException {
        toggleAddOrderFields(false);
        toggleCancelOrderField(false);

        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream("Properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Error loading properties file: " + e.getMessage());
            return;
        }

        // Citim tipurile de repository din fișierul de proprietăți
        String repositoryType1 = properties.getProperty("ProductRepository", "").toLowerCase();
        String repositoryType2 = properties.getProperty("OrderRepository", "").toLowerCase();
        String repositoryType3 = properties.getProperty("UserRepository", "").toLowerCase();
        String productFile = properties.getProperty("ProductFile", "");
        String orderFile = properties.getProperty("OrderFile", "");
        String userFile = properties.getProperty("UserFile", "");

        Repo<Product> productRepository;
        Repo<Order> orderRepository;
        Repo<User> userRepository;

        switch (repositoryType1) {
            case "binary":
                productRepository = new BinaryFileRepo<>(productFile);
                break;
            case "text":
                productRepository = new TextFileRepo<>(productFile) {
                    @Override
                    public Product createEntityFromCsv(String csvLine) {
                        return null;
                    }

                    @Override
                    protected String toCsv(Product entity) {
                        return "";
                    }
                };
                break;
            case "memory":
                productRepository = new Repo<>();
                break;
            case "sql":
                productRepository = new SQLRepository<>(Product.class);
                break;
            default:
                System.out.println("Invalid repository type for Products: " + repositoryType1);
                return;
        }

        switch (repositoryType2) {
            case "binary":
                orderRepository = new BinaryFileRepo<>(orderFile);
                break;
            case "text":
                orderRepository = new TextFileRepo<>(orderFile) {
                    @Override
                    public Order createEntityFromCsv(String csvLine) {
                        return null;
                    }

                    @Override
                    protected String toCsv(Order entity) {
                        return "";
                    }
                };
                break;
            case "memory":
                orderRepository = new Repo<>();
                break;
            case "sql":
                orderRepository = new SQLRepository<>(Order.class);
                break;
            default:
                System.out.println("Invalid repository type for Orders: " + repositoryType2);
                return;
        }

        switch (repositoryType3) {
            case "binary":
                userRepository = new BinaryFileRepo<>(userFile);break;
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
                };break;

            case "memory":
                userRepository = new Repo<>();break;
            case "sql":
                userRepository = new SQLRepository<>(User.class);break;
            default:
                System.out.println("Invalid repository type for Users: " + repositoryType3);
                return;
        }

        this.productService = new productService(productRepository);
        this.orderService = new orderService(orderRepository);
        this.userService = new userService(userRepository);
    }


    @FXML
    private void toggleAddOrderFields(boolean visible) {
        addOrderSection.setVisible(visible);
        addOrderSection.setManaged(visible);
    }


    @FXML
    private void toggleCancelOrderField(boolean visible) {
        cancelOrderSection.setVisible(visible);
    }


    @FXML
    private void showAddOrderSection() {
        addOrderSection.setVisible(true);
        addOrderSection.setManaged(true);
        cancelOrderSection.setVisible(false);
        cancelOrderSection.setManaged(false);
    }


    @FXML
    private void showCancelOrderSection() {
        cancelOrderSection.setVisible(true);
        cancelOrderSection.setManaged(true);
        addOrderSection.setVisible(false);
        addOrderSection.setManaged(false);
    }


    @FXML
    private void submitAddOrder(){
        try {
            String products = orderProductsField.getText();
            String dateText = orderDateField.getText();
            String address = orderAdressField.getText();

            if (products.isEmpty() || dateText.isEmpty() || address.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required for adding an order!");
                return;
            }

            Date date = sdf.parse(dateText);

            List<Integer> ids = Arrays.stream(orderProductsField.getText().split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            List<Product> selectedProducts = new ArrayList<>();
            for (int i : ids) {
                try {
                    Product p = productService.getProductById(i);
                    selectedProducts.add(p);
                } catch (RepoException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid ID", "Order ID must be an integer!");
                    return;
                }
            }

            Order newOrder = new Order(selectedProducts, date, address, "not delivered",loggedInUser.getId());
            orderService.addOrder(newOrder);

            orderObservableList.add(newOrder);
            ordersTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order added successfully!");

        } catch (ParseException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please enter a valid date in the format: dd-MM-yyyy HH:mm");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the order: " + e.getMessage());
        }

        toggleAddOrderFields(false);
    }


    @FXML
    private void submitCancelOrder() {
        try {
            String orderIdText = orderIDField.getText();
            if (orderIdText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Order ID is required to cancel an order!");
                return;
            }

            int orderId = Integer.parseInt(orderIdText);


            Order orderToCancel = orderService.getOrderById(orderId);
            if (orderToCancel == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No order found with the given ID!");
                return;
            }
            if(orderToCancel.getStatus().equals("delivered")){
                showAlert(Alert.AlertType.ERROR, "Error", "You cannot cancel a delivered order!");
                return;
            }

            orderService.removeOrder(orderToCancel.getId());

            orderObservableList.remove(orderToCancel);
            ordersTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order canceled successfully!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID", "Order ID must be a number!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while canceling the order: " + e.getMessage());
        }

        toggleCancelOrderField(false);
    }


    @FXML
    private void deleteAccount() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action is permanent and cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                List<Order>orders = (List<Order>) orderService.getOrdersByUserId(loggedInUser.getId());
                for (Order order : orders) {
                    orderService.removeOrder(order.getId());
                }
                userService.removeUser(loggedInUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Account Deleted", "Your account has been successfully deleted.");
                navigateToLogin();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the account: " + e.getMessage());
            }
        }
    }


    @FXML
    private void navigateToLogin() {
        try {
            GlobalAccount.setAccount(null);
            Stage stage = (Stage) deleteAccountButton.getScene().getWindow();
            stage.close();
            FXMLLoader fxmlLoader = new FXMLLoader(StoreApplication.class.getResource("store-view.fxml"));
            VBox root = fxmlLoader.load();

            Scene scene = new Scene(root, 750, 550);
            stage.setTitle("Store");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}