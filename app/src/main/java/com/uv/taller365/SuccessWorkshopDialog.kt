import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.uv.taller365.R

class SuccessWorkshopDialog : DialogFragment() {

    private var workshopCode: String = ""

    companion object {
        fun newInstance(code: String): SuccessWorkshopDialog {
            val args = Bundle()
            args.putString("WORKSHOP_CODE", code)
            val fragment = SuccessWorkshopDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        workshopCode = arguments?.getString("WORKSHOP_CODE") ?: ""

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_success_workshop, null)

        val tvCode = view.findViewById<MaterialTextView>(R.id.tvWorkshopCode)
        val btnCopy = view.findViewById<ShapeableImageView>(R.id.btnCopyCode)
        val btnAccept = view.findViewById<MaterialButton>(R.id.btnAccept)

        tvCode.text = workshopCode

        btnCopy.setOnClickListener {
            copyToClipboard(workshopCode)
            Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
        }

        btnAccept.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Código del taller", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}