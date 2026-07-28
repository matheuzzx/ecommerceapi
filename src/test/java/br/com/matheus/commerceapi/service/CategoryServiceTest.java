package br.com.matheus.commerceapi.service;

import br.com.matheus.commerceapi.dto.request.category.CreateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.request.category.UpdateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.response.category.CategoryResponseDto;
import br.com.matheus.commerceapi.entity.Category;
import br.com.matheus.commerceapi.exception.NameAlreadyExistsException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.repository.CategoryRepository;
import br.com.matheus.commerceapi.utils.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private ValidationUtils validationUtils;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private static final Long CATEGORY_ID = 1L;
    private static final String DISPLAY_NAME = "Eletrônicos";
    private static final String DESCRIPTION = "Category description";
    private static final String ADAPTED_NAME = "eletronicos";

    private Category createCategory() {
        return Category.builder()
                .id(CATEGORY_ID)
                .name(ADAPTED_NAME)
                .displayName(DISPLAY_NAME)
                .description(DESCRIPTION)
                .active(true)
                .build();
    }

    private CreateCategoryRequestDto createCategoryRequest() {
        return new CreateCategoryRequestDto(DISPLAY_NAME, DESCRIPTION);
    }

    // ============================================
    // CREATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategorySuccessfully() {
            CreateCategoryRequestDto request = createCategoryRequest();
            Category savedCategory = createCategory();

            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryRepository.existsByName(ADAPTED_NAME)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryResponseDto result = categoryService.createCategory(request);

            assertThat(result.id()).isEqualTo(CATEGORY_ID);
            assertThat(result.displayName()).isEqualTo(DISPLAY_NAME);
            assertThat(result.description()).isEqualTo(DESCRIPTION);
            assertThat(result.active()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when name already exists")
        void shouldThrowExceptionWhenNameAlreadyExists() {
            CreateCategoryRequestDto request = createCategoryRequest();

            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryRepository.existsByName(ADAPTED_NAME)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(request))
                    .isInstanceOf(NameAlreadyExistsException.class)
                    .hasMessageContaining(ADAPTED_NAME);

            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should convert display name to slug correctly")
        void shouldConvertDisplayNameToSlugCorrectly() {
            CreateCategoryRequestDto request = new CreateCategoryRequestDto("  Novo Nome  ", DESCRIPTION);
            Category savedCategory = Category.builder()
                    .id(CATEGORY_ID)
                    .name("novo_nome")
                    .displayName("Novo Nome")
                    .description(DESCRIPTION)
                    .active(true)
                    .build();

            doNothing().when(validationUtils).validateRequiredString(any());
            when(categoryRepository.existsByName("novo_nome")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryResponseDto result = categoryService.createCategory(request);

            assertThat(result.displayName()).isEqualTo("Novo Nome");

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("novo_nome");
            assertThat(captor.getValue().getDisplayName()).isEqualTo("Novo Nome");
        }
    }

    // ============================================
    // GET CATEGORIES TESTS
    // ============================================

    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should return paginated categories")
        void shouldReturnPaginatedCategories() {
            Pageable pageable = PageRequest.of(0, 20);
            Category category = createCategory();
            Page<Category> categoryPage = new PageImpl<>(List.of(category));

            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

            Page<CategoryResponseDto> result = categoryService.getCategories(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(CATEGORY_ID);
        }
    }

    // ============================================
    // UPDATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update all fields successfully")
        void shouldUpdateAllFieldsSuccessfully() {
            Category category = createCategory();
            UpdateCategoryRequestDto request = new UpdateCategoryRequestDto("Novo Nome", "New description");

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndIdNot("novo_nome", CATEGORY_ID)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            CategoryResponseDto result = categoryService.updateCategory(CATEGORY_ID, request);

            assertThat(result.displayName()).isEqualTo("Novo Nome");
            assertThat(category.getName()).isEqualTo("novo_nome");
            assertThat(category.getDescription()).isEqualTo("New description");
        }

        @Test
        @DisplayName("Should keep existing name when display name is unchanged")
        void shouldKeepExistingNameWhenDisplayNameUnchanged() {
            Category category = createCategory();
            UpdateCategoryRequestDto request = new UpdateCategoryRequestDto(DISPLAY_NAME, "New description");

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryService.updateCategory(CATEGORY_ID, request);

            assertThat(category.getName()).isEqualTo(ADAPTED_NAME);
            assertThat(category.getDescription()).isEqualTo("New description");

            verify(categoryRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
        }

        @Test
        @DisplayName("Should update only description when display name is null")
        void shouldUpdateOnlyDescriptionWhenDisplayNameIsNull() {
            Category category = createCategory();
            UpdateCategoryRequestDto request = new UpdateCategoryRequestDto(null, "Only description");

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryService.updateCategory(CATEGORY_ID, request);

            assertThat(category.getDisplayName()).isEqualTo(DISPLAY_NAME);
            assertThat(category.getName()).isEqualTo(ADAPTED_NAME);
            assertThat(category.getDescription()).isEqualTo("Only description");

            verify(categoryRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
        }

        @Test
        @DisplayName("Should update only display name when description is null")
        void shouldUpdateOnlyDisplayNameWhenDescriptionIsNull() {
            Category category = createCategory();
            UpdateCategoryRequestDto request = new UpdateCategoryRequestDto("Novo Nome", null);

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndIdNot("novo_nome", CATEGORY_ID)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryService.updateCategory(CATEGORY_ID, request);

            assertThat(category.getDisplayName()).isEqualTo("Novo Nome");
            assertThat(category.getName()).isEqualTo("novo_nome");
            assertThat(category.getDescription()).isEqualTo(DESCRIPTION);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            UpdateCategoryRequestDto request = new UpdateCategoryRequestDto(DISPLAY_NAME, DESCRIPTION);

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, request))
                    .isInstanceOf(NotFoundException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when new name already exists")
        void shouldThrowExceptionWhenNewNameAlreadyExists() {
            Category category = createCategory();
            UpdateCategoryRequestDto request = new UpdateCategoryRequestDto("Outro Nome", DESCRIPTION);

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByNameAndIdNot("outro_nome", CATEGORY_ID)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, request))
                    .isInstanceOf(NameAlreadyExistsException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    // ============================================
    // DEACTIVATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Deactivate Category Tests")
    class DeactivateCategoryTests {

        @Test
        @DisplayName("Should deactivate category successfully")
        void shouldDeactivateCategorySuccessfully() {
            Category category = createCategory();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryService.deactivateCategory(CATEGORY_ID);

            assertThat(category.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deactivateCategory(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    // ============================================
    // ACTIVATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Activate Category Tests")
    class ActivateCategoryTests {

        @Test
        @DisplayName("Should activate category successfully")
        void shouldActivateCategorySuccessfully() {
            Category inactiveCategory = createCategory();
            inactiveCategory.setActive(false);

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(inactiveCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(inactiveCategory);

            categoryService.activateCategory(CATEGORY_ID);

            assertThat(inactiveCategory.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.activateCategory(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    // ============================================
    // DELETE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void shouldDeleteCategorySuccessfully() {
            Category category = createCategory();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

            categoryService.deleteCategory(CATEGORY_ID);

            verify(categoryRepository).delete(category);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(categoryRepository, never()).delete(any(Category.class));
        }
    }

    // ============================================
    // FIND CATEGORY BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("Find Category By ID Tests")
    class FindCategoryByIdTests {

        @Test
        @DisplayName("Should return category when it exists")
        void shouldReturnCategoryWhenExists() {
            Category category = createCategory();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

            Category result = categoryService.findCategoryById(CATEGORY_ID);

            assertThat(result).isEqualTo(category);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findCategoryById(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(CATEGORY_ID));
        }
    }

    // ============================================
    // FIND ACTIVE CATEGORY BY ID TESTS
    // ============================================

    @Nested
    @DisplayName("Find Active Category By ID Tests")
    class FindActiveCategoryByIdTests {

        @Test
        @DisplayName("Should return category when active")
        void shouldReturnCategoryWhenActive() {
            Category category = createCategory();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

            Category result = categoryService.findActiveCategoryById(CATEGORY_ID);

            assertThat(result).isEqualTo(category);
        }

        @Test
        @DisplayName("Should throw exception when category is inactive")
        void shouldThrowExceptionWhenInactive() {
            Category category = createCategory();
            category.setActive(false);

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

            assertThatThrownBy(() -> categoryService.findActiveCategoryById(CATEGORY_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not active");

            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenNotFound() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findActiveCategoryById(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
