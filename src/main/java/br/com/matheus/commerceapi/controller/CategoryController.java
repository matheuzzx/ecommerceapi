package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.category.CreateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.response.Category.CategoryResponseDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CreateCategoryRequestDto request){
        CategoryResponseDto category = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponseDto>> getCategories(
            @PageableDefault(size = 10, sort = "displayName", direction = Sort.Direction.ASC)
            Pageable pageable) {

        Page<CategoryResponseDto> categories = categoryService.getCategories(pageable);
        return ResponseEntity.ok(categories);
    }

}
