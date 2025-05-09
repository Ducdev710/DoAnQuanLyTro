package com.app.motel.data.model

sealed class CommonUser {

    abstract val id: String
    abstract val name: String
    abstract val phone: String?
    abstract val username: String
    abstract val password: String
    abstract val birthDate: String?
    abstract val homeTown: String?
    abstract val idCard: String?
    abstract val email: String?

    abstract val role: Role

    val isAdmin get() = role == Role.admin
    abstract val child: Any

    // Add extension properties to access bank information
    val bankName: String?
        get() = if (isAdmin && child is User) (child as User).bankName else null

    val accountNumber: String?
        get() = if (isAdmin && child is User) (child as User).accountNumber else null

    data class AdminUser(
        override val child: User,
    ): CommonUser() {
        override val id = child.id
        override val name = child.fullName
        override val phone = child.phoneNumber
        override val username = child.username
        override val password = child.password
        override val birthDate = child.birthDate
        override val homeTown = null
        override val idCard = null
        override val email = child.email

        override val role = Role.admin
    }

    data class NormalUser(
        override val child: Tenant,
    ): CommonUser() {
        override val id = child.id
        override val name = child.fullName
        override val phone = child.phoneNumber
        override val username = child.username
        override val password = child.password
        override val birthDate = child.birthDay
        override val email = null
        override val idCard = child.idCard
        override val homeTown = child.homeTown

        override val role = Role.user
    }

    fun copy(
        fullName: String,
        birthDay: String?,
        phoneNumber: String?,
        email: String?,
        idCard: String?,
        homeTown: String?,
        password: String?,
        username: String?,
    ): CommonUser {
        return if(isAdmin) AdminUser(child = (child as User).copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            birthDate = birthDay,
            email = email,
            username = username ?: this.username,
            password = password ?: this.password,
            bankName = (child as User).bankName,
            accountNumber = (child as User).accountNumber
        )) else NormalUser(child = (child as Tenant).copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            idCard = idCard,
            birthDay = birthDay,
            homeTown = homeTown,
            username = username ?: this.username,
            password = password ?: this.password
        ))
    }

    // Add an overloaded copy method that handles bank information
    fun copy(
        fullName: String,
        birthDay: String?,
        phoneNumber: String?,
        email: String?,
        idCard: String?,
        homeTown: String?,
        password: String?,
        username: String?,
        bankName: String?,
        accountNumber: String?
    ): CommonUser {
        return if(isAdmin) AdminUser(child = (child as User).copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            birthDate = birthDay,
            email = email,
            username = username ?: this.username,
            password = password ?: this.password,
            bankName = bankName,
            accountNumber = accountNumber
        )) else NormalUser(child = (child as Tenant).copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            idCard = idCard,
            birthDay = birthDay,
            homeTown = homeTown,
            username = username ?: this.username,
            password = password ?: this.password
        ))
    }
}