package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.FragmentPixelmonPlayButtonBinding


/**
 *
 */
class PixelmonPlayButton : Fragment() {
    var _binding: FragmentPixelmonPlayButtonBinding? = null
    val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPixelmonPlayButtonBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.btnPlay.setOnClickListener {
            Log.d("PixelmonPlayButton", "Play button clicked")
        }
    }
}