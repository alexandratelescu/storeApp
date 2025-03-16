package UI;

import Domain.*;
import Repository.*;
import Service.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController {

    public Button submitAddProductButton;
    private Admin loggedInAdmin;

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
    private TableColumn<Order, Integer> orderUserIDColumn;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> userIDColumn;
    @FXML
    private TableColumn<User, String> userFirstNameColumn;
    @FXML
    private TableColumn<User, String> userLastNameColumn;
    @FXML
    private TableColumn<User, String> userEmailColumn;

    @FXML
    private TableView<Admin> adminsTable;
    @FXML
    private TableColumn<Admin, Integer> adminIDColumn;
    @FXML
    private TableColumn<Admin, String> adminFirstNameColumn;
    @FXML
    private TableColumn<Admin, String> adminLastNameColumn;
    @FXML
    private TableColumn<Admin, String> adminEmailColumn;

    @FXML
    private TableView <Map.Entry<String, Long>> report1Table;
    @FXML
    private TableColumn<Map.Entry<String, Long>, Long> numberOfProductsColumn;
    @FXML
    TableColumn<Map.Entry<String, Long>, String> categoryColumn;

    @FXML
    private TableView<Map.Entry<Integer, Map<String, Double>>> report2Table;
    @FXML
    private TableColumn<Map.Entry<Integer, Map<String, Double>>, String> monthColumn;
    @FXML
    private TableColumn<Map.Entry<Integer, Map<String, Double>>, String> numberOfOrdersColumn;
    @FXML
    private TableColumn<Map.Entry<Integer, Map<String, Double>>, String> totalRevenueColumn;

    @FXML
    private TableView<Map.Entry<Product, Long>> report3Table;
    @FXML
    private TableColumn<Map.Entry<Product, Long>, Integer> productColumn;
    @FXML
    private TableColumn<Map.Entry<Product, Long>, Long> totalRevenueProductColumn;

    @FXML
    private Label adminDetailsLabel;

    @FXML
    private Button deleteAccountButton;

    private productService productService;
    private orderService orderService;
    private userService userService;
    private adminService adminService;

    private ObservableList<Product> productObservableList;
    private ObservableList<Order> orderObservableList;
    private ObservableList<User> userObservableList;
    private ObservableList<Admin> adminObservableList;
    private ObservableList<Map.Entry<String, Long>> report1ObservableList;
    private ObservableList<Map.Entry<Integer, Map<String, Double>>> report2ObservableList;
    private ObservableList<Map.Entry<Product, Long>> report3ObservableList;

    @FXML
    private VBox addProductSection;
    @FXML
    private VBox deleteProductSection;
    @FXML
    private VBox updateProductSection;
    @FXML
    private VBox updateOrderSection;

    @FXML
    private TextField productCategoryField;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField productPriceField;

    @FXML
    private TextField productIDField;

    @FXML
    private TextField productToUpdateIDField;
    @FXML
    private TextField newCategoryField;
    @FXML
    private TextField newNameField;
    @FXML
    private TextField newPriceField;

    @FXML
    private TextField orderToUpdateIDField;
    @FXML
    private TextField newStatusField;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public void setAdmin(Admin admin) throws RepoException {
        this.loggedInAdmin = admin;
        updateAdminDetails();
    }

    private void updateAdminDetails() throws RepoException {
        if (loggedInAdmin != null) {
            adminDetailsLabel.setText("Welcome, " + loggedInAdmin.getFirstName() + " " + loggedInAdmin.getLastName() + "!");
            adminDetailsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            loadOrdersTable();
            loadProductsTable();
            loadUsersTable();
            loadAdminsTable();
            loadReport1table();
            loadReport2table();
            loadReport3table();
        } else {
            adminDetailsLabel.setText("No admin details available.");
        }
    }

    @FXML
    public void initialize() {
        try {
            initializeServices();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeServices() {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream("Properties")) {
            properties.load(input);
            productService = new productService(createRepository(properties, Product.class));
            orderService = new orderService(createRepository(properties, Order.class));
            userService = new userService(createRepository(properties, User.class));
            adminService = new adminService(createRepository(properties, Admin.class));
        }catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Initialization Error", "Failed to initialize services: " + e.getMessage());
        }
    }

    private <T extends AbstractEntity> Repo<T> createRepository(Properties properties, Class<T> entityClass)
            throws SQLException, IOException {
        String keyPrefix = entityClass.getSimpleName();
        String repoType = properties.getProperty(keyPrefix + "Repository", "").toLowerCase();
        String fileName = properties.getProperty(keyPrefix + "File", "");

        return switch (repoType) {
            case "binary" -> new BinaryFileRepo<>(fileName);
            case "text" -> new TextFileRepo<>(fileName) {
                @Override
                public T createEntityFromCsv(String csvLine) {
                    return null;
                }

                @Override
                protected String toCsv(T entity) {
                    return "";
                }
            };
            case "memory" -> new Repo<>();
            case "sql" -> new SQLRepository<>(entityClass);
            default -> throw new IllegalArgumentException("Invalid repository type for " + keyPrefix + ": " + repoType);
        };
    }

    private void loadOrdersTable() {
        orderObservableList = FXCollections.observableArrayList(orderService.getAllOrders());
        orderIDColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        orderProductsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getProducts().stream()
                        .map(product -> String.valueOf(product.getId()))
                        .collect(Collectors.joining(", "))
        ));
        orderDateColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDate();
            return new SimpleStringProperty(sdf.format(date));
        });
        orderAddressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        orderStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        orderUserIDColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getUserID()).asObject());
        ordersTable.setItems(orderObservableList);
        ordersTable.refresh();
    }

    private void loadProductsTable() throws RepoException {
        productObservableList = FXCollections.observableArrayList(productService.getAllProducts());
        productIDColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        productCategoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        productNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        productPriceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPrice()).asObject());
        productsTable.setItems(productObservableList);
        productsTable.refresh();
    }

    private void loadUsersTable() throws RepoException {
        userObservableList = FXCollections.observableArrayList(userService.getAllUsers());
        userIDColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        userFirstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        userLastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        userEmailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        usersTable.setItems(userObservableList);
        usersTable.refresh();
    }

    private void loadAdminsTable() throws RepoException {
        adminObservableList = FXCollections.observableArrayList(adminService.getAllAdmins());
        adminIDColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        adminFirstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        adminLastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        adminEmailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        adminsTable.setItems(adminObservableList);
        adminsTable.refresh();
    }

    private void loadReport1table() throws RepoException {
        report1ObservableList = FXCollections.observableArrayList(orderService.orderedProductsPerCategory((List<Order>) Service.orderService.getAllOrders()));
        numberOfProductsColumn.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getValue()).asObject());
        categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey()));
        report1Table.setItems(report1ObservableList);
        report1Table.refresh();
    }


    @FXML
    public void refreshReport1Table() {
        try {
            List<Map.Entry<String, Long>> updatedReport = orderService.orderedProductsPerCategory((List<Order>) Service.orderService.getAllOrders());
            report1ObservableList.setAll(updatedReport);
            report1Table.setItems(report1ObservableList);
            report1Table.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,"Update failed","Could not update table: " + e.getMessage());
        }
    }

    private void loadReport2table() throws RepoException {
        report2ObservableList = FXCollections.observableArrayList(orderService.mostProfitableMonths((List<Order>) Service.orderService.getAllOrders()));
        monthColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("Month " + cellData.getValue().getKey())
        );
        numberOfOrdersColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getValue().get("Number of orders").intValue()))
        );
        totalRevenueColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getValue().get("Total revenue")))
        );
        report2Table.setItems(report2ObservableList);
        report2Table.refresh();
    }


    @FXML
    public void refreshReport2Table() {
        try {
            List<Map.Entry<Integer, Map<String, Double>>> updatedReport =orderService.mostProfitableMonths((List<Order>) Service.orderService.getAllOrders());
            report2ObservableList.setAll(updatedReport);
            report2Table.setItems(report2ObservableList);
            report2Table.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,"Update failed","Could not update table: " + e.getMessage());
        }
    }

    private void loadReport3table() throws RepoException {
      report3ObservableList = FXCollections.observableArrayList(orderService.productsSortedByRevenue((List<Order>) Service.orderService.getAllOrders()));
        productColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getKey().getId()).asObject());

        totalRevenueProductColumn.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getValue()).asObject());
        report3Table.setItems(report3ObservableList);
        report3Table.refresh();
    }


    @FXML
    public void refreshReport3Table() {
        try{
            List<Map.Entry<Product, Long>> updatedReport = orderService.productsSortedByRevenue((List<Order>) Service.orderService.getAllOrders());
            report3ObservableList.setAll(updatedReport);
            report3Table.setItems(report3ObservableList);
            report3Table.refresh();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,"Update failed","Could not update table: " + e.getMessage());
        }
    }


    @FXML
    private void toggleAddProductFields(boolean visible) {
        addProductSection.setVisible(visible);
        addProductSection.setManaged(visible);
    }


    @FXML
    private void toggleDeleteProductField(boolean visible) {
        deleteProductSection.setVisible(visible);
        deleteProductSection.setManaged(visible);
    }


    @FXML
    private void toggleUpdateProductFields(boolean visible) {
        updateProductSection.setVisible(visible);
        updateProductSection.setManaged(visible);
    }


    @FXML
    private void toggleUpdateOrderFields(boolean visible) {
        updateOrderSection.setVisible(visible);
        updateOrderSection.setManaged(visible);
    }


    @FXML
    private void submitAddProduct() {
        try {

            String category = productCategoryField.getText();
            String name = productNameField.getText();
            String priceText = productPriceField.getText();

            if (category.isEmpty() || name.isEmpty() || priceText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required to add a product!");
                return;
            }

            int price = Integer.parseInt(priceText);

            Product newProduct = new Product(category, name, price);


            productService.addProduct(newProduct);
            productObservableList.add(newProduct);
            productsTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");


        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Price must be a valid integer!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the product: " + e.getMessage());
        }
        productCategoryField.clear();
        productNameField.clear();
        productPriceField.clear();
        toggleAddProductFields(false);
    }


    @FXML
    private void submitDeleteProduct() {
        try {
            String productIdText = productIDField.getText();
            if (productIdText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Product ID is required to delete a product!");
                return;
            }

            int productId = Integer.parseInt(productIdText);

            Product productToDelete = productService.getProductById(productId);
            if (productToDelete == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No product found with the given ID!");
                return;
            }
            productService.removeProduct(productToDelete.getId());


            productObservableList.remove(productToDelete);
            productsTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Success", "The product has been deleted successfully!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID", "Product ID must be an integer!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the product: " + e.getMessage());
        }
        productIDField.clear();
        toggleDeleteProductField(false);
    }


    @FXML
    private void submitUpdateProduct() {
        try {
            String productIdText = productToUpdateIDField.getText();
            if (productIdText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Product ID is required to delete a product!");
                return;
            }

            int productId = Integer.parseInt(productIdText);

            Product productToUpdate = productService.getProductById(productId);
            if (productToUpdate == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No product found with the given ID!");
                return;
            }
            if(newCategoryField.getText().isEmpty() || newNameField.getText().isEmpty() || newPriceField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required to update a product!");
                return;
            }
            String newCategory = newCategoryField.getText();
            String newName = newNameField.getText();
            int newPrice = Integer.parseInt(newPriceField.getText());
            Product newProduct = new Product(newCategory,newName,newPrice);
            newProduct.id = productId;
            productService.updateProduct(newProduct);
            productObservableList.setAll(Service.productService.getAllProducts());
            productsTable.setItems(productObservableList);
            productsTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Success", "The product has been updated successfully!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID / price", "Product ID and product price must be integers!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while deleting the product: " + e.getMessage());
        }
        productToUpdateIDField.clear();
        newCategoryField.clear();
        newNameField.clear();
        newPriceField.clear();
        toggleUpdateProductFields(false);
    }


    @FXML
    private void submitUpdateOrder(){
        try{
           String orderIDText =orderToUpdateIDField.getText();
           if(orderIDText.isEmpty()){
               showAlert(Alert.AlertType.ERROR, "Validation Error", "Order ID is required to update an order status!");
               return;
           }
           int orderId = Integer.parseInt(orderIDText);
           Order orderToUpdate = Service.orderService.getOrderById(orderId);
            if (orderToUpdate == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No order found with the given ID!");
                return;
            }
            String newStatus =newStatusField.getText();
            if(newStatus.isEmpty()){
                showAlert(Alert.AlertType.ERROR, "Validation Error", "New status field is required to update an order status!");
                return;
            }
            Order newOrder = new Order(orderToUpdate.getProducts(),orderToUpdate.getDate(),orderToUpdate.getAddress(),newStatus,orderToUpdate.getUserID());
            newOrder.id = orderId;
            Service.orderService.updateOrder(newOrder);
            orderObservableList.setAll(Service.orderService.getAllOrders());
            ordersTable.setItems(orderObservableList);
            ordersTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "The order has been updated successfully!");

        } catch (RepoException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        orderToUpdateIDField.clear();
        newStatusField.clear();
        toggleUpdateOrderFields(false);

    }


    @FXML
    private void showAddProductSection() {
        addProductSection.setVisible(true);
        addProductSection.setManaged(true);
        deleteProductSection.setVisible(false);
        deleteProductSection.setManaged(false);
        updateProductSection.setVisible(false);
        updateProductSection.setManaged(false);
        updateOrderSection.setVisible(false);
        updateOrderSection.setManaged(false);
    }


    @FXML
    private void showDeleteProductSection() {
        deleteProductSection.setVisible(true);
        deleteProductSection.setManaged(true);
        addProductSection.setVisible(false);
        addProductSection.setManaged(false);
        updateProductSection.setVisible(false);
        updateProductSection.setManaged(false);
        updateOrderSection.setVisible(false);
        updateOrderSection.setManaged(false);
    }


    @FXML
    private void showUpdateProductSection() {
        updateProductSection.setVisible(true);
        updateProductSection.setManaged(true);
        deleteProductSection.setVisible(false);
        deleteProductSection.setManaged(false);
        addProductSection.setVisible(false);
        addProductSection.setManaged(false);
        updateProductSection.setVisible(false);
        updateProductSection.setManaged(false);
    }


    @FXML
    private void showUpdateOrderSection(){
        updateOrderSection.setVisible(true);
        updateOrderSection.setManaged(true);
        deleteProductSection.setVisible(false);
        deleteProductSection.setManaged(false);
        addProductSection.setVisible(false);
        addProductSection.setManaged(false);
        updateProductSection.setVisible(false);
        updateProductSection.setManaged(false);

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
                adminService.removeAdmin(loggedInAdmin.getId());
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
