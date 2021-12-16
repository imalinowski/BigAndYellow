package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malinowski.bigandyellow.databinding.BottomSheetBinding
import com.malinowski.bigandyellow.model.data.*


class BottomSheet : BottomSheetDialogFragment() {
    private val binding by lazy { BottomSheetBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO USE GENERATE LIST AND RETURN CLICKED POS
        val messageId: Int = arguments?.getInt(MESSAGE_KEY)!!
        with(binding) {
            addReaction.setOnClickListener {
                SmileBottomSheet()
                    .apply {
                        arguments = bundleOf(
                            SmileBottomSheet.MESSAGE_KEY to messageId
                        )
                    }
                    .show(parentFragmentManager, SmileBottomSheet.TAG)
                dismiss()
            }
            copy.setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    ChatFragment.BOTTOM_SHEET_RES, bundleOf(
                        ChatFragment.BOTTOM_SHEET_RES to Copy(messageId)
                    )
                )
                dismiss()
            }
            edit.setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    ChatFragment.BOTTOM_SHEET_RES, bundleOf(
                        ChatFragment.BOTTOM_SHEET_RES to Edit(messageId)
                    )
                )
                dismiss()
            }
            changeTopic.setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    ChatFragment.BOTTOM_SHEET_RES, bundleOf(
                        ChatFragment.BOTTOM_SHEET_RES to ChangeTopic(messageId)
                    )
                )
                dismiss()
            }
            delete.setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    ChatFragment.BOTTOM_SHEET_RES, bundleOf(
                        ChatFragment.BOTTOM_SHEET_RES to Delete(messageId)
                    )
                )
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "BottomSheet"
        const val MESSAGE_KEY = "message key"
        const val COPY = "copy"
    }

}
