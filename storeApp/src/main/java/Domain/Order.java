package Domain;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Order extends AbstractEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private List<Product> products;
    private Date date;
    private String address;
    private String status;
    private int userID;

    public Order(List<Product> products, Date date, String address, String status, int userID) {
        super();
        this.products = products;
        this.date = date;
        this.address = address;
        this.status = status;
        this.userID = userID;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAdress(String adress) {
        this.address = adress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getUserID() {
        return userID;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return "Order {" +
                "id = " + id +
                ", products = " + products +
                ", date = " + sdf.format(date) +
                ", adress = " + address +
                ", status = " + status +
                ", userID = " + userID +
                '}';
    }

    public String toCsv() {
        String produseCsv = products.stream()
                .map(p -> p.getId() + "-" + p.getCategory() + "-" + p.getName() + "-" + p.getPrice())
                .collect(Collectors.joining(" & "));
        return getId() + " , " + produseCsv + " , " + date.getTime() + " , " + address + " , " + status + " , " + userID;
    }

    public static Order fromCsv(String csv) {
        String[] parts = csv.split(" , ");
        int id = Integer.parseInt(parts[0]);
        List<Product> products = new ArrayList<>();
        for (String productPart : parts[1].split(" & ")) {
            String[] productData = productPart.split("-");
            String productCategory = productData[1];
            String productName = productData[2];
            int productPrice = Integer.parseInt(productData[3]);
            products.add(new Product(productCategory, productName, productPrice));
        }
        Date date = new Date(Long.parseLong(parts[2]));
        String productAdress = parts[3];
        String productStatus = parts[4];
        int userID = Integer.parseInt(parts[5]);
        Order order = new Order(products, date, productAdress, productStatus, userID);
        order.id = id;
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
