package com.alien.brainean.todoapp.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.*
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.databinding.TaskListItemBinding

class TasksAdapter(private val viewModel: TasksViewModel) :
    ListAdapter<Task, TasksAdapter.TasksViewHolder>(TaskDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        return TasksViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        holder.onBind(viewModel, getItem(position))
    }

    fun getTaskAt(position: Int): Task {
        return getItem(position)
    }

    class TasksViewHolder(val binding: TaskListItemBinding) :
        ViewHolder(binding.root) {


        fun onBind(viewModel: TasksViewModel, item: Task) {
            binding.viewModel = viewModel
            binding.task = item
            binding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup): TasksViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskListItemBinding.inflate(layoutInflater, parent, false)
                return TasksViewHolder(binding)
            }
        }
    }

}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.title == newItem.title && oldItem.description == newItem.description
                && oldItem.isCompleted == newItem.isCompleted
    }
}