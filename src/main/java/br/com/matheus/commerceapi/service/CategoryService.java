package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.category.CreateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.request.category.UpdateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.response.Category.CategoryResponseDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.exception.NameAlreadyExistsException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.CategoryRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ValidationUtils validationUtils;
    private final CategoryRepository categoryRepository;

    public CategoryResponseDto createCategory(CreateCategoryRequestDto request) {
        log.info("📝 Creating category: {}", request.displayName());

        Map<String, String> fields = new HashMap<>();
        fields.put("displayName", request.displayName());
        fields.put("description", request.description());

        validationUtils.validateRequired(fields);

        String uniqueName = adaptName(request.displayName());
        validateUniqueName(uniqueName);

        Category category = Category.builder()
                .name(uniqueName)
                .displayName(request.displayName().trim())
                .description(request.description().trim())
                .active(true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("✅ Category created: {} (ID: {})", savedCategory.getName(), savedCategory.getId());

        return CategoryResponseDto.fromEntity(savedCategory);
    }

    public Page<CategoryResponseDto> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryResponseDto::fromEntity);
    }

    public CategoryResponseDto updateCategory(Long categoryId, UpdateCategoryRequestDto request) {
        log.info("📝 Updating category: {}", categoryId);

        Category category = getCategory(categoryId);

        if (request.displayName() != null && !request.displayName().isEmpty()) {
            String newDisplayName = request.displayName().trim();
            String newName = adaptName(newDisplayName);

            if (!category.getName().equals(newName)) {
                validateUniqueName(newName, categoryId);
                category.setName(newName);
            }

            category.setDisplayName(newDisplayName);
        }

        if (request.description() != null && !request.description().isEmpty()) {
            category.setDescription(request.description().trim());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("✅ Category updated: {} (ID: {})", updatedCategory.getName(), updatedCategory.getId());

        return CategoryResponseDto.fromEntity(updatedCategory);
    }

    public void deactivateCategory(Long categoryId){
        Category category = getCategory(categoryId);
        category.deactivate();
        categoryRepository.save(category);
    }

    public void activateCategory(Long categoryId){
        Category category = getCategory(categoryId);
        category.activate();
        categoryRepository.save(category);
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found, id: " + categoryId));
    }

    private void validateUniqueName(String uniqueName) {
        if (categoryRepository.existsByName(uniqueName)) {
            throw new NameAlreadyExistsException(uniqueName);
        }
    }

    private void validateUniqueName(String uniqueName, Long excludeId) {
        if (categoryRepository.existsByNameAndIdNot(uniqueName, excludeId)) {
            throw new NameAlreadyExistsException(uniqueName);
        }
    }

    private String adaptName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return displayName;
        }

        String normalized = Normalizer.normalize(displayName.trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        return normalized
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase()
                .replaceAll("_+", "_");
    }
}