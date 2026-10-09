package com.ntvelop.goldengoosepda.feature_orders.vm;

import com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository;
import com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository;
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
public final class PaidOrdersViewModel_Factory implements Factory<PaidOrdersViewModel> {
  private final Provider<OrdersRepository> repositoryProvider;

  private final Provider<ShiftsRepository> shiftRepositoryProvider;

  public PaidOrdersViewModel_Factory(Provider<OrdersRepository> repositoryProvider,
      Provider<ShiftsRepository> shiftRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.shiftRepositoryProvider = shiftRepositoryProvider;
  }

  @Override
  public PaidOrdersViewModel get() {
    return newInstance(repositoryProvider.get(), shiftRepositoryProvider.get());
  }

  public static PaidOrdersViewModel_Factory create(Provider<OrdersRepository> repositoryProvider,
      Provider<ShiftsRepository> shiftRepositoryProvider) {
    return new PaidOrdersViewModel_Factory(repositoryProvider, shiftRepositoryProvider);
  }

  public static PaidOrdersViewModel newInstance(OrdersRepository repository,
      ShiftsRepository shiftRepository) {
    return new PaidOrdersViewModel(repository, shiftRepository);
  }
}
