package com.cymelle.app.products;

import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.products.dto.ProductCreateRequest;
import com.cymelle.app.products.dto.ProductResponse;
import com.cymelle.app.products.dto.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse create(ProductCreateRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setCategory(req.getCategory());
        productRepository.save(p);
        return ProductResponse.from(p);
    }

    public ProductResponse update(Long id, ProductUpdateRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setCategory(req.getCategory());
        productRepository.save(p);
        return ProductResponse.from(p);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public ProductResponse get(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return ProductResponse.from(p);
    }

    public Page<ProductResponse> list(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public Page<ProductResponse> search(String name, String category, Pageable pageable) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasCat = category != null && !category.isBlank();

        if (hasName && hasCat) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndCategoryIgnoreCase(name, category, pageable)
                    .map(ProductResponse::from);
        }
        if (hasName) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable).map(ProductResponse::from);
        }
        if (hasCat) {
            return productRepository.findByCategoryIgnoreCase(category, pageable).map(ProductResponse::from);
        }
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }
}
