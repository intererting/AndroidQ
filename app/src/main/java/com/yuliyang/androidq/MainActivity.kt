package com.yuliyang.androidq

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.Settings
import android.util.Size
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import androidx.core.content.ContextCompat.getSystemService
import android.app.NotificationChannel
import android.view.View
import android.view.View.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_STABLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }

        val inputStream = assets.open("test.jpg")
        val outFile =
            //获取沙盒地址
            File(Environment.getExternalStoragePublicDirectory("Android"), "testCopy.jpg")
        if (!outFile.exists()) {
            outFile.createNewFile()
        }
        val outputStream = FileOutputStream(outFile)
//        writeFile(inputStream = BufferedInputStream(inputStream), outputStream = BufferedOutputStream(outputStream))
        //
//        useMediaStore()
        //
//        pendingNewMediaFile()
        //
//        testVolumeName()
        //
//        testSettingPannel()

        showBubbleTest.setOnClickListener {
            //            showBubble()
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
    }

    @SuppressLint("NewApi")
    private fun showBubble() {
        val channelID = "testBubble"

        val channelName = "channel_name"

        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(channel)


        // Create bubble intent
        val target = Intent(this, BubbleActivity::class.java)
        val bubbleIntent = PendingIntent.getActivity(this, 0, target, 0 /* flags */)

// Create bubble metadata
        val bubbleData = Notification.BubbleMetadata.Builder()
            .setDesiredHeight(600)
            // Note: although you can set the icon is not displayed in Q Beta 2
            .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
            .setIntent(bubbleIntent)
            .setAutoExpandBubble(true)
            .build()

// Create notification
        val chatBot = Person.Builder()
            .setBot(true)
            .setName("BubbleBot")
            .setImportant(true)
            .build()

        val builder = Notification.Builder(this, "testBubble")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setBubbleMetadata(bubbleData)
//                .addPerson(chatBot)

        manager.notify(1, builder.build())
        manager.notify(2, builder.build())
    }

    @SuppressLint("InlinedApi")
    private fun testSettingPannel() {
        val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
        startActivityForResult(panelIntent, 100)
    }

    @SuppressLint("NewApi")
    private fun testVolumeName() {
        for (volumeName in MediaStore.getAllVolumeNames(this)) {
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

    @SuppressLint("NewApi")
    private fun useMediaStore() {
        //1:
//        val mediaThumbnail = contentResolver.loadThumbnail(item, Size(640, 480), null)
        //2:
//        contentResolver.openFileDescriptor(Uri.EMPTY, Context.MODE_PRIVATE).use { pfd ->
//            // ...
//        }

        //可以获取到媒体库文件
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val collectionWithPending = MediaStore.setIncludePending(collection)
        contentResolver.query(collectionWithPending, null, null, null).use { c ->
            c?.apply {
                if (moveToFirst()) {
                    do {
                        val title = c.getString(c.getColumnIndex("title"))
                        println("title  ${title}")
                    } while (moveToNext())
                }
                close()
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun pendingNewMediaFile() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG1024.JPG")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media
            .getContentUri(MediaStore.VOLUME_EXTERNAL)
        val item = contentResolver.insert(collection, values)
        item?.apply {
            contentResolver.openFileDescriptor(item, "w", null).use { pfd ->
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
}
