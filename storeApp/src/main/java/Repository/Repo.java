package Repository;

import Domain.AbstractEntity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Repo<T extends AbstractEntity> implements IRepo<T> {

    protected  List<T> items = new ArrayList<>();

    @Override
    public void add(T item) throws RepoException, IOException {
        if(findById(item.getId()) != null) {
            throw new RepoException("This ID already exists!");
        }
        items.add(item);
    }

    @Override
    public void remove(int id) throws RepoException, IOException {
        T itemToRemove = findById(id);
        if(itemToRemove == null) {
            throw new RepoException("Invalid ID!");
        }
        items.remove(itemToRemove);
    }

    @Override
    public void update(T item) throws RepoException, IOException {
        T itemToUpdate = findById(item.getId());
        if(itemToUpdate == null) {
            throw new RepoException("Invalid ID!");
        }
        remove(itemToUpdate.getId());
        add(item);
    }

    @Override
    public T findById(int id) {
        for (T item : items) {
            if (id == item.getId()) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Collection<T> getAll() {
        return new ArrayList<>(items);
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

}



