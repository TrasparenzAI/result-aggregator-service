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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/*
 *  Handles OAuth2 client credentials security flow using Spring Security 5x.
 * Vedi https://amithkumarg.medium.com/spring-cloud-openfeign-oauth-2-0-68f75f06836f
 */
@Configuration
public class OAuth2Config {

  /**
   * creates AuthManager in spring context for OAuth token management in InMemory cache.
   *
   * @param clientRegistrationRepository - repo to retrieve auto configured registrations in spring
   *     context.
   * @param authorizedClientService - service to fetch & refresh auth token in memory.
   * @return AuthorizedClientManager
   */
  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      final ClientRegistrationRepository clientRegistrationRepository, 
      final OAuth2AuthorizedClientService authorizedClientService) {
      return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
          clientRegistrationRepository, authorizedClientService);
  }

}