package com.tharuka.lendexchange.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemTransactionHistoryRepository extends JpaRepository<ItemTransactionHistory, Integer> {
    @Query("""
            SELECT
            (COUNT (*) > 0) AS isBorrowed
            FROM ItemTransactionHistory itemTransactionHistory
            WHERE itemTransactionHistory.user.id = :userId
            AND itemTransactionHistory.item.id = :itemId
            AND itemTransactionHistory.returnApproved = false
            """)
    boolean isAlreadyBorrowedByUser(@Param("itemId") Integer itemId, @Param("userId") Integer userId);

    @Query("""
            SELECT
            (COUNT (*) > 0) AS isBorrowed
            FROM ItemTransactionHistory itemTransactionHistory
            WHERE itemTransactionHistory.item.id = :itemId
            AND itemTransactionHistory.returnApproved = false
            """)
    boolean isAlreadyBorrowed(@Param("itemId") Integer itemId);

    @Query("""
            SELECT transaction
            FROM ItemTransactionHistory  transaction
            WHERE transaction.user.id = :userId
            AND transaction.item.id = :itemId
            AND transaction.returned = false
            AND transaction.returnApproved = false
            """)
    Optional<ItemTransactionHistory> findByItemIdAndUserId(@Param("itemId") Integer itemId, @Param("userId") Integer userId);

    @Query("""
            SELECT transaction
            FROM ItemTransactionHistory  transaction
            WHERE transaction.item.owner.id = :userId
            AND transaction.item.id = :itemId
            AND transaction.returned = true
            AND transaction.returnApproved = false
            """)
    Optional<ItemTransactionHistory> findByItemIdAndOwnerId(@Param("itemId") Integer itemId, @Param("userId") Integer userId);

    @Query("""
            SELECT history
            FROM ItemTransactionHistory history
            WHERE history.user.id = :userId
            """)
    Page<ItemTransactionHistory> findAllBorrowedItems(Pageable pageable, Integer userId);
    @Query("""
            SELECT history
            FROM ItemTransactionHistory history
            WHERE history.item.owner.id = :userId
            """)
    Page<ItemTransactionHistory> findAllReturnedItems(Pageable pageable, Integer userId);
}
