package com.ntvelop.goldengoosepda.feature_setup.vm;

import com.ntvelop.goldengoosepda.network.SettingsManager;
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
public final class SetupViewModel_Factory implements Factory<SetupViewModel> {
  private final Provider<SettingsManager> settingsManagerProvider;

  public SetupViewModel_Factory(Provider<SettingsManager> settingsManagerProvider) {
    this.settingsManagerProvider = settingsManagerProvider;
  }

  @Override
  public SetupViewModel get() {
    return newInstance(settingsManagerProvider.get());
  }

  public static SetupViewModel_Factory create(Provider<SettingsManager> settingsManagerProvider) {
    return new SetupViewModel_Factory(settingsManagerProvider);
  }

  public static SetupViewModel newInstance(SettingsManager settingsManager) {
    return new SetupViewModel(settingsManager);
  }
}
