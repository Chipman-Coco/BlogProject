package blog.android.examples.ui

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import blog.android.examples.R

class WifiAdapter(
    private var onClick: (Int) -> Unit
) : RecyclerView.Adapter<WifiAdapter.WifiHolder>() {

    val dataList = mutableListOf<ScanResult>()

    fun updateData(list: List<ScanResult>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wifi, parent, false)
        return WifiHolder(view)
    }

    override fun onBindViewHolder(holder: WifiHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size

    inner class WifiHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ScanResult) {
            itemView.apply {
                findViewById<TextView>(R.id.tv_name).text = item.SSID
                findViewById<TextView>(R.id.tv_bssid).text = item.BSSID
                setOnClickListener {
                    onClick.invoke(adapterPosition)
                }
            }
        }
    }
}