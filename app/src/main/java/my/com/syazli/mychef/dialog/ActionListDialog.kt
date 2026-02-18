package my.com.syazli.mychef.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.color.MaterialColors
import my.com.syazli.mychef.R
import my.com.syazli.mychef.helpers.GeneralHelper

class ActionListDialog : DialogFragment()  {
    private lateinit var actionListener: ActionListener
    private var title = ""
    private var subtitle = ""
    private var actions = 0
    private var actionsString = ""
    private var dismissButton = false

    companion object {
        fun newInstance(title: String, subtitle: String, @IdRes actions: Int, actionsString: String, dismissButton: Boolean, actionListener: ActionListener): ActionListDialog {
            val actionListDialog = ActionListDialog()
            val bundle = Bundle(1)
            bundle.putString("title", title)
            bundle.putString("subtitle", subtitle)
            bundle.putInt("actions", actions)
            bundle.putString("actionsString", actionsString)
            bundle.putBoolean("dismissButton", dismissButton)
            actionListDialog.arguments = bundle
            actionListDialog.actionListener = actionListener
            return actionListDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = requireArguments().getString("title").toString()
        subtitle = requireArguments().getString("subtitle").toString()
        actions = requireArguments().getInt("actions")
        actionsString = requireArguments().getString("actionsString").toString()
        dismissButton = requireArguments().getBoolean("dismissButton")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!GeneralHelper.android5AndAbove()) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.AlertDialogTheme)


        val dialogAction: View? = activity?.layoutInflater?.inflate(R.layout.dialog_action, null)
        val tvTitle = dialogAction?.findViewById<TextView>(R.id.tv_action_title) as TextView
        val tvSubtitle = dialogAction.findViewById<TextView>(R.id.tv_action_subtitle) as TextView
        tvTitle.text = title
        tvSubtitle.text = subtitle

        if (!dismissButton) {
            builder
                .setCustomTitle(dialogAction)
                .setNegativeButton("Cancel") { dialog, id -> dialog.dismiss() }
        } else {
            builder
                .setCustomTitle(dialogAction)
        }


        if (actionsString.isNotEmpty()) {
            val tireLabels: List<String> = actionsString.split(",")
            val actionList = tireLabels.toTypedArray<CharSequence>()
            builder.setItems(actionList) { _, action ->
                actionListener.onSelected(action)
            }
        } else {
            builder.setItems(actions) { _, action ->
                actionListener.onSelected(action)
            }
        }

        val dialog = builder.create()
        val listView = dialog.listView
        val sage = ColorDrawable(MaterialColors.getColor(listView, R.attr.color_list_item_divider))
        listView.divider = sage
        listView.dividerHeight = 2

        if (!dismissButton) {
            dialog.setCanceledOnTouchOutside(true)
        } else {
            dialog.setCanceledOnTouchOutside(false)
        }
        return dialog
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

    fun interface ActionListener {
        fun onSelected(actionSelected: Int)
    }


}