package com.example.foodordering.adminapp.Service // Thay thế bằng package thực tế của bạn trong app Admin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat // R file của app Admin
import com.example.adminfoodordering.MainActivity
import com.example.adminfoodordering.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyAdminFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "AdminFCMService"

    /**
     * Được gọi khi một tin nhắn FCM mới được nhận.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message From: ${remoteMessage.from}")

        var notificationTitle: String? = null
        var notificationBody: String? = null
        var orderId: String? = null // Để lưu orderId nếu có

        // Ưu tiên xử lý 'data' payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload for Admin: " + remoteMessage.data)
            notificationTitle = remoteMessage.data["title"]
            notificationBody = remoteMessage.data["body"]
            orderId = remoteMessage.data["orderId"] // Lấy orderId từ data payload
            // Bạn có thể lấy thêm các dữ liệu khác như customerName, totalAmount, notificationType
            // val notificationType = remoteMessage.data["notificationType"]
            // if (notificationType == "NEW_ORDER") { /* Xử lý đặc biệt cho đơn hàng mới */ }
        }

        // Xử lý 'notification' payload nếu có và 'data' payload không cung cấp thông tin
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body for Admin: ${it.body}")
            if (notificationTitle == null) notificationTitle = it.title
            if (notificationBody == null) notificationBody = it.body
        }

        // Chỉ hiển thị thông báo nếu có tiêu đề và nội dung
        if (notificationTitle != null && notificationBody != null) {
            sendAdminSystemNotification(notificationTitle.toString(), notificationBody.toString(), orderId)
        } else {
            Log.d(TAG, "Admin Notification title or body is null. Not showing notification.")
        }
    }

    /**
     * Được gọi khi một token mới được tạo hoặc token hiện tại được làm mới.
     * Gửi token này lên Firebase Realtime Database.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token for Admin app: $token")
        sendAdminFCMTokenToDatabase(token)
    }

    /**
     * Tạo và hiển thị một thông báo hệ thống đơn giản.
     */
    private fun sendAdminSystemNotification(title: String, messageBody: String, orderId: String?) {
        // Intent sẽ được kích hoạt khi người dùng nhấn vào thông báo
        val intent = Intent(this, MainActivity::class.java) // Mở MainActivity của app Admin
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Nếu có orderId, bạn có thể truyền nó qua Intent để MainActivity xử lý
        if (orderId != null) {
            intent.putExtra("navigateToOrderDetails", orderId) // Key để MainActivity nhận biết
        }

        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        // Sử dụng orderId (hoặc một giá trị ngẫu nhiên khác) làm requestCode để tạo PendingIntent khác nhau
        // nếu bạn muốn các thông báo khác nhau mở ra các nội dung khác nhau hoặc không ghi đè lên nhau.
        val requestCode = orderId?.hashCode() ?: System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, pendingIntentFlag)

        val channelId = getString(R.string.admin_notification_channel_id) // Lấy từ strings.xml của app Admin
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.bell) // Tạo icon này trong drawable của app Admin
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true) // Tự động xóa thông báo khi người dùng nhấn vào
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao để hiện head-up notification
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Kể từ Android Oreo (API 26), Notification Channel là bắt buộc.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Thông báo Admin" // Tên channel hiển thị cho người dùng trong cài đặt app
            val channelDescription = "Thông báo quan trọng cho quản trị viên"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Hiển thị thông báo. Sử dụng một ID thông báo duy nhất nếu bạn muốn cập nhật thông báo này sau.
        // Sử dụng System.currentTimeMillis().toInt() để tạo ID khác nhau cho mỗi thông báo mới.
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val COMPANION_TAG = "AdminFCMStaticUtil"

        /**
         * Lưu FCM token của Admin vào Firebase Realtime Database.
         * Hàm này có thể được gọi từ onNewToken hoặc sau khi admin đăng nhập thành công.
         */
        fun sendAdminFCMTokenToDatabase(token: String?) {
            // Giả sử admin đăng nhập bằng Firebase Authentication và bạn lấy được adminId (UID)
            val adminId = FirebaseAuth.getInstance().currentUser?.uid

            if (adminId != null && token != null) {
                // Đường dẫn lưu token: /admin/{adminId}/fcmToken
                // (Phù hợp với cấu trúc bạn đã cung cấp: admin -> 0MSW1f5Kk2YDX43GV6qrneCxSoJ3 -> fcmToken)
                val tokenRef = FirebaseDatabase.getInstance().getReference("admin")
                    .child(adminId)
                    .child("fcmToken")

                tokenRef.setValue(token)
                    .addOnSuccessListener {
                        Log.i(COMPANION_TAG, "FCM Token for admin $adminId updated successfully in Database.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(COMPANION_TAG, "Failed to update FCM Token for admin $adminId: ${e.message}")
                    }
            } else {
                Log.w(COMPANION_TAG, "Cannot send Admin FCM token: Admin not logged in (adminId is null) or token is null.")
            }
        }
    }
}
