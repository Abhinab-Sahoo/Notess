package com.example.notess.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notess.databinding.ListItemBinding
import com.example.notess.data.model.Note

class NoteAdapter(

    private val clickListener: (Note) -> Unit

) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallBack) {

    class NoteViewHolder(
        private val binding: ListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(note: Note) {
            binding.note = note
            binding.executePendingBindings()
        }
    }

    companion object DiffCallBack : DiffUtil.ItemCallback<Note>() {

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NoteViewHolder(ListItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)

        holder.itemView.setOnClickListener {
            clickListener(note)
        }
        holder.bind(note)
    }
}