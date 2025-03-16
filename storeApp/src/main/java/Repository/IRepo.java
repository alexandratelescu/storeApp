package Repository;

import Domain.AbstractEntity;

import java.io.IOException;
import java.util.Collection;

public interface IRepo<T extends AbstractEntity> extends Iterable<T>{

    void add(T item) throws RepoException, IOException;
    void remove(int id) throws RepoException, IOException;
    void update(T item) throws RepoException, IOException;
    T findById(int id) throws RepoException;
    Collection<T> getAll();
}

