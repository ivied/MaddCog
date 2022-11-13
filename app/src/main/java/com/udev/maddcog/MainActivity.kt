package com.udev.maddcog

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.udev.maddcog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var fileCatcher: EpochFileCatcher = EpochFileCatcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.apply {
            getXls.setOnClickListener {
                selectFile()
            }
        }

        val view = binding.root
        setContentView(view)
    }

    var someActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { documentUri ->
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val file = getFileFromUri(documentUri)
                fileCatcher.setupFile(file)
                val sleepInformation = fileCatcher?.findSleepPeriod()
                sleepInformation?.let {
                    binding.apply {
                        sleepFrame.text = it.sleepPeriodStr
                        sleepTime.text = it.sleepTimeStr
                    }
                }
            }
        }
    }

    fun selectFile() {
        val intent = Intent()
            .setType("*/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setAction(Intent.ACTION_OPEN_DOCUMENT)
        someActivityResultLauncher.launch(intent)
    }
}
