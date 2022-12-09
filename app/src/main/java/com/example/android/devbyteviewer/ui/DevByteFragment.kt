package com.example.android.devbyteviewer.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.devbyteviewer.R
import com.example.android.devbyteviewer.databinding.DevbyteItemBinding
import com.example.android.devbyteviewer.databinding.FragmentDevByteBinding
import com.example.android.devbyteviewer.domain.DevByteVideo
import com.example.android.devbyteviewer.viewmodels.DevByteViewModel

/**
 * Tampilan daftar DevBytes di layar.
 */
class DevByteFragment : Fragment() {

    /**
     * Salah satu cara untuk menunda pembuatan viewModel hingga metode daur hidup yang sesuai adalah dengan menggunakan
     * malas. Ini mengharuskan viewModel tidak direferensikan sebelum onActivityCreated, yang kami
     * lakukan di Fragmen ini.
     */
    private val viewModel: DevByteViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, DevByteViewModel.Factory(activity.application))
                .get(DevByteViewModel::class.java)
    }

    /**
     * Adaptor RecyclerView untuk mengonversi daftar Video ke kartu.
     */
    private var viewModelAdapter: DevByteAdapter? = null

    /**
     * Dipanggil segera setelah onCreateView() dikembalikan, dan milik fragmen
     * hierarki tampilan telah dibuat. Ini dapat digunakan untuk melakukan final
     * inisialisasi setelah potongan-potongan ini terpasang, seperti mengambil
     * melihat atau memulihkan status.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.playlist.observe(viewLifecycleOwner, Observer<List<DevByteVideo>> { videos ->
            videos?.apply {
                viewModelAdapter?.videos = videos
            }
        })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentDevByteBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_dev_byte,
                container,
                false)
        // Setel lifecycleOwner agar DataBinding dapat mengamati LiveData
        binding.setLifecycleOwner(viewLifecycleOwner)

        binding.viewModel = viewModel

        viewModelAdapter = DevByteAdapter(VideoClick {
            // Saat video diklik, blok atau lambda ini akan dipanggil oleh DevByteAdapter

            // konteks tidak ada, kita dapat membuang klik ini dengan aman karena Fragmen tidak ada
            // lebih lama di layar
            val packageManager = context?.packageManager ?: return@VideoClick

            // Coba buat langsung ke aplikasi YouTube
            var intent = Intent(Intent.ACTION_VIEW, it.launchUri)
            if(intent.resolveActivity(packageManager) == null) {
                // YouTube app isn't found, use the web url
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
            }

            startActivity(intent)
        })

        binding.root.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }


        // Observer for the network error.
        viewModel.eventNetworkError.observe(viewLifecycleOwner, Observer<Boolean> { isNetworkError ->
            if (isNetworkError) onNetworkError()
        })

        return binding.root
    }

    /**
     * Metode untuk menampilkan pesan kesalahan Toast untuk kesalahan jaringan.
     */
    private fun onNetworkError() {
        if(!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }

    /**
     * Metode pembantu untuk menghasilkan tautan aplikasi YouTube
     */
    private val DevByteVideo.launchUri: Uri
        get() {
            val httpUri = Uri.parse(url)
            return Uri.parse("vnd.youtube:" + httpUri.getQueryParameter("v"))
        }
}

/**
 * Klik pendengar untuk Video. Dengan memberi nama blok itu membantu pembaca memahami apa yang dilakukannya.
 *
 */
class VideoClick(val block: (DevByteVideo) -> Unit) {
    /**
     * Dipanggil saat video diklik
     *
     * @param video video yang diklik
     */
    fun onClick(video: DevByteVideo) = block(video)
}

/**
 * Adaptor RecyclerView untuk menyiapkan pengikatan data pada item dalam daftar.
 */
class DevByteAdapter(val callback: VideoClick) : RecyclerView.Adapter<DevByteViewHolder>() {

    /**
     * Video yang akan ditampilkan Adaptor
     */
    var videos: List<DevByteVideo> = emptyList()
        set(value) {
            field = value
            // Untuk tantangan tambahan, perbarui ini untuk menggunakan perpustakaan paging.
            //
            //  kumpulan data telah berubah. Ini akan menyebabkan setiap
            //  elemen di RecyclerView kita menjadi tidak valid.
            notifyDataSetChanged()
        }

    /**
     * Dipanggil saat RecyclerView membutuhkan {@link ViewHolder} baru dari jenis tertentu untuk direpresentasikan
     * sebuah benda.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevByteViewHolder {
        val withDataBinding: DevbyteItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                DevByteViewHolder.LAYOUT,
                parent,
                false)
        return DevByteViewHolder(withDataBinding)
    }

    override fun getItemCount() = videos.size

    override fun onBindViewHolder(holder: DevByteViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.video = videos[position]
            it.videoCallback = callback
        }
    }

}

/**
 * ViewHolder untuk item DevByte. Semua pekerjaan dilakukan dengan pengikatan data.
 */
class DevByteViewHolder(val viewDataBinding: DevbyteItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.devbyte_item
    }
}