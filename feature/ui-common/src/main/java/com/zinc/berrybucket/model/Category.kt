package com.zinc.berrybucket.model

import com.zinc.berrybucket.util.parseNavigationValue
import com.zinc.berrybucket.util.toNavigationValue
import com.zinc.common.models.CategoryInfo
import com.zinc.common.models.YesOrNo
import kotlinx.serialization.Serializable

@Serializable
data class UICategoryInfo(
    val id: Int,
    val name: String,
    val defaultYn: YesOrNo = YesOrNo.Y,
    val count: String
) : java.io.Serializable {

    companion object {
        fun toNavigationValue(value: UICategoryInfo): String =
            value.toNavigationValue()

        fun parseNavigationValue(value: String): UICategoryInfo =
            value.parseNavigationValue()
    }
}

fun List<CategoryInfo>.parseUI() = map {
    UICategoryInfo(
        id = it.id,
        name = it.name,
        defaultYn = it.defaultYn,
        count = it.bucketlistCount
    )
}

sealed interface CategoryLoadFailStatus {
    data object LoadFail : CategoryLoadFailStatus
    data object AddFail : CategoryLoadFailStatus
    data object EditFail : CategoryLoadFailStatus
    data object DeleteFail : CategoryLoadFailStatus
    data object ReorderFail : CategoryLoadFailStatus
    data object BucketLoadFail : CategoryLoadFailStatus
}