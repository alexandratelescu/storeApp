package Service;

import Domain.User;
import Repository.IRepo;
import Repository.RepoException;
import java.io.IOException;
import java.util.Collection;

public class userService {
    private static IRepo<User> usersRepo;

    public userService(IRepo<User> usersRepo) {
        this.usersRepo = usersRepo;
    }

    public static void addUser(User user) throws RepoException, IOException {
        usersRepo.add(user);
    }

    public void removeUser(int id) throws RepoException, IOException {
        usersRepo.remove(id);
    }

    public void updateUser(User user) throws RepoException, IOException {
        usersRepo.update(user);
    }

    public User getUserById(int id) throws RepoException {
        User user = usersRepo.findById(id);
        if (user == null) {
            throw new RepoException("There is no user with this ID.");
        }
        return user;
    }


    public static Collection<User> getAllUsers() {
        Collection<User> users =  usersRepo.getAll();
        return users;
    }

    public User findByEmailAndPassword(String email, String password) {
        for (User user : usersRepo.getAll()) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

}
