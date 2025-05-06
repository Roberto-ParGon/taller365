import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.uv.taller365.R

class ConfirmDeleteDialog : DialogFragment() {

    private var title: String = ""
    private var message: String = ""
    private var cancelText: String = ""
    private var deleteText: String = ""
    private var listener: OnDeleteListener? = null

    interface OnDeleteListener {
        fun onDeleteConfirmed()
    }

    companion object {
        fun newInstance(
            title: String = "Confirmar eliminación",
            message: String,
            cancelText: String = "Cancelar",
            deleteText: String = "Eliminar"
        ): ConfirmDeleteDialog {
            val dialog = ConfirmDeleteDialog()
            val args = Bundle().apply {
                putString("title", title)
                putString("message", message)
                putString("cancelText", cancelText)
                putString("deleteText", deleteText)
            }
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        title = arguments?.getString("title") ?: title
        message = arguments?.getString("message") ?: message
        cancelText = arguments?.getString("cancelText") ?: cancelText
        deleteText = arguments?.getString("deleteText") ?: deleteText

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_confirm_delete, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val btnCancel = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
        val btnDelete = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDelete)

        tvTitle.text = title
        tvMessage.text = message
        btnCancel.text = cancelText
        btnDelete.text = deleteText

        btnCancel.setOnClickListener { dismiss() }
        btnDelete.setOnClickListener {
            listener?.onDeleteConfirmed()
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    fun setDeleteListener(listener: OnDeleteListener) {
        this.listener = listener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}

/*

val dialog = ConfirmDeleteDialog.newInstance(
    title = "Confirmar eliminación de taller",
    message = "¿Estás seguro que deseas eliminar al taller \"Nombre del taller\"?"
)

dialog.setDeleteListener(object : ConfirmDeleteDialog.OnDeleteListener {
    override fun onDeleteConfirmed() {
        Toast.makeText(context, "Taller eliminado", Toast.LENGTH_SHORT).show()
    }
})

dialog.show(parentFragmentManager, "ConfirmDeleteDialog")
 */