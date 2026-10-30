package com.spamguard.lite

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.spamguard.lite.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyHorizontalInsets(binding.root)
        setupNavigation(binding)
    }

    private fun setupNavigation(binding: ActivityMainBinding) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val topLevelDestinations = AppBarConfiguration(
            setOf(R.id.callLogFragment, R.id.blocklistFragment),
        )
        binding.toolbar.setupWithNavController(navController, topLevelDestinations)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    /** Thanh trên và dưới tự xử lý inset; còn lại cạnh trái/phải khi xoay ngang. */
    private fun applyHorizontalInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            view.updatePadding(left = bars.left, right = bars.right)
            insets
        }
    }
}
