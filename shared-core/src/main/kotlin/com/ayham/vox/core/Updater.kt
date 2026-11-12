package com.ayham.vox.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class UpdateInfo(val build: Int, val url: String)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data class Downloading(val percent: Int) : UpdateState()
    object NeedPermission : UpdateState()
    object Installing : UpdateState()
    data class Error(val message: String) : UpdateState()
}

fun UpdateState.label(): String = when (this) {
    UpdateState.Idle -> ""
    UpdateState.Checking -> "جاري فحص التحديثات..."
    UpdateState.UpToDate -> "التطبيق محدّث ✓"
    is UpdateState.Available -> "يوجد تحديث جديد (build ${info.build})"
    is UpdateState.Downloading -> "جاري التحميل $percent%"
    UpdateState.NeedPermission -> "فعّل «السماح بالتثبيت» ثم اضغط تحديث مرة ثانية"
    UpdateState.Installing -> "جاري فتح المثبّت..."
    is UpdateState.Error -> "تعذّر التحديث: $message"
}

/**
 * يفحص آخر Release على GitHub (الوسم build-N) ويحمّل الـ APK ويفتح المثبّت.
 * التحديث يشتغل فقط إذا كان التوقيع نفس توقيع التطبيق المثبّت (نفس keystore).
 */
class UpdateController(
    private val context: Context,
    private val assetName: String,
    private val onChange: (UpdateState) -> Unit,
) {
    private val worker = Executors.newSingleThreadExecutor()
    @Volatile private var latest: UpdateInfo? = null

    fun installedBuild(): Int {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    }

    fun check() {
        onChange(UpdateState.Checking)
        worker.execute {
            try {
                val info = fetchLatest()
                if (info != null && info.build > installedBuild()) {
                    latest = info
                    onChange(UpdateState.Available(info))
                } else {
                    onChange(UpdateState.UpToDate)
                }
            } catch (e: Exception) {
                onChange(UpdateState.Error(e.message ?: "خطأ شبكة"))
            }
        }
    }

    /** يحمّل آخر نسخة ويفتح المثبّت. يحتاج صلاحية «تثبيت تطبيقات غير معروفة». */
    fun downloadAndInstall() {
        val info = latest ?: return check()
        if (Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()) {
            onChange(UpdateState.NeedPermission)
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }
        worker.execute {
            try {
                val file = download(info)
                onChange(UpdateState.Installing)
                install(file)
            } catch (e: Exception) {
                onChange(UpdateState.Error(e.message ?: "فشل التحميل"))
            }
        }
    }

    private fun fetchLatest(): UpdateInfo? {
        val conn = URL("https://api.github.com/repos/$REPO/releases/latest")
            .openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "ayham-vox-updater")
        try {
            if (conn.responseCode != 200) return null
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val build = json.optString("tag_name").removePrefix("build-").toIntOrNull() ?: return null
            val assets = json.optJSONArray("assets") ?: return null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.optString("name") == assetName) {
                    return UpdateInfo(build, a.getString("browser_download_url"))
                }
            }
            return null
        } finally {
            conn.disconnect()
        }
    }

    private fun download(info: UpdateInfo): File {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "update.apk")
        val conn = URL(info.url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        conn.setRequestProperty("User-Agent", "ayham-vox-updater")
        try {
            val total = conn.contentLength.toLong()
            var done = 0L
            var lastPercent = -1
            conn.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        output.write(buffer, 0, n)
                        done += n
                        if (total > 0) {
                            val p = (done * 100 / total).toInt()
                            if (p != lastPercent) {
                                lastPercent = p
                                onChange(UpdateState.Downloading(p))
                            }
                        }
                    }
                }
            }
        } finally {
            conn.disconnect()
        }
        return file
    }

    private fun install(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.vox.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    companion object {
        const val REPO = "AyhamAmerAlj/ayham-vox"
    }
}
