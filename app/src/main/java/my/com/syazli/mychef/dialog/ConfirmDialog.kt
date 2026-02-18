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

class ConfirmDialog : DialogFragment() {

    private var onClickListener: DialogInterface.OnClickListener? = null
    private var onNegativeClickListener: DialogInterface.OnClickListener? = null
    private var title: String? = null
    private var message: String? = null
    private var action: String? = null
    private var negativeAction: String? = null
    private var src = 0

    companion object {

        fun newInstance(title: String, message: String, action: String, negativeAction: String, onClickListener: DialogInterface.OnClickListener?, onNegativeClickListener: DialogInterface.OnClickListener?): ConfirmDialog {
            val confirmDialog: ConfirmDialog = ConfirmDialog()
            val bundle = Bundle(1)
            bundle.putString("title", title)
            bundle.putString("message", message)
            bundle.putString("action", action)
            bundle.putString("negativeAction", negativeAction)
            confirmDialog.setArguments(bundle)
            confirmDialog.onClickListener = onClickListener
            confirmDialog.onNegativeClickListener = onNegativeClickListener
            return confirmDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = requireArguments().getString("title")
        message = requireArguments().getString("message")
        action = requireArguments().getString("action")
        negativeAction = requireArguments().getString("negativeAction")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!GeneralHelper.android5AndAbove()) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.AlertDialogTheme)
        val dialogConfirm: View? = activity?.layoutInflater?.inflate(R.layout.dialog_confirm, null)
        val messageTextView = dialogConfirm?.findViewById<View>(R.id.textView_confirm_message) as TextView
        messageTextView.text = message
        builder.setTitle(title)
        builder.setPositiveButton(action, onClickListener)
        builder.setNegativeButton(negativeAction, onNegativeClickListener)
        builder.setView(dialogConfirm)
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