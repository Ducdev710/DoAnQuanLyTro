package com.app.motel.di

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.app.motel.AppApplication
import com.app.motel.di.modules.FragmentModule
import com.app.motel.di.modules.AppModule
import com.app.motel.di.modules.ViewModelModule
import com.app.motel.feature.boardingHouse.CreateBoardingHouseFragment
import com.app.motel.feature.boardingHouse.GreetingBoardingHouseFragment
import com.app.motel.feature.createContract.CreateContractFormFragment
import com.app.motel.feature.createContract.CreateContractActivity
import com.app.motel.feature.createContract.CreateContractListFragment
import com.app.motel.feature.handleContract.DetailContractBottomSheet
import com.app.motel.feature.handleContract.HandleContractActivity
import com.app.motel.feature.handleContract.HandleContractListFragment
import com.app.motel.feature.handleContract.HandleContractTerminalFragment
import com.app.motel.feature.handleContract.RefreshContractBottomSheet
import com.app.motel.feature.home.GeneralBoardingHouseFragment
import com.app.motel.feature.home.HomeFragment
import com.app.motel.feature.home.ManagementBoardingHouseFragment
import com.app.motel.feature.MainActivity
import com.app.motel.feature.service.ServiceActivity
import com.app.motel.feature.service.ServiceFormFragment
import com.app.motel.feature.service.ServiceListFragment
import com.app.motel.feature.auth.AuthActivity
import com.app.motel.feature.auth.LoginFragment
import com.app.motel.feature.auth.RegisterFragment
import com.app.motel.feature.boardingHouse.BoardingHouseActivity
import com.app.motel.feature.boardingHouse.BoardingHouseListFragment
import com.app.motel.feature.complaint.ComplaintActivity
import com.app.motel.feature.complaint.ComplaintFormFragment
import com.app.motel.feature.complaint.ComplaintListFragment
import com.app.motel.feature.createBill.CreateBillActivity
import com.app.motel.feature.createBill.CreateBillFormFragment
import com.app.motel.feature.createBill.CreateBillListFragment
import com.app.motel.feature.handleBill.HandleBillActivity
import com.app.motel.feature.handleBill.HandleBillListFragment
import com.app.motel.feature.handleBill.HandleDetailBillBottomSheet
import com.app.motel.feature.handleContract.ContractListActivity
import com.app.motel.feature.handleContract.ContractListFragment
import com.app.motel.feature.news.NewsFragment
import com.app.motel.feature.notify.NotifyFragment
import com.app.motel.feature.profile.ProfileDetailActivity
import com.app.motel.feature.profile.ProfileDetailFragment
import com.app.motel.feature.profile.ProfileFragment
import com.app.motel.feature.room.RoomActivity
import com.app.motel.feature.room.RoomDetailFragment
import com.app.motel.feature.room.RoomDetailInformationFragment
import com.app.motel.feature.room.RoomDetailTenantFragment
import com.app.motel.feature.room.RoomFormFragment
import com.app.motel.feature.room.RoomListFragment
import com.app.motel.feature.rules.MainRulesFragment
import com.app.motel.feature.rules.RuleFormFragment
import com.app.motel.feature.rules.RulesActivity
import com.app.motel.feature.rules.RulesContentFragment
import com.app.motel.feature.tenant.TenantActivity
import com.app.motel.feature.tenant.TenantFormFragment
import com.app.motel.feature.tenant.TenantListAddRoomFragment
import com.app.motel.feature.tenant.TenantListFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [
    ViewModelModule::class,
    FragmentModule::class,
    AppModule::class,
])
@Singleton
interface AppComponent {

    fun inject(appApplication: AppApplication)

    fun inject(mainActivity: MainActivity)
    fun inject(authActivity: AuthActivity)
    fun inject(createContractActivity: CreateContractActivity)
    fun inject(serviceActivity: ServiceActivity)
    fun inject(handleContractActivity: HandleContractActivity)
    fun inject(createBillActivity: CreateBillActivity)
    fun inject(roomActivity: RoomActivity)
    fun inject(tenantActivity: TenantActivity)
    fun inject(handleBillActivity: HandleBillActivity)
    fun inject(contractListActivity: ContractListActivity)
    fun inject(rulesActivity: RulesActivity)
    fun inject(boardingHouseActivity: BoardingHouseActivity)
    fun inject(profileDetailActivity: ProfileDetailActivity)
    fun inject(complaintActivity: ComplaintActivity)

    fun inject(homeFragment: HomeFragment)
    fun inject(newsFragment: NewsFragment)
    fun inject(notifyFragment: NotifyFragment)
    fun inject(profileFragment: ProfileFragment)
    fun inject(managementBoardingHouseFragment: ManagementBoardingHouseFragment)
    fun inject(generalBoardingHouseFragment: GeneralBoardingHouseFragment)
    fun inject(loginFragment: LoginFragment)
    fun inject(registerFragment: RegisterFragment)
    fun inject(greetingBoardingHouseFragment: GreetingBoardingHouseFragment)
    fun inject(createBoardingHouseFragment: CreateBoardingHouseFragment)
    fun inject(createContractListFragment: CreateContractListFragment)
    fun inject(createContractFormFragment: CreateContractFormFragment)
    fun inject(serviceListFragment: ServiceListFragment)
    fun inject(serviceFormFragment: ServiceFormFragment)
    fun inject(handleContractListFragment: HandleContractListFragment)
    fun inject(handleContractTerminalFragment: HandleContractTerminalFragment)
    fun inject(detailContractBottomSheet: DetailContractBottomSheet)
    fun inject(refreshContractBottomSheet: RefreshContractBottomSheet)
    fun inject(createBillListFragment: CreateBillListFragment)
    fun inject(createBillFormFragment: CreateBillFormFragment)
    fun inject(roomDetailFragment: RoomDetailFragment)
    fun inject(roomListFragment: RoomListFragment)
    fun inject(roomDetailInformationFragment: RoomDetailInformationFragment)
    fun inject(roomDetailTenantFragment: RoomDetailTenantFragment)
    fun inject(roomFormFragment: RoomFormFragment)
    fun inject(tenantListFragment: TenantListFragment)
    fun inject(tenantFormFragment: TenantFormFragment)
    fun inject(handleBillListFragment: HandleBillListFragment)
    fun inject(handleDetailBillBottomSheet: HandleDetailBillBottomSheet)
    fun inject(tenantListAddRoomFragment: TenantListAddRoomFragment)
    fun inject(contractListFragment: ContractListFragment)
    fun inject(mainRulesFragment: MainRulesFragment)
    fun inject(rulesContentFragment: RulesContentFragment)
    fun inject(ruleFormFragment: RuleFormFragment)
    fun inject(boardingHouseListFragment: BoardingHouseListFragment)
    fun inject(profileDetailFragment: ProfileDetailFragment)
    fun inject(complaintListFragment: ComplaintListFragment)
    fun inject(complaintFormFragment: ComplaintFormFragment)

    fun fragmentFactory(): FragmentFactory
    fun viewModelFactory(): ViewModelProvider.Factory

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance context: Context): AppComponent
    }
}