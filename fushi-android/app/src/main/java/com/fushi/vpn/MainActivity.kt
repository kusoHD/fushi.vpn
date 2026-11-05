package com.fushi.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val vpn by lazy { FushiVpn(applicationContext) }
    private var pendingConfig: String? = null
    private val reqVpn = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            addJavascriptInterface(Bridge(), "Android")
            loadUrl("file:///android_asset/index.html")
        }
        setContentView(web)
    }

    /** JS: Android.connect(configText) / Android.disconnect() */
    inner class Bridge {
        @JavascriptInterface
        fun connect(configText: String) {
            val prep = VpnService.prepare(this@MainActivity)
            if (prep != null) {
                pendingConfig = configText
                startActivityForResult(prep, reqVpn) // tizim VPN ruxsatini so'raydi
            } else {
                startTunnel(configText)
            }
        }

        @JavascriptInterface
        fun disconnect() {
            scope.launch { vpn.down() }
        }
    }

    private fun startTunnel(cfg: String) {
        scope.launch { vpn.up(cfg) }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqVpn && resultCode == RESULT_OK) {
            pendingConfig?.let { startTunnel(it) }
            pendingConfig = null
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
