package com.example.bamfinalproject.common

import com.example.bamfinalproject.R
import android.content.Context
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import com.example.bamfinalproject.MainActivity
import com.example.bamfinalproject.database.entity.UserData
import kotlin.Int

class UserDataAdapter(context: Context, userDatas: ArrayList<UserData>) :
    ArrayAdapter<UserData?>(context, 0, userDatas as List<UserData?>) {
    private lateinit var loginTextView: TextView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var loginEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var passwordTextView: TextView
    private lateinit var currentUserData: UserData

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) listItemView = from(context).inflate(R.layout.list_item, parent, false)

        initUI(listItemView!!)

        currentUserData = getItem(position)!!
        loginTextView.text = currentUserData.getUserLogin()
        loginEdit.setText(currentUserData.getUserLogin())
        passwordTextView.text = currentUserData.getUserPassword()
        passwordEdit.setText(currentUserData.getUserPassword())

        editButton.setOnClickListener {switchMode(position, listItemView)}
        deleteButton.setOnClickListener { deleteItem(position) }

        return listItemView
    }

    private fun switchMode(position: Int, listItemView: View){
        initUI(listItemView)
        switchVisibility(loginEdit)
        switchVisibility(passwordEdit)
        switchVisibility(loginTextView)
        switchVisibility(passwordTextView)
        switchListeners(position, listItemView)
        switchButtonText()
    }

    private fun switchVisibility(view: View){
        if(view.isVisible){
            view.visibility = View.GONE
        }else{
            view.visibility = View.VISIBLE
        }
    }

    private fun switchButtonText(){
        if(editButton.text == "Edycja"){
            editButton.text = "Aktualizuj"
            deleteButton.text = "Anuluj"
        }else{
            editButton.text = "Edycja"
            deleteButton.text = "Usuń"
        }
    }

    private fun switchListeners(position: Int, listItemView: View){
        initUI(listItemView)
        if(editButton.text == "Edycja"){
            editButton.setOnClickListener { updateItem(position, listItemView) }
            deleteButton.setOnClickListener { switchMode(position, listItemView) }
        }else{
            editButton.setOnClickListener { switchMode(position, listItemView) }
            deleteButton.setOnClickListener { deleteItem(position) }
        }
    }

    private fun updateItem(position: Int, listItemView: View){
        currentUserData = getItem(position)!!
        initUI(listItemView)
        switchMode(0, listItemView)
        MainActivity.db.userDataDao().updateUserData(currentUserData.id.toLong(), loginEdit.text.toString(), passwordEdit.text.toString())

        val userData = MainActivity.db.userDataDao().get(currentUserData.id.toLong())

        loginTextView.text = userData.getUserLogin()
        passwordTextView.text = userData.getUserPassword()
        loginEdit.setText(userData.getUserLogin())
        passwordEdit.setText(userData.getUserPassword())

        clear()
        addAll(MainActivity.db.userDataDao().getAllByLogin(currentUserData.createdUser!!))
        notifyDataSetChanged()
    }

    private fun deleteItem(position: Int) {
        getItem(position)?.let { MainActivity.db.userDataDao().delete(it) }
        remove(getItem(position))
        notifyDataSetChanged()
    }

    private fun initUI(listItemView: View){
        loginTextView = listItemView.findViewById(R.id.login) as TextView
        editButton = listItemView.findViewById(R.id.editButton) as Button
        deleteButton = listItemView.findViewById(R.id.deleteButton) as Button
        loginEdit = listItemView.findViewById(R.id.loginEdit) as EditText
        passwordEdit = listItemView.findViewById(R.id.passwordEdit) as EditText
        passwordTextView = listItemView.findViewById(R.id.password) as TextView
    }
}