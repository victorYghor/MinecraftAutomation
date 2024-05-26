package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import net.kdt.pojavlaunch.databinding.FragmentPixelmonProgressBarBinding
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import pixelmon.Loading
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class PixelmonProgressBar : Fragment() {
    companion object {
        const val TAG = "PixelmonProgressBar"

        @JvmStatic
        var currentProcess: Loading? = null
        @JvmStatic
        var duration: Long? = null
    }

    var _binding: FragmentPixelmonProgressBarBinding? = null
    val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPixelmonProgressBarBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = b.progressBarPixelmonHome
        Log.d(TAG, "duration: $duration, currentProcess: $currentProcess")
        if(duration != null && currentProcess != null) {
                createTimelaspProgressBar(progressBar, duration!!, currentProcess!!)
        } else {
            Log.d(TAG, "Ocorreu um erro ao tentar criar a barra de progresso")
            Toast.makeText(requireContext(), "Ocorreu um erro ao tentar criar a barra de progresso", Toast.LENGTH_LONG).show()
        }
    }

    private fun createTimelaspProgressBar(progressBar: ProgressBar, durantion: Long, process: Loading) {
        val handler = Handler(Looper.getMainLooper())
        val interactionTime = durantion / 100L
        handler.post(object : Runnable {
            override fun run() {
                if (progressBar.progress < 100) {
                    progressBar.progress += 1
                    handler.postDelayed(this, interactionTime)
                } else {
                    try {
                        Toast.makeText(requireContext(), "O app completou de ${process.messageLoading}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao tentar mostrar a mensagem de conclusão do processo")
                    }
                    Log.e(TAG, "finish of the download process")
                    // handle when the process is finished
                    when {
                        process == Loading.MOVING_FILES ->  {
                            ExtraCore.setValue(ExtraConstants.LOADING_INTERNAL, Loading.DOWNLOAD_MOD_ONE_DOT_TWELVE)
                        }
                        process == Loading.DOWNLOAD_ONE_DOT_SIXTEEN ->  {
                            ExtraCore.setValue(ExtraConstants.LOADING_INTERNAL, Loading.DOWNLOAD_MOD_ONE_DOT_SIXTEEN)
                        }
                    }
                }
            }
        })
    }

}