package com.uv.taller365.helpers

import android.content.*
import android.net.Uri
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.*
import com.bumptech.glide.request.RequestOptions
import com.uv.taller365.R
import android.content.Context
import com.uv.taller365.SupabaseClient
import io.github.jan.supabase.storage.storage

suspend fun uploadImageToSupabase(uri: Uri, context: Context): String? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val bytes = inputStream.readBytes()
    val filename = "repair_${System.currentTimeMillis()}.jpg"

    val storage = SupabaseClient.client.storage
    val bucket = storage["repair-images"]

    bucket.upload(filename, bytes)

    return bucket.publicUrl(filename)
}

object ImageHelper {

    fun loadImageFromUri(
        context: Context,
        uri: Uri,
        imageView: ImageView,
        placeholder: ImageView,
        container: View,
        radius: Int
    ) {
        placeholder.setImageResource(R.drawable.ic_edit_24px)
        imageView.visibility = View.VISIBLE
        container.alpha = 0.7f

        Glide.with(context)
            .load(uri)
            .apply(
                RequestOptions.bitmapTransform(
                    MultiTransformation(CenterCrop(), RoundedCorners(radius))
                )
            )
            .placeholder(R.drawable.ic_upload_24px)
            .error(R.drawable.ic_upload_24px)
            .into(imageView)
    }

    fun loadImageFromResource(
        context: Context,
        resId: Int,
        imageView: ImageView,
        placeholder: ImageView,
        container: View
    ) {
        placeholder.setImageResource(R.drawable.ic_edit_24px)
        imageView.visibility = View.VISIBLE
        imageView.setImageResource(resId)
        container.alpha = 0.7f
    }

    fun resetImage(
        container: View,
        imageView: ImageView,
        placeholder: ImageView,
        defaultHeight: Int
    ) {
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
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
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
        iconView.setImageResource(R.drawable.camara)

        acceptButton.text = "Tomar \n foto"
        cancelButton.text = "Seleccionar de galería"

        acceptButton.setOnClickListener {
            dialog.dismiss()
            createUri()?.let(onCameraSelected)
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
