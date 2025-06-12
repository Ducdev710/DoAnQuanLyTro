package com.app.motel.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.motel.feature.home.viewmodel.HomeViewModel
import com.app.motel.di.factory.AppViewModelFactory
import com.app.motel.feature.boardingHouse.viewmodel.BoardingHouseViewModel
import com.app.motel.feature.createContract.viewmodel.CreateContractViewModel
import com.app.motel.feature.handleContract.viewmodel.HandleContractViewModel
import com.app.motel.feature.service.viewmodel.ServiceViewModel
import com.app.motel.feature.auth.viewmodel.AuthViewModel
import com.app.motel.feature.complaint.viewmodel.ComplaintViewModel
import com.app.motel.feature.createBill.viewmodel.CreateBillViewModel
import com.app.motel.feature.handleBill.viewmodel.HandleBillViewModel
import com.app.motel.feature.news.viewmodel.NewsViewModel
import com.app.motel.feature.notify.viewmodel.NotifyViewModel
import com.app.motel.feature.revenue.viewmodel.RevenueViewModel
import com.app.motel.feature.room.viewmodel.RoomViewModel
import com.app.motel.feature.rules.viewmodel.RulesViewModel
import com.app.motel.feature.tenant.viewmodel.TenantViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

/**
 * Module Dagger cho việc cung cấp các ViewModel.
 *
 * Sử dụng Dagger Multibinding để tạo một map các ViewModel,
 * cho phép Dagger tạo các ViewModel với constructor injection và quản lý
 * chúng thông qua ViewModelProvider.Factory.
 */
@Module
interface ViewModelModule {

    @Binds
    fun bindHomeViewModelFactory(factory: AppViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateContractViewModel::class)
    fun bindCreateContractViewModel(authViewModel: CreateContractViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HandleContractViewModel::class)
    fun bindHandleContractViewModel(authViewModel: HandleContractViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ServiceViewModel::class)
    fun bindServiceViewModel(authViewModel: ServiceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateBillViewModel::class)
    fun bindCreateBillViewModel(authViewModel: CreateBillViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RoomViewModel::class)
    fun bindRoomViewModel(authViewModel: RoomViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TenantViewModel::class)
    fun bindTenantViewModel(authViewModel: TenantViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HandleBillViewModel::class)
    fun bindHandleBillViewModel(authViewModel: HandleBillViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RulesViewModel::class)
    fun bindRulesViewModel(authViewModel: RulesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel::class)
    fun bindNewsViewModel(authViewModel: NewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotifyViewModel::class)
    fun bindNotifyViewModel(authViewModel: NotifyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BoardingHouseViewModel::class)
    fun bindBoardingHouseViewModel(boardingHouseViewModel: BoardingHouseViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ComplaintViewModel::class)
    fun bindComplaintViewModel(boardingHouseViewModel: ComplaintViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RevenueViewModel::class)
    fun bindRevenueViewModel(viewModel: RevenueViewModel): ViewModel

}

/**
 * Annotation key cho việc multibinding ViewModel.
 *
 * Sử dụng để định danh các entry trong map
 * ViewModel multibinding, sử dụng KClass của ViewModel làm key.
 *
 * @property value Class của ViewModel dùng làm key trong map
 */
@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)