package nl.wiegman.np30.repository;

import nl.wiegman.np30.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepo extends JpaRepository<Item, String> {

    List<Item> findByIsContainerFalse();

    List<Item> findByParentId(String parentId);

    List<Item> findByParentIdAndIsContainerTrue(String parentId);
}
