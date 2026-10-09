package com.ntvelop.goldengoosepda.feature_orders.vm;

import com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository;
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
public final class OrderViewModel_Factory implements Factory<OrderViewModel> {
  private final Provider<OrdersRepository> repositoryProvider;

  private final Provider<SettingsManager> settingsManagerProvider;

  public OrderViewModel_Factory(Provider<OrdersRepository> repositoryProvider,
      Provider<SettingsManager> settingsManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.settingsManagerProvider = settingsManagerProvider;
  }

  @Override
  public OrderViewModel get() {
    return newInstance(repositoryProvider.get(), settingsManagerProvider.get());
  }

  public static OrderViewModel_Factory create(Provider<OrdersRepository> repositoryProvider,
      Provider<SettingsManager> settingsManagerProvider) {
    return new OrderViewModel_Factory(repositoryProvider, settingsManagerProvider);
  }

  public static OrderViewModel newInstance(OrdersRepository repository,
      SettingsManager settingsManager) {
    return new OrderViewModel(repository, settingsManager);
  }
}
