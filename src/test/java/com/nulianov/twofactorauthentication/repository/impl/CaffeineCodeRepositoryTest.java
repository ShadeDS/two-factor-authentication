package com.nulianov.twofactorauthentication.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nulianov.twofactorauthentication.model.CodeData;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CaffeineCodeRepositoryTest {

  private CaffeineCodeRepository codeRepository;

  private final String email = "test@example.com";
  private final long validityDuration = 10L;

  @BeforeEach
  void setUp() {
    Cache<String, CodeData> codeCache = Caffeine.newBuilder()
        .expireAfterWrite(validityDuration, TimeUnit.MILLISECONDS)
        .build();
    codeRepository = new CaffeineCodeRepository(codeCache);
  }

  @Test
  void saveAndFindByEmail_ShouldWorkCorrectly() {
    CodeData codeData = new CodeData("123456", Instant.now().getEpochSecond());

    codeRepository.save(email, codeData);
    CodeData retrievedCodeData = codeRepository.findByEmail(email);

    assertNotNull(retrievedCodeData);
    assertEquals(codeData.code(), retrievedCodeData.code());
  }

  @Test
  void deleteByEmail_ShouldRemoveCodeData() {
    CodeData codeData = new CodeData("123456", Instant.now().getEpochSecond());
    codeRepository.save(email, codeData);

    codeRepository.deleteByEmail(email);
    CodeData retrievedCodeData = codeRepository.findByEmail(email);

    assertNull(retrievedCodeData);
  }

  @Test
  void codeData_ShouldExpireAfterValidityDuration() throws InterruptedException {
    CodeData codeData = new CodeData("123456", Instant.now().getEpochSecond());
    codeRepository.save(email, codeData);

    Thread.sleep(validityDuration + 1);
    CodeData retrievedCodeData = codeRepository.findByEmail(email);

    assertNull(retrievedCodeData);
  }
}