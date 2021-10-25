package com.malinowski.bigandyellow.view

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.view.customview.FlexBoxLayout


class SmileBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.smiles_bottom_sheet_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val store = (view as ViewGroup).findViewById<FlexBoxLayout>(R.id.smile_store)
        val smiles = resources.getStringArray(R.array.smiles)
        store.paddingRows = 5
        store.paddingColumns = 5
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
        for (i in smiles.indices)
            Button(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                text = smiles[i]
                textSize = 40f
                store?.addView(this)
                setOnClickListener {
                    parentFragmentManager.setFragmentResult(
                        SMILE_RESULT,
                        bundleOf(SMILE_KEY to i, MESSAGE_KEY to arguments?.getInt(MESSAGE_KEY))
                    )
                    dismiss()
                }
                setBackgroundResource(outValue.resourceId)
            }
    }

    companion object {
        const val TAG = "SmileBottomSheet"
        const val SMILE_RESULT = "smile result"
        const val SMILE_KEY = "smile key"
        const val MESSAGE_KEY = "message key"
    }
}
