/*
 * Copyright (C) 2024 Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.cnr.anac.transparency.resultaggregator.v1;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.geojson.Feature;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.anac.transparency.resultaggregator.service.AggreatorService;
import it.cnr.anac.transparency.resultaggregator.service.ResultWithGeoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Tag(
    name = "Aggregator Controller", 
    description = "Gestione delle informazioni dei risultati aggregati di validazione dei siti delle PA")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiRoutes.BASE_PATH + "/aggregator")
public class AggregatorController {

  private final AggreatorService aggregatorService;
  private final ResultWithGeoRepository aggregatorRepo;

  @Operation(
      summary = "Visualizzazione dei risultati di validazione presenti nel sistema, filtrabili "
          + "utilizzando alcuni parametri.",
          description = "Le informazioniono restituite in formato geoJson.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituita una pagina della lista risultati di validazione presenti.")
  })
  @GetMapping(ApiRoutes.LIST + "/geojson/nocache")
  public ResponseEntity<List<Feature>> resultNoCache(
      @RequestParam("workflowId") String workflowId, 
      @RequestParam("ruleName") Optional<String> ruleName) {
    log.info("Estrazione dinamica dei dati per workflowId = {}, ruleName = {}", workflowId, ruleName);
    val featureCollection = aggregatorService.aggregatedFeatureCollection(workflowId, ruleName);

    return ResponseEntity.ok(featureCollection.getFeatures());
  }

  @GetMapping(value = ApiRoutes.LIST + "/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody byte[] result(
      @RequestParam("workflowId") String workflowId, 
      @RequestParam("ruleName") String ruleName) {
    log.info("Estrazione dati per workflowId = {}, ruleName = {}", workflowId, ruleName);
    val result = aggregatorRepo.findByWorkflowIdAndRuleName(workflowId, ruleName)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("Result non trovato con workflowId = %s e ruleName = %s", 
                workflowId, ruleName)));
    return result.getGeoJson();
  }

  @GetMapping(value = ApiRoutes.LIST + "/geojson/gzip", 
      produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody byte[] resultJsonb(
      @RequestParam("workflowId") String workflowId, 
      @RequestParam("ruleName") String ruleName, HttpServletResponse response) throws IOException {
    log.info("Estrazione dati per workflowId = {}, ruleName = {}", workflowId, ruleName);
    response.setHeader("Content-Encoding", "gzip");
    val result = aggregatorRepo.findByWorkflowIdAndRuleName(workflowId, ruleName)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("Result non trovato con workflowId = %s e ruleName = %s", 
                workflowId, ruleName)));
    return aggregatorService.gzip(result.getGeoJson());
  }

  @Operation(
      summary = "Creazione di un risultato aggregato geojson di validazione, un geoJson per ogni singola regola.",
      description = "Questa è la creazione di risultato aggregato di validazione per ogni regola.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Risultato creato correttamente."),
      @ApiResponse(responseCode = "400", description = "Validazione delle informazioni obbligatorie fallita.",
      content = @Content)
  })
  @PutMapping(ApiRoutes.CREATE + "/geojson")
  public ResponseEntity<Void> create(
      @RequestParam("workflowId") String workflowId,
      @RequestParam("ruleName") Optional<String> ruleName) throws JsonProcessingException {
    val featureCollections = aggregatorService.aggregatedFeatureCollections(workflowId, ruleName);
    for (String featureRuleName : featureCollections.keySet()) {
      aggregatorService.save(workflowId, Optional.of(featureRuleName), featureCollections.get(featureRuleName));
    }
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Eliminazione di un risultato aggregato di validazione.",
      description = "Eliminazione definitiva un risultato aggregato di validazione.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Risultato eliminato correttamente")
  })
  @DeleteMapping(ApiRoutes.DELETE + "/geojson")
  ResponseEntity<Void> delete(
      @NotNull @PathVariable("id") Long id) {
    log.debug("ResultController::delete id = {}", id);
    val result = aggregatorRepo.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Result non trovato con id = " + id));
    aggregatorRepo.delete(result);
    log.info("Eliminato definitivamente result {}", result);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Eliminazione di un risultato aggregato di validazione.",
      description = "Eliminazione definitiva un risultato aggregato di validazione.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Risultato eliminato correttamente")
  })
  @DeleteMapping(ApiRoutes.LIST + "/geojson")
  ResponseEntity<Void> deleteByWorkflowIdAndRuleName(
      @RequestParam("workflowId") String workflowId, 
      @RequestParam("ruleName") Optional<String> ruleName) {
    log.debug("ResultController::delete by workflowId {} and ruleName {}", workflowId, ruleName);
    if (ruleName.isPresent()) {
      val result = aggregatorRepo.findByWorkflowIdAndRuleName(workflowId, ruleName.get())
          .orElseThrow(() -> new EntityNotFoundException(
              String.format("Result non trovato con workflowId = %s e ruleName = %s", 
                  workflowId, ruleName)));
      aggregatorRepo.delete(result);
      log.info("Eliminato definitivamente result {}", result);
    } else {
      val results = aggregatorRepo.findIdsByWorkflowId(workflowId);
      if (results.isEmpty()) {
        log.info("Nessun result trovato con workflowId = {}", workflowId);
      } else {
        results.stream().forEach(result -> {
          aggregatorRepo.deleteById(result);
          log.info("Eliminato definitivamente result {}", result);
        });
      }
    }
    return ResponseEntity.ok().build();
  }
}
