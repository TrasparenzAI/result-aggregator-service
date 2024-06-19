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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.SerializationUtils;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ObjectMapper objectMapper;

  @Value("${transparency.results-per-page}")
  private int RESULTS_PER_PAGE;

  @Value("${transparency.results-default-rule-name}")
  private String DEFAULT_RULE_NAME;

  public Set<String> getRules(List<ResultShowDto> results) {
    return results.stream().map(r -> r.getRuleName()).collect(Collectors.toSet());
  }

  public Map<String, FeatureCollection> aggregatedFeatureCollections(String workflowId) {
    val featureCollections = Maps.<String, FeatureCollection>newHashMap();
    log.debug("Sto per prelevare le features dal public-site-service");
    val featureCollection = pssClient.geoJson();
    log.info("Prelevate {} features dal public-site-service", featureCollection.getFeatures().size());

    List<ResultShowDto> results = getResults(workflowId, Optional.empty());
    log.info("Prelevati {} risultati per workflowId = {}", results.size(), workflowId);

    val rules = getRules(results);
    rules.forEach(ruleName -> {
      log.debug("Processo i dati della regola {}", ruleName);
      val ruleResults = results.stream().filter(r -> r.getRuleName().equals(ruleName)).collect(Collectors.toList());
      val ruleFeatureCollection = SerializationUtils.clone(featureCollection);
      featureCollections.put(ruleName, aggregatedFeatureCollection(workflowId, ruleFeatureCollection, ruleResults));
      log.info("Aggiunta la featureCollection per workflowId = {}, regola {}", workflowId, ruleName);
    });
    return featureCollections;
  }

  public FeatureCollection aggregatedFeatureCollection(
      String workflowId, Optional<String> ruleName) {
    log.debug("Sto per prelevare le features dal public-site-service");
    val featureCollection = pssClient.geoJson();
    log.info("Prelevate {} features dal public-site-service", featureCollection.getFeatures().size());

    List<ResultShowDto> results = getResults(workflowId, ruleName);
    log.info("Prelevati {} risultati per workflowId = {}, ruleName = {}", 
        results.size(), workflowId, ruleName);

    return aggregatedFeatureCollection(workflowId, featureCollection, results);
  }

  public FeatureCollection aggregatedFeatureCollection(
      String workflowId, 
      FeatureCollection featureCollection, List<ResultShowDto> results) {

    //Risultati indicizzati per codiceIpa
    Map<String, List<ResultShowDto>> resultsMap = getResultsMap(results);

    log.info("resultMap.keys() = {}", resultsMap.keySet());
    featureCollection.forEach(feature -> {
      @SuppressWarnings("unchecked")
      ArrayList<Map<String, Object>> companies = 
      (ArrayList<Map<String, Object>>) feature.getProperties().get("companies");
      for (Map<String, Object> company : companies) {
        List<ResultShowDto> companyResults = resultsMap.get(company.get("codiceIpa"));
        if (companyResults != null) {
          val validazioniBuilder = new HashMap<String, Integer>();
          companyResults.forEach(r -> {
            validazioniBuilder.put(r.getRuleName(), r.getStatus());
          });
          company.put("validazioni", validazioniBuilder);
        } else {
          log.info("Risultati di validazione per codiceIpa = {} non trovati", company.get("codiceIpa"));
        }
      }
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
    Map<String, List<ResultShowDto>> resultsMap = new HashMap<>();
    results.stream().forEach(result -> {
      if (resultsMap.containsKey(result.getCodiceIpa())) {
        resultsMap.get(result.getCodiceIpa()).add(result);
      } else {
        resultsMap.put(result.getCodiceIpa(), Lists.newArrayList(result));
      }
    });
    return resultsMap;
  }

  public void save(String workflowId, Optional<String> ruleName, FeatureCollection featureCollection) throws JsonProcessingException {
    val currentCollection = repo.findByWorkflowIdAndRuleName(workflowId, ruleName.orElse(null));
    if (!currentCollection.isEmpty()) {
      repo.deleteByWorkflowIdAndRuleName(workflowId, ruleName.orElse(null));
    }
    val resultWithGeo = new ResultWithGeo();
    resultWithGeo.setWorkflowId(workflowId);
    resultWithGeo.setRuleName(ruleName.orElse(null));
    resultWithGeo.setGeoJson(toJson(featureCollection));
    repo.save(resultWithGeo);
  }

  public byte[] toJson(FeatureCollection featureCollection) throws JsonProcessingException {
    return objectMapper.writeValueAsBytes(featureCollection);
  }

  private static final int BUFFER_SIZE = 512;

  public void gzip(InputStream is, OutputStream os) throws IOException {
    GZIPOutputStream gzipOs = new GZIPOutputStream(os);
    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead = 0;
    while ((bytesRead = is.read(buffer)) > -1) {
      gzipOs.write(buffer, 0, bytesRead);
    }
    gzipOs.close();
  }

  public byte[] gzip(byte[] byteArray) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    gzip(new ByteArrayInputStream(byteArray), os);
    byte[] compressed = os.toByteArray();
    return compressed;
  }
}