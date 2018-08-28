package com.aleksanderkapera.liveback.ui.adapter

import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View


abstract class BaseRecyclerAdapter<VH: BaseRecyclerAdapter.ViewHolder,T>(context: Context) : RecyclerView.Adapter<VH>() {

    protected  var mData: ArrayList<T> = arrayListOf()
    protected  var mInflater: LayoutInflater = LayoutInflater.from(context)
    protected  var mContext: Context = context
    protected  var mRes: Resources = context.resources

    protected fun isLastItem(position: Int): Boolean { return position  == mData.size - 1}

    override fun getItemCount(): Int { return mData.size}

    override fun onBindViewHolder(holder: VH, position: Int) { holder.bind(position)}

    fun addData(data: List<T>?) {
        if(data == null) return
        mData.addAll(data)
        notifyDataSetChanged()
    }

    fun replaceData(data: List<T>){
        mData.clear()
        addData(data)
    }

    fun removeItem(position: Int): T {
        val item= mData.removeAt(position)
        notifyItemRemoved(position)
        return item
    }

    fun removeItem(item: T): T {
        val position = mData.indexOf(item)
        val removed = mData.remove(item)
        notifyItemRemoved(position)
        return item
    }

    fun addItem(position: Int, item: T){
        mData.add(position, item)
        notifyItemInserted(position)
    }

    fun moveItem(fromPositon: Int, toPosition: Int){
        val item = mData.removeAt(fromPositon)
        mData.add(toPosition, item)
        notifyItemMoved(fromPositon, toPosition)
    }

    fun animateTo(items: List<T>){
        applyAndAnimateRemovals(items)
        applyAndAnimateAdditions(items)
        applyAndAnimateMovedItems(items)
    }

    private fun applyAndAnimateRemovals(newModels: List<T>) {
        for (i in mData.size-1 downTo 0) {
            val model = mData[i]
            if (!newModels.contains(model)) {
                removeItem(i)
            }
        }
    }

    private fun applyAndAnimateAdditions(newModels: List<T>){
        for(i in newModels.indices) {
            val model= newModels[i]
            if(!mData.contains(model)){
                addItem(i,model)
            }
        }
    }

    private fun applyAndAnimateMovedItems(newModels: List<T>){
        for(toPosition in newModels.size-1 downTo 0) {
            val model = newModels[toPosition]
            val fromPosition = mData.indexOf(model)
            if(fromPosition >= 0 && fromPosition != toPosition){
                moveItem(fromPosition, toPosition)
            }
        }
    }

    abstract class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        abstract fun bind(position: Int)
    }
}