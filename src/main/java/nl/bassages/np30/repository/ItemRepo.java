package nl.bassages.np30.repository;

import java.util.List;

import nl.bassages.np30.domain.Item;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepo extends JpaRepository<Item, String> {

    @Cacheable(cacheNames = "itemsWhichAreNotContainer")
    List<Item> findByIsContainerFalse();

    List<Item> findByParentId(String parentId);
}
