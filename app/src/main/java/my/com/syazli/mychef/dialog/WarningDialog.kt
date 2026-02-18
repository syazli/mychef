package my.com.syazli.mychef.dialog


import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import my.com.syazli.mychef.R
import my.com.syazli.mychef.helpers.GeneralHelper

class WarningDialog : DialogFragment() {

    private var onClickListener: DialogInterface.OnClickListener? = null
    private var title: String? = null
    private var message: String? = null
    private var action: String? = null
    private var src = 0

    companion object {

        fun newInstance(message: String?, title: String?, onClickListener: DialogInterface.OnClickListener?): WarningDialog {
            val warningInfoDialog: WarningDialog = WarningDialog()
            val bundle = Bundle(1)
            bundle.putString("message", message)
            bundle.putString("title", title)
            warningInfoDialog.setArguments(bundle)
            warningInfoDialog.onClickListener = onClickListener
            return warningInfoDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        src = requireArguments().getInt("src")
        title = requireArguments().getString("title")
        message = requireArguments().getString("message")
        action = requireArguments().getString("action")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!GeneralHelper.android5AndAbove()) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
        val dialogWarning: View? = activity?.layoutInflater?.inflate(R.layout.dialog_warning, null)
        val messageTextView = dialogWarning?.findViewById<View>(R.id.textView_warning_message) as TextView
        messageTextView.text = message
        builder.setView(dialogWarning)
        builder.setPositiveButton(getString(R.string.ok), onClickListener)
        builder.setTitle(title)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(getDialogWidth(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun getDialogWidth(): Int {
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels
        return (screenWidth * 0.90).toInt()
    }
    override fun onPause() {
        super.onPause()
        this.dismiss()
    }
}