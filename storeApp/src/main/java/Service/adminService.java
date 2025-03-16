package Service;

import Domain.Admin;
import Repository.IRepo;
import Repository.RepoException;
import java.io.IOException;
import java.util.Collection;

public class adminService {
    private static IRepo<Admin> adminsRepo;

    public adminService(IRepo<Admin> adminsRepo) {
        this.adminsRepo = adminsRepo;
    }

    public static void addAdmin(Admin admin) throws RepoException, IOException {
        adminsRepo.add(admin);
    }

    public void removeAdmin(int id) throws RepoException, IOException {
        adminsRepo.remove(id);
    }

    public void updateAdmin(Admin admin) throws RepoException, IOException {
        adminsRepo.update(admin);
    }

    public Admin getAdminById(int id) throws RepoException {
        Admin admin = adminsRepo.findById(id);
        if (admin == null) {
            throw new RepoException("There is no user with this ID.");
        }
        return admin;
    }

    public Admin findByEmailAndPassword(String email, String password) {
        for (Admin admin : adminsRepo.getAll()) {
            if (admin.getEmail().equals(email) && admin.getPassword().equals(password)) {
                return admin;
            }
        }
        return null;
    }


    public static Collection<Admin> getAllAdmins() {
        Collection<Admin> admins =  adminsRepo.getAll();
        return admins;
    }
}
