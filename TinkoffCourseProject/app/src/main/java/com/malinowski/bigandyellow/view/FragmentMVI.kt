package com.malinowski.bigandyellow.view

import androidx.fragment.app.Fragment
import com.malinowski.bigandyellow.view.states.State

abstract class FragmentMVI : Fragment() {
    abstract fun render(state: State)
}