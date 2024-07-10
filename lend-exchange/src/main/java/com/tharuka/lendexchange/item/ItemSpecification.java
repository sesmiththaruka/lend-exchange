package com.tharuka.lendexchange.item;

import org.springframework.data.jpa.domain.Specification;

public class ItemSpecification {

    public static Specification<Item> withOwnerId(Integer ownerId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"), ownerId);
    }
}
