package com.yuliyang.androidq

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yuliyang.androidq.databinding.ActivityMainBinding
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }

//        useMediaStore()

//        pendingNewMediaFile()
        //
        testVolumeName()
        //
//        testSettingPannel()

        binding.showBubbleTest.setOnClickListener {
            showBubble()

//            pendingNewMediaFile()

//            startActivity(Intent(this@MainActivity, SecondActivity::class.java))

//            queryMediaStore()
        }
    }

    @SuppressLint("NewApi")
    //$ git clone git@github.com:googlecodelabs/android-people.git
    @Deprecated("详情查看上述地址")
    private fun showBubble() {
        val channelID = "testBubble"

        val channelName = "channel_name"

        val channel =
            NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(channel)


        // Create bubble intent
        val target = Intent(this, BubbleActivity::class.java)
        val bubbleIntent = PendingIntent.getActivity(this, 0, target, FLAG_MUTABLE /* flags */)

// Create bubble metadata
        val bubbleData = Notification.BubbleMetadata.Builder(
            bubbleIntent, Icon.createWithResource(this, R.mipmap.ic_launcher)
        ).setDesiredHeight(600)
            // Note: although you can set the icon is not displayed in Q Beta 2
            .setAutoExpandBubble(true).build()

// Create notification
        val chatBot = Person.Builder().setBot(true).setName("BubbleBot").setImportant(true).build()

        val builder = Notification.Builder(this, "testBubble").setSmallIcon(R.mipmap.ic_launcher)
            .setBubbleMetadata(bubbleData).addPerson(chatBot)

        manager.notify(1, builder.build())
    }

    @SuppressLint("InlinedApi")
    private fun testSettingPannel() {
        val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
        startActivityForResult(panelIntent, 100)
    }

    @SuppressLint("NewApi")
    private fun testVolumeName() {
        for (volumeName in MediaStore.getExternalVolumeNames(this)) {
            println("volumeName ${volumeName}")
        }
    }

    private fun writeFile(inputStream: BufferedInputStream, outputStream: BufferedOutputStream) {
        try {
            var hasRead: Int
            val basket = ByteArray(1024)
            while (inputStream.read(basket).also {
                    hasRead = it
                } != -1) {
                outputStream.write(basket, 0, hasRead)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }

/*    val urisToModify = listOf(uri,uri,...)
val editPendingIntent = MediaStore.createWriteRequest(contentResolver,
        urisToModify)

// 申请权限
startIntentSenderForResult(editPendingIntent.intentSender, EDIT_REQUEST_CODE,
    null, 0, 0, 0)

override fun onActivityResult(requestCode: Int, resultCode: Int,data: Intent?) {
    when (requestCode) {
        EDIT_REQUEST_CODE ->
            if (resultCode == Activity.RESULT_OK) {
                /*获得权限*/
            } else {
                /*未获得权限*/
            }
    }
}
*/


    @SuppressLint("NewApi")
    //https://developer.android.google.cn/training/data-storage/shared/media#query-collection
    private fun useMediaStore() {
        //1:
//        val mediaThumbnail = contentResolver.loadThumbnail(item, Size(640, 480), null)
        //2:
//        contentResolver.openFileDescriptor(Uri.EMPTY, Context.MODE_PRIVATE).use { pfd ->
//            // ...
//        }

        //可以获取到媒体库文件

        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_INTERNAL)
        //用户获取图片的原始信息
//        val collectionWithPending = MediaStore.setRequireOriginal(collection)
        contentResolver.query(collection, null, null, null).use { c ->
            c?.apply {
                if (moveToFirst()) {
                    do {
                        val id =
                            //_ID可以替代_DATA的作用
                            c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns._ID))
                    } while (moveToNext())
                }
                close()
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun pendingNewMediaFile() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "test_insert.jpg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/wechat")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val item = contentResolver.insert(collection, values)
        item?.apply {
            val pfd = contentResolver.openFileDescriptor(item, "w", null).use { pfd ->
                // Write data into the pending image.
                pfd?.apply {
                    val inputStream = assets.open("pendingTest.jpg")
                    val outputStream = ParcelFileDescriptor.AutoCloseOutputStream(this)
                    writeFile(
                        inputStream = BufferedInputStream(inputStream),
                        outputStream = BufferedOutputStream(outputStream)
                    )
                }
            }

// Now that we're finished, release the "pending" status, and allow other apps
// to view the image.
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(item, values, null, null)
            pfd?.close()
        }

    }

    override fun onTopResumedActivityChanged(topResumed: Boolean) {
        if (topResumed) {
            // Top resumed activity
            // Can be a signal to re-acquire exclusive resources
        } else {
            // No longer the top resumed activity
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryMediaStore() {
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        //这个id可以通过contentResolver获取
        val url = ContentUris.withAppendedId(collection, 32)
        val inputStream = contentResolver.openInputStream(url)
        val outFile = File(cacheDir, "query_test.jpg")
        if (!outFile.exists()) {
            outFile.createNewFile()
        }
        val outputStream = FileOutputStream(outFile)
        writeFile(
            inputStream = BufferedInputStream(inputStream), BufferedOutputStream(outputStream)
        )
    }
}
