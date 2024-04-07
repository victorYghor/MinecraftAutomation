package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.PixelmonHomeBinding
import pixelmon.SocialMedia

class PixelmonMenuFragment: Fragment(R.layout.pixelmon_home) {
    var _binding: PixelmonHomeBinding? = null
    val binding = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PixelmonHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnDiscord.setOnClickListener {
            startActivity(SocialMedia.DISCORD.open)
        }
        binding.btnOfficialSite.setOnClickListener {
            startActivity(SocialMedia.OFFICIAL_SITE.open)
        }
        binding.btnTiktok.setOnClickListener {
            startActivity(SocialMedia.TIK_TOK.open)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}