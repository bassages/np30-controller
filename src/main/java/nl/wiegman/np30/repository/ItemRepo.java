package nl.wiegman.np30.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import nl.wiegman.np30.domain.Item;

public interface ItemRepo extends JpaRepository<Item, String> {

    @Cacheable(cacheNames = "itemsWhichAreNotContainer")
    List<Item> findByIsContainerFalse();

    List<Item> findByParentId(String parentId);
}
