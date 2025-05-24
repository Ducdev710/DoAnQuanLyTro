package com.app.motel.feature.profile

import com.app.motel.data.model.BoardingHouse
import com.app.motel.data.model.CommonUser
import com.app.motel.data.model.Resource
import com.app.motel.data.repository.BoardingHouseRepository
import com.app.motel.data.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserController @Inject constructor(
    private val repo: ProfileRepository,
    private val boardingHouseRepository: BoardingHouseRepository,
) {
    val state: ProfileViewState = ProfileViewState()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun getCurrentUser(){
        scope.launch {
            val user = repo.getCurrentUser()
            if(user == null){
                state.currentUser.postValue(Resource.Error(message = "Không tìm thấy người dùng"))
            }else{
                state.currentUser.postValue(Resource.Success(user))
                getCurrentBoardingHouse(user)
            }
        }
    }

    suspend fun getCurrentBoardingHouse(user: CommonUser){
        if(user.id.isEmpty() || !user.isAdmin) return

        val boardingHouse = boardingHouseRepository.getCurrentBoardingHouse(user.id)
        if(boardingHouse == null){
            state.currentBoardingHouse.postValue(Resource.Error(message = "Không tìm thấy người dùng"))
        }else{
            state.currentBoardingHouse.postValue(Resource.Success(boardingHouse))
        }
    }

    fun logout(){
        scope.launch {
            repo.removeCurrentUser()
            state.currentUser.postValue(Resource.Initialize())
            state.currentBoardingHouse.postValue(Resource.Initialize())
        }
    }

    fun clearCoroutine() {
        job.cancel()
    }

    fun setCurrentBoardingHouse(boardingHouse: BoardingHouse): Boolean{
        val currentUser = state.currentUser.value?.data
        if(currentUser?.id == null || !currentUser.isAdmin){
            return false
        }

        scope.launch {
            try {
                // Call the suspend function within a coroutine
                boardingHouseRepository.setCurrentBoardingHouse(currentUser.id, boardingHouse)
                getCurrentBoardingHouse(currentUser)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
        return true
    }

    fun updateBoardingHousePrices(electricityPrice: Int, waterPrice: Int): Boolean {
        val currentUser = state.currentUser.value?.data
        val currentBoardingHouse = state.currentBoardingHouse.value?.data

        if(currentUser?.id == null || !currentUser.isAdmin || currentBoardingHouse == null){
            return false
        }

        // Create updated boarding house with new prices
        val updatedBoardingHouse = currentBoardingHouse.copy(
            giaDien = electricityPrice,
            giaNuoc = waterPrice
        )

        // Update in repository and set as current
        scope.launch {
            try {
                val result = boardingHouseRepository.updateBoardingHousePrices(updatedBoardingHouse)
                if (result.isSuccess()) {
                    boardingHouseRepository.setCurrentBoardingHouse(currentUser.id, updatedBoardingHouse)
                    getCurrentBoardingHouse(currentUser)
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
        return true
    }
}