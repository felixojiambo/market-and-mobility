package com.cymelle.app.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(
            String name,
            String category,
            Pageable pageable
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIdInForUpdate(@Param("ids") List<Long> ids);
}
