package com.ayham.vox.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayham.vox.core.Protocol
import com.ayham.vox.core.UpdateState
import com.ayham.vox.core.label

private val Black = Color(0xFF05050A)
private val Purple = Color(0xFF7B2FF7)
private val White = Color(0xFFF5F5F7)
private val Gray = Color(0xFF9A9AA8)

@Composable
fun TvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(primary = Purple, background = Black, surface = Black),
        content = content,
    )
}

@Composable
fun TvScreen(vm: TvViewModel) {
    val state by vm.state.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Black)
            .background(
                Brush.radialGradient(
                    listOf(Purple.copy(alpha = 0.22f), Color.Transparent),
                    radius = 1400f,
                )
            )
            .padding(48.dp)
    ) {
        val sub = state.subtitle
        if (sub == null) {
            IdleInfo(state, Modifier.align(Alignment.Center))
        } else {
            Text(
                text = sub.translated,
                color = White,
                fontSize = state.fontSp.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = (state.fontSp * 1.3f).sp,
                modifier = Modifier
                    .align(if (state.atBottom) Alignment.BottomCenter else Alignment.TopCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 32.dp, vertical = 20.dp),
            )
        }
    }
}

@Composable
private fun IdleInfo(state: TvUiState, modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("AYHAM VOX", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = White)
        Text("GAMING WITHOUT LANGUAGE BARRIERS.", fontSize = 16.sp, color = Gray)

        Text("عنوان التلفزيون", fontSize = 20.sp, color = Gray)
        Text(
            state.ip?.let { "$it" } ?: "غير متصل بشبكة محلية",
            fontSize = 40.sp,
            color = White,
            fontWeight = FontWeight.SemiBold,
        )

        Text("رمز الاقتران", fontSize = 20.sp, color = Gray)
        Text(state.code, fontSize = 64.sp, color = Purple, fontWeight = FontWeight.Bold)

        Text(
            if (state.clients > 0) "الهاتف متصل ✓" else "بانتظار الهاتف... (المنفذ ${Protocol.PORT})",
            fontSize = 22.sp,
            color = if (state.clients > 0) Color(0xFF4ADE80) else Gray,
        )
        state.error?.let { Text(it, fontSize = 18.sp, color = Color(0xFFF87171)) }

        Text(
            "▲ ▼ حجم الخط (${state.fontSp})    •    OK نقل الترجمة (${if (state.atBottom) "أسفل" else "أعلى"})    •    ▶ تحديث",
            fontSize = 16.sp,
            color = Gray,
        )
        val updateLabel = state.update.label()
        Text(
            "build ${state.build}" + if (updateLabel.isNotEmpty()) "  —  $updateLabel" else "",
            fontSize = 16.sp,
            color = if (state.update is UpdateState.Available) Color(0xFF4ADE80) else Gray,
        )
    }
}
