/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.server.connect.generator.endpoints.json;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.Version;

import com.vaadin.flow.server.connect.Endpoint;
import com.vaadin.flow.server.connect.auth.AnonymousAllowed;

/**
 * This class is used for OpenApi generator test
 */
@Endpoint
public class JsonTestEndpoint {
  /**
   * Get all users
   *
   * @return list of users
   */
  public List<User> getAllUsers() {
    return Collections.emptyList();
  }

  /**
   * Get the map of user and roles
   *
   * @return map of user and roles
   */
  public Map<String, User> getAllUserRolesMap() {
    return Collections.emptyMap();
  }

  /**
   * Update a user
   *
   * @param user
   *          User to be updated
   */
  public void updateUser(User user) {
    // NO implementation
  }

  /**
   * Get number of users
   *
   * @return number of user
   */
  public int countUser() {
    return 0;
  }

  /**
   * Get user by id
   *
   * @param id
   *          id of user
   * @return user with given id
   */
  @AnonymousAllowed
  @RolesAllowed("overridden_by_anonymous")
  public User getUserById(int id) {
    return null;
  }

  /**
   * Get array int
   *
   * @param input
   *          input string array
   * @return array of int
   */
  @AnonymousAllowed
  @PermitAll
  public int[] getArrayInt(String[] input) {
    return new int[] { 1, 2 };
  }

  /**
   * Get boolean value
   *
   * @param input
   *          input map
   * @return boolean value
   */
  public boolean getBooleanValue(Map<String, User> input) {
    return false;
  }

  /**
   * The method won't be generated because {@link DenyAll} annotation is present
   * on it.
   *
   * @param input
   *          input map
   * @return boolean value
   */
  @AnonymousAllowed
  @DenyAll
  public boolean restrictedGetBooleanValue(Map<String, User> input) {
    return false;
  }

  /**
   * Two parameters input method
   *
   * @param input
   *          first input description
   * @param secondInput
   *          second input description
   * @return boolean value
   */
  @AnonymousAllowed
  public boolean getTwoParameters(String input, int secondInput) {
    return false;
  }

  /**
   * Get instant nano
   *
   * @param input
   *          input parameter
   * @return current time as an Instant
   */
  public Instant fullFQNMethod(Integer input) {
    return Instant.now();
  }

  protected void hiddenMethod() {
    // No implementation
  }

  public void reservedWordInParameter(boolean delete) {
    // No implementation
  }

  public void inputBeanTypeDependency(Version input) {
    // No implementation
  }

  public void inputBeanTypeLocal(Status input) {
    // No implementation
  }

  public Optional<User> optionalReturn() {
    return Optional.empty();
  }

  public void optionalParameter(Optional<List<String>> parameter, String requiredParameter) {
    // No implementation
  }

  public static class User {
    private String name;
    private String password;
    private transient int hiddenField;
    private Map<String, Role> roles;
    Optional<String> optionalField;
  }

  public enum Permissions {
    ADMIN, USER, GUEST;
  }

  /**
   * Role bean
   */
  public static class Role {
    /**
     * Description for roleName.
     */
    private String roleName;
    /**
     * Description for permissions.
     */
    private Permissions permissions;
  }

  /**
   * Status bean. Used only in request parameters to verify that request
   * parameter type descriptions are generated.
   */
  public static class Status {
    private Instant createdAt;
    /**
     * Multiple line description should work.This is very very very very very
     * very very very long.
     */
    private String text;
  }

  /**
   * This nested class is also used in the OpenApi generator test
   */
  @Endpoint("customName")
  @AnonymousAllowed
  public static class GeneratorAnonymousAllowedTestClass {
    public void anonymousAllowed() {
    }

    @DenyAll
    public void restricted() {
    }

    @RolesAllowed("whatever")
    public void permissionAltered1() {
    }

    @PermitAll
    public void permissionAltered2() {
    }
  }

}
