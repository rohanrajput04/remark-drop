package com.inkdrop.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.inkdrop.app.ui.share.ShareResult
import com.inkdrop.app.ui.share.ShareViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    private val viewModel: ShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe result to finish activity when done
        lifecycleScope.launch {
            viewModel.result.collectLatest { result ->
                when (result) {
                    is ShareResult.Idle -> { /* waiting */ }
                    is ShareResult.Processing -> { /* show nothing, notification handles it */ }
                    is ShareResult.Success -> {
                        // Short toast for immediate feedback
                        Toast.makeText(
                            this@ShareReceiverActivity,
                            "Saving to Dropbox...",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    is ShareResult.Error -> {
                        Toast.makeText(
                            this@ShareReceiverActivity,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }

        // Process the incoming share intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                    viewModel.processSharedUrl(sharedText)
                } ?: run {
                    Toast.makeText(this, "No URL found in shared content", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            Intent.ACTION_VIEW -> {
                intent.dataString?.let { url ->
                    viewModel.processSharedUrl(url)
                } ?: run {
                    Toast.makeText(this, "No URL found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            else -> {
                Toast.makeText(this, "Unsupported action", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
