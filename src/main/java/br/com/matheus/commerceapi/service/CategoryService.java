package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.category.CreateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.response.Category.CategoryResponseDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.exception.NameAlreadyExistsException;
import br.com.matheus.commerceapi.repository.CategoryRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ValidationUtils validationUtils;
    private final CategoryRepository categoryRepository;

    public CategoryResponseDto createCategory(CreateCategoryRequestDto request) {

        Map<String, String> fields = new HashMap<>();
        fields.put("displayName", request.displayName());
        fields.put("description", request.description());

        validationUtils.validateRequired(fields);

        String uniqueName = adaptName(request.displayName());

        validateUniqueName(uniqueName);

        Category category = Category.builder()
                .name(uniqueName)
                .displayName(request.displayName())
                .description(request.description())
                .active(true)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponseDto.fromEntity(category);

    }

    public Page<CategoryResponseDto> getCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.map(CategoryResponseDto::fromEntity);
    }

    public Page<CategoryResponseDto> searchCategories(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getCategories(pageable);
        }

        Page<Category> categories = categoryRepository
                .findByDisplayNameContaining(searchTerm, pageable);

        return categories.map(CategoryResponseDto::fromEntity);
    }

    private void validateUniqueName(String uniqueName){
        if(categoryRepository.existsByName(uniqueName)) throw new NameAlreadyExistsException(uniqueName);
    }

    private String adaptName(String displayName){
        String uniqueName = displayName.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase();

        return uniqueName.replaceAll("_+", "_");
    }

}
