package com.nulianov.twofactorauthentication.repository;

import com.nulianov.twofactorauthentication.model.CodeData;

/**
 * Repository interface for managing verification codes associated with user emails.
 */
public interface CodeRepository {

  /**
   * Saves the code data associated with the specified email.
   *
   * @param email    the user's email address
   * @param codeData the code data to save
   */
  void save(String email, CodeData codeData);

  /**
   * Retrieves the code data associated with the specified email.
   *
   * @param email the user's email address
   * @return the code data if present; {@code null} otherwise
   */
  CodeData findByEmail(String email);

  /**
   * Deletes the code data associated with the specified email.
   *
   * @param email the user's email address
   */
  void deleteByEmail(String email);
}
