package blog.android.examples.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class TipsDialogFragment private constructor() : DialogFragment() {

    private var builder: AlertDialog.Builder? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder =
            builder ?: throw IllegalStateException("AlertDialog.Builder must be set.")
        return alertDialogBuilder.create()
    }

    companion object {
        fun builder(context: Context): Builder {
            return Builder(context)
        }
    }

    class Builder(private val context: Context) {

        private val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)

        fun setTitle(title: String?): Builder {
            alertDialogBuilder.setTitle(title)
            return this
        }

        fun setMessage(msg: String?): Builder {
            alertDialogBuilder.setMessage(msg)
            return this
        }

        fun setPositiveButton(
            title: String?,
            listener: DialogInterface.OnClickListener? = null
        ): Builder {
            alertDialogBuilder.setPositiveButton(title, listener)
            return this
        }

        fun setNegativeButton(
            title: String?,
            listener: DialogInterface.OnClickListener? = null
        ): Builder {
            alertDialogBuilder.setNegativeButton(title, listener)
            return this
        }

        fun create(): TipsDialogFragment {
            val alertDialogFragment = TipsDialogFragment()
            alertDialogFragment.builder = alertDialogBuilder
            return alertDialogFragment
        }
    }
}