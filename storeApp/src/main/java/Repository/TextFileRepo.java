package Repository;

import Domain.AbstractEntity;
import java.io.*;

public abstract class TextFileRepo<T extends AbstractEntity> extends Repo<T> {

    private final String fileName;

    public TextFileRepo(String fileName) throws IOException{
        this.fileName = fileName;
        loadFromFile();
    }

    public void loadFromFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                T entity = createEntityFromCsv(line);
                items.add(entity);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found. A new one will be created.");
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void saveToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (T object : items) {
                writer.write(toCsv(object));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
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

    public abstract T createEntityFromCsv(String csvLine);

    protected abstract String toCsv(T entity);
}


