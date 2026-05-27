package com.cipecma.flexit.ui.editprogram

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.cipecma.flexit.R
import com.cipecma.flexit.databinding.FragmentEditProgramBinding
import com.cipecma.flexit.ui.program.ProgramViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProgramFragment : Fragment() {

    private var _binding: FragmentEditProgramBinding? = null
    private val binding get() = _binding!!

    private val programViewModel: ProgramViewModel by activityViewModels()
    private val editViewModel: EditProgramViewModel by activityViewModels()

    private var programId: Int = -1
    private var userId: Int = -1

    private val displayFormat = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

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

        val initialName = arguments?.getString("programNameKey").orEmpty()
        programId = arguments?.getInt("programIdKey") ?: -1
        userId = arguments?.getInt("userId") ?: -1

        // Si on arrive sur un nouveau programme, on remet le VM à zéro AVANT
        // d'observer programName / workouts, pour éviter d'afficher l'état du précédent.
        val isNewProgram = programId > 0 && editViewModel.currentProgramId != programId
        if (isNewProgram) {
            editViewModel.reset()
            editViewModel.setProgramName(initialName)
        }

        editViewModel.programName.observe(viewLifecycleOwner) { name ->
            if (binding.editProgramName.text?.toString() != name) {
                binding.editProgramName.setText(name)
                binding.editProgramName.setSelection(name.length)
            }
        }
        binding.editProgramName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editViewModel.setProgramName(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.fabAddWorkout.setOnClickListener {
            pickDate { date -> editViewModel.addWorkout(date) }
        }

        binding.btnSaveProgram.setOnClickListener {
            val newName = editViewModel.programName.value.orEmpty().trim()
            if (newName.isBlank()) {
                binding.editProgramName.error = "Le nom ne peut pas être vide"
                return@setOnClickListener
            }
            programViewModel.updateProgram(programId, newName, userId)
            editViewModel.save(programId)
        }

        editViewModel.workouts.observe(viewLifecycleOwner) { renderWorkouts() }

        editViewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            result.onSuccess {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                editViewModel.consumeSaveResult()
                findNavController().navigateUp()
            }.onFailure {
                Toast.makeText(requireContext(), "Erreur : ${it.message}", Toast.LENGTH_LONG).show()
                editViewModel.consumeSaveResult()
            }
        }

        if (isNewProgram) {
            editViewModel.load(programId)
        }
    }

    private fun renderWorkouts() {
        val container = binding.workoutsContainer
        container.removeAllViews()

        val workouts = editViewModel.workouts.value ?: emptyList()
        binding.textWorkoutsCount.text = workouts.size.toString()
        binding.textEmptyWorkouts.visibility = if (workouts.isEmpty()) View.VISIBLE else View.GONE

        val inflater = LayoutInflater.from(requireContext())

        workouts.forEachIndexed { wIdx, workout ->
            val cardView = inflater.inflate(R.layout.item_workout_summary, container, false)
            val textDate = cardView.findViewById<TextView>(R.id.textWorkoutDate)
            val textCount = cardView.findViewById<TextView>(R.id.textExerciseCount)
            val btnDelete = cardView.findViewById<ImageButton>(R.id.btnDeleteWorkout)

            textDate.text = formatDate(workout.date)
            val count = workout.exercises.size
            textCount.text = if (count <= 1) "$count exercice" else "$count exercices"

            cardView.setOnClickListener {
                val bundle = Bundle().apply { putInt("workoutIndex", wIdx) }
                findNavController().navigate(R.id.nav_edit_workout, bundle)
            }
            btnDelete.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Supprimer la séance")
                    .setMessage("Confirmer la suppression de cette séance ?")
                    .setPositiveButton("Supprimer") { _, _ -> editViewModel.removeWorkout(wIdx) }
                    .setNegativeButton("Annuler", null)
                    .show()
            }

            container.addView(cardView)
        }
    }

    private fun formatDate(iso: String): String {
        if (iso.isBlank()) return "Date à définir"
        return runCatching {
            val date = isoFormat.parse(iso) ?: return iso
            displayFormat.format(date).replaceFirstChar { it.uppercase() }
        }.getOrDefault(iso)
    }

    private fun pickDate(initial: String = "", onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        runCatching {
            if (initial.isNotBlank()) {
                val parts = initial.split("-")
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        }
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val formatted = "%04d-%02d-%02d".format(y, m + 1, d)
                onPicked(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
