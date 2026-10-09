package com.ntvelop.goldengoosepda.feature_orders.vm;

import com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository;
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
public final class OpenOrdersViewModel_Factory implements Factory<OpenOrdersViewModel> {
  private final Provider<OrdersRepository> repositoryProvider;

  public OpenOrdersViewModel_Factory(Provider<OrdersRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public OpenOrdersViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static OpenOrdersViewModel_Factory create(Provider<OrdersRepository> repositoryProvider) {
    return new OpenOrdersViewModel_Factory(repositoryProvider);
  }

  public static OpenOrdersViewModel newInstance(OrdersRepository repository) {
    return new OpenOrdersViewModel(repository);
  }
}
