package my.com.syazli.mychef.helpers

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import my.com.syazli.mychef.dialog.ActionListDialog
import my.com.syazli.mychef.dialog.ConfirmDialog
import my.com.syazli.mychef.dialog.WarningDialog

class DialogHelper {
    private var dialogFragment: DialogFragment? = null

    fun showActionListDialog(context: Context?, title: String, subtitle: String, actions: Int, actionsString: String, actionListener: ActionListDialog.ActionListener, dismissButton: Boolean = false, ) {
        if (context != null) {
            if (dialogFragment != null) {
                try {
                    dialogFragment!!.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            dialogFragment = ActionListDialog.newInstance(title, subtitle, actions, actionsString, dismissButton, actionListener)
            dialogFragment!!.isCancelable = true
            try {
                dialogFragment!!.show((context as AppCompatActivity).supportFragmentManager, "ActionListDialog")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showConfirmDialog(context: Context?, title: String, message: String, action: String, negativeAction: String, onClickListener: DialogInterface.OnClickListener?, onNegativeClickListener: DialogInterface.OnClickListener?){
        if (context != null){
            if (dialogFragment != null){
                try {
                   dialogFragment!!.dismiss()
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
            dialogFragment = ConfirmDialog.newInstance(title, message, action, negativeAction, onClickListener, onNegativeClickListener)
            dialogFragment!!.isCancelable = false
            try {
                dialogFragment!!.show((context as AppCompatActivity).supportFragmentManager, "ConfirmDialog")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showWarningDialog(context: Context?, title: String, message: String, onClickListener: DialogInterface.OnClickListener?){
        if (context != null){
            if (dialogFragment != null){
                try {
                   dialogFragment!!.dismiss()
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
            dialogFragment = WarningDialog.newInstance(message, title, onClickListener)
            dialogFragment!!.isCancelable = false
            try {
                dialogFragment!!.show((context as AppCompatActivity).supportFragmentManager, "WarningDialog")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}