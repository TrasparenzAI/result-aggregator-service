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
package it.cnr.anac.transparency.resultaggregator.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.anac.transparency.resultaggregator.service.AggreatorService;
import it.cnr.anac.transparency.resultaggregator.v1.ApiRoutes;
import lombok.RequiredArgsConstructor;
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

  @Operation(
      summary = "Visualizzazione dei risultati di validazione presenti nel sistema, filtrabili "
          + "utilizzando alcuni parametri.",
      description = "Le informazioni sono restituite in formato geoJson.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", 
          description = "Restituita una pagina della lista risultati di validazione presenti.")
  })
  @GetMapping(ApiRoutes.LIST)
  public ResponseEntity<Void> result(
      @RequestParam("workflowId") String workflowId, 
      @RequestParam("ruleName") Optional<String> ruleName) {
    log.info("Estrazione dati per workflowId = {}, ruleName = {}", workflowId, ruleName);
    aggregatorService.aggregatedFeatureCollection(workflowId, ruleName);
    return ResponseEntity.ok().build();
  }

}
