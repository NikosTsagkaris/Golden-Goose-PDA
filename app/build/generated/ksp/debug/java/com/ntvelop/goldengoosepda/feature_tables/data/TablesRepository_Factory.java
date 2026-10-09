package com.ntvelop.goldengoosepda.feature_tables.data;

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
public final class TablesRepository_Factory implements Factory<TablesRepository> {
  private final Provider<GoldenGooseApiService> apiServiceProvider;

  public TablesRepository_Factory(Provider<GoldenGooseApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public TablesRepository get() {
    return newInstance(apiServiceProvider.get());
  }

  public static TablesRepository_Factory create(
      Provider<GoldenGooseApiService> apiServiceProvider) {
    return new TablesRepository_Factory(apiServiceProvider);
  }

  public static TablesRepository newInstance(GoldenGooseApiService apiService) {
    return new TablesRepository(apiService);
  }
}
