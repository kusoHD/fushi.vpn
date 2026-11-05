package com.fushi.vpn

import android.content.Context
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.StringReader

/** WireGuard tunnelini boshqaradi. configText: wg-quick formatidagi konfiguratsiya. */
class FushiVpn(private val ctx: Context) {
    private val backend by lazy { GoBackend(ctx) }
    private val tunnel = object : Tunnel {
        override fun getName() = "fushi"
        override fun onStateChange(newState: Tunnel.State) {}
    }

    suspend fun up(configText: String) = withContext(Dispatchers.IO) {
        val cfg = Config.parse(BufferedReader(StringReader(configText)))
        backend.setState(tunnel, Tunnel.State.UP, cfg)
    }

    suspend fun down() = withContext(Dispatchers.IO) {
        backend.setState(tunnel, Tunnel.State.DOWN, null)
    }
}
