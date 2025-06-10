package com.uv.taller365.helpers

import android.content.*
import android.net.Uri
import android.provider.MediaStore
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.*
import com.bumptech.glide.request.RequestOptions
import com.uv.taller365.R
import android.content.Context
import com.uv.taller365.database.SupabaseClient
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
        placeholder.setImageResource(R.drawable.ic_edit_image_24px)
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
        placeholder.setImageResource(R.drawable.ic_edit_image_24px)
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
}
