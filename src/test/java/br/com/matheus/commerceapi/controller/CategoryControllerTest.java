package br.com.matheus.commerceapi.controller;

import br.com.matheus.commerceapi.dto.request.category.CreateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.request.category.UpdateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.response.category.CategoryResponseDto;
import br.com.matheus.commerceapi.exception.NameAlreadyExistsException;
import br.com.matheus.commerceapi.exception.NotFoundException;
import br.com.matheus.commerceapi.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private static final Long CATEGORY_ID = 1L;
    private static final String DISPLAY_NAME = "Eletrônicos";
    private static final String DESCRIPTION = "Category description";

    private CreateCategoryRequestDto createRequest() {
        return new CreateCategoryRequestDto(DISPLAY_NAME, DESCRIPTION);
    }

    private UpdateCategoryRequestDto updateRequest() {
        return new UpdateCategoryRequestDto(DISPLAY_NAME, DESCRIPTION);
    }

    private CategoryResponseDto createResponse() {
        return new CategoryResponseDto(CATEGORY_ID, "eletronicos", DISPLAY_NAME, DESCRIPTION, true);
    }

    // ============================================
    // CREATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Create Category Tests")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category and return 201")
        void shouldCreateCategoryAndReturnCreated() {
            CreateCategoryRequestDto request = createRequest();
            CategoryResponseDto response = createResponse();

            when(categoryService.createCategory(request)).thenReturn(response);

            ResponseEntity<CategoryResponseDto> result = categoryController.createCategory(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(response);
        }

        @Test
        @DisplayName("Should propagate exception when name already exists")
        void shouldPropagateExceptionWhenNameAlreadyExists() {
            CreateCategoryRequestDto request = createRequest();

            when(categoryService.createCategory(request)).thenThrow(new NameAlreadyExistsException(DISPLAY_NAME));

            assertThatThrownBy(() -> categoryController.createCategory(request))
                    .isInstanceOf(NameAlreadyExistsException.class);
        }
    }

    // ============================================
    // GET CATEGORIES TESTS
    // ============================================

    @Nested
    @DisplayName("Get Categories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("Should return paginated categories with 200")
        void shouldReturnPaginatedCategories() {
            CategoryResponseDto response = createResponse();
            Page<CategoryResponseDto> page = new PageImpl<>(List.of(response));
            PageRequest pageable = PageRequest.of(0, 10);

            when(categoryService.getCategories(pageable)).thenReturn(page);

            ResponseEntity<Page<CategoryResponseDto>> result = categoryController.getCategories(pageable);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getContent()).hasSize(1);
        }
    }

    // ============================================
    // UPDATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Update Category Tests")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category and return 200")
        void shouldUpdateCategoryAndReturnOk() {
            UpdateCategoryRequestDto request = updateRequest();
            CategoryResponseDto response = createResponse();

            when(categoryService.updateCategory(CATEGORY_ID, request)).thenReturn(response);

            ResponseEntity<CategoryResponseDto> result = categoryController.updateCategory(request, CATEGORY_ID);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(response);
        }

        @Test
        @DisplayName("Should propagate 404 when category not found")
        void shouldPropagateNotFoundWhenCategoryNotFound() {
            UpdateCategoryRequestDto request = updateRequest();

            when(categoryService.updateCategory(CATEGORY_ID, request)).thenThrow(new NotFoundException("Category not found, id: " + CATEGORY_ID));

            assertThatThrownBy(() -> categoryController.updateCategory(request, CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(String.valueOf(CATEGORY_ID));
        }
    }

    // ============================================
    // DEACTIVATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Deactivate Category Tests")
    class DeactivateCategoryTests {

        @Test
        @DisplayName("Should deactivate category and return 204")
        void shouldDeactivateCategoryAndReturnNoContent() {
            ResponseEntity<Void> result = categoryController.deactivateCategory(CATEGORY_ID);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(categoryService).deactivateCategory(CATEGORY_ID);
        }

        @Test
        @DisplayName("Should propagate exception when category not found")
        void shouldPropagateNotFoundWhenCategoryNotFound() {
            doThrow(new NotFoundException("Category not found, id: " + CATEGORY_ID))
                    .when(categoryService).deactivateCategory(CATEGORY_ID);

            assertThatThrownBy(() -> categoryController.deactivateCategory(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ============================================
    // ACTIVATE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Activate Category Tests")
    class ActivateCategoryTests {

        @Test
        @DisplayName("Should activate category and return 204")
        void shouldActivateCategoryAndReturnNoContent() {
            ResponseEntity<Void> result = categoryController.activateCategory(CATEGORY_ID);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(categoryService).activateCategory(CATEGORY_ID);
        }

        @Test
        @DisplayName("Should propagate exception when category not found")
        void shouldPropagateNotFoundWhenCategoryNotFound() {
            doThrow(new NotFoundException("Category not found, id: " + CATEGORY_ID))
                    .when(categoryService).activateCategory(CATEGORY_ID);

            assertThatThrownBy(() -> categoryController.activateCategory(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ============================================
    // DELETE CATEGORY TESTS
    // ============================================

    @Nested
    @DisplayName("Delete Category Tests")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category and return 204")
        void shouldDeleteCategoryAndReturnNoContent() {
            ResponseEntity<Void> result = categoryController.deleteCategory(CATEGORY_ID);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(categoryService).deleteCategory(CATEGORY_ID);
        }

        @Test
        @DisplayName("Should propagate exception when category not found")
        void shouldPropagateNotFoundWhenCategoryNotFound() {
            doThrow(new NotFoundException("Category not found, id: " + CATEGORY_ID))
                    .when(categoryService).deleteCategory(CATEGORY_ID);

            assertThatThrownBy(() -> categoryController.deleteCategory(CATEGORY_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
