package com.app.motel.data.model

/**
 * Lớp trừu tượng đóng gói thông tin người dùng trong hệ thống.
 * Sử dụng sealed class để giới hạn các loại người dùng có thể tồn tại.
 * Hỗ trợ hai loại người dùng: Admin(chủ nhà) và Người dùng thông thường(khách thuê).
 */
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

    /**
     * Đối tượng gốc chứa thông tin chi tiết của người dùng
     * (User cho Chủ nhà, Tenant cho khach thuê).
     */
    abstract val child: Any

    /**
     * Thuộc tính mở rộng để truy cập thông tin ngân hàng
     * Chỉ có giá trị với người dùng chủ nhà
     */
    val bankName: String?
        get() = if (isAdmin && child is User) (child as User).bankName else null

    val accountNumber: String?
        get() = if (isAdmin && child is User) (child as User).accountNumber else null

    /**
     * Đại diện cho người dùng có quyền admin (chủ nhà)
     * @param child Đối tượng User chứa thông tin chi tiết
     */
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

    /**
     * Đại diện cho người dùng thông thường (khách thuê)
     * @param child Đối tượng Tenant chứa thông tin chi tiết
     */
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

    /**
     * Tạo bản sao của đối tượng với các thông tin được cập nhật
     */
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

    /**
     * Phiên bản nạp chồng của phương thức copy bổ sung thông tin ngân hàng
     * @return Đối tượng CommonUser mới với thông tin đã cập nhật
     */
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