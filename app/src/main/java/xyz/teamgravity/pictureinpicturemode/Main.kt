package xyz.teamgravity.pictureinpicturemode

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.viewinterop.AndroidView
import xyz.teamgravity.pictureinpicturemode.ui.theme.PictureInPictureModeTheme

class Main : ComponentActivity() {

    companion object {
        private const val BABY_CHANGING_STATION_REQUEST = 0x7878
    }

    private val pipSupported: Boolean by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        else false
    }

    private var viewBounds = Rect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PictureInPictureModeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(
                        factory = { context ->
                            VideoView(context, null).apply {
                                setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.tate}"))
                                start()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                viewBounds = coordinates
                                    .boundsInWindow()
                                    .toAndroidRect()
                            }
                    )
                }
            }
        }
    }

    private fun pipParams(): PictureInPictureParams? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return PictureInPictureParams.Builder()
            .setSourceRectHint(viewBounds)
            .setAspectRatio(Rational(16, 9))
            .setActions(
                listOf(
                    RemoteAction(
                        Icon.createWithResource(
                            applicationContext,
                            R.drawable.ic_baby_changing_station
                        ),
                        getString(R.string.baby_changing_station),
                        getString(R.string.baby_changing_station),
                        PendingIntent.getBroadcast(
                            applicationContext,
                            BABY_CHANGING_STATION_REQUEST,
                            Intent(applicationContext, BabyChangingStationReceiver::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                )
            ).build()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (!pipSupported) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        pipParams()?.let { enterPictureInPictureMode(it) }
    }

    class BabyChangingStationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("raheem: Congratulations, baby is a TOP G!")
        }
    }
}
