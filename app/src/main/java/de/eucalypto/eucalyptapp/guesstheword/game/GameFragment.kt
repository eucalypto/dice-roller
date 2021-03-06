/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.eucalypto.eucalyptapp.guesstheword.game

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import de.eucalypto.eucalyptapp.databinding.FragmentGuessthewordGameBinding
import timber.log.Timber

/**
 * Fragment where the game is played
 */
class GameFragment : Fragment() {

    private val viewModel: GameViewModel by viewModels()

    private lateinit var binding: FragmentGuessthewordGameBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        binding = FragmentGuessthewordGameBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.eventGameFinish.observe(viewLifecycleOwner, Observer { hasFinished ->
            if (hasFinished) gameFinished()
        })

        viewModel.eventBuzz.observe(viewLifecycleOwner, Observer { buzzType ->
            buzz(buzzType.pattern)
        })

        return binding.root
    }

    /**
     * Called when the game is finished
     */
    private fun gameFinished() {
        val action =
            GameFragmentDirections.actionGuessthewordShowScore(
                viewModel.score.value ?: 0
            )
        findNavController(this).navigate(action)
    }

    private fun buzz(pattern: LongArray) {
        // The game crashes if the pattern is just [0] aka no pattern and is given to createWaveform()
        if (pattern.contentEquals(longArrayOf(0))) return

        val buzzer = activity?.getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Timber.d("Using vibrate() system newer/equal than Oreo")
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                Timber.d("Using vibrate() for system older than Oreo")
                //deprecated in API 26
                @Suppress("DEPRECATION")
                buzzer.vibrate(pattern, -1)
            }
        }

        viewModel.onBuzzComplete()
    }
}
