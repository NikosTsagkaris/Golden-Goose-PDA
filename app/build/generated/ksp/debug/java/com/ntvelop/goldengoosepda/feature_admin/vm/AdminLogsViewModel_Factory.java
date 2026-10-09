package com.ntvelop.goldengoosepda.feature_admin.vm;

import com.ntvelop.goldengoosepda.feature_admin.data.AdminRepository;
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
public final class AdminLogsViewModel_Factory implements Factory<AdminLogsViewModel> {
  private final Provider<AdminRepository> repositoryProvider;

  public AdminLogsViewModel_Factory(Provider<AdminRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AdminLogsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static AdminLogsViewModel_Factory create(Provider<AdminRepository> repositoryProvider) {
    return new AdminLogsViewModel_Factory(repositoryProvider);
  }

  public static AdminLogsViewModel newInstance(AdminRepository repository) {
    return new AdminLogsViewModel(repository);
  }
}
