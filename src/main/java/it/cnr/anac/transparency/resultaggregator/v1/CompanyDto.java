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

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CompanyDto {

  private String denominazioneEnte;
  private String codiceIpa;
  private String codiceFiscaleEnte;
  @Builder.Default
  private Map<String, Integer> validazioni = new HashMap<>();

  public static CompanyDto build(Map<String, String> attributeMap) {
    return CompanyDto.builder()
      .denominazioneEnte(attributeMap.getOrDefault(attributeMap.get("denominazioneEnte"), null))
      .codiceIpa(attributeMap.getOrDefault(attributeMap.get("codiceIpa"), null))
      .codiceFiscaleEnte(attributeMap.getOrDefault(attributeMap.get("codiceFiscaleEnte"), null))
      .build();
    
  }
}