package com.ntvelop.goldengoosepda.feature_auth.data;

import com.ntvelop.goldengoosepda.network.GoldenGooseApiService;
import com.ntvelop.goldengoosepda.network.TokenManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<GoldenGooseApiService> apiServiceProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  public AuthRepository_Factory(Provider<GoldenGooseApiService> apiServiceProvider,
      Provider<TokenManager> tokenManagerProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(apiServiceProvider.get(), tokenManagerProvider.get());
  }

  public static AuthRepository_Factory create(Provider<GoldenGooseApiService> apiServiceProvider,
      Provider<TokenManager> tokenManagerProvider) {
    return new AuthRepository_Factory(apiServiceProvider, tokenManagerProvider);
  }

  public static AuthRepository newInstance(GoldenGooseApiService apiService,
      TokenManager tokenManager) {
    return new AuthRepository(apiService, tokenManager);
  }
}
