package com.tharuka.lendexchange.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<Item, Integer>, JpaSpecificationExecutor<Item> {
    @Query("""
            SELECT item
            FROM Item item
            WHERE item.archived = false
            AND item.shareable = true
            AND item.owner.id != :userId
            """)
    Page<Item> findAllDisplayableItems(Pageable pageable, Integer userId);
}
