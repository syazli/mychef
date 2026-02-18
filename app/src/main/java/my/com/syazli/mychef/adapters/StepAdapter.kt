package my.com.syazli.mychef.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import my.com.syazli.mychef.R
import my.com.syazli.mychef.databinding.ItemStepRowBinding
import java.util.Collections

class StepAdapter : RecyclerView.Adapter<StepAdapter.StepViewHolder>() {

    private val steps = mutableListOf<String>()
    private var itemTouchHelper: ItemTouchHelper? = null

    fun attachTouchHelper(recyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(rv: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                val from = source.adapterPosition
                val to = target.adapterPosition
                syncStepText(rv, from)
                Collections.swap(steps, from, to)
                notifyItemMoved(from, to)
                rv.post { refreshStepNumbers(rv) }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.animate()?.scaleX(1.02f)?.scaleY(1.02f)
                        ?.translationZ(8f)?.setDuration(150)?.start()
                }
            }

            override fun clearView(rv: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(rv, viewHolder)
                viewHolder.itemView.animate().scaleX(1f).scaleY(1f)
                    .translationZ(0f).setDuration(150).start()
            }
        }

        itemTouchHelper = ItemTouchHelper(callback).also { it.attachToRecyclerView(recyclerView) }
    }


    fun addStep(text: String = "") {
        steps.add(text)
        notifyItemInserted(steps.lastIndex)
    }

    fun removeStep(position: Int) {
        if (steps.size > 1) {
            steps.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, steps.size)
        } else {
            steps[0] = ""
            notifyItemChanged(0)
        }
    }

    fun syncAllText(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            syncStepText(recyclerView, i)
        }
    }

    fun collectSteps(): List<String> = steps.filter { it.isNotBlank() }


    private fun syncStepText(recyclerView: RecyclerView, position: Int) {
        val vh = recyclerView.findViewHolderForAdapterPosition(position) as? StepViewHolder
        vh?.let { steps[position] = it.binding.etStep.text?.toString() ?: "" }
    }

    fun clearAllSteps() {
        steps.clear()
        notifyDataSetChanged()
    }



    private fun refreshStepNumbers(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            val vh = recyclerView.findViewHolderForAdapterPosition(i) as? StepViewHolder
            vh?.binding?.tvStepNumber?.text = (i + 1).toString()
        }
    }


    inner class StepViewHolder(val binding: ItemStepRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, position: Int) {
            binding.tvStepNumber.text = (position + 1).toString()

            binding.etStep.setText(text)
            binding.etStep.setSelection(text.length)

            binding.etStep.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (adapterPosition != RecyclerView.NO_ID.toInt()) {
                        steps[adapterPosition] = s?.toString() ?: ""
                    }
                }
            })

            binding.ivDragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper?.startDrag(this)
                }
                false
            }

            binding.btnRemoveStep.setOnClickListener {
                removeStep(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemStepRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(steps[position], position)
    }

    override fun getItemCount() = steps.size
}