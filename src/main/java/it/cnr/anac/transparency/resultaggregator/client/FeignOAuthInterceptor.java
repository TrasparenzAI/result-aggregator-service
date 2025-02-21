/*
 * Copyright (C) 2025 Consiglio Nazionale delle Ricerche
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

import com.google.common.base.Strings;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import it.cnr.anac.transparency.resultaggregator.security.OAuth2Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignOAuthInterceptor implements RequestInterceptor {

  private final OAuth2Provider oAuth2Provider;
  private final String AUTHZ_SERVER_NAME = "oidc";

  @Override
  public void apply(RequestTemplate requestTemplate) {

    final String token = oAuth2Provider.getAuthenticationToken(AUTHZ_SERVER_NAME);
    if (! Strings.isNullOrEmpty(token)) {
      log.info("Ottenuto token client_credentials grant: {}", token);
      requestTemplate.header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
    } else {
      log.error("Impossibile determinare il token client-credentials-grant");
    }
  }
}