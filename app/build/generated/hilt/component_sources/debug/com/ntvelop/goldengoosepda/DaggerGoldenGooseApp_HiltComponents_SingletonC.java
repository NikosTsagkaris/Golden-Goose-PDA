package com.ntvelop.goldengoosepda;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.ntvelop.goldengoosepda.di.NetworkModule_ProvideApiServiceFactory;
import com.ntvelop.goldengoosepda.di.NetworkModule_ProvideLoggingInterceptorFactory;
import com.ntvelop.goldengoosepda.di.NetworkModule_ProvideOkHttpClientFactory;
import com.ntvelop.goldengoosepda.di.NetworkModule_ProvideRetrofitFactory;
import com.ntvelop.goldengoosepda.feature_admin.data.AdminRepository;
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminLogsViewModel;
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminLogsViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminSettingsViewModel;
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminSettingsViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminTotalsViewModel;
import com.ntvelop.goldengoosepda.feature_admin.vm.AdminTotalsViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_auth.data.AuthRepository;
import com.ntvelop.goldengoosepda.feature_auth.vm.LoginViewModel;
import com.ntvelop.goldengoosepda.feature_auth.vm.LoginViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository;
import com.ntvelop.goldengoosepda.feature_orders.vm.OpenOrdersViewModel;
import com.ntvelop.goldengoosepda.feature_orders.vm.OpenOrdersViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel;
import com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_orders.vm.PaidOrdersViewModel;
import com.ntvelop.goldengoosepda.feature_orders.vm.PaidOrdersViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_setup.vm.SetupViewModel;
import com.ntvelop.goldengoosepda.feature_setup.vm.SetupViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository;
import com.ntvelop.goldengoosepda.feature_shifts.vm.ShiftViewModel;
import com.ntvelop.goldengoosepda.feature_shifts.vm.ShiftViewModel_HiltModules;
import com.ntvelop.goldengoosepda.feature_tables.data.TablesRepository;
import com.ntvelop.goldengoosepda.feature_tables.vm.TableMapViewModel;
import com.ntvelop.goldengoosepda.feature_tables.vm.TableMapViewModel_HiltModules;
import com.ntvelop.goldengoosepda.network.AuthInterceptor;
import com.ntvelop.goldengoosepda.network.GoldenGooseApiService;
import com.ntvelop.goldengoosepda.network.HostSelectionInterceptor;
import com.ntvelop.goldengoosepda.network.SettingsManager;
import com.ntvelop.goldengoosepda.network.TokenManager;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

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
public final class DaggerGoldenGooseApp_HiltComponents_SingletonC {
  private DaggerGoldenGooseApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public GoldenGooseApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements GoldenGooseApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public GoldenGooseApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements GoldenGooseApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public GoldenGooseApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements GoldenGooseApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public GoldenGooseApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements GoldenGooseApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public GoldenGooseApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements GoldenGooseApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public GoldenGooseApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements GoldenGooseApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public GoldenGooseApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements GoldenGooseApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public GoldenGooseApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends GoldenGooseApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends GoldenGooseApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends GoldenGooseApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends GoldenGooseApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(10).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_admin_vm_AdminLogsViewModel, AdminLogsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_admin_vm_AdminSettingsViewModel, AdminSettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_admin_vm_AdminTotalsViewModel, AdminTotalsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_auth_vm_LoginViewModel, LoginViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_orders_vm_OpenOrdersViewModel, OpenOrdersViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_orders_vm_OrderViewModel, OrderViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_orders_vm_PaidOrdersViewModel, PaidOrdersViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_setup_vm_SetupViewModel, SetupViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_shifts_vm_ShiftViewModel, ShiftViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_tables_vm_TableMapViewModel, TableMapViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectTokenManager(instance, singletonCImpl.tokenManagerProvider.get());
      MainActivity_MembersInjector.injectSettingsManager(instance, singletonCImpl.settingsManagerProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_ntvelop_goldengoosepda_feature_admin_vm_AdminLogsViewModel = "com.ntvelop.goldengoosepda.feature_admin.vm.AdminLogsViewModel";

      static String com_ntvelop_goldengoosepda_feature_admin_vm_AdminTotalsViewModel = "com.ntvelop.goldengoosepda.feature_admin.vm.AdminTotalsViewModel";

      static String com_ntvelop_goldengoosepda_feature_orders_vm_OpenOrdersViewModel = "com.ntvelop.goldengoosepda.feature_orders.vm.OpenOrdersViewModel";

      static String com_ntvelop_goldengoosepda_feature_shifts_vm_ShiftViewModel = "com.ntvelop.goldengoosepda.feature_shifts.vm.ShiftViewModel";

      static String com_ntvelop_goldengoosepda_feature_setup_vm_SetupViewModel = "com.ntvelop.goldengoosepda.feature_setup.vm.SetupViewModel";

      static String com_ntvelop_goldengoosepda_feature_auth_vm_LoginViewModel = "com.ntvelop.goldengoosepda.feature_auth.vm.LoginViewModel";

      static String com_ntvelop_goldengoosepda_feature_orders_vm_OrderViewModel = "com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel";

      static String com_ntvelop_goldengoosepda_feature_orders_vm_PaidOrdersViewModel = "com.ntvelop.goldengoosepda.feature_orders.vm.PaidOrdersViewModel";

      static String com_ntvelop_goldengoosepda_feature_admin_vm_AdminSettingsViewModel = "com.ntvelop.goldengoosepda.feature_admin.vm.AdminSettingsViewModel";

      static String com_ntvelop_goldengoosepda_feature_tables_vm_TableMapViewModel = "com.ntvelop.goldengoosepda.feature_tables.vm.TableMapViewModel";

      @KeepFieldType
      AdminLogsViewModel com_ntvelop_goldengoosepda_feature_admin_vm_AdminLogsViewModel2;

      @KeepFieldType
      AdminTotalsViewModel com_ntvelop_goldengoosepda_feature_admin_vm_AdminTotalsViewModel2;

      @KeepFieldType
      OpenOrdersViewModel com_ntvelop_goldengoosepda_feature_orders_vm_OpenOrdersViewModel2;

      @KeepFieldType
      ShiftViewModel com_ntvelop_goldengoosepda_feature_shifts_vm_ShiftViewModel2;

      @KeepFieldType
      SetupViewModel com_ntvelop_goldengoosepda_feature_setup_vm_SetupViewModel2;

      @KeepFieldType
      LoginViewModel com_ntvelop_goldengoosepda_feature_auth_vm_LoginViewModel2;

      @KeepFieldType
      OrderViewModel com_ntvelop_goldengoosepda_feature_orders_vm_OrderViewModel2;

      @KeepFieldType
      PaidOrdersViewModel com_ntvelop_goldengoosepda_feature_orders_vm_PaidOrdersViewModel2;

      @KeepFieldType
      AdminSettingsViewModel com_ntvelop_goldengoosepda_feature_admin_vm_AdminSettingsViewModel2;

      @KeepFieldType
      TableMapViewModel com_ntvelop_goldengoosepda_feature_tables_vm_TableMapViewModel2;
    }
  }

  private static final class ViewModelCImpl extends GoldenGooseApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AdminLogsViewModel> adminLogsViewModelProvider;

    private Provider<AdminSettingsViewModel> adminSettingsViewModelProvider;

    private Provider<AdminTotalsViewModel> adminTotalsViewModelProvider;

    private Provider<LoginViewModel> loginViewModelProvider;

    private Provider<OpenOrdersViewModel> openOrdersViewModelProvider;

    private Provider<OrderViewModel> orderViewModelProvider;

    private Provider<PaidOrdersViewModel> paidOrdersViewModelProvider;

    private Provider<SetupViewModel> setupViewModelProvider;

    private Provider<ShiftViewModel> shiftViewModelProvider;

    private Provider<TableMapViewModel> tableMapViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.adminLogsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.adminSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.adminTotalsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.openOrdersViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.orderViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.paidOrdersViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.setupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.shiftViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.tableMapViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(10).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_admin_vm_AdminLogsViewModel, ((Provider) adminLogsViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_admin_vm_AdminSettingsViewModel, ((Provider) adminSettingsViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_admin_vm_AdminTotalsViewModel, ((Provider) adminTotalsViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_auth_vm_LoginViewModel, ((Provider) loginViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_orders_vm_OpenOrdersViewModel, ((Provider) openOrdersViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_orders_vm_OrderViewModel, ((Provider) orderViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_orders_vm_PaidOrdersViewModel, ((Provider) paidOrdersViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_setup_vm_SetupViewModel, ((Provider) setupViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_shifts_vm_ShiftViewModel, ((Provider) shiftViewModelProvider)).put(LazyClassKeyProvider.com_ntvelop_goldengoosepda_feature_tables_vm_TableMapViewModel, ((Provider) tableMapViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_ntvelop_goldengoosepda_feature_orders_vm_OrderViewModel = "com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel";

      static String com_ntvelop_goldengoosepda_feature_admin_vm_AdminTotalsViewModel = "com.ntvelop.goldengoosepda.feature_admin.vm.AdminTotalsViewModel";

      static String com_ntvelop_goldengoosepda_feature_orders_vm_OpenOrdersViewModel = "com.ntvelop.goldengoosepda.feature_orders.vm.OpenOrdersViewModel";

      static String com_ntvelop_goldengoosepda_feature_orders_vm_PaidOrdersViewModel = "com.ntvelop.goldengoosepda.feature_orders.vm.PaidOrdersViewModel";

      static String com_ntvelop_goldengoosepda_feature_admin_vm_AdminLogsViewModel = "com.ntvelop.goldengoosepda.feature_admin.vm.AdminLogsViewModel";

      static String com_ntvelop_goldengoosepda_feature_setup_vm_SetupViewModel = "com.ntvelop.goldengoosepda.feature_setup.vm.SetupViewModel";

      static String com_ntvelop_goldengoosepda_feature_admin_vm_AdminSettingsViewModel = "com.ntvelop.goldengoosepda.feature_admin.vm.AdminSettingsViewModel";

      static String com_ntvelop_goldengoosepda_feature_auth_vm_LoginViewModel = "com.ntvelop.goldengoosepda.feature_auth.vm.LoginViewModel";

      static String com_ntvelop_goldengoosepda_feature_shifts_vm_ShiftViewModel = "com.ntvelop.goldengoosepda.feature_shifts.vm.ShiftViewModel";

      static String com_ntvelop_goldengoosepda_feature_tables_vm_TableMapViewModel = "com.ntvelop.goldengoosepda.feature_tables.vm.TableMapViewModel";

      @KeepFieldType
      OrderViewModel com_ntvelop_goldengoosepda_feature_orders_vm_OrderViewModel2;

      @KeepFieldType
      AdminTotalsViewModel com_ntvelop_goldengoosepda_feature_admin_vm_AdminTotalsViewModel2;

      @KeepFieldType
      OpenOrdersViewModel com_ntvelop_goldengoosepda_feature_orders_vm_OpenOrdersViewModel2;

      @KeepFieldType
      PaidOrdersViewModel com_ntvelop_goldengoosepda_feature_orders_vm_PaidOrdersViewModel2;

      @KeepFieldType
      AdminLogsViewModel com_ntvelop_goldengoosepda_feature_admin_vm_AdminLogsViewModel2;

      @KeepFieldType
      SetupViewModel com_ntvelop_goldengoosepda_feature_setup_vm_SetupViewModel2;

      @KeepFieldType
      AdminSettingsViewModel com_ntvelop_goldengoosepda_feature_admin_vm_AdminSettingsViewModel2;

      @KeepFieldType
      LoginViewModel com_ntvelop_goldengoosepda_feature_auth_vm_LoginViewModel2;

      @KeepFieldType
      ShiftViewModel com_ntvelop_goldengoosepda_feature_shifts_vm_ShiftViewModel2;

      @KeepFieldType
      TableMapViewModel com_ntvelop_goldengoosepda_feature_tables_vm_TableMapViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.ntvelop.goldengoosepda.feature_admin.vm.AdminLogsViewModel 
          return (T) new AdminLogsViewModel(singletonCImpl.adminRepositoryProvider.get());

          case 1: // com.ntvelop.goldengoosepda.feature_admin.vm.AdminSettingsViewModel 
          return (T) new AdminSettingsViewModel(singletonCImpl.provideApiServiceProvider.get());

          case 2: // com.ntvelop.goldengoosepda.feature_admin.vm.AdminTotalsViewModel 
          return (T) new AdminTotalsViewModel(singletonCImpl.adminRepositoryProvider.get(), singletonCImpl.shiftsRepositoryProvider.get());

          case 3: // com.ntvelop.goldengoosepda.feature_auth.vm.LoginViewModel 
          return (T) new LoginViewModel(singletonCImpl.authRepositoryProvider.get());

          case 4: // com.ntvelop.goldengoosepda.feature_orders.vm.OpenOrdersViewModel 
          return (T) new OpenOrdersViewModel(singletonCImpl.ordersRepositoryProvider.get());

          case 5: // com.ntvelop.goldengoosepda.feature_orders.vm.OrderViewModel 
          return (T) new OrderViewModel(singletonCImpl.ordersRepositoryProvider.get(), singletonCImpl.settingsManagerProvider.get());

          case 6: // com.ntvelop.goldengoosepda.feature_orders.vm.PaidOrdersViewModel 
          return (T) new PaidOrdersViewModel(singletonCImpl.ordersRepositoryProvider.get(), singletonCImpl.shiftsRepositoryProvider.get());

          case 7: // com.ntvelop.goldengoosepda.feature_setup.vm.SetupViewModel 
          return (T) new SetupViewModel(singletonCImpl.settingsManagerProvider.get());

          case 8: // com.ntvelop.goldengoosepda.feature_shifts.vm.ShiftViewModel 
          return (T) new ShiftViewModel(singletonCImpl.shiftsRepositoryProvider.get(), singletonCImpl.settingsManagerProvider.get());

          case 9: // com.ntvelop.goldengoosepda.feature_tables.vm.TableMapViewModel 
          return (T) new TableMapViewModel(singletonCImpl.tablesRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends GoldenGooseApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends GoldenGooseApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends GoldenGooseApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<TokenManager> tokenManagerProvider;

    private Provider<SettingsManager> settingsManagerProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<HttpLoggingInterceptor> provideLoggingInterceptorProvider;

    private Provider<HostSelectionInterceptor> hostSelectionInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<GoldenGooseApiService> provideApiServiceProvider;

    private Provider<AdminRepository> adminRepositoryProvider;

    private Provider<ShiftsRepository> shiftsRepositoryProvider;

    private Provider<AuthRepository> authRepositoryProvider;

    private Provider<OrdersRepository> ordersRepositoryProvider;

    private Provider<TablesRepository> tablesRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.tokenManagerProvider = DoubleCheck.provider(new SwitchingProvider<TokenManager>(singletonCImpl, 0));
      this.settingsManagerProvider = DoubleCheck.provider(new SwitchingProvider<SettingsManager>(singletonCImpl, 1));
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 6));
      this.provideLoggingInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HttpLoggingInterceptor>(singletonCImpl, 7));
      this.hostSelectionInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HostSelectionInterceptor>(singletonCImpl, 8));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 5));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 4));
      this.provideApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<GoldenGooseApiService>(singletonCImpl, 3));
      this.adminRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AdminRepository>(singletonCImpl, 2));
      this.shiftsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ShiftsRepository>(singletonCImpl, 9));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 10));
      this.ordersRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<OrdersRepository>(singletonCImpl, 11));
      this.tablesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<TablesRepository>(singletonCImpl, 12));
    }

    @Override
    public void injectGoldenGooseApp(GoldenGooseApp arg0) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.ntvelop.goldengoosepda.network.TokenManager 
          return (T) new TokenManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.ntvelop.goldengoosepda.network.SettingsManager 
          return (T) new SettingsManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.ntvelop.goldengoosepda.feature_admin.data.AdminRepository 
          return (T) new AdminRepository(singletonCImpl.provideApiServiceProvider.get());

          case 3: // com.ntvelop.goldengoosepda.network.GoldenGooseApiService 
          return (T) NetworkModule_ProvideApiServiceFactory.provideApiService(singletonCImpl.provideRetrofitProvider.get());

          case 4: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.settingsManagerProvider.get());

          case 5: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.authInterceptorProvider.get(), singletonCImpl.provideLoggingInterceptorProvider.get(), singletonCImpl.hostSelectionInterceptorProvider.get());

          case 6: // com.ntvelop.goldengoosepda.network.AuthInterceptor 
          return (T) new AuthInterceptor(singletonCImpl.tokenManagerProvider.get());

          case 7: // okhttp3.logging.HttpLoggingInterceptor 
          return (T) NetworkModule_ProvideLoggingInterceptorFactory.provideLoggingInterceptor();

          case 8: // com.ntvelop.goldengoosepda.network.HostSelectionInterceptor 
          return (T) new HostSelectionInterceptor(singletonCImpl.settingsManagerProvider.get());

          case 9: // com.ntvelop.goldengoosepda.feature_shifts.data.ShiftsRepository 
          return (T) new ShiftsRepository(singletonCImpl.provideApiServiceProvider.get());

          case 10: // com.ntvelop.goldengoosepda.feature_auth.data.AuthRepository 
          return (T) new AuthRepository(singletonCImpl.provideApiServiceProvider.get(), singletonCImpl.tokenManagerProvider.get());

          case 11: // com.ntvelop.goldengoosepda.feature_orders.data.OrdersRepository 
          return (T) new OrdersRepository(singletonCImpl.provideApiServiceProvider.get());

          case 12: // com.ntvelop.goldengoosepda.feature_tables.data.TablesRepository 
          return (T) new TablesRepository(singletonCImpl.provideApiServiceProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
