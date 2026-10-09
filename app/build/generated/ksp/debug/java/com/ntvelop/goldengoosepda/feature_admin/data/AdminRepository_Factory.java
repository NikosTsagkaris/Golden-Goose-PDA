package com.ntvelop.goldengoosepda.feature_admin.data;

import com.ntvelop.goldengoosepda.network.GoldenGooseApiService;
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
public final class AdminRepository_Factory implements Factory<AdminRepository> {
  private final Provider<GoldenGooseApiService> apiServiceProvider;

  public AdminRepository_Factory(Provider<GoldenGooseApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public AdminRepository get() {
    return newInstance(apiServiceProvider.get());
  }

  public static AdminRepository_Factory create(Provider<GoldenGooseApiService> apiServiceProvider) {
    return new AdminRepository_Factory(apiServiceProvider);
  }

  public static AdminRepository newInstance(GoldenGooseApiService apiService) {
    return new AdminRepository(apiService);
  }
}
