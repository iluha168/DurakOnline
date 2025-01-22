package com.durakcheat.net.json

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

open class StringPreference (
    private val preferences: SharedPreferences,
    private val name: String
){
    open var str: String?
        get() = preferences.getString(name, null)
        set(value) = preferences.edit().apply {
            if(value == null) remove(name)
            else putString(name, value)
        }.apply()
}

class StateStringPreference (
    preferences: SharedPreferences,
    name: String
): StringPreference(preferences, name) {
    private val stateValue: MutableState<String?> = mutableStateOf(super.str)
    override var str: String?
        get() = stateValue.value
        set(value){
            stateValue.value = value
            super.str = value
        }
}

open class JSONPreference<T : Any> (
    preferences: SharedPreferences,
    name: String,
    clazz: Class<T>
): StringPreference(preferences, name) {
    private val adapter = DMoshi.adapter(clazz)
    open var value: T?
        get() = super.str?.let { adapter.fromJson(it) }
        set(value){ super.str = adapter.toJson(value)!! }
}

class StateJSONPreference<T : Any> (
    preferences: SharedPreferences,
    name: String,
    clazz: Class<T>
): JSONPreference<T>(preferences, name, clazz) {
    private val stateValue: MutableState<T?> = mutableStateOf(super.value)
    override var value: T?
        get() = stateValue.value
        set(value){
            stateValue.value = value
            super.value = value
        }
}