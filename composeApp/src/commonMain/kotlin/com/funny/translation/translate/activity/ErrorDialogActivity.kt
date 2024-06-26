package com.funny.translation.translate.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.BaseActivity
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.toastOnUi
import com.funny.translation.helper.trimLineStart
import com.funny.translation.kmp.appCtx
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.strings.ResStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

expect class ErrorDialogActivity : BaseActivity {
    internal var crashMessage : String?

    fun saveCrashMessage(msg: String)

    internal fun destroy()
}

private fun sendCrashReport(crashMessage: String?, actionDesc: String, contact: String){
    crashMessage?.let {
        runCatching {
            val url = OkHttpUtils.removeExtraSlashOfUrl("${ServiceCreator.BASE_URL}/api/report_crash")
            val postData = hashMapOf(
                "contact" to contact,
                "action_desc" to actionDesc,
                "text" to it.trimLineStart,
                "version" to "${AppConfig.versionName}(${AppConfig.versionCode})"
            ).map { "${it.key}=${URLEncoder.encode(it.value, "utf-8")}" }.joinToString("&")
            doPost(url, postData)
        }.onFailure {
            appCtx.toastOnUi(ResStrings.err_send_crash_report)
            it.printStackTrace()
        }
    }
}

// OkHttp 的连接池已经被关了，这里手动 HttpUrlConnection 实现 post 请求
private fun doPost(url: String, postData: String){
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    conn.setRequestProperty("Content-Length", postData.length.toString())
    conn.useCaches = false

    DataOutputStream(conn.outputStream).use { it.writeBytes(postData) }

    Log.d("doPost", "doPost:url: $url conn.code: ${conn.responseCode}")
    when (conn.responseCode){
        301, 302, 307, 308 -> doPost(conn.getHeaderField("Location"), postData)
        else -> BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                println(line)
            }
        }
    }
}

@Composable
fun ErrorDialog(
    crashMessage: String,
    destroy: SimpleAction,
) {
    var showDialog by remember {
        mutableStateOf(true)
    }
    var actionDesc by remember {
        mutableStateOf("")
    }
    var contact by remember {
        mutableStateOf("")
    }
    if (showDialog)
        AlertDialog(
            title = {
                Text(ResStrings.oops)
            },
            text = {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )) {
                    TextField(value = actionDesc, onValueChange = { actionDesc = it}, placeholder = {
                        Text(text = ResStrings.describe_your_operation)
                    })
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(value = contact, onValueChange = { contact = it}, placeholder = {
                        Text(text = ResStrings.your_contact)
                    })
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ResStrings.tip_app_crash + crashMessage,
                        fontSize = 12.sp,
                        lineHeight = 13.sp
                    )
                }

            },
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    runBlocking(Dispatchers.IO) { sendCrashReport(crashMessage, actionDesc, contact) }
                    destroy()
                }) {
                    Text(ResStrings.send_report)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    destroy()
                }) {
                    Text(ResStrings.cancel)
                }
            }
        )
}