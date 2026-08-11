package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.store.CreateStoreRequestDto;
import br.com.matheus.commerceapi.dto.request.store.UpdateStoreRequestDto;
import br.com.matheus.commerceapi.dto.response.store.StoreResponseDto;
import br.com.matheus.commerceapi.handler.dto.ErrorResponse;
import br.com.matheus.commerceapi.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Stores", description = "Store management for STOREOWNER and ADMIN roles")
public interface StoreApi {

    @Operation(summary = "Register a store",
            description = "Creates a store. Only allowed for STOREOWNER users that do not own a store yet.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Store created",
                    content = @Content(schema = @Schema(implementation = StoreResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload or slug",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Owner already has a store or slug in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<StoreResponseDto> register(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @RequestBody @Valid CreateStoreRequestDto request);

    @Operation(summary = "Get a store", description = "Returns a store. STOREOWNER can only access their own store.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store returned",
                    content = @Content(schema = @Schema(implementation = StoreResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Store not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<StoreResponseDto> getStore(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long storeId);

    @Operation(summary = "Update a store", description = "Updates one of the owner's stores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Store updated",
                    content = @Content(schema = @Schema(implementation = StoreResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Store not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slug already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<StoreResponseDto> updateStore(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long storeId,
            @RequestBody @Valid UpdateStoreRequestDto request);

    @Operation(summary = "Delete a store",
            description = "Deletes a store. STOREOWNER can only delete their own store.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Store deleted"),
            @ApiResponse(responseCode = "404", description = "Store not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> deleteStore(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long storeId);
}
