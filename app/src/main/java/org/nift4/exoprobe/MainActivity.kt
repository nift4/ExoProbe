package org.nift4.exoprobe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaLibraryInfo
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import org.nift4.exoprobe.ui.theme.ExoProbeTheme

class MainActivity : ComponentActivity(), Player.Listener {
	private lateinit var player: ExoPlayer
	private val handler = Handler(Looper.getMainLooper())
	private val stopPlaybackRunnable = Runnable {
		player.stop()
	}
	private val trackState = MutableStateFlow<Tracks?>(null)
	private val errorState = MutableStateFlow<PlaybackException?>(null)
	private val resultReceiver = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
		if (it != null) {
			player.setMediaItem(MediaItem.fromUri(it))
			player.prepare()
			player.play()
		}
	}

	@OptIn(UnstableApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		player = ExoPlayer.Builder(this).build()
		player.volume = 0f
		player.addListener(this)
		enableEdgeToEdge()
		setContent {
			ExoProbeTheme {
				Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					val stateV = rememberScrollState()
					val tracksAsState = trackState.collectAsState()
					val errorAsState = errorState.collectAsState()
					Column(modifier = Modifier
						.fillMaxSize()
						.verticalScroll(stateV).padding(4.dp)
						.padding(innerPadding)) {
						Text("Media3 ExoPlayer ${MediaLibraryInfo.VERSION}")
						Button(onClick = {
							resultReceiver.launch(arrayOf("*/*"))
						}) {
							Text("Open file...")
						}
						Spacer(Modifier.size(5.dp))
						val tracks = tracksAsState.value
						val error = errorAsState.value

						if (tracks == null && error == null) {
							Text("Select something to get started")
						} else if (tracks != null) {
							Text("Results: (${tracks.groups.size} tracks total)")
							var i = 0
							for (trackGroup in tracks.groups) {
								Card(Modifier.padding(8.dp)) {
									Column(Modifier
										.fillMaxWidth()
										.padding(8.dp)) {
										Text(
											text = "Track group #${i}",
											style = MaterialTheme.typography.headlineLarge
										)
										Text(
											"Type: ${
												when (trackGroup.type) {
													C.TRACK_TYPE_NONE -> "None"
													C.TRACK_TYPE_TEXT -> "Text"
													C.TRACK_TYPE_AUDIO -> "Audio"
													C.TRACK_TYPE_IMAGE -> "Image"
													C.TRACK_TYPE_VIDEO -> "Video"
													C.TRACK_TYPE_CAMERA_MOTION -> "Camera Motion"
													C.TRACK_TYPE_DEFAULT -> "Default"
													C.TRACK_TYPE_METADATA -> "Metadata"
													C.TRACK_TYPE_UNKNOWN -> "Unknown"
													else -> "? (${trackGroup.type})"
												}
											}"
										)
										Text("Group selected: ${trackGroup.isSelected}")
										Text("Group supported: ${trackGroup.isSupported(false)}")
										Text(
											"Group supported if exceeding capabilities: ${
												trackGroup.isSupported(
													true
												)
											}"
										)
										Text("Group is adaptive supported: ${trackGroup.isAdaptiveSupported}")
										for (j in 0..<trackGroup.length) {
											val trackFormat = trackGroup.getTrackFormat(j)
											val trackSupport =
												when (trackGroup.getTrackSupport(j)) {
													C.FORMAT_HANDLED -> "Format handled"
													C.FORMAT_UNSUPPORTED_DRM -> "Format not supported due to unsupported DRM"
													C.FORMAT_UNSUPPORTED_TYPE -> "Format not supported due to unsupported type"
													C.FORMAT_UNSUPPORTED_SUBTYPE -> "Format not supported due to unsupported subtype"
													C.FORMAT_EXCEEDS_CAPABILITIES -> "Format exceeds capabilities"
													else -> "? (${trackGroup.getTrackSupport(j)})"
												}
											val trackIsSelected = trackGroup.isTrackSelected(j)
											Card(
												Modifier.padding(8.dp),
												elevation = CardDefaults.elevatedCardElevation(),
												colors = CardDefaults.elevatedCardColors()
											) {
												Column(Modifier
													.fillMaxWidth()
													.padding(8.dp)) {
													Text(
														"Track #$j (group #$i)",
														style = MaterialTheme.typography.headlineMedium
													)
													Text("Track is selected: $trackIsSelected")
													Text("Track is supported: $trackSupport")
													Text("Track format: ${Format.toLogString(trackFormat)}") // TODO
												}
											}
										}
									}
								}
								i++
							}
						}
						if (error != null) {
							Card(Modifier.padding(8.dp)) {
								Column(
									Modifier
										.fillMaxWidth()
										.padding(8.dp)
								) {
									Text(
										text = "An error occurred",
										style = MaterialTheme.typography.headlineLarge
									)
									Text(Log.getStackTraceString(error))
								}
							}
						}
					}
				}
			}
		}
	}

	override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
		trackState.value = null
	}

	override fun onTracksChanged(tracks: Tracks) {
		handler.removeCallbacks(stopPlaybackRunnable)
		trackState.value = tracks
	}

	override fun onPlayerErrorChanged(error: PlaybackException?) {
		errorState.value = error
	}

	override fun onPlaybackStateChanged(playbackState: Int) {
		handler.removeCallbacks(stopPlaybackRunnable)
		if (playbackState == Player.STATE_READY) {
			handler.postDelayed(stopPlaybackRunnable, 5000)
		}
	}
}