package com.nulianov.twofactorauthentication.repository.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.nulianov.twofactorauthentication.model.CodeData;
import com.nulianov.twofactorauthentication.repository.CodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class CaffeineCodeRepository implements CodeRepository {

  private final Cache<String, CodeData> codeCache;

  public CaffeineCodeRepository(Cache<String, CodeData> codeCache) {
    this.codeCache = codeCache;
  }

  @Override
  public void save(String email, CodeData codeData) {
    codeCache.put(email, codeData);
    log.debug("Saved code data for email: {}", email);
  }

  @Override
  public CodeData findByEmail(String email) {
    log.debug("Retrieved code data for email: {}", email);
    return codeCache.getIfPresent(email);
  }

  @Override
  public void deleteByEmail(String email) {
    codeCache.invalidate(email);
    log.debug("Deleted code data for email: {}", email);
  }
}
