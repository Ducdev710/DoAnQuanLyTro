package com.app.motel.feature.auth.viewmodel

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.app.motel.core.AppBaseViewModel
import com.app.motel.data.model.Resource
import com.app.motel.data.model.User
import com.app.motel.data.repository.AuthRepository
import com.app.motel.data.repository.ProfileRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : AppBaseViewModel<AuthViewState, AuthViewAction, AuthViewEvent>(AuthViewState()) {

    override fun handle(action: AuthViewAction) {

    }

    /*// First step of registration - initiates OTP sending
    fun initiateRegistration(
        fullName: String?,
        username: String?,
        password: String?,
        email: String?,
        birthDate: String?,
        phoneNumber: String?
    ) {
        liveData.registerInit.postValue(Resource.Loading())

        when {
            fullName.isNullOrBlank() -> {
                liveData.registerInit.postValue(Resource.Error(message = "Họ tên không được để trống"))
                return
            }
            username.isNullOrBlank() -> {
                liveData.registerInit.postValue(Resource.Error(message = "Tên đăng nhập không được để trống"))
                return
            }
            password.isNullOrBlank() -> {
                liveData.registerInit.postValue(Resource.Error(message = "Mật khẩu không được để trống"))
                return
            }
            email.isNullOrBlank() -> {
                liveData.registerInit.postValue(Resource.Error(message = "Email không được để trống"))
                return
            }
            phoneNumber.isNullOrBlank() -> {
                liveData.registerInit.postValue(Resource.Error(message = "Số điện thoại không được để trống"))
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                liveData.registerInit.postValue(Resource.Error(message = "Lỗi định dạng email"))
                return
            }
        }

        viewModelScope.launch {
            val newUser = User(
                id = "",
                fullName = fullName!!,
                username = username!!,
                password = password!!,
                email = email,
                birthDate = birthDate,
                phoneNumber = phoneNumber,
                state = -1
            )
            val result = authRepository.sendRegistrationOtp(newUser)
            liveData.registerInit.postValue(result)
        }
    }

    // Second step of registration - verifies OTP and completes registration
    fun verifyOtpAndRegister(phoneNumber: String, otp: String) {
        liveData.register.postValue(Resource.Loading())

        if (otp.isBlank() || otp.length != 6) {
            liveData.register.postValue(Resource.Error(message = "Mã OTP phải có 6 chữ số"))
            return
        }

        viewModelScope.launch {
            val result = authRepository.verifyOtpAndRegister(phoneNumber, otp)

            if (result.isSuccess() && result.data != null) {
                profileRepository.saveCurrentUser(result.data)
            }

            liveData.register.postValue(result)
        }
    }
*/
    // Legacy registration method - kept for backward compatibility
    fun register(
        fullName: String?,
        username: String?,
        password: String?,
        email: String?,
        birthDate: String?,
        phoneNumber: String?
    ) {
        liveData.register.postValue(Resource.Loading())
        when {
            fullName.isNullOrBlank() -> {
                liveData.register.postValue(Resource.Error(message = "Họ tên không được để trống"))
                return
            }
            username.isNullOrBlank() -> {
                liveData.register.postValue(Resource.Error(message = "Tên đăng nhập không được để trống"))
                return
            }
            password.isNullOrBlank() -> {
                liveData.register.postValue(Resource.Error(message = "Mật khẩu không được để trống"))
                return
            }
            email.isNullOrBlank() -> {
                liveData.register.postValue(Resource.Error(message = "Email không được để trống"))
                return
            }
            phoneNumber.isNullOrBlank() -> {
                liveData.register.postValue(Resource.Error(message = "Số điện thoại không được để trống"))
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                liveData.register.postValue(Resource.Error(message = "Lỗi định dạng email"))
                return
            }
        }

        viewModelScope.launch {
            val newUser = User(
                id = "",
                fullName = fullName!!,
                username = username!!,
                password = password!!,
                email = email,
                birthDate = birthDate,
                phoneNumber = phoneNumber,
                state = -1
            )
            val userLogin = authRepository.register(newUser)
            if (userLogin.isSuccess() && userLogin.data != null) {
                profileRepository.saveCurrentUser(userLogin.data)
            }
            liveData.register.postValue(userLogin)
        }
    }

    fun login(
        username: String?,
        password: String?,
    ) {
        liveData.login.postValue(Resource.Loading())
        when {
            username.isNullOrBlank() -> {
                liveData.login.postValue(Resource.Error(message = "Tên đăng nhập không được để trống"))
                return
            }
            password.isNullOrBlank() -> {
                liveData.login.postValue(Resource.Error(message = "Mật khẩu không được để trống"))
                return
            }
        }

        viewModelScope.launch {
            val userRegister = authRepository.login(username!!, password!!)
            if (userRegister.isSuccess() && userRegister.data != null) {
                profileRepository.saveCurrentUser(userRegister.data)
            }
            liveData.login.postValue(userRegister)
        }
    }
}