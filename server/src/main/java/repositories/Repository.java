package repositories;
import dataClasses.HumanBeing;
import specifications.base.Specification;

import java.time.ZonedDateTime;
import java.util.List;

public interface Repository<T> {
    /**
     * @param entity object to insert into collection
     */
    List<HumanBeing> insertEntity(T entity);

    List<HumanBeing> insertEntity(List<T> entities);

    List<HumanBeing> removeEntity(List<HumanBeing> humanBeings);

    /**
     * @param entity object ot update
     */
    List<T> updateEntity(T entity);

    List<T> updateEntity(List<T> entities);

    /**
     * @param specification condition
     * @return list of entities
     */
    List<HumanBeing> query(Specification<T> specification);

    /**
     * @return complete collection
     */
    List<HumanBeing> query();

    /**
     * method to save collection to file
     */
    void save();

    /**
     * @return collections initialize date
     */
    ZonedDateTime getInitDate();

    /**
     * @return entities type name
     */
    String getTypeName();

    /**
     * @return key counter
     */
    int getPrimaryKeyCounter();

    /**
     * @param key primary key
     * @return true if contains, false otherwise
     */
    boolean containsPrimaryKey(String key);

}
