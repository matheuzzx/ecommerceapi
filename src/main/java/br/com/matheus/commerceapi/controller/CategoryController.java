package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.category.CreateCategoryRequestDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequestDto request){
        Category category = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

}
