package com.example.apuniliapp.ui.adapter

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.Document
import com.example.apuniliapp.databinding.ItemDocumentCardBinding
import com.google.android.material.snackbar.Snackbar

class DocumentAdapter : ListAdapter<Document, DocumentAdapter.ViewHolder>(DocumentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDocumentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(private val binding: ItemDocumentCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(document: Document) {
            binding.apply {
                tvDocumentTitle.text = document.title
                tvDocumentCategory.text = document.category.label
                tvDocumentType.text = buildString {
                    append(document.type.ifBlank { "PDF" })
                    if (document.fileSize.isNotBlank()) {
                        append(" • ")
                        append(document.fileSize)
                    }
                }

                if (document.description.isNotBlank()) {
                    tvDocumentDescription.visibility = View.VISIBLE
                    tvDocumentDescription.text = document.description
                } else {
                    tvDocumentDescription.visibility = View.GONE
                }

                // Bouton Télécharger — enregistre le fichier sur l'appareil
                btnDownload.setOnClickListener {
                    val url = document.fileUrl
                    if (url.isNotBlank()) {
                        downloadFile(it.context, url, document.title, document.type)
                    } else {
                        Snackbar.make(binding.root, R.string.document_no_link, Snackbar.LENGTH_SHORT).show()
                    }
                }

                // Bouton Ouvrir — ouvre dans le navigateur
                btnOpen.setOnClickListener {
                    val url = document.fileUrl
                    if (url.isNotBlank()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            it.context.startActivity(intent)
                        } catch (e: Exception) {
                            Snackbar.make(binding.root, R.string.document_open_error, Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(binding.root, R.string.document_no_link, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun downloadFile(context: Context, url: String, title: String, type: String) {
            try {
                val extension = when (type.lowercase()) {
                    "pdf" -> ".pdf"
                    "doc", "docx" -> ".docx"
                    "xls", "xlsx" -> ".xlsx"
                    "ppt", "pptx" -> ".pptx"
                    "image", "jpg", "jpeg" -> ".jpg"
                    "png" -> ".png"
                    else -> {
                        // Déduire l'extension depuis l'URL
                        val urlExt = url.substringAfterLast('.', "pdf").substringBefore('?')
                        ".$urlExt"
                    }
                }

                val fileName = title.replace(Regex("[^a-zA-Z0-9àâäéèêëïîôùûüÿçÀÂÄÉÈÊËÏÎÔÙÛÜŸÇ\\s-]"), "")
                    .replace("\\s+".toRegex(), "_") + extension

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setTitle(title)
                    setDescription(context.getString(R.string.document_downloading))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Apunili/$fileName")
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)

                Toast.makeText(context, context.getString(R.string.document_download_started), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.document_download_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    class DocumentDiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Document, newItem: Document) = oldItem == newItem
    }
}
