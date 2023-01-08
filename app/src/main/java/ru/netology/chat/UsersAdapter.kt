package ru.netology.chat

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.chat.databinding.ItemUserBinding
import ru.netology.chat.model.User

interface UserActionListener {
    fun onUserMove(user: User, moveBy: Int)
    fun onUserDelete(user: User)
    fun onUserDetails(user: User)
    fun onUserFire(user: User)
}
/*Android RecyclerViews, отображающий списки, являются частью почти каждого приложения Android.
В списках содержится большой объем информации, поэтому важно обеспечить удобство работы как при прокрутке списка,
так и при обновлении его содержимого. DiffUtil – служебный класс, созданный для улучшения производительности RecyclerView при обновлении списка.
Даже если он связан с компонентом UI пользовательского интерфейса, вы можете использовать его в любой части приложения
для сравнения двух списков одного типа Item. Чтобы алгоритм, используемый DiffUtil, работал, списки должны быть неизменяемыми.
 В противном случае при изменении содержимого результат может отличаться от ожидаемого.
 Следовательно, чтобы обновить элемент в списке, создайте и установите копию этого элемента.
 Чтобы показать разницу между двумя списками (в случае для RecyclerView) - тем, который вы уже показываете,
 и тем, который вы хотите показать (при изменении любого из элементов в списке),
 DiffUtil использует разностный алгоритм, позволяя вычислять разницу между двумя наборами элементов.
DiffUtil.Callback- это собственный класс, ответственный за вычисление разницы между двумя списками. Поскольку ОС не знает,
какие поля следует редактировать, приложение обязано переопределить areItemsTheSame и areContentsTheSame для предоставления данной информации.
Item состоит из id, его value, timeStamp и информации о том, проверен done или нет.
Сам id является уникальным и неизменным, но вы можете редактировать все остальные поля.
Таким образом, вы можете считать два элемента из разных списков одинаковыми, если они имеют одно и то же id.
Чтобы избежать пересмотра всего списка при изменении, будут обновлены только те элементы, которые имеют разные значения в обоих списках.
 */

class UsersDiffCallback(
    private val oldList: List<User>,
    private val newList: List<User>
) : DiffUtil.Callback() {
    /*метод возвращает длину старого списка*/
    override fun getOldListSize(): Int = oldList.size
    /*метод возвращает длину нового списка*/
    override fun getNewListSize(): Int = newList.size
    /*метод сравнения пользователей из старого и нового списков по id*/
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        return oldUser.id == newUser.id
    }
    /*метод сравнения содержимого всех полей пользователей из старого и нового списков*/
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        /*такая запись возможна только потому что класс User явлется data
        * иначе пришлось бы сравнивать отдельно по каждому полю класса
        * типа oldUser.company == newUser.company && ...*/
        return oldUser == newUser
    }

class UsersAdapter(
    private val actionListener: UserActionListener
    /*View.OnClickListener отслеживает нажатия на все элементы и требуется реализовать метод onClick*/
) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>(), View.OnClickListener {

    var users: List<User> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        /*set метод необходим чтобы уведомить recyclerview что нужно обновить список*/
        set(newValue) {
            val diffCallback = UsersDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            /*присвоение переменной нового списков*/
            field = newValue
            /*метод передачи обновленного списка адаптеру this*/
            diffResult.dispatchUpdatesTo(this)
            /*метод позволяет после каждого действия обновлять весь список*/
//            notifyDataSetChanged()
        }

    override fun onClick(v: View) {
        /*вызов пользователя по тэгу при нажатии на элемент View*/
        val user = v.tag as User

        /*при нажатии на кнопку меню оно раскрывается
        * или при нажатие на элемент списка он становится активным*/
        when (v.id) {
            R.id.moreImageViewButton -> {
                showPopupMenu(v)
            }
            else -> {
                actionListener.onUserDetails(user)
            }
        }
    }
    /*метод возвращения числа элементов в списке*/
    override fun getItemCount(): Int = users.size

    /*метод вызывается тогда когда recyclerview хочет создать новый элемент в списке*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemUserBinding.inflate(inflater, parent, false)

        /*инициализация слушателей при нажатии на элемент списка и на кнопку меню*/
        /*написать this можно только потому что в UsersAdapter реализован View.OnClickListener*/

        binding.root.setOnClickListener(this)
        binding.moreImageViewButton.setOnClickListener(this)

        return UsersViewHolder(binding)
    }
    /*метод вызывается чтобы обновить элемент в списке*/
    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context
        with(holder.binding) {
            /*тэг инициализируется  при нажатии на любой элемент View и отдельно на кнопку меню*/
            holder.itemView.tag = user
            moreImageViewButton.tag = user
            userNameTextView.text = user.name
            /*проверка что если поле компании не пустое то можно уволить пользователя*/
            userCompanyTextView.text = if (user.company.isNotBlank()) user.company else context.getString(R.string.unemployed)
            if (user.photo.isNotBlank()) {
                Glide.with(photoImageView.context)
                    .load(user.photo)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_avatar)
                    .error(R.drawable.ic_user_avatar)
                    .into(photoImageView)
            } else {
                Glide.with(photoImageView.context).clear(photoImageView)
                photoImageView.setImageResource(R.drawable.ic_user_avatar)

            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, view)
        val context = view.context
        val user = view.tag as User
        val position = users.indexOfFirst { it.id == user.id }

        popupMenu.menu.add(0, ID_MOVE_UP, Menu.NONE, context.getString(R.string.move_up)).apply {
            isEnabled = position > 0
        }
        popupMenu.menu.add(0, ID_MOVE_DOWN, Menu.NONE, context.getString(R.string.move_down)).apply {
            isEnabled = position < users.size - 1
        }
        popupMenu.menu.add(0, ID_REMOVE, Menu.NONE, context.getString(R.string.remove))
        /*проверка что пользователь работает в компании и если да то его можно уволить*/
        if (user.company.isNotBlank()) {
            popupMenu.menu.add(0, ID_FIRE, Menu.NONE, context.getString(R.string.fire))
        }
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_MOVE_UP -> {
                    actionListener.onUserMove(user, -1)
                }
                ID_MOVE_DOWN -> {
                    actionListener.onUserMove(user, 1   )
                }
                ID_REMOVE -> {
                    actionListener.onUserDelete(user)
                }
                ID_FIRE -> {
                    actionListener.onUserFire(user)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }
    class UsersViewHolder(
        val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val ID_MOVE_UP = 1
        private const val ID_MOVE_DOWN = 2
        private const val ID_REMOVE = 3
        private const val ID_FIRE = 4
    }
}
}