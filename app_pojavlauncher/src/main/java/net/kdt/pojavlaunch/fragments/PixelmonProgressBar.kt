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
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.FragmentPixelmonProgressBarBinding

class PixelmonProgressBar : Fragment() {
    companion object {
        const val TAG = "PixelmonProgressBar"
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
        createTimelaspProgressBar(progressBar, durantion = 3_000)
    }

    private fun createTimelaspProgressBar(progressBar: ProgressBar, durantion: Long) {
        val handler = Handler(Looper.getMainLooper())
        val interactionTime = durantion / 100L
        handler.post(object : Runnable {
            override fun run() {
                if (progressBar.progress < 100) {
                    progressBar.progress += 1
                    handler.postDelayed(this, interactionTime)
                }
            }
        })
    }

}