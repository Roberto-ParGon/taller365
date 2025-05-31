package com.uv.taller365.helpers

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.uv.taller365.R
import java.io.ByteArrayOutputStream

object ImageHelper {

    fun uriToBase64(contentResolver: ContentResolver, uri: Uri): String? = try {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        bitmapToBase64(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    fun loadImageFromBase64(context: Context, base64: String, imageView: ImageView, placeholder: ImageView, container: View, radius: Int) {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        showImage(context, bitmap, imageView, placeholder, container, radius)
    }

    fun loadImageFromUri(context: Context, uri: Uri, imageView: ImageView, placeholder: ImageView, container: View, radius: Int) {
        placeholder.setImageResource(R.drawable.ic_edit_24px)
        imageView.visibility = View.VISIBLE
        container.alpha = 0.7f

        Glide.with(context)
            .load(uri)
            .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(radius))))
            .placeholder(R.drawable.ic_upload_24px)
            .error(R.drawable.ic_upload_24px)
            .into(imageView)
    }

    fun loadImageFromResource(context: Context, resId: Int, imageView: ImageView, placeholder: ImageView, container: View) {
        placeholder.setImageResource(R.drawable.ic_edit_24px)
        imageView.visibility = View.VISIBLE
        imageView.setImageResource(resId)
        container.alpha = 0.7f
    }

    fun resetImage(container: View, imageView: ImageView, placeholder: ImageView, defaultHeight: Int) {
        placeholder.setImageResource(R.drawable.ic_upload_24px)
        imageView.visibility = View.GONE
        container.layoutParams.height = defaultHeight
        container.requestLayout()
        container.alpha = 1f
    }

    fun createImageUri(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "repair_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun showImage(context: Context, bitmap: Bitmap, imageView: ImageView, placeholder: ImageView, container: View, radius: Int) {
        placeholder.setImageResource(R.drawable.ic_edit_24px)
        imageView.visibility = View.VISIBLE

        Glide.with(context)
            .load(bitmap)
            .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(radius))))
            .placeholder(R.drawable.ic_upload_24px)
            .error(R.drawable.ic_upload_24px)
            .into(imageView)

        container.alpha = 0.7f
    }

    fun showImagePickerDialog(
        activity: AppCompatActivity,
        createUri: () -> Uri?,
        onCameraSelected: (Uri) -> Unit,
        onGallerySelected: () -> Unit
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_custom_alert, null)
        val dialog = android.app.Dialog(activity).apply {
            setContentView(view)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val titleView = view.findViewById<TextView>(R.id.dialogTitle)
        val messageView = view.findViewById<TextView>(R.id.dialogMessage)
        val iconView = view.findViewById<ImageView>(R.id.dialogIcon)
        val acceptButton = view.findViewById<Button>(R.id.dialogAcceptButton)
        val cancelButton = view.findViewById<Button>(R.id.dialogCancelButton)

        titleView.text = "Selecciona una opción"
        messageView.visibility = View.GONE
        iconView.setImageResource(R.drawable.noimage)

        acceptButton.text = "Tomar \n foto"
        cancelButton.text = "Seleccionar de galería"

        acceptButton.setOnClickListener {
            dialog.dismiss()
            val uri = createUri()
            uri?.let { onCameraSelected(it) }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
            onGallerySelected()
        }

        dialog.show()
        val width = (activity.resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

}
