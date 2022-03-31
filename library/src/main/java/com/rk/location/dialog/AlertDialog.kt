package com.rk.location.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rk.location.R
import kotlinx.android.synthetic.main.dialog_confirmation.*

class AlertDialog(private val callback: (isPositive: Boolean) -> Unit) : BaseDialog(),
    View.OnClickListener {

    private var message: String? = null
    private var isAlertDialog: Boolean = false
    private var isIconVisible = View.GONE
    private var positiveText = ""
    private var negativeText = ""


    fun setMessage(message: String) {
        this.message = message
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        return inflater.inflate(R.layout.dialog_confirmation, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        imgSuccess?.visibility = isIconVisible
        txtMessage?.text = message

        btnPositive?.text =
            positiveText.takeIf { positiveText.isNotBlank() } ?: getString(R.string.yes)

        btnNegative?.text =
            negativeText.takeIf { negativeText.isNotBlank() } ?: getString(R.string.no)

        if (isAlertDialog) {
            btnAlert?.visibility = View.VISIBLE
            btnNegative?.visibility = View.GONE
            btnPositive?.visibility = View.GONE
        }

        btnPositive.setOnClickListener(this)
        btnNegative.setOnClickListener(this)
        btnAlert.setOnClickListener(this)
    }

    companion object {
        fun newInstance(
            message: String, positiveText: String, negativeText: String, isAlert: Boolean = false,
            callback: (isPositive: Boolean) -> Unit
        ): AlertDialog {
            val fragment = AlertDialog(callback)
            fragment.message = message
            fragment.isAlertDialog = isAlert
            fragment.positiveText = positiveText
            fragment.negativeText = negativeText
            return fragment
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btnPositive -> {
                callback.invoke(true)
            }
            btnAlert -> {
                callback.invoke(true)
            }
            btnNegative -> {
                callback.invoke(false)
            }
        }
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}