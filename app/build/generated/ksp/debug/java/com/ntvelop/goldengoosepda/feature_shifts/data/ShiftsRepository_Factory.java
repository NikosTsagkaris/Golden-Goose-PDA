package com.ntvelop.goldengoosepda.feature_shifts.data;

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
public final class ShiftsRepository_Factory implements Factory<ShiftsRepository> {
  private final Provider<GoldenGooseApiService> apiServiceProvider;

  public ShiftsRepository_Factory(Provider<GoldenGooseApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public ShiftsRepository get() {
    return newInstance(apiServiceProvider.get());
  }

  public static ShiftsRepository_Factory create(
      Provider<GoldenGooseApiService> apiServiceProvider) {
    return new ShiftsRepository_Factory(apiServiceProvider);
  }

  public static ShiftsRepository newInstance(GoldenGooseApiService apiService) {
    return new ShiftsRepository(apiService);
  }
}
