package com.ntvelop.goldengoosepda.feature_tables.vm;

import com.ntvelop.goldengoosepda.feature_tables.data.TablesRepository;
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
public final class TableMapViewModel_Factory implements Factory<TableMapViewModel> {
  private final Provider<TablesRepository> repositoryProvider;

  public TableMapViewModel_Factory(Provider<TablesRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TableMapViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static TableMapViewModel_Factory create(Provider<TablesRepository> repositoryProvider) {
    return new TableMapViewModel_Factory(repositoryProvider);
  }

  public static TableMapViewModel newInstance(TablesRepository repository) {
    return new TableMapViewModel(repository);
  }
}
