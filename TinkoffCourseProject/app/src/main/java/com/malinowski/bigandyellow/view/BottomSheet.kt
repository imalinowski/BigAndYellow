package com.malinowski.bigandyellow.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malinowski.bigandyellow.App
import com.malinowski.bigandyellow.R
import com.malinowski.bigandyellow.databinding.BottomSheetBinding
import com.malinowski.bigandyellow.model.data.*
import com.malinowski.bigandyellow.viewmodel.recyclerViewUtils.SimpleItemsAdapter


class BottomSheet : BottomSheetDialogFragment() {

    private val binding by lazy { BottomSheetBinding.inflate(layoutInflater) }
    private val adapter by lazy {
        SimpleItemsAdapter { itemClicked(it) }
    }
    private val layoutManager = LinearLayoutManager(context)

    private val intents by lazy {
        arguments?.getParcelableArrayList<MessageIntent>(INTENTS)
    }

    private val topics by lazy {
        arguments?.getParcelableArrayList<ChangeTopic>(TOPICS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.simpleItemsRecycler.apply {
            adapter = this@BottomSheet.adapter
            layoutManager = this@BottomSheet.layoutManager
        }

        val items: List<String> = when {
            intents != null -> intents!!.map { messageIntentToString(it) }
            topics != null -> topics!!.map { it.topic }
            else -> {
                Log.e("BOTTOM_SHEET", "no items")
                dismiss()
                return
            }
        }

        adapter.submitList(items)
    }

    private fun messageIntentToString(item: MessageIntent): String {
        return when (item) {
            is MessageIntent.AddEmoji -> App.appContext.getString(R.string.add_reaction)
            is MessageIntent.Copy -> App.appContext.getString(R.string.copy)
            is MessageIntent.Edit -> App.appContext.getString(R.string.edit)
            is MessageIntent.ChangeTopic -> App.appContext.getString(R.string.change_topic)
            is MessageIntent.Delete -> App.appContext.getString(R.string.delete)
        }
    }

    private fun itemClicked(n: Int) {
        parentFragmentManager.setFragmentResult(
            ChatFragment.BOTTOM_SHEET_RES,
            bundleOf(ChatFragment.BOTTOM_SHEET_RES to (intents ?: topics!!)[n])
        )
        dismiss()
    }

    companion object {
        const val TAG = "BottomSheet"
        const val TOPICS = "topics"
        const val INTENTS = "intents"

        @JvmName("ChangeTopic")
        fun newInstance(items: List<ChangeTopic>) =
            BottomSheet().apply {
                arguments = Bundle().apply { putParcelableArrayList(TOPICS, ArrayList(items)) }
            }

        @JvmName("MessageIntents")
        fun newInstance(items: List<MessageIntent>) =
            BottomSheet().apply {
                arguments = Bundle().apply { putParcelableArrayList(INTENTS, ArrayList(items)) }
            }
    }
}
