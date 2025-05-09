package com.app.motel.common.service

import java.util.UUID

object IDManager {
    fun createID(): String = UUID.randomUUID().toString()
}