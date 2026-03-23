package com.example.apuniliapp.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apuniliapp.databinding.FragmentContactBinding
import com.google.android.material.snackbar.Snackbar

class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)

        binding.btnSend.setOnClickListener {
            val name = binding.etContactName.text.toString()
            val email = binding.etContactEmail.text.toString()
            val subject = binding.etContactSubject.text.toString()
            val message = binding.etContactMessage.text.toString()

            if (name.isBlank() || email.isBlank() || subject.isBlank() || message.isBlank()) {
                Snackbar.make(binding.root, "Veuillez remplir tous les champs", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Snackbar.make(binding.root, "Message envoyé avec succès !", Snackbar.LENGTH_LONG).show()
            binding.etContactName.text?.clear()
            binding.etContactEmail.text?.clear()
            binding.etContactSubject.text?.clear()
            binding.etContactMessage.text?.clear()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

