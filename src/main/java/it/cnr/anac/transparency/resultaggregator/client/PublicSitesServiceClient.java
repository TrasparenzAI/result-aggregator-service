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

import org.geojson.FeatureCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Client feign per il prelevamento delle informazioni geografiche dal public-sites-service.
 *
 * @author Cristian Lucchesi
 */
@FeignClient(name = "public-sites-service-client", url = "${transparency.public-sites-service.url}")
public interface PublicSitesServiceClient {

  @GetMapping("/v1/geo/geojson")
  abstract FeatureCollection geoJson();
}
