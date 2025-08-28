package org.gridsuite.network.map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.ElementType;
import org.gridsuite.network.map.services.SchemaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/" + NetworkMapApi.API_VERSION + "/schemas")
@Tag(name = "Network map server - Schemas")
@AllArgsConstructor
public class SchemaController {
    public static final String APPLICATION_JSON_SCHEMA_VALUE = "application/schema+json";
    private final SchemaService schemaService;

    @GetMapping(value = "/{elementType}/{infoType}", produces = APPLICATION_JSON_SCHEMA_VALUE)
    @Operation(summary = "Get network elements")
    @ApiResponse(responseCode = "200", description = "Elements description")
    public String getElementSchema(@Parameter(description = "Element type") @PathVariable(name = "elementType") ElementType elementType,
                                   @Parameter(description = "Info type") @PathVariable(name = "infoType") InfoType infoType) {
        try {
            return schemaService.getSchema(elementType, infoType);
        } catch (final UnsupportedOperationException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "The view " + infoType + " for " + elementType + " type is not available yet.");
        }
    }
}
