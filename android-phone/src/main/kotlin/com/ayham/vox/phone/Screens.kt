package com.ayham.vox.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayham.vox.core.DialectMode
import com.ayham.vox.core.UpdateState
import com.ayham.vox.core.VoxClient
import com.ayham.vox.core.label

@Composable
fun VoxApp(vm: PhoneViewModel) {
    val state by vm.state.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = VoxColors.Black,
        bottomBar = {
            NavigationBar(containerColor = VoxColors.Card) {
                val colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VoxColors.Purple,
                    selectedTextColor = VoxColors.Purple,
                    unselectedIconColor = VoxColors.Gray,
                    unselectedTextColor = VoxColors.Gray,
                    indicatorColor = VoxColors.PurpleDeep,
                )
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Text("◉") },
                    label = { Text("الرئيسية") },
                    colors = colors,
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Text("≡") },
                    label = { Text("السجل") },
                    colors = colors,
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(VoxColors.Purple.copy(alpha = 0.18f), Color.Transparent),
                        radius = 900f,
                    )
                )
        ) {
            if (tab == 0) HomeScreen(vm, state) else HistoryScreen(state)
        }
    }
}

@Composable
private fun HomeScreen(vm: PhoneViewModel, state: PhoneUiState) {
    var host by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var custom by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("AYHAM VOX", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = VoxColors.White)
        Text("GAMING WITHOUT LANGUAGE BARRIERS.", fontSize = 11.sp, color = VoxColors.Gray)

        Card {
            Text("الاتصال بالتلفزيون", color = VoxColors.White, fontWeight = FontWeight.SemiBold)
            VoxField(host, { host = it }, "عنوان التلفزيون (مثال 192.168.1.20)", KeyboardType.Uri)
            VoxField(code, { code = it }, "رمز الاقتران (6 أرقام)", KeyboardType.Number)
            Text(statusText(state.connection), color = statusColor(state.connection), fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VoxButton("اتصال", true) { vm.connect(host, code) }
                VoxButton("قطع", false) { vm.disconnect() }
            }
        }

        Card {
            Text("لغة الترجمة", color = VoxColors.White, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialectMode.values().forEach { m ->
                    VoxButton(m.label, state.mode == m) { vm.setMode(m) }
                }
            }
        }

        Card {
            Text("التحديث", color = VoxColors.White, fontWeight = FontWeight.SemiBold)
            Text("الإصدار الحالي: build ${state.build}", color = VoxColors.Gray, fontSize = 12.sp)
            val label = state.update.label()
            if (label.isNotEmpty()) Text(label, color = VoxColors.White, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VoxButton("فحص", state.update !is UpdateState.Available) { vm.checkUpdate() }
                if (state.update is UpdateState.Available || state.update is UpdateState.NeedPermission) {
                    VoxButton("حدّث الآن", true) { vm.installUpdate() }
                }
            }
        }

        Card {
            Text("وضع التجربة (Mock)", color = VoxColors.White, fontWeight = FontWeight.SemiBold)
            Text(
                "يرسل جمل ألعاب تجريبية مترجمة إلى التلفزيون. لا يحتاج إنترنت ولا API.",
                color = VoxColors.Gray,
                fontSize = 12.sp,
            )
            VoxButton(if (state.running) "إيقاف التجربة" else "ابدأ التجربة", !state.running) {
                vm.toggleMock()
            }
            VoxField(custom, { custom = it }, "اكتب جملة إنكليزية للتجربة", KeyboardType.Text)
            VoxButton("إرسال الجملة", true) {
                vm.sendCustom(custom)
                custom = ""
            }
        }
    }
}

@Composable
private fun HistoryScreen(state: PhoneUiState) {
    if (state.history.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(20.dp)) {
            Text("لا توجد ترجمات بعد.", color = VoxColors.Gray)
        }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Spacer(Modifier.height(16.dp)) }
        items(state.history, key = { it.id }) { s ->
            Card {
                Text(s.original, color = VoxColors.Gray, fontSize = 12.sp)
                Text(s.translated, color = VoxColors.White, fontSize = 18.sp)
                Text(s.mode.label, color = VoxColors.Purple, fontSize = 11.sp)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(VoxColors.Card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}

@Composable
private fun VoxField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboard: KeyboardType,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VoxColors.Purple,
            unfocusedBorderColor = VoxColors.Gray.copy(alpha = 0.4f),
            focusedTextColor = VoxColors.White,
            unfocusedTextColor = VoxColors.White,
            cursorColor = VoxColors.Purple,
            focusedLabelColor = VoxColors.Purple,
            unfocusedLabelColor = VoxColors.Gray,
        ),
    )
}

@Composable
private fun VoxButton(text: String, highlighted: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (highlighted) VoxColors.Purple else VoxColors.PurpleDeep.copy(alpha = 0.35f),
            contentColor = VoxColors.White,
        ),
    ) { Text(text) }
}

private fun statusText(s: VoxClient.State): String = when (s) {
    VoxClient.State.DISCONNECTED -> "غير متصل"
    VoxClient.State.CONNECTING -> "جاري الاتصال..."
    VoxClient.State.CONNECTED -> "متصل بالتلفزيون ✓"
    VoxClient.State.DENIED -> "رمز الاقتران خاطئ"
}

private fun statusColor(s: VoxClient.State): Color = when (s) {
    VoxClient.State.CONNECTED -> Color(0xFF4ADE80)
    VoxClient.State.DENIED -> Color(0xFFF87171)
    else -> VoxColors.Gray
}
