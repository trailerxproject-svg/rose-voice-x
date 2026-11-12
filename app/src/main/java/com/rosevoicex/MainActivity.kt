package com.rosevoicex

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private var recorder: AudioRecord? = null
    @Volatile private var recording = false

    private var status by mutableStateOf("Microphone is off")

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) startMicrophone()
            else status = "Microphone permission denied"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "ROSE VOICE X",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Voice Enhancement Studio")

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (recording) {
                                stopMicrophone()
                            } else if (
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                startMicrophone()
                            } else {
                                permissionLauncher.launch(
                                    Manifest.permission.RECORD_AUDIO
                                )
                            }
                        }
                    ) {
                        Text(if (recording) "Stop Microphone" else "Start Microphone")
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(status)
                }
            }
        }
    }

    private fun startMicrophone() {
        if (recording) return

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (bufferSize <= 0) {
            status = "Audio recording is unavailable"
            return
        }

        try {
            val audio = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            )

            if (audio.state != AudioRecord.STATE_INITIALIZED) {
                audio.release()
                status = "Could not initialize microphone"
                return
            }

            recorder = audio
            audio.startRecording()
            recording = true
            status = "Microphone is active"

            thread {
                val buffer = ShortArray(bufferSize)

                while (recording) {
                    val count = audio.read(buffer, 0, buffer.size)
                    if (count < 0) break
                }
            }
        } catch (e: Exception) {
            stopMicrophone()
            status = "Microphone error: ${e.message}"
        }
    }

    private fun stopMicrophone() {
        recording = false

        try {
            recorder?.stop()
        } catch (_: Exception) {
        }

        recorder?.release()
        recorder = null
        status = "Microphone is off"
    }

    override fun onDestroy() {
        stopMicrophone()
        super.onDestroy()
    }
}
