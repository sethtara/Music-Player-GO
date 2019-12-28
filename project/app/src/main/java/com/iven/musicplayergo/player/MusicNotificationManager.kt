package com.iven.musicplayergo.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.iven.musicplayergo.MainActivity
import com.iven.musicplayergo.R
import com.iven.musicplayergo.ui.ThemeHelper

// Notification params
private const val CHANNEL_ID = "CHANNEL_ID_GO"
private const val REQUEST_CODE = 100
const val NOTIFICATION_ID = 101

// Notification actions
const val PREV_ACTION = "PREV_GO"
const val PLAY_PAUSE_ACTION = "PLAY_PAUSE_GO"
const val NEXT_ACTION = "NEXT_GO"
const val REPEAT_ACTION = "REPEAT_GO"
const val CLOSE_ACTION = "CLOSE_GO"

class MusicNotificationManager(private val playerService: PlayerService) {

    //notification manager/builder
    val notificationManager =
        playerService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    lateinit var notificationBuilder: NotificationCompat.Builder

    private fun playerAction(action: String): PendingIntent {

        val pauseIntent = Intent()
        pauseIntent.action = action

        return PendingIntent.getBroadcast(
            playerService,
            REQUEST_CODE,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun createNotification(): Notification {

        playerService.mediaPlayerHolder.currentSong.first.let { song ->
            notificationBuilder = NotificationCompat.Builder(playerService, CHANNEL_ID)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

            val openPlayerIntent = Intent(playerService, MainActivity::class.java)
            openPlayerIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val contentIntent = PendingIntent.getActivity(
                playerService, REQUEST_CODE,
                openPlayerIntent, 0
            )

            val style = MediaStyle().setShowActionsInCompactView(1, 2, 3)

            notificationBuilder.let {
                it.apply {
                    setShowWhen(false)
                    setStyle(style)
                    setSmallIcon(R.drawable.ic_music_note)
                    color = ThemeHelper.resolveThemeAccent(playerService)
                    setContentTitle(
                        ThemeHelper.buildSpanned(
                            playerService.getString(
                                R.string.song_title_notification,
                                song?.title
                            )
                        )
                    )
                    setContentText(
                        playerService.getString(
                            R.string.artist_and_album,
                            song?.artist,
                            song?.album
                        )
                    )
                    setContentIntent(contentIntent)
                    addAction(notificationAction(REPEAT_ACTION))
                    addAction(notificationAction(PREV_ACTION))
                    addAction(notificationAction(PLAY_PAUSE_ACTION))
                    addAction(notificationAction(NEXT_ACTION))
                    addAction(notificationAction(CLOSE_ACTION))
                    setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                }
            }
        }
        return notificationBuilder.build()
    }

    private fun notificationAction(action: String): NotificationCompat.Action {

        var icon =
            if (playerService.mediaPlayerHolder.state != PAUSED) R.drawable.ic_pause else R.drawable.ic_play

        when (action) {
            REPEAT_ACTION -> icon =
                if (playerService.mediaPlayerHolder.isRepeat) R.drawable.ic_repeat_one else R.drawable.ic_repeat_one_notif_disabled
            PREV_ACTION -> icon = R.drawable.ic_skip_previous
            NEXT_ACTION -> icon = R.drawable.ic_skip_next
            CLOSE_ACTION -> icon = R.drawable.ic_round_close
        }
        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel(
                CHANNEL_ID,
                playerService.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = playerService.getString(R.string.app_name)
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
                notificationManager.createNotificationChannel(this)
            }
        }
    }
}
