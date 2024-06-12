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
package it.cnr.anac.transparency.resultaggregator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.cnr.anac.transparency.resultaggregator.client.PublicSitesServiceClient;
import it.cnr.anac.transparency.resultaggregator.client.ResultServiceClient;
import it.cnr.anac.transparency.resultaggregator.client.dto.ResultShowDto;
import it.cnr.anac.transparency.resultaggregator.models.ResultWithGeo;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AggreatorService {

  private final PublicSitesServiceClient pssClient;
  private final ResultServiceClient rsClient;
  private final ResultWithGeoRepository repo;

  @Value("${transparency.results-per-page}")
  private int RESULTS_PER_PAGE;

  @Value("${transparency.results-default-rule-name}")
  private String DEFAULT_RULE_NAME;

  public FeatureCollection aggregatedFeatureCollection(String workflowId, Optional<String> ruleName) {
    log.info("Sto per prelevare le features dal public-site-service");
    val featureCollection = pssClient.geoJson();
    log.info("Prelevate {} features dal public-site-service", featureCollection.getFeatures().size());

    List<ResultShowDto> results = getResults(workflowId, ruleName);
    log.info("Prelevati {} risultati per workflowId = {}, ruleName = {}", results.size(), workflowId, ruleName);

    //Risultati indicizzati per codiceIpa
    Map<String, List<ResultShowDto>> resultsMap = getResultsMap(results);

    featureCollection.forEach(feature -> {
      @SuppressWarnings("unchecked")
      ArrayList<Map<String, Object>> companies = 
      (ArrayList<Map<String, Object>>) feature.getProperties().get("companies");
      companies.stream().forEach(company -> {
        List<ResultShowDto> companyResults = resultsMap.get(company.get("codiceIpa"));
        if (companyResults != null) {
          val validazioniBuilder = ImmutableMap.<String, Integer>builder();
          companyResults.forEach(r -> {
            validazioniBuilder.put(r.getRuleName(), r.getStatus());
          });
          company.put("validazioni", validazioniBuilder.build());
        } else {
          log.warn("Risultati di validazione per codiceIpa = {} non trovati", company.get("codiceIpa"));
        }
      });
    });

    return featureCollection;
  }

  private List<ResultShowDto> getResults(String workflowId, Optional<String> ruleName) {
    Page<ResultShowDto> resultPage = null;
    int page = 0;
    List<ResultShowDto> results = new ArrayList<>();
    while(resultPage == null || !resultPage.isLast()) {
      resultPage = rsClient.results(workflowId, ruleName.orElse(null), page, RESULTS_PER_PAGE);
      results.addAll(resultPage.getContent());
      log.info("Prelevata la pagina {} dal result-service con {} risultati, "
          + "presenti {} risultati totali, isLast = {}", 
          page, resultPage.getSize(), resultPage.getTotalElements(), resultPage.isLast());
      page++;
    }
    return results;
  }

  private Map<String, List<ResultShowDto>> getResultsMap(List<ResultShowDto> results) {
    Map<String, List<ResultShowDto>> resultsMap = Maps.newHashMap();
    results.stream().forEach(result -> {
      if (resultsMap.containsKey(result.getCodiceIpa())) {
        resultsMap.get(result.getCodiceIpa()).add(result);
      } else {
        resultsMap.put(result.getCodiceIpa(), Lists.newArrayList(result));
      }
    });
    return resultsMap;
  }

  public void save(String workflowId, Optional<String> ruleName, FeatureCollection featureCollection) {
    val currentCollection = repo.findByWorkflowIdAndRuleName(workflowId, ruleName.orElse(null));
    if (!currentCollection.isEmpty()) {
      repo.deleteByWorkflowIdAndRuleName(workflowId, workflowId);
    }
    val resultWithGeo = new ResultWithGeo();
    resultWithGeo.setWorkflowId(workflowId);
    resultWithGeo.setRuleName(ruleName.orElse(null));
    resultWithGeo.setGeoJson(featureCollection.toString().getBytes());
    repo.save(resultWithGeo);
  }
}