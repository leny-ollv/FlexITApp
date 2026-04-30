package com.cipecma.flexit.ui.program

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cipecma.flexit.R
import com.cipecma.flexit.auth.AuthManager
import com.cipecma.flexit.databinding.FragmentProgramBinding
import com.cipecma.flexit.network.ApiService

class ProgramFragment : Fragment() {

    private var _binding: FragmentProgramBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val programViewModel: ProgramViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgramBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // On récupère le RecyclerView défini dans fragment_program.xml
        val recyclerView = binding.recyclerViewPrograms

        // Récupération de l'id du user
        val currentUserId: Int = AuthManager.getUserId()
        // Si l'id est bien configuré (> 0), on lance l'appel
        if (currentUserId != -1) {
            programViewModel.fetchPrograms(userId = currentUserId)
        }

        // Observation des données venant de l'API
        programViewModel.programNames.observe(viewLifecycleOwner) { programs ->
            if (programs.isNotEmpty()) {
                // On crée l'Adapter avec la liste de programmes
                val adapter = ProgramAdapter(programs, onClick = { program ->
                    // On ouvre une alerte de confirmation
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer le programme")
                        .setMessage("Voulez-vous vraiment supprimer ${program.name} ?")
                        .setPositiveButton("Supprimer") { _, _ ->
                            // Si l'utilisateur clique sur "Supprimer"
                            programViewModel.deleteProgram(program.id, currentUserId)
                            android.widget.Toast.makeText(requireContext(), "Programme supprimé", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Annuler", null) // Ne fait rien si on annule
                        .show()
                    },
                    onEditClick = { program ->
                        val bundle = Bundle().apply {
                            putString("programNameKey", program.name)
                            putInt("programIdKey", program.id)
                            putInt("userId", currentUserId)
                        }
                        findNavController().navigate(R.id.nav_edit_program, bundle)
                    }
                )

                // On lie l'adapter au RecyclerView
                recyclerView.adapter = adapter
            } else {
                Log.d("PROGRAM_STATUS", "La liste est vide ou l'API n'a pas répondu.")
            }
        }

        // Lancement initial
        programViewModel.fetchPrograms(currentUserId)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ProgramAdapter(
        private val programs: List<ApiService.Program>,
        private val onClick: (ApiService.Program) -> Unit,
        private val onEditClick: (ApiService.Program) -> Unit
    ) : RecyclerView.Adapter<ProgramAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.textViewProgramName)
            val btnDelete: ImageButton = view.findViewById(R.id.buttonDelete)
            val btnEdit: ImageButton = view.findViewById(R.id.buttonEdit)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_program, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val program = programs[position]
            holder.text.text = program.name

            holder.btnDelete.setOnClickListener {
                onClick(program)
            }

            holder.btnEdit.setOnClickListener {
                onEditClick(program)
            }
        }

        override fun getItemCount() = programs.size
    }
}