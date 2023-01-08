package ru.netology.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.chat.databinding.ActivityMainBinding
import ru.netology.chat.model.User
import ru.netology.chat.model.UsersListener
import ru.netology.chat.model.UsersService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UsersDiffCallback.UsersAdapter

    private val usersService: UsersService
        /*get() позволяет получить доступ к модели usersService*/
        get() = (applicationContext as App).usersService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*в конструктор адаптера передаются три метода из interface UserActionListener*/
        adapter = UsersDiffCallback.UsersAdapter(object : UserActionListener {

            override fun onUserMove(user: User, moveBy: Int) {
                usersService.moveUser(user, moveBy)
            }

            override fun onUserDelete(user: User) {
                usersService.deleteUser(user)
            }

            /*для отображения имени пользователя при нажатии на элемент
             для анимации при нажатии на элемент android:background="?android:attr/selectableItemBackground"
            */
            override fun onUserDetails(user: User) {
                Toast.makeText(this@MainActivity, "User: ${user.name}", Toast.LENGTH_SHORT).show()
            }

            override fun onUserFire(user: User) {
                usersService.fireUser(user)
            }


        })

        /*отображение в layoutManager вертикального списка recyclerView*/
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        /*метод исключения мерцания аватарки при увольнении пользователя*/
        val itemAnimator = binding.recyclerView.itemAnimator
        if (itemAnimator is DefaultItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
        }
        usersService.addListener(usersListener)
    }

    /*удаление слушателя usersService позволяет избежать утечек памяти*/
    override fun onDestroy() {
        super.onDestroy()
        usersService.removeListener(usersListener)
    }

    /*слушатель прослушивает изменения в классе usersService
    * в качестве аргумента приходит обновленный список
    * и он присваивается в виде it на адаптере*/
    private val usersListener: UsersListener = {
        adapter.users = it
    }
}