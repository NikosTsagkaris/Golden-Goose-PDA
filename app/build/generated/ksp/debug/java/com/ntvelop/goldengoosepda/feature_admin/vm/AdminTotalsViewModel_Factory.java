package com.ntvelop.goldengoosepda.feature_admin.vm;

import com.ntvelop.goldengoosepda.feature_admin.data.AdminRepository;
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
public final class AdminTotalsViewModel_Factory implements Factory<AdminTotalsViewModel> {
  private final Provider<AdminRepository> adminRepositoryProvider;

  private final Provider<ShiftsRepository> shiftRepositoryProvider;

  public AdminTotalsViewModel_Factory(Provider<AdminRepository> adminRepositoryProvider,
      Provider<ShiftsRepository> shiftRepositoryProvider) {
    this.adminRepositoryProvider = adminRepositoryProvider;
    this.shiftRepositoryProvider = shiftRepositoryProvider;
  }

  @Override
  public AdminTotalsViewModel get() {
    return newInstance(adminRepositoryProvider.get(), shiftRepositoryProvider.get());
  }

  public static AdminTotalsViewModel_Factory create(
      Provider<AdminRepository> adminRepositoryProvider,
      Provider<ShiftsRepository> shiftRepositoryProvider) {
    return new AdminTotalsViewModel_Factory(adminRepositoryProvider, shiftRepositoryProvider);
  }

  public static AdminTotalsViewModel newInstance(AdminRepository adminRepository,
      ShiftsRepository shiftRepository) {
    return new AdminTotalsViewModel(adminRepository, shiftRepository);
  }
}
