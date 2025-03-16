package Repository;

import Domain.*;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SQLRepository<T extends AbstractEntity> extends Repo<T> {
    Connection connection;
    private Class<T> entityClass;
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private String DB_URL = "jdbc:sqlite:src/Store";

    public SQLRepository(Class<T> entityClass) throws SQLException {
        this.entityClass = entityClass;
        openConnection();
        createTable();
        loadData();
    }

    List<Product> availableProducts = new ArrayList<>();

    private void loadData() {
        items.clear();
        availableProducts.clear();
        try {
            if (entityClass.equals(Product.class)) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products");
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Product product = new Product( rs.getString("Category"), rs.getString("Name"), rs.getInt("Price"));
                        product.id = rs.getInt("ID");
                        items.add((T) product);
                    }
                }
            } else if (entityClass.equals(Order.class)) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products");
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Product product = new Product( rs.getString("Category"), rs.getString("Name"), rs.getInt("Price"));
                        product.id =rs.getInt("ID");
                        availableProducts.add(product);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Orders");
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String productsString = rs.getString("Products");
                        String date = rs.getString("Date");
                        String adress = rs.getString("Adress");
                        String status = rs.getString("Status");
                        int userID = rs.getInt("UserID");
                        List<Product> products = new ArrayList<>();
                        String[] ids = productsString.split(",");
                        for (String i : ids) {
                            try {
                                int productId = Integer.parseInt(i);
                                for (Product pr : availableProducts) {
                                    if (productId == pr.getId()) {
                                        products.add(pr);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                throw new NumberFormatException("Invalid product ID: " + i);
                            }
                        }
                        try {
                            Order order = new Order(products, formatter.parse(date),adress,status,userID);
                            order.id = rs.getInt("ID");
                            items.add((T) order);
                        } catch (ParseException e) {
                            throw new RepoException("Error parsing date");
                        }

                    }
                }
            }
            else if (entityClass.equals(User.class)) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users");
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        User user = new User(rs.getString("FirstName"), rs.getString("LastName"), rs.getString("Email"), rs.getString("Password"));
                        user.id = rs.getInt("ID");
                        items.add((T) user);
                    }
                }
            }
            else if(entityClass.equals(Admin.class)) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Admins");
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Admin admin = new Admin(rs.getString("FirstName"), rs.getString("LastName"), rs.getString("Email"), rs.getString("Password"));
                        admin.id = rs.getInt("ID");
                        items.add((T) admin);
                    }
                }
            }
        } catch (SQLException | RepoException ex) {
            ex.printStackTrace();
        }
    }


    private void openConnection() throws SQLException {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(DB_URL);
        try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
            }
        } catch (SQLException e) {
            throw new SQLException("Error: Could not connect to the database");
        }

    }

    private void createTable() throws SQLException {

        String createUsersTable = "CREATE TABLE IF NOT EXISTS Users ("+
                "ID INTEGER PRIMARY KEY, " +
                "FirstName VARCHAR(50), " +
                "LastName VARCHAR(50), " +
                "Email VARCHAR(50), "+
                "Password VARCHAR(50))";

        String createAdminsTable = "CREATE TABLE IF NOT EXISTS Admins ("+
                "ID INTEGER PRIMARY KEY, " +
                "FirstName VARCHAR(50), " +
                "LastName VARCHAR(50), " +
                "Email VARCHAR(50), "+
                "Password VARCHAR(50))";

        String createOrdersTable = "CREATE TABLE IF NOT EXISTS Orders (" +
                "ID INTEGER PRIMARY KEY, " +
                "Products VARCHAR(50), " +
                "Date VARCHAR(50), " +
                "Address VARCHAR(50), " +
                "Status VARCHAR(50), " +
                "UserID INTEGER, " +
                "FOREIGN KEY (UserID) REFERENCES Users(ID))";


        String createProductsTable = "CREATE TABLE IF NOT EXISTS Products (" +
                "ID INTEGER PRIMARY KEY, " +
                "Category VARCHAR(50)," +
                "Name VARCHAR(50)," +
                "Price INTEGER)";

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(createAdminsTable);
            statement.execute(createUsersTable);
            statement.execute(createProductsTable);
            statement.execute(createOrdersTable);
        } catch (SQLException e) {
            throw new SQLException("Error: Could not create table");
        }
    }

    //                               ADD FUNCTIONS

    public void add(T entity) throws RepoException {
        if (entity instanceof Product) {
            addProduct((Product) entity);
        } else if (entity instanceof Order) {
            addOrder((Order) entity);
        }else if (entity instanceof User) {
            addUser((User) entity);
        }
        else if (entity instanceof Admin){
            addAdmin((Admin)entity);
        }else {
            throw new RepoException("Unsupported entity type");
        }
    }

    private void addProduct(Product product) throws RepoException {
        if (findById(product.getId()) != null) {
            throw new RepoException("Product with ID " + product.getId() + " already exists");
        }
        String sql = "INSERT INTO Products (ID, Category,Name, Price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, product.getId());
            statement.setString(2, product.getCategory());
            statement.setString(3, product.getName());
            statement.setInt(4, product.getPrice());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepoException("Could not add product");
        }
        loadData();
    }
    private void addUser(User user) throws RepoException {
        if (findById(user.getId()) != null) {
            throw new RepoException("User with ID " + user.getId() + " already exists");
        }
        String sql = "INSERT INTO Users (ID, FirstName, LastName, Email, Password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPassword());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepoException("Could not create account");
        }
        loadData();

    }

    private void addAdmin(Admin admin) throws RepoException {
        if (findById(admin.getId()) != null) {
            throw new RepoException("Admin with ID " + admin.getId() + " already exists");
        }
        String sql = "INSERT INTO Admins (ID, FirstName, LastName, Email, Password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, admin.getId());
            statement.setString(2, admin.getFirstName());
            statement.setString(3, admin.getLastName());
            statement.setString(4, admin.getEmail());
            statement.setString(5, admin.getPassword());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepoException("Could not create account");
        }
        loadData();

    }

    private void addOrder(Order order) throws RepoException {
        for (Product pr : order.getProducts()) {
            boolean exists = availableProducts.stream()
                    .anyMatch(p -> p.getId() == pr.getId());
            if (!exists) {
                throw new RepoException("Product with id " + pr.getId() + " does not exist");
            }
        }
        String sql = "INSERT INTO Orders (ID, Products, Date, Adress, Status, UserID) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.getId());
            String ids = order.getProducts().stream()
                    .map(produs -> String.valueOf(produs.getId()))
                    .collect(Collectors.joining(","));
            statement.setString(2, ids);
            statement.setString(3, formatter.format(order.getDate()));
            statement.setString(4, order.getAddress());
            statement.setString(5, order.getStatus());
            statement.setInt(6, order.getUserID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepoException("Could not create order");
        }
        loadData();
    }

    //                            GET FUNCTIONS

    @Override
    public List<T> getAll() {
        loadData();
        try {
            if (Product.class.isAssignableFrom(entityClass)) {
                return (List<T>) getAllProducts();
            } else if (Order.class.isAssignableFrom(entityClass)) {
                return (List<T>) getAllOrders();
            } else if (User.class.isAssignableFrom(entityClass)) {
                return(List<T>) getAllUsers();
            }else if(Admin.class.isAssignableFrom(entityClass)){
                return (List<T>) getAllAdmins();
            }
            else {
                throw new RepoException("Unsupported entity type");
            }
        } catch (RepoException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Product> getAllProducts() throws RepoException {
        loadData();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String category = rs.getString("Category");
                String name = rs.getString("Name");
                int price = rs.getInt("Price");
                Product product = new Product(category, name, price);
                product.id =rs.getInt("ID");
                products.add(product);
            }
        } catch (SQLException e) {
            throw new RepoException(e.getMessage());
        }
        return products;
    }

    public List<User> getAllUsers() throws RepoException {
        loadData();
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String email = rs.getString("Email");
                String password = rs.getString("Password");
                User user = new User(firstName,lastName,email,password);
                user.id =rs.getInt("ID");
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RepoException(e.getMessage());
        }
        return users;
    }

    public List<Admin> getAllAdmins() throws RepoException {
        loadData();
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM Admins";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String email = rs.getString("Email");
                String password = rs.getString("Password");
                Admin admin = new Admin(firstName,lastName,email,password);
                admin.id =rs.getInt("ID");
                admins.add(admin);
            }
        } catch (SQLException e) {
            throw new RepoException(e.getMessage());
        }
        return admins;
    }

    public List<Order> getAllOrders() throws RepoException {
        loadData();
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String productsString = rs.getString("Products");
                String data = rs.getString("Date");
                String adress = rs.getString("Adress");
                String status = rs.getString("Status");
                int userID = rs.getInt("UserID");
                List<Product> products = new ArrayList<>();
                String[] ids = productsString.split(",");
                for (String i : ids) {
                    int productId = Integer.parseInt(i);
                    for (Product pr : availableProducts) {
                        if (productId == pr.getId()) {
                            products.add(pr);
                        }
                    }
                }
                Order order = new Order(products, formatter.parse(data), adress, status, userID);
                order.id = rs.getInt("ID");
                orders.add(order);
            }
        } catch (SQLException | ParseException e) {
            throw new RepoException(e.getMessage());
        }
        return orders;
    }

//                                 DELETE FUNCTIONS

    public void remove(int id) throws RepoException {
        try {
            if (Product.class.isAssignableFrom(entityClass)) {
                removeProduct(id);
            } else if (Order.class.isAssignableFrom(entityClass)) {
                removeOrder(id);
            } else if (User.class.isAssignableFrom(entityClass)) {
                removeUser(id);
            } else if(Admin.class.isAssignableFrom(entityClass)) {
                removeAdmin(id);
            }else {
                throw new RepoException("Unsupported entity type");
            }
        } catch (RepoException e) {
            throw new RepoException(e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeProduct(int id) throws RepoException {
        String deleteProductSql = "DELETE FROM Products WHERE ID = ?";
        String updateOrderSql = "UPDATE Orders SET Products = REPLACE(Products, ?, '') WHERE Products LIKE ?";
        try (PreparedStatement deleteProductStmt = connection.prepareStatement(deleteProductSql);
             PreparedStatement updateOrderStmt = connection.prepareStatement(updateOrderSql)) {

            deleteProductStmt.setInt(1, id);
            int rowsAffected = deleteProductStmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RepoException("Product with ID " + id + " does not exist");
            }

            String idStr = id + ",";
            updateOrderStmt.setString(1, idStr);
            updateOrderStmt.setString(2, "%" + idStr + "%");
            updateOrderStmt.executeUpdate();

            loadData();

            updateOrderStmt.setString(1, "," + idStr);
            updateOrderStmt.setString(2, "%" + "," + idStr + "%");
            updateOrderStmt.executeUpdate();

            loadData();

            updateOrderStmt.setString(1, String.valueOf(id));
            updateOrderStmt.setString(2, "%" + id + "%");
            updateOrderStmt.executeUpdate();

            loadData();

            String findEmptyOrdersSql = "SELECT ID FROM Orders WHERE Products = '' OR Products IS NULL";
            String deleteEmptyOrdersSql = "DELETE FROM Orders WHERE ID = ?";
            try (PreparedStatement findEmptyStmt = connection.prepareStatement(findEmptyOrdersSql);
                 PreparedStatement deleteEmptyStmt = connection.prepareStatement(deleteEmptyOrdersSql);
                 ResultSet rs = findEmptyStmt.executeQuery()) {

                while (rs.next()) {
                    int orderId = rs.getInt("ID");
                    deleteEmptyStmt.setInt(1, orderId);
                    deleteEmptyStmt.executeUpdate();
                }
            }

            loadData();

        } catch (SQLException e) {
            throw new RepoException("Could not remove product");
        }
    }

    private void removeOrder(int id) throws SQLException {
        String sql = "DELETE FROM Orders WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Could not cancel order");
        }
        loadData();
    }

    private void removeUser(int id) throws SQLException {
        String sql = "DELETE FROM Users WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }catch (SQLException e) {
            throw new SQLException("Could not delete account");
        }
        loadData();
    }

    private void removeAdmin(int id) throws SQLException {
        String sql = "DELETE FROM Admins WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }catch (SQLException e) {
            throw new SQLException("Could not delete account");
        }
        loadData();
    }

    //                        UPDATE FUNCTIONS

    public void update(T item) throws RepoException {
        try {
            if (Product.class.isAssignableFrom(entityClass)) {
                updateProduct((Product) item);
            } else if (User.class.isAssignableFrom(entityClass)) {
                updateUser((User) item);
            }
            else if (Order.class.isAssignableFrom(entityClass)) {
                updateOrderStatus((Order) item);
            }
            else {
                throw new RepoException("Unsupported entity type");
            }
        } catch (RepoException e) {
            throw new RepoException(e.getMessage());
        }

    }

    private void updateProduct(Product item) throws RepoException {
        String sql = "UPDATE Products SET Category = ?,Name = ?,Price = ? WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getCategory());
            statement.setString(2, item.getName());
            statement.setInt(3, item.getPrice());
            statement.setInt(4, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepoException("Could not update product");
        }
        loadData();
    }
    private void updateOrderStatus(Order item) throws RepoException {

        String sql = "UPDATE Orders SET Status = ? WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getStatus());
            statement.setInt(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepoException("Could not update order status");
        }
        loadData();
    }

    private void updateUser(User item) throws RepoException {
        String sql = "UPDATE Users SET FirstName = ?,LastName = ?, Email=?, Password = ? WHERE ID = ?";
        List<User> users = getAllUsers();
        for(User user : users) {
            if (user.getEmail() == item.getEmail()) {
                throw new RepoException("This email address is already taken");
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getFirstName());
            statement.setString(2, item.getLastName());
            statement.setString(3, item.getEmail());
            statement.setString(4, item.getPassword());
            statement.setInt(5, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepoException("Could not update account");
        }
        loadData();

    }

}