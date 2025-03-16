package Service;

import Domain.Order;
import Domain.Product;
import Repository.IRepo;
import Repository.RepoException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class orderService {

    private static IRepo<Order> ordersRepo;

    public orderService(IRepo<Order> ordersRepo) {
        this.ordersRepo = ordersRepo;
    }

    public static void addOrder(Order order) throws RepoException, IOException {
        ordersRepo.add(order);
    }

    public static void removeOrder(int id) throws RepoException, IOException {
        ordersRepo.remove(id);
    }

    public static void updateOrder(Order order) throws RepoException, IOException {
        ordersRepo.update(order);
    }

    public static Order getOrderById(int id) throws RepoException {
        Order order = ordersRepo.findById(id);
        return order;
    }

    public static Collection<Order> getAllOrders() {
        Collection<Order> orders = ordersRepo.getAll();
        return orders;
    }

    // Ordered products per category (the number of ordered products from each category)
    public List<Map.Entry<String, Long>> orderedProductsPerCategory(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getProducts().stream())
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()))
                .entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .collect(Collectors.toList());
    }

    // Most profitable months of the year (the number of orders and the total revenue per month)
    public List<Map.Entry<Integer, Map<String, Double>>> mostProfitableMonths(List<Order> orders) {

        Map<Integer, List<Order>> ordersPerMonth = orders.stream()
                .collect(Collectors.groupingBy(order -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(order.getDate());
                    return calendar.get(Calendar.MONTH) + 1;
                }));

        List<Map.Entry<Integer, Map<String, Double>>> monthlyReport = ordersPerMonth.entrySet().stream()
                .map(entry -> {
                    int luna = entry.getKey();
                    List<Order> monthlyOrders = entry.getValue();
                    long numberOfOrders = monthlyOrders.size();
                    double totalRevenue = monthlyOrders.stream()
                            .flatMap(order -> order.getProducts().stream())
                            .mapToDouble(Product::getPrice)
                            .sum();
                    Map<String, Double> monthDetails = new HashMap<>();
                    monthDetails.put("Number of orders", (double) numberOfOrders);
                    monthDetails.put("Total revenue", totalRevenue);

                    return new AbstractMap.SimpleEntry<>(luna, monthDetails);
                })
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().get("Total revenue"), entry1.getValue().get("Total revenue")))
                .collect(Collectors.toList());

        return monthlyReport;
    }

    // Products sorted by revenue (based on the total price)
    public List<Map.Entry<Product, Long>> productsSortedByRevenue(List<Order> orders) {
        Map<Product, Long> orderedProducts = orders.stream()
                .flatMap(order -> order.getProducts().stream())
                .collect(Collectors.groupingBy(prod -> prod, Collectors.counting()));

        return orderedProducts.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getKey().getPrice() * entry.getValue()))
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .collect(Collectors.toList());
    }

    public static Collection<Order> getOrdersByUserId(int userId) throws RepoException {
        Collection<Order> orders = ordersRepo.getAll();
        Collection<Order> userOrders = orders.stream()
                .filter(order -> order.getUserID() == userId)
                .collect(Collectors.toList());

        return userOrders;
    }

}
