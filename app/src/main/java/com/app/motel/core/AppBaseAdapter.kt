package com.app.motel.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Lớp Adapter cơ sở cho RecyclerView, tích hợp ViewBinding để hiển thị dữ liệu.
 *
 * @param T Kiểu dữ liệu của các item hiển thị trong adapter
 * @param VB Kiểu ViewBinding được sử dụng để hiển thị item
 */
abstract class AppBaseAdapter<T, VB : ViewBinding>(
): RecyclerView.Adapter<AppBaseAdapter.BaseViewHolder<VB>>() {

    /**
     * Khởi tạo ViewBinding cho item view.
     * Phương thức này sẽ được gọi trong onCreateViewHolder để tạo layout cho mỗi item.
     */
    abstract fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): VB

    /**
     * Liên kết dữ liệu của item với ViewBinding tại một vị trí cụ thể.
     * Phương thức này sẽ được gọi trong onBindViewHolder để hiển thị dữ liệu.
     */
    abstract fun bind(binding: VB, item: T, position: Int)

    /**
     * Danh sách các item dữ liệu hiện tại của adapter.
     */
    private var items: List<T> = listOf()


    /**
     * Cập nhật toàn bộ danh sách và thông báo thay đổi.
     * Sử dụng để thay thế hoàn toàn dữ liệu hiện tại bằng dữ liệu mới.
     */
    @Suppress("NotifyDataSetChanged")
    open fun updateData(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = inflateBinding(LayoutInflater.from(parent.context), parent)
        return BaseViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        bind(holder.binding, items[position], position)
    }

    /**
     * ViewHolder cơ bản để giữ binding của item view.
     */
    class BaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    /**
     * Lớp listener trừu tượng để xử lý sự kiện click item với các action khác nhau.
     * Được sử dụng để tương tác với các item trong RecyclerView.
     */
    abstract class AppListener<T> {
        abstract fun onClickItem(item: T, action: ItemAction = ItemAction.CLICK)
    }

//    interface AppListener<T> {
//        fun onClickItem(item: T, action: ItemAction = ItemAction.ACTION_CLICK) {}
//    }

    /**
     * Enum định nghĩa các loại action có thể thực hiện trên một item.
     * Sử dụng để phân biệt các hành động khác nhau trong phương thức onClickItem.
     */
    enum class ItemAction {
        CLICK,        // Click thông thường
        LONG_CLICK,   // Click giữ lâu
        EDIT,         // Sửa item
        DELETE,       // Xóa item
        SHARE,        // Chia sẻ item
        DOWNLOAD,     // Tải xuống item
        CUSTOM,        // Hành động tùy chỉnh khác
        UPDATE_STATUS // Cập nhật trạng thái khi click vào item
    }

}
