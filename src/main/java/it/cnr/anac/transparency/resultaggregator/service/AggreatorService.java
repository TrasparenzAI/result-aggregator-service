package it.cnr.anac.transparency.resultaggregator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import it.cnr.anac.transparency.resultaggregator.client.PublicSitesServiceClient;
import it.cnr.anac.transparency.resultaggregator.client.ResultServiceClient;
import it.cnr.anac.transparency.resultaggregator.client.dto.ResultShowDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AggreatorService {

  private final PublicSitesServiceClient pssClient;
  private final ResultServiceClient rsClient;

  @Value("${transparency.results-per-page}")
  private int RESULTS_PER_PAGE;

  @Value("${transparency.results-default-rule-name}")
  private String DEFAULT_RULE_NAME;

  public FeatureCollection aggregatedFeatureCollection(String workflowId, Optional<String> ruleName) {
    log.info("Sto per prelevare le features dal public-site-service");
    val featureCollection = pssClient.geoJson();
    log.info("Prelevate {} features dal public-site-service", featureCollection.getFeatures().size());
    Page<ResultShowDto> resultPage = null;
    int page = 0;
    List<ResultShowDto> results = new ArrayList<>();
    while(resultPage == null || !resultPage.isLast()) {
      resultPage = rsClient.results(workflowId, ruleName.orElse(DEFAULT_RULE_NAME), page, RESULTS_PER_PAGE);
      results.addAll(resultPage.getContent());
      log.info("Prelevata la pagina {} dal result-service, presenti {} risultati, isLast = {}", 
          page, resultPage.getContent().size(), resultPage.isLast());
      page++;
    }
    Map<String, ResultShowDto> resultsMap = 
        results.stream().collect(
            Collectors.toMap(ResultShowDto::getCodiceIpa, Function.identity(),
                (result1, result2) -> {
                  if (result1.getStatus().equals(result2.getStatus())) {
                    log.info("Trovato risultato duplicato ma uguale per workflowId = {}, ruleName = {}, "
                        + "codiceIpa = {}, risultato = {}", workflowId, ruleName.orElse(DEFAULT_RULE_NAME),
                        result1.getCodiceIpa(), result2.getStatus());
                  } else {
                    log.warn("Trovato risultato duplicato per workflowId = {}, ruleName = {}, "
                        + "codiceIpa = {}, risultato_1 = {}, risultato_2 = {}", 
                        workflowId, ruleName.orElse(DEFAULT_RULE_NAME),
                        result1.getCodiceIpa(), result1.getStatus(), result2.getStatus());
                  }
                  return result1;
              }));
    return featureCollection;
  }
}
