package com.grunge.app

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import androidx.glance.appwidget.updateAll
import com.grunge.app.widgets.MusicWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MediaListener : NotificationListenerService() {
    private var ctl: MediaController? = null
    private val cb = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = push()
        override fun onPlaybackStateChanged(state: PlaybackState?) = push()
    }
    private val sl = MediaSessionManager.OnActiveSessionsChangedListener { attach(it?.firstOrNull()) }

    override fun onListenerConnected() {
        val msm = getSystemService(MediaSessionManager::class.java)
        val cn = ComponentName(this, MediaListener::class.java)
        msm.addOnActiveSessionsChangedListener(sl, cn)
        attach(msm.getActiveSessions(cn).firstOrNull())
    }

    private fun attach(c: MediaController?) {
        ctl?.unregisterCallback(cb); ctl = c; c?.registerCallback(cb); push()
    }

    private fun push() {
        val c = ctl
        val m = c?.metadata
        Store.put(this, "music.title", m?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Nothing playing")
        Store.put(this, "music.artist", m?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "")
        Store.put(this, "music.playing", if (c?.playbackState?.state == PlaybackState.STATE_PLAYING) "1" else "0")
        val bmp = m?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: m?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        if (bmp != null) FileOutputStream(File(filesDir, "art.png")).use {
            Bitmap.createScaledBitmap(bmp, 240, (240f * bmp.height / bmp.width).toInt().coerceAtLeast(1), true)
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        CoroutineScope(Dispatchers.Default).launch { MusicWidget().updateAll(applicationContext) }
    }

    companion object {
        fun active(ctx: Context): MediaController? = try {
            ctx.getSystemService(MediaSessionManager::class.java)
                .getActiveSessions(ComponentName(ctx, MediaListener::class.java)).firstOrNull()
        } catch (e: SecurityException) { null }
    }
}
