package com.example.coficall

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.coficall.data.DataRepository
import com.example.coficall.data.DefaultDataRepository
import com.example.coficall.theme.CofiCallTheme
import com.example.coficall.ui.MainViewModel
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var repository: DataRepository
    private lateinit var viewModel: MainViewModel
    private var downloadId: Long = -1L

    private val onDownloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId && downloadId != -1L) {
                installApk()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        android.util.Log.d("CofiCallDebug", "MainActivity onCreate iniciado")
        repository = DefaultDataRepository(applicationContext)
        android.util.Log.d("CofiCallDebug", "MainActivity: Repositório criado")
        viewModel = MainViewModel(repository)
        android.util.Log.d("CofiCallDebug", "MainActivity: ViewModel criado")
        
        // Registar o Receiver para download completo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                onDownloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(
                onDownloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        enableEdgeToEdge()
        setContent {
            android.util.Log.d("CofiCallDebug", "MainActivity: setContent executando")
            CofiCallTheme(darkTheme = viewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainNavigation(
                        viewModel = viewModel,
                        onTriggerUpdate = { url -> startApkDownload(url) }
                    )
                }
            }
        }
    }

    private fun startApkDownload(url: String) {
        try {
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Atualização do CofiCall")
                setDescription("A transferir nova versão da aplicação...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                val destinationFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CofiCallUpdate.apk")
                if (destinationFile.exists()) {
                    destinationFile.delete()
                }
                setDestinationUri(Uri.fromFile(destinationFile))
            }
            downloadId = dm.enqueue(request)
            android.widget.Toast.makeText(this, "A transferir atualização em segundo plano...", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.util.Log.e("CofiCallDebug", "Erro ao iniciar download", e)
            android.widget.Toast.makeText(this, "Erro ao descarregar atualização: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun installApk() {
        val apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CofiCallUpdate.apk")
        if (!apkFile.exists()) {
            android.widget.Toast.makeText(this, "Ficheiro de instalação não encontrado.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(installIntent)
        } catch (e: Exception) {
            android.util.Log.e("CofiCallDebug", "Erro ao instalar APK", e)
            android.widget.Toast.makeText(this, "Erro ao iniciar instalação: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(onDownloadCompleteReceiver)
        } catch (e: Exception) {
            // Ignorar se já não estiver registado
        }
    }
}
