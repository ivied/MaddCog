package com.udev.maddcog

data class SleepInformation(
    val startSleepTime: Long,
    val awakeTime: Long,
    val sleepTime: Long,
    val sleepPeriodStr: String,
    val sleepTimeStr: String
)
