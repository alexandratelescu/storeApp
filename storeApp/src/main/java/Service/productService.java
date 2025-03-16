package Service;

import Domain.Product;
import Repository.IRepo;
import Repository.RepoException;
import java.io.IOException;
import java.util.Collection;

public class productService {

    private static IRepo<Product> productsRepo;

    public productService(IRepo<Product> productsRepo) {
        this.productsRepo = productsRepo;
    }

    public static void addProduct(Product product) throws RepoException, IOException {
        productsRepo.add(product);
    }

    public void removeProduct(int id) throws RepoException, IOException {
        productsRepo.remove(id);
    }

    public void updateProduct(Product product) throws RepoException, IOException {
        productsRepo.update(product);
    }

    public Product getProductById(int id) throws RepoException {
        Product product = productsRepo.findById(id);
        if (product == null) {
            throw new RepoException("There is no product with this ID.");
        }
        return product;
    }


    public static Collection<Product> getAllProducts() {
        Collection<Product> products =  productsRepo.getAll();
        return products;
    }
}


