package com.ntvelop.goldengoosepda.feature_admin.vm;

import com.ntvelop.goldengoosepda.network.GoldenGooseApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AdminSettingsViewModel_Factory implements Factory<AdminSettingsViewModel> {
  private final Provider<GoldenGooseApiService> apiServiceProvider;

  public AdminSettingsViewModel_Factory(Provider<GoldenGooseApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public AdminSettingsViewModel get() {
    return newInstance(apiServiceProvider.get());
  }

  public static AdminSettingsViewModel_Factory create(
      Provider<GoldenGooseApiService> apiServiceProvider) {
    return new AdminSettingsViewModel_Factory(apiServiceProvider);
  }

  public static AdminSettingsViewModel newInstance(GoldenGooseApiService apiService) {
    return new AdminSettingsViewModel(apiService);
  }
}
