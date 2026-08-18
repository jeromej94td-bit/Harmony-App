package com.example.ui.introspection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import java.util.Locale

@Composable
fun MysticBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        IntrospectionColors.TopViolet,
                        IntrospectionColors.BaseBackground,
                        Color(0xFF06010B)
                    )
                )
            )
    ) {
        // Atmospheric subtle radial glows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            IntrospectionColors.PortalViolet.copy(alpha = 0.12f),
                            IntrospectionColors.Magenta.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )
        content()
    }
}

@Composable
fun EyebrowCapsule(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("eyebrow_capsule"),
        shape = RoundedCornerShape(50),
        color = IntrospectionColors.SurfaceHighlighted.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, IntrospectionColors.PrimaryPink.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            color = IntrospectionColors.PeachLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MysticCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = IntrospectionColors.SurfaceDark,
    borderColor: Color = IntrospectionColors.PortalViolet.copy(alpha = 0.35f),
    cornerRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

@Composable
fun MysticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingEmoji: String? = null,
    testTag: String = "mystic_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.975f else 1.0f,
        animationSpec = tween(
            durationMillis = if (isPressed) 90 else 160,
            easing = FastOutSlowInEasing
        ),
        label = "btnScale"
    )

    val gradientBrush = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                IntrospectionColors.PrimaryPink,
                IntrospectionColors.Magenta
            )
        )
    } else {
        SolidColor(IntrospectionColors.SurfaceHighlighted.copy(alpha = 0.28f))
    }

    val contentAlpha = if (enabled) 1.0f else 0.28f

    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingEmoji != null) {
                Text(
                    text = leadingEmoji,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = text,
                color = IntrospectionColors.PrimaryText.copy(alpha = contentAlpha),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun MysticSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = IntrospectionColors.PortalViolet.copy(alpha = 0.45f),
    testTag: String = "mystic_sec_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = tween(if (isPressed) 90 else 160),
        label = "secBtnScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = IntrospectionColors.SurfaceHighlighted.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = IntrospectionColors.PrimaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MysticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 3,
    maxLines: Int = 6,
    onDone: () -> Unit = {}
) {
    MysticCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = IntrospectionColors.SurfaceDark,
        borderColor = if (value.isNotBlank()) IntrospectionColors.PrimaryPink.copy(alpha = 0.6f)
        else IntrospectionColors.PortalViolet.copy(alpha = 0.35f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = IntrospectionColors.SecondaryText.copy(alpha = 0.55f),
                    fontSize = 16.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mystic_text_field"),
                textStyle = TextStyle(
                    color = IntrospectionColors.PrimaryText,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(IntrospectionColors.PrimaryPink),
                minLines = minLines,
                maxLines = maxLines,
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
        }
    }
}

@Composable
fun RecordingVisualizer(
    durationMs: Long,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 60000)
    val timeFormatted = String.format(Locale.US, "%02d:%02d / 05:00", minutes, seconds)

    MysticCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = IntrospectionColors.SurfaceDark,
        borderColor = IntrospectionColors.PrimaryPink.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = IntrospectionStrings.tr(IntrospectionStringKey.RECORDING_ACTIVE_BADGE, appLanguage),
                color = IntrospectionColors.PrimaryPink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(14.dp))

            // Pulsing Mic Button Center
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing outer halo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(IntrospectionColors.PrimaryPink.copy(alpha = pulseAlpha))
                )

                // Stop button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    IntrospectionColors.PrimaryPink,
                                    IntrospectionColors.Magenta
                                )
                            )
                        )
                        .clickable(onClick = onStop)
                        .testTag("stop_recording_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = IntrospectionStrings.tr(IntrospectionStringKey.STOP_RECORDING_CD, appLanguage),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = timeFormatted,
                color = IntrospectionColors.PrimaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            TextButton(
                onClick = onDiscard,
                modifier = Modifier.testTag("discard_recording_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = IntrospectionStrings.tr(IntrospectionStringKey.DELETE_RECORDING_CD, appLanguage),
                    tint = IntrospectionColors.SecondaryText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = IntrospectionStrings.tr(IntrospectionStringKey.DELETE_RECORDING_CD, appLanguage),
                    color = IntrospectionColors.SecondaryText,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AudioPlaybackCard(
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onRecordAgain: () -> Unit,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    MysticCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = IntrospectionColors.SurfaceDark,
        borderColor = IntrospectionColors.PortalViolet.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(IntrospectionColors.PeachLight)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = IntrospectionStrings.tr(IntrospectionStringKey.AUDIO_RECORDED_BADGE, appLanguage),
                        color = IntrospectionColors.PrimaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onRecordAgain,
                    modifier = Modifier.size(36.dp).testTag("record_again_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = IntrospectionStrings.tr(IntrospectionStringKey.RECORD_AGAIN_BUTTON, appLanguage),
                        tint = IntrospectionColors.SecondaryText
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IntrospectionColors.SurfaceHighlighted)
                        .testTag("play_pause_audio_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) {
                            IntrospectionStrings.tr(IntrospectionStringKey.PAUSE_RECORDING_CD, appLanguage)
                        } else {
                            IntrospectionStrings.tr(IntrospectionStringKey.PLAY_RECORDING_CD, appLanguage)
                        },
                        tint = IntrospectionColors.PrimaryPink,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = IntrospectionColors.PrimaryPink,
                    trackColor = IntrospectionColors.SurfaceHighlighted
                )
            }
        }
    }
}

@Composable
fun ContinueOrRestartDialog(
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
    appLanguage: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IntrospectionColors.SurfaceHighlighted,
        title = {
            Text(
                text = IntrospectionStrings.tr(IntrospectionStringKey.CONTINUE_DIALOG_TITLE, appLanguage),
                color = IntrospectionColors.PrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = IntrospectionStrings.tr(IntrospectionStringKey.CONTINUE_DIALOG_MESSAGE, appLanguage),
                color = IntrospectionColors.SecondaryText,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onContinue,
                modifier = Modifier.testTag("dialog_continue_button")
            ) {
                Text(
                    text = IntrospectionStrings.tr(IntrospectionStringKey.CONTINUE_BUTTON, appLanguage),
                    color = IntrospectionColors.PrimaryPink,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dialog_cancel_button")
                ) {
                    Text(
                        text = IntrospectionStrings.tr(IntrospectionStringKey.CANCEL_BUTTON, appLanguage),
                        color = IntrospectionColors.SecondaryText
                    )
                }
                TextButton(
                    onClick = onRestart,
                    modifier = Modifier.testTag("dialog_restart_button")
                ) {
                    Text(
                        text = IntrospectionStrings.tr(IntrospectionStringKey.RESTART_BUTTON, appLanguage),
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}

@Composable
fun LeaveConfirmDialog(
    onConfirmLeave: () -> Unit,
    onDismiss: () -> Unit,
    appLanguage: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IntrospectionColors.SurfaceHighlighted,
        title = {
            Text(
                text = IntrospectionStrings.tr(IntrospectionStringKey.LEAVE_DIALOG_TITLE, appLanguage),
                color = IntrospectionColors.PrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = IntrospectionStrings.tr(IntrospectionStringKey.LEAVE_DIALOG_MESSAGE, appLanguage),
                color = IntrospectionColors.SecondaryText,
                fontSize = 15.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmLeave,
                modifier = Modifier.testTag("dialog_leave_confirm_button")
            ) {
                Text(
                    text = IntrospectionStrings.tr(IntrospectionStringKey.LEAVE_DIALOG_CONFIRM, appLanguage),
                    color = IntrospectionColors.PrimaryPink,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_leave_cancel_button")
            ) {
                Text(
                    text = IntrospectionStrings.tr(IntrospectionStringKey.LEAVE_DIALOG_CANCEL, appLanguage),
                    color = IntrospectionColors.SecondaryText
                )
            }
        }
    )
}
