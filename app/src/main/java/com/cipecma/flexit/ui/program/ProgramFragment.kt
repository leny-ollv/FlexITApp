package com.cipecma.flexit.ui.program

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cipecma.flexit.databinding.FragmentProgramBinding

class ProgramFragment : Fragment() {

    private var _binding: FragmentProgramBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 1. Initialisation
        val programViewModel = ViewModelProvider(this).get(ProgramViewModel::class.java)
        _binding = FragmentProgramBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 2. Liaison avec ton TextView (ID: text_program dans ton XML)
        val textView: TextView = binding.textProgram

        // 3. Observation des données
        programViewModel.programNames.observe(viewLifecycleOwner) { programs ->
            if (programs.isNotEmpty()) {
                // On transforme la liste d'objets en liste de noms (String)
                val listeDeNoms = programs.map { it.name }
                // On les affiche séparés par un retour à la ligne
                textView.text = listeDeNoms.joinToString("\n")
            } else {
                textView.text = "Aucun programme trouvé ou erreur de connexion."
            }
        }

        // 4. Lancement de l'appel (on teste avec l'ID 1 par exemple)
        programViewModel.fetchPrograms(userId = 1)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}