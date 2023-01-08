package ru.netology.chat

import android.app.Application
import ru.netology.chat.model.UsersService

class App : Application() {

    /*создается экземпляр класса usersService в виде Singleton*/

    val usersService = UsersService()
}