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
package it.cnr.anac.transparency.resultaggregator.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;

/*
 * Vedi https://amithkumarg.medium.com/spring-cloud-openfeign-oauth-2-0-68f75f06836f
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Provider {

  // https://amithkumarg.medium.com/spring-cloud-openfeign-oauth-2-0-68f75f06836f
  private static final Authentication ANONYMOUS_USER_AUTHENTICATION =
          new AnonymousAuthenticationToken(
                  "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

  private final OAuth2AuthorizedClientManager authorizedClientManager;

  public String getAuthenticationToken(final String authZServerName) {
      try {
          final OAuth2AuthorizeRequest request =
                  OAuth2AuthorizeRequest.withClientRegistrationId(authZServerName)
                          .principal(ANONYMOUS_USER_AUTHENTICATION)
                          .build();

          return authorizedClientManager.authorize(request).getAccessToken().getTokenValue();
      } catch ( RuntimeException e) {
          log.error("Si è verificato un errore durante la richiesta del token client");
          throw e;
      }
  }
}
