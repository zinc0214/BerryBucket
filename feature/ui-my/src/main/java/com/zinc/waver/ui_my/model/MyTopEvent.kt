package com.zinc.waver.ui_my.model

sealed class MyTopEvent {
    object Alarm : MyTopEvent()
    object Following : MyTopEvent()
    object Follower : MyTopEvent()
}