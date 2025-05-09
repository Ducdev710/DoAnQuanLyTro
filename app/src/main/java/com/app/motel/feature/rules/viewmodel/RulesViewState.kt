package com.app.motel.feature.rules.viewmodel

import androidx.lifecycle.MutableLiveData
import com.app.motel.core.AppViewLiveData
import com.app.motel.data.entity.QuyDinhEntity
import com.app.motel.data.model.Resource
import com.app.motel.data.model.Rules

class RulesViewState: AppViewLiveData {
    val rules = MutableLiveData<Resource<List<Rules>>>()

    val addRules = MutableLiveData<Resource<Rules>>()
    val saveRules = MutableLiveData<Resource<Boolean>>()

    val getAllRulesActive get () = rules.value?.data?.filter { it.status == QuyDinhEntity.STATUS_ACTIVE } ?: arrayListOf()
    val getAllRules get () = rules.value?.data ?: arrayListOf()
}