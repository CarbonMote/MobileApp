package com.example.carbonmoteapp.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

class StepCounterController(
//    private val dayUseCases: DayUseCases,
    private val coroutineScope: CoroutineScope,
    currentDateFlow: StateFlow<LocalDate>,
) {

    private val _stats = MutableStateFlow(StepCounterState(0,0.0))
    val stats: StateFlow<StepCounterState> = _stats.asStateFlow()

    private var getStatsJob: Job? = null

//    init {
//        coroutineScope.launch {
////            currentDateFlow.collect { getStats(it) }
//        }
//    }

//    private fun getStats(date: LocalDate) {
//        getStatsJob?.cancel()
//
//        getStatsJob = dayUseCases.getDay(date).onEach { day ->
//            _stats.value = day.run {
//                StepCounterState(
//                    date = date,
//                    steps = steps,
//                    goal = goal,
//                    distanceTravelled = distanceTravelled,
//                    calorieBurned = calorieBurned.roundToInt()
//                )
//            }
//        }.launchIn(coroutineScope)
//    }

    private val rawStepSensorReadings = MutableStateFlow(StepCounterEvent(0))
    private var previousStepCount: Int? = null

    init {
        rawStepSensorReadings.drop(1).onEach { event ->
            val stepCountDifference = event.stepCount - (previousStepCount ?: event.stepCount)
            previousStepCount = event.stepCount
//            dayUseCases.incrementStepCount(event.eventDate, stepCountDifference)
        }.launchIn(coroutineScope)
    }

    fun onStepCountChanged(newStepCount: Int) {
        rawStepSensorReadings.value = StepCounterEvent(newStepCount)
    }
}
