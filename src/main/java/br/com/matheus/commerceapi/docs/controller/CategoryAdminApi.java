package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.category.CreateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.request.category.UpdateCategoryRequestDto;
import br.com.matheus.commerceapi.dto.response.category.CategoryResponseDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin - Categories", description = "Category management. Restricted to ADMIN.")
public interface CategoryAdminApi {

    @Operation(summary = "Create a category", description = "Creates a new product category.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Name or slug already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<CategoryResponseDto> createCategory(@RequestBody @Valid CreateCategoryRequestDto request);

    @Operation(summary = "List categories", description = "Returns all categories, paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories returned",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Page<CategoryResponseDto>> getCategories(Pageable pageable);

    @Operation(summary = "Update a category", description = "Updates the display name and/or slug of a category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slug already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<CategoryResponseDto> updateCategory(
            @RequestBody @Valid UpdateCategoryRequestDto request,
            @PathVariable Long categoryId);

    @Operation(summary = "Deactivate a category", description = "Makes the category no longer selectable for new products.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deactivated"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> deactivateCategory(@PathVariable Long categoryId);

    @Operation(summary = "Activate a category", description = "Makes the category selectable again.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category activated"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> activateCategory(@PathVariable Long categoryId);

    @Operation(summary = "Delete a category", description = "Deletes a category.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId);
}
