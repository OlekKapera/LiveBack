package com.aleksanderkapera.liveback.ui.adapter

interface OnItemClickListener<T> {

    fun onItemClick(item: T, position: Int)
}
