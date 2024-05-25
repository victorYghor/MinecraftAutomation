package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (progressBar.progress < 100) {
                    progressBar.progress += 10
                    handler.postDelayed(this, 100)
                } else {
                    progressBar.progress = 0
                    handler.postDelayed(this, 100)
                }
            }
        })
    }

}