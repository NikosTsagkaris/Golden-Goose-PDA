package com.ntvelop.goldengoosepda.network;

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
public final class HostSelectionInterceptor_Factory implements Factory<HostSelectionInterceptor> {
  private final Provider<SettingsManager> settingsManagerProvider;

  public HostSelectionInterceptor_Factory(Provider<SettingsManager> settingsManagerProvider) {
    this.settingsManagerProvider = settingsManagerProvider;
  }

  @Override
  public HostSelectionInterceptor get() {
    return newInstance(settingsManagerProvider.get());
  }

  public static HostSelectionInterceptor_Factory create(
      Provider<SettingsManager> settingsManagerProvider) {
    return new HostSelectionInterceptor_Factory(settingsManagerProvider);
  }

  public static HostSelectionInterceptor newInstance(SettingsManager settingsManager) {
    return new HostSelectionInterceptor(settingsManager);
  }
}
