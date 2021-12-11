package com.malinowski.bigandyellow.di.users

import com.malinowski.bigandyellow.view.UsersFragment
import dagger.Subcomponent

@Subcomponent(modules = [UserModule::class])
abstract class UserComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): UserComponent
    }

    abstract fun inject(fragment: UsersFragment)

}