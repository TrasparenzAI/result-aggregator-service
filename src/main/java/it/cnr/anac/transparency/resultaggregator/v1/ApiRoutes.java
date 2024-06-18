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

/**
 * Costanti utili per le definizioni delle rotte delle API.
 *
 */
public class ApiRoutes {

  private static final String ONLY_DIGITS_REGEX = "^\\d+$";
  //private static final String ALPHANUMERIC_SPECIALS_REGEX = "^\\d*[a-zA-Z\\W].*$";

  public static final String BASE_PATH = "/v1";

  public static final String ID_REGEX = "{id:" + ONLY_DIGITS_REGEX + "}";

  public static final String LIST = "";
  public static final String LIST_ALL = "/all";
  public static final String LIST_AS_CSV = "/csv";
  public static final String SHOW = "/" + ID_REGEX;
  public static final String CREATE = "";
  public static final String UPDATE = "";
  public static final String PATCH = "/patch/" + ID_REGEX;
  public static final String DELETE = "/" + ID_REGEX;

}