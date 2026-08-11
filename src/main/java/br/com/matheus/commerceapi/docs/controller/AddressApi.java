package br.com.matheus.commerceapi.docs.controller;

import br.com.matheus.commerceapi.dto.request.address.CreateAddressRequestDto;
import br.com.matheus.commerceapi.dto.request.address.UpdateAddressRequestDto;
import br.com.matheus.commerceapi.dto.response.address.AddressResponseDto;
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

import java.util.List;

@Tag(name = "Addresses", description = "Shipping address book of the authenticated user")
public interface AddressApi {

    @Operation(summary = "Create an address", description = "Adds a new shipping address to the user's address book.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address created",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<AddressResponseDto> createAddress(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @RequestBody @Valid CreateAddressRequestDto request);

    @Operation(summary = "List my addresses", description = "Returns all shipping addresses of the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addresses returned",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<List<AddressResponseDto>> getAddresses(@Parameter(hidden = true) UserDetailsImpl userDetails);

    @Operation(summary = "Update an address", description = "Updates one of the user's shipping addresses.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address updated",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Address not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<AddressResponseDto> updateAddress(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long addressId,
            @RequestBody @Valid UpdateAddressRequestDto request);

    @Operation(summary = "Delete an address", description = "Removes one of the user's shipping addresses.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address deleted"),
            @ApiResponse(responseCode = "404", description = "Address not found or not owned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<Void> deleteAddress(
            @Parameter(hidden = true) UserDetailsImpl userDetails,
            @PathVariable Long addressId);
}
