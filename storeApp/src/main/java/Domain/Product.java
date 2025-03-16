package Domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Product extends AbstractEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private String category;
    private String name;
    private int price;

    public Product(String category, String name, int price) {
        super(); // id este generat automat în AbstractEntity
        this.category = category;
        this.name = name;
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product {" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    public String toCsv() {
        return getId() + "-" + category + "-" + name + "-" + price;
    }

    public static Product fromCsv(String csv) {
        String[] parts = csv.split("-");
        int id = Integer.parseInt(parts[0]);
        String category = parts[1];
        String name = parts[2];
        int price = Integer.parseInt(parts[3]);
        Product product = new Product(category, name, price);
        product.id = id;
        return product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
