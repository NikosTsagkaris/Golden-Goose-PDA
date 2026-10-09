package com.ntvelop.goldengoosepda.feature_shifts.vm;

import com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository;
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
public final class ShiftViewModel_Factory implements Factory<ShiftViewModel> {
  private final Provider<ShiftsRepository> repositoryProvider;

  private final Provider<SettingsManager> settingsManagerProvider;

  public ShiftViewModel_Factory(Provider<ShiftsRepository> repositoryProvider,
      Provider<SettingsManager> settingsManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.settingsManagerProvider = settingsManagerProvider;
  }

  @Override
  public ShiftViewModel get() {
    return newInstance(repositoryProvider.get(), settingsManagerProvider.get());
  }

  public static ShiftViewModel_Factory create(Provider<ShiftsRepository> repositoryProvider,
      Provider<SettingsManager> settingsManagerProvider) {
    return new ShiftViewModel_Factory(repositoryProvider, settingsManagerProvider);
  }

  public static ShiftViewModel newInstance(ShiftsRepository repository,
      SettingsManager settingsManager) {
    return new ShiftViewModel(repository, settingsManager);
  }
}
