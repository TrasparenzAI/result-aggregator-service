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
package it.cnr.anac.transparency.resultaggregator.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.Data;
import lombok.ToString;

/**
 * Data transfer object per le informazioni sui risultati di validazione.
 *
 */
@ToString
@Data
public class ResultShowDto {

  private Long id;

  private CompanyShowDto company;

  private String realUrl;

  // "/it/amministrazione-trasparente?searchterm=amministrazione+trasparente"
  private String url;

  // "amministrazione-trasparente"
  private String ruleName;

  // "Amministrazione Trasparente"
  private String term;

  // "Amministrazione trasparente"
  private String content;

  // false
  private boolean isLeaf;

  // 200
  private Integer status;

  // 5.466414
  private BigDecimal score;

  // "6d7e4bd7-a890-439d-9dc7-f9f3f515d8b5"
  private String workflowId;
  private String workflowChildId;

  //  Messaggio di errore restituito dal crawler di tipo stringa
  private String errorMessage;
  // Lunghezza in byte della pagina
  private Integer length;
  // Valore restituito dal motore delle regole, indica dove è stata trovata l'occorrenza del termine, di tipo stringa
  private String where;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  
  //Url di destinazione della risorsa web individuata
  private Optional<String> destinationUrl;
  
  public String getCodiceIpa() {
    if (company == null) {
      return null;
    }
    return company.getCodiceIpa();
  }
}