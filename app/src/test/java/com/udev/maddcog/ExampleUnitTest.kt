package com.udev.maddcog

import io.mockk.MockKAnnotations
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    lateinit var epochFileCatcher: EpochFileCatcher

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        epochFileCatcher = EpochFileCatcher()
    }

    @Test
    fun isValid_validStringInsert_returnTrue() {
        assertEquals(true, epochFileCatcher.isValidSleep("8:0"))
        assertEquals(true, epochFileCatcher.isValidSleep("8:1"))
    }

    @Test
    fun isValid_invalidStringInsert_returnFalse() {
        assertEquals(false, epochFileCatcher.isValidSleep("7:0"))
        assertEquals(false, epochFileCatcher.isValidSleep("7:1"))
        assertEquals(false, epochFileCatcher.isValidSleep("1:1"))
    }

    @Test
    fun isValid_validStringInsertWithSpaceInsert_returnTrue() {
        assertEquals(true, epochFileCatcher.isValidSleep(" 8:0"))
        assertEquals(true, epochFileCatcher.isValidSleep(" 8:1"))
        assertEquals(true, epochFileCatcher.isValidSleep(" 8:0 "))
        assertEquals(true, epochFileCatcher.isValidSleep(" 8:1 "))
        assertEquals(true, epochFileCatcher.isValidSleep("8:0 "))
        assertEquals(true, epochFileCatcher.isValidSleep("8:1 "))
    }
}
