package com.udev.maddcog

import com.opencsv.CSVReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EpochFileCatcher {

    private val MISTAKE_PERCENT = 96
    private val SLEEP_EPOCHS = 150
    private val ENTRY_SLEEP_SEQUENCE_COUNT = 10
    private val ENTRY_AWAKE_SEQUENCE_COUNT = 25
    private val AWAKE_EPOCHS = 375
    private val EPOCH_SEQUENCE_POSITION = 17
    private val START_EPOCH_POSITION = 2
    private val FINISH_EPOCH_POSITION = 16
    private val sleepArray = arrayOf("8:1", "8:0")

    val fullSleepData = ArrayList<String>()
    val epochMap = HashMap<Int, EpochSequence>()
    var startTime = 0L
    val TIME_ZONE = "GMT-0:00"
    val dateFormat = SimpleDateFormat("hh:mm:ss a")
    val hoursFormat = SimpleDateFormat("HH:mm:ss")
    var tempData: ArrayList<EpochPoint> = ArrayList()

    fun setupFile(file: File) {
        val reader = CSVReader(FileReader(file))
        var nextLine: Array<String>?
        reader.readNext() // unused values
        nextLine = reader.readNext() // first usefull line
        startTime = getStartTime(nextLine[0]) // startTime
        tempData.clear()
        readData(nextLine)
        while (reader.readNext().also { nextLine = it } != null) {
            // nextLine[] is an array of values from the line
            nextLine?.let { readData(it) }
        }
    }

    init {
        dateFormat.timeZone = TimeZone.getTimeZone(TIME_ZONE)
        hoursFormat.timeZone = TimeZone.getTimeZone(TIME_ZONE)
    }

    fun getStartTime(time: String): Long {
        val date = dateFormat.parse(time)
        return date?.time ?: 0L
    }

    fun findSleepPeriod(): SleepInformation {
        val sleepPosition = findAsleepPoint()
        val awakePoint = findAwakePoint(sleepPosition)
        val sleepHours = findSleepTimeInSeconds(sleepPosition, awakePoint)

        val startSleepTime = sleepPosition * 10 * 1000 + startTime
        val awakeTime = awakePoint * 10 * 1000 + startTime

        val sleepTime = sleepHours * 10 * 1000L

        val sleepFrame = "Sleep period - ${dateFormat.format(Date(startSleepTime))} till ${dateFormat.format(Date(awakeTime))}"
        val sleepTimeStr = "Sleep duration - ${hoursFormat.format(Date(sleepTime))}"

        return SleepInformation(startSleepTime, awakeTime, sleepTime, sleepFrame, sleepTimeStr)
    }

    fun findSleepTimeInSeconds(sleepPosition: Int, awakePosition: Int): Int {
        var sleepTime = 0
        for (position in sleepPosition..awakePosition) {
            if (isValidSleep(fullSleepData[position])) {
                sleepTime++
            }
        }
        return sleepTime
    }

    fun findAsleepPoint(): Int {
        var currentEpochSequence: EpochSequence? = null
        var validCount = 0
        run lit@{
            epochMap.forEach {
                if (it.value.percentage >= MISTAKE_PERCENT) {
                    if (validCount == 0) {
                        currentEpochSequence = it.value
                    }
                    validCount++
                    if (validCount == ENTRY_SLEEP_SEQUENCE_COUNT) {
                        return@lit
                    }
                } else {
                    currentEpochSequence = null
                    validCount = 0
                }
            }
        }
        currentEpochSequence?.let {
            val firstPoint = it.epochPointList[0]
            if (isValidSleep(firstPoint.value)) { // It could be that we missed some sleep points, so, let's find them
                for (position in firstPoint.position downTo 0) {
                    if (!isValidSleep(fullSleepData[position])) {
                        return position + 1
                    }
                }
            } else {
                it.epochPointList.forEach { epochPoint ->
                    if (isValidSleep(epochPoint.value)) {
                        return epochPoint.position
                    }
                }
            }
        }
        return -1
    }

    fun findAwakePoint(startEpochPosition: Int): Int {
        var currentEpochSequence: EpochSequence? = null
        var validCount = 0
        run lit@{
            epochMap.forEach {
                if (it.value.epochPointList[0].position < startEpochPosition) return@forEach
                if (it.value.percentage < MISTAKE_PERCENT) {
                    if (validCount == 0) {
                        currentEpochSequence = it.value
                    }
                    validCount++
                    if (validCount == ENTRY_AWAKE_SEQUENCE_COUNT) {
                        return@lit
                    }
                } else {
                    currentEpochSequence = null
                    validCount = 0
                }
            }
        }

        currentEpochSequence?.let {
            it.epochPointList.asReversed().forEach { epoachPoint ->
                if (isValidSleep(epoachPoint.value)) {
                    return epoachPoint.position + 1 // Next point is awake, so +1
                }
            }
        }
        return -1
    }

    fun isValidSleep(s: String): Boolean {
        return sleepArray.contains(s.trim())
    }

    private fun readData(lData: Array<String>) {
        for (i in START_EPOCH_POSITION..FINISH_EPOCH_POSITION) {
            val data = lData[i].trim()
            val epochPoint = EpochPoint(data, fullSleepData.size)
            fullSleepData.add(data)
            tempData.add(epochPoint)
        }
        if (tempData.size >= SLEEP_EPOCHS) {
            val tempList: ArrayList<EpochPoint> = ArrayList()
            tempList.addAll(tempData.subList(tempData.size - SLEEP_EPOCHS, tempData.size).toList())
            epochMap[epochMap.size] = EpochSequence(
                lData[EPOCH_SEQUENCE_POSITION].replace("%", "").toInt(),
                tempList
            )
        }
    }
}
