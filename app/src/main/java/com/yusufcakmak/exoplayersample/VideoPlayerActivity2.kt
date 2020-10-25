package com.yusufcakmak.exoplayersample

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.playerView
import kotlinx.android.synthetic.main.activity_video_player.progressBar
import kotlinx.android.synthetic.main.activity_video_player2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.DecimalFormat

class VideoPlayerActivity2 : Activity() {
    private val TAG = "TEST12345"

    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var coroutineScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player2)
        coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    }

    private fun initializePlayer() {
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        simpleExoPlayer.prepare(mediaSource, false, false)
        simpleExoPlayer.playWhenReady = true

        coroutineScope.launch {
            //get file size
            Log.d("debugger", convertKbToMb(getFileSize(URL(STREAM_URL)).toLong()))
        }


        mediaSource.prepareSourceInternal(object : TransferListener{
            override fun onTransferInitializing(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

            }

            override fun onTransferStart(source: DataSource?, dadebuggertaSpec: DataSpec?, isNetwork: Boolean) {

            }

            override fun onTransferEnd(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean) {

            }

            override fun onBytesTransferred(source: DataSource?, dataSpec: DataSpec?, isNetwork: Boolean, bytesTransferred: Int) {
                Log.d("debugger", bytesTransferred.toString())
            }

        })

        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = simpleExoPlayer
        playerView.requestFocus()



        /** Default repeat mode is REPEAT_MODE_OFF */
        btnChangeRepeatMode.setOnClickListener {
            when (simpleExoPlayer.repeatMode) {
                REPEAT_MODE_OFF -> simpleExoPlayer.repeatMode = REPEAT_MODE_ONE
                REPEAT_MODE_ONE -> {
                    simpleExoPlayer.repeatMode = REPEAT_MODE_ALL
                }
                else -> {
                    simpleExoPlayer.repeatMode = REPEAT_MODE_OFF
                }
            }
        }

        simpleExoPlayer.addListener( object : EventListener{
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                Log.d(TAG, "onPlaybackParametersChanged: ")
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                Log.d(TAG, "onTracksChanged: ")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Log.d(TAG, "onPlayerError: ")
            }

            /** 4 playbackState exists */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when(playbackState){
                    STATE_BUFFERING -> {
                        progressBar.visibility = View.VISIBLE
                        Log.d(TAG, "onPlayerStateChanged - STATE_BUFFERING" )
                        toast("onPlayerStateChanged - STATE_BUFFERING")
                    }
                    STATE_READY -> {
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "onPlayerStateChanged - STATE_READY" )
                        toast("onPlayerStateChanged - STATE_READY")
                    }
                    STATE_IDLE -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_IDLE" )
                        toast("onPlayerStateChanged - STATE_IDLE")
                    }
                    STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_ENDED" )
                        toast("onPlayerStateChanged - STATE_ENDED")
                    }
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.d(TAG, "onLoadingChanged: ")
            }

            override fun onPositionDiscontinuity(reason: Int) {
                Log.d(TAG, "onPositionDiscontinuity: ")
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                Log.d(TAG, "onRepeatModeChanged: ")
                Toast.makeText(baseContext, "repeat mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                Log.d(TAG, "onTimelineChanged: ")
            }
        })

    }


    private fun getFileSize(url: URL): Int {
        var conn: URLConnection? = null
        return try {
            conn = url.openConnection()
            if (conn is HttpURLConnection) {
                (conn as HttpURLConnection?)?.requestMethod = "HEAD"
            }
            conn.getInputStream()
            conn.contentLength
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            if (conn is HttpURLConnection) {
                (conn as HttpURLConnection?)?.disconnect()
            }
        }
    }



    fun convertKbToMb(size: Long): String {
        val mB: String
        val m = size / 1024.0
        val g = size / 1048576.0
        val t = size / 1073741824.0
        val decimalFormat = DecimalFormat("0.0")
        mB = when {
            t > 1 -> {
                decimalFormat.format(t) + "TB"
            }
            g > 1 -> {
                decimalFormat.format(g) + "MB"
            }
            m > 1 -> {
                decimalFormat.format(m) + "KB"
            }
            else -> {
                decimalFormat.format(size) + "GB"
            }
        }
        return mB
    }



    private fun releasePlayer() {
        simpleExoPlayer.release()
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) releasePlayer()
    }

    companion object {
        const val STREAM_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    }



}