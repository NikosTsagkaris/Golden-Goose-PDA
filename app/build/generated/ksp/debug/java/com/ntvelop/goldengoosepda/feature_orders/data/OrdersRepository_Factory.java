package com.ntvelop.goldengoosepda.feature_orders.data;

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
public final class OrdersRepository_Factory implements Factory<OrdersRepository> {
  private final Provider<GoldenGooseApiService> apiServiceProvider;

  public OrdersRepository_Factory(Provider<GoldenGooseApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public OrdersRepository get() {
    return newInstance(apiServiceProvider.get());
  }

  public static OrdersRepository_Factory create(
      Provider<GoldenGooseApiService> apiServiceProvider) {
    return new OrdersRepository_Factory(apiServiceProvider);
  }

  public static OrdersRepository newInstance(GoldenGooseApiService apiService) {
    return new OrdersRepository(apiService);
  }
}
