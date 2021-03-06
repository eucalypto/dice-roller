package de.eucalypto.eucalyptapp.guesstheword.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import de.eucalypto.eucalyptapp.util.toLiveData
import timber.log.Timber

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

enum class BuzzType(val pattern: LongArray) {
    CORRECT(CORRECT_BUZZ_PATTERN),
    GAME_OVER(GAME_OVER_BUZZ_PATTERN),
    COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
    NO_BUZZ(NO_BUZZ_PATTERN)
}

class GameViewModel : ViewModel() {

    companion object {
        // These represent different important times
        // This is when the game is over
        const val DONE = 0L

        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L

        // This is the total time of the game
        const val COUNTDOWN_TIME = 8000L

        const val PANIC_TIME = 4L
    }

    // The current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String> by this::_word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int> by this::_score

    private val timer: CountDownTimer

    private val _secondsLeft = MutableLiveData<Long>()
    val secondsLeft: LiveData<Long> by this::_secondsLeft

    val secondsLeftString: LiveData<String> = Transformations.map(secondsLeft) { remainingSeconds ->
        Timber.d("map for secondsLeftString executed: $remainingSeconds")
        DateUtils.formatElapsedTime(remainingSeconds)
    }

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish = _eventGameFinish.toLiveData()

    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz = _eventBuzz.toLiveData()

    init {
        Timber.d("GameViewModel created")
        _eventGameFinish.value = false
        resetList()
        nextWord()
        _score.value = 0
        _secondsLeft.value = (COUNTDOWN_TIME / ONE_SECOND) + 1

        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                Timber.d("timer onTick()")
                _secondsLeft.value = secondsLeft.value?.minus(1)

                secondsLeft.value?.let {
                    if (it in 1..PANIC_TIME) _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            override fun onFinish() {
                Timber.d("timer onFinish()")
                _eventBuzz.value = BuzzType.GAME_OVER
                _eventGameFinish.value = true
            }
        }
        timer.start()
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = _score.value?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _eventBuzz.value = BuzzType.CORRECT
        _score.value = _score.value?.plus(1)
        nextWord()
    }

    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Timber.d("GameViewModel destroyed")
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }
}