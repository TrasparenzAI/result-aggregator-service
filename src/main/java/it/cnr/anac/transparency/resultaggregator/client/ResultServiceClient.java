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
package it.cnr.anac.transparency.resultaggregator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.cnr.anac.transparency.resultaggregator.client.dto.ResultShowDto;

/**
 * Client feign per il prelevamento delle informazioni dei risultati dal result-service.
 *
 * @author Cristian Lucchesi
 */
@FeignClient(name = "result-service-client", url = "${transparency.result-service.url}")
public interface ResultServiceClient {

  @GetMapping("/v1/results?noCache=true&sort=id")
  abstract Page<ResultShowDto> results(
      @RequestParam(name="workflowId") String workflowId, 
      @RequestParam(name="ruleName") String ruleName,
      @RequestParam(name="page") int page, @RequestParam(name="size") int size);
}
