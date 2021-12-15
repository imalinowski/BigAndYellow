package com.malinowski.bigandyellow.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.model.data.AddEmoji
import com.malinowski.bigandyellow.model.data.emojiMap
import com.malinowski.bigandyellow.view.customview.FlexBoxLayout


class SmileBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.smiles_bottom_sheet_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageId = arguments?.getInt(MESSAGE_KEY)!!
        val store = (view as ViewGroup).findViewById<FlexBoxLayout>(R.id.smile_store)
        val smiles = emojiMap
        store.paddingRows = 5
        store.paddingColumns = 5
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)

        for (key in smiles.keys)
            Button(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                text = smiles[key]
                textSize = 40f
                store?.addView(this)
                setOnClickListener {
                    parentFragmentManager.setFragmentResult(
                        ChatFragment.BOTTOM_SHEET_RES,
                        bundleOf(ChatFragment.BOTTOM_SHEET_RES to AddEmoji(messageId, smiles[key]!!, key))
                    )
                    dismiss()
                }
                setBackgroundResource(outValue.resourceId)
            }
    }

    companion object {
        const val TAG = "SmileBottomSheet"
        const val MESSAGE_KEY = "message key"
    }


}
