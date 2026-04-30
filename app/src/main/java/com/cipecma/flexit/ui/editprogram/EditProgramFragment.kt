package com.cipecma.flexit.ui.editprogram

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.cipecma.flexit.databinding.FragmentEditProgramBinding
import com.cipecma.flexit.ui.program.ProgramViewModel
import kotlin.getValue

class EditProgramFragment : Fragment() {

    private var _binding: FragmentEditProgramBinding? = null
    private val binding get() = _binding!!
    private val programViewModel: ProgramViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProgramBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // On récupère le nom passé dans le bundle
        val programName = arguments?.getString("programNameKey")
        val programId = arguments?.getInt("programIdKey")
        val userId = arguments?.getInt("userId")

        // On l'affiche dans l'EditText via le binding
        if (programName != null) {
            binding.editProgramName.setText(programName)
        }

        // Action au clic sur le bouton
        binding.btnSaveProgram.setOnClickListener {
            val newName = binding.editProgramName.text.toString()

            if (newName.isNotBlank()) {
                // On appelle la fonction du ViewModel
                programViewModel.updateProgram(programId, newName, userId)

                // Petit message de confirmation
                android.widget.Toast.makeText(requireContext(), "Modification enregistrée", android.widget.Toast.LENGTH_SHORT).show()

                // RETOUR ARRIÈRE
                // Cette ligne ferme le fragment actuel et te renvoie à la liste
                findNavController().navigateUp()
            } else {
                binding.editProgramName.error = "Le nom ne peut pas être vide"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}