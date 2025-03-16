package Domain;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractEntity implements Serializable {
    private static final AtomicInteger idGenerator = new AtomicInteger(1);
    public int id;

    public AbstractEntity() {
        this.id = idGenerator.getAndIncrement();
    }

    public int getId() {
        return id;
    }
}



