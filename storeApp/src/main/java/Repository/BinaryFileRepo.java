package Repository;

import Domain.AbstractEntity;
import Domain.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BinaryFileRepo<T extends AbstractEntity> extends Repo<T> {

    private final String fileName;

    public BinaryFileRepo(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
        loadFromFile();
    }

    private void loadFromFile() throws FileNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(fileName))) {
            List<T> list = (List<T>) input.readObject();
            items.clear();
            items.addAll(list);
        } catch (IOException | ClassNotFoundException e) {
            throw new FileNotFoundException("File not found. A new one will be created.");

        }
    }


    private void saveToFile() {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(fileName))) {
            output.writeObject(new ArrayList<>(items));
        } catch (IOException e) {
            System.out.println("Error while saving file. A new one will be created.");
        }
    }

    @Override
    public void add(T item) throws RepoException, IOException {
        super.add(item);
        saveToFile();
    }

    @Override
    public void remove(int id) throws RepoException, IOException {
        super.remove(id);
        saveToFile();
    }

    @Override
    public void update(T item) throws RepoException, IOException {
        super.update(item);
        saveToFile();
    }
}

