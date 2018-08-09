package com.example.moham.instagramapp.dialogs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.example.moham.instagramapp.R

class ConfirmPasswordDialog : DialogFragment() {
    internal lateinit var mOnConfirmPasswordListener : OnConfirmPasswordListener

    //vars
    internal lateinit var mPassword: EditText

    interface OnConfirmPasswordListener {
        fun onConfirmPassword(password: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_confirm_password, container, false)
        mPassword = view.findViewById(R.id.confirm_password)

        Log.d(TAG, "onCreateView: started.")

        val confirmDialog = view.findViewById<TextView>(R.id.dialogConfirm)
        confirmDialog.setOnClickListener {
            Log.d(TAG, "onClick: captured password and confirming.")

            val password = mPassword.text.toString()
            if (password != "") {
                mOnConfirmPasswordListener.onConfirmPassword(password)
                dialog.dismiss()
            } else {
                Toast.makeText(activity, "you must enter a password", Toast.LENGTH_SHORT).show()
            }
        }

        val cancelDialog = view.findViewById<TextView>(R.id.dialogCancel)
        cancelDialog.setOnClickListener {
            Log.d(TAG, "onClick: closing the dialog")
            dialog.dismiss()
        }


        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            mOnConfirmPasswordListener = (targetFragment as OnConfirmPasswordListener)
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.message)
        }

    }

    companion object {

        private const val TAG = "ConfirmPasswordDialog"
    }

}
