package com.ayham.vox.tv

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    private val vm: TvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvTheme {
                TvScreen(vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshIp()
    }

    /** ▲ ▼ = حجم الخط، OK = نقل الترجمة بين الأعلى والأسفل. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { vm.biggerFont(); true }
            KeyEvent.KEYCODE_DPAD_DOWN -> { vm.smallerFont(); true }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> { vm.togglePosition(); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
