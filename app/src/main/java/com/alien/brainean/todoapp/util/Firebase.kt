package com.alien.brainean.todoapp.util

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

object Firebase {

    private val firestore : FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val tasksCollection : CollectionReference by lazy {
        firestore.collection("tasks/")
    }
}