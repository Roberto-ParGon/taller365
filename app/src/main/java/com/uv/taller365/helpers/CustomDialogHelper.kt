package com.uv.taller365.helpers

import android.app.*
import android.graphics.Typeface
import android.net.Uri
import android.text.*
import android.text.style.*
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.uv.taller365.R

object CustomDialogHelper {

    fun showImagePickerDialog(
        activity: Activity,
        iconResId: Int = R.drawable.ic_camera_24px,
        onCameraSelected: (Uri) -> Unit,
        onGallerySelected: () -> Unit,
        createUri: () -> Uri?
    ) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_custom_alert, null)
        val dialog = Dialog(activity).apply {
            setContentView(view)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val title = view.findViewById<TextView>(R.id.dialogTitle)
        val message = view.findViewById<TextView>(R.id.dialogMessage)
        val icon = view.findViewById<ImageView>(R.id.dialogIcon)
        val accept = view.findViewById<Button>(R.id.dialogAcceptButton)
        val cancel = view.findViewById<Button>(R.id.dialogCancelButton)

        title.text = "Selecciona una opción"
        message.visibility = View.GONE
        icon.setImageResource(iconResId)

        accept.text = "Tomar \n foto"
        cancel.text = "Seleccionar de galería"

        accept.setOnClickListener {
            val uri = createUri()
            if (uri != null) {
                dialog.dismiss()
                onCameraSelected(uri)
            } else {
                Toast.makeText(activity, "Error al crear URI de imagen", Toast.LENGTH_SHORT).show()
            }
        }

        cancel.setOnClickListener {
            dialog.dismiss()
            onGallerySelected()
        }

        dialog.show()
        val width = (activity.resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    fun showConfirmationDialog(
        activity: Activity,
        title: String,
        message: String,
        iconResId: Int = R.drawable.ic_warning_24px,
        confirmText: String = "Aceptar",
        cancelText: String = "Cancelar",
        onConfirm: () -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_custom_alert, null)
        val dialog = Dialog(activity).apply {
            setContentView(view)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val titleView = view.findViewById<TextView>(R.id.dialogTitle)
        val messageView = view.findViewById<TextView>(R.id.dialogMessage)
        val iconView = view.findViewById<ImageView>(R.id.dialogIcon)
        val acceptButton = view.findViewById<Button>(R.id.dialogAcceptButton)
        val cancelButton = view.findViewById<Button>(R.id.dialogCancelButton)

        titleView.text = title
        val spannable = SpannableString(message)
        val start = message.indexOf("'") + 1
        val end = message.lastIndexOf("'")
        if (start in 1 until end) {
            spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(activity, R.color.blue)),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        messageView.text = spannable
        messageView.visibility = View.VISIBLE
        iconView.setImageResource(iconResId)

        acceptButton.text = confirmText
        acceptButton.setBackgroundColor(ContextCompat.getColor(activity, R.color.red))
        cancelButton.text = cancelText

        acceptButton.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
            onCancel()
        }

        dialog.show()
        val width = (activity.resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    fun showInfoDialog(
        activity: Activity,
        title: String,
        message: String,
        iconResId: Int = R.drawable.ic_warning_24px,
        buttonText: String = "Aceptar",
        onAccept: (() -> Unit)? = null
    ) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_custom_alert, null)
        val dialog = Dialog(activity).apply {
            setContentView(view)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val titleView = view.findViewById<TextView>(R.id.dialogTitle)
        val messageView = view.findViewById<TextView>(R.id.dialogMessage)
        val iconView = view.findViewById<ImageView>(R.id.dialogIcon)
        val acceptButton = view.findViewById<Button>(R.id.dialogAcceptButton)
        val cancelButton = view.findViewById<Button>(R.id.dialogCancelButton)

        titleView.text = title
        messageView.text = message
        messageView.visibility = View.VISIBLE
        iconView.setImageResource(iconResId)

        acceptButton.text = buttonText
        cancelButton.visibility = View.GONE

        acceptButton.setOnClickListener {
            dialog.dismiss()
            onAccept?.invoke()
        }

        dialog.show()
        val width = (activity.resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    fun showFilterDialog(
        activity: Activity,
        tipos: List<String>,
        selectedTipo: String? = null,
        onFilterSelected: (String) -> Unit
    ) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_filter_options, null)
        val dialog = Dialog(activity).apply {
            setContentView(view)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val radioGroup = view.findViewById<RadioGroup>(R.id.filterRadioGroup)

        val typeface = ResourcesCompat.getFont(activity, R.font.inter_medium)

        tipos.forEach { tipo ->
            val radioButton = RadioButton(activity).apply {
                text = tipo
                id = View.generateViewId()
                setTextColor(ContextCompat.getColor(context, R.color.black))
                typeface?.let { setTypeface(it) }
                textSize = 16f
            }
            radioGroup.addView(radioButton)

            if (tipo.equals(selectedTipo, ignoreCase = true)) {
                radioButton.isChecked = true
            }
        }

        view.findViewById<Button>(R.id.dialogFilterApply).setOnClickListener {
            val selectedRadioId = radioGroup.checkedRadioButtonId
            val selectedRadio = view.findViewById<RadioButton>(selectedRadioId)
            val selectedType = selectedRadio.text.toString()
            dialog.dismiss()
            onFilterSelected(selectedType)
        }

        dialog.show()
        val width = (activity.resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

}
