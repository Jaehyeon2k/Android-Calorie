package com.example.calorie.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calorie.data.FoodItemEntity
import com.example.calorie.data.FoodRepository
import com.example.calorie.data.FoodWithIngredients
import com.example.calorie.domain.CalorieCalculator
import com.example.calorie.domain.FoodInput
import com.example.calorie.network.MealDbApi
import com.example.calorie.network.MealDbRecipe
import com.example.calorie.network.RecipeSearchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.calorie.data.MealRecordEntity
import com.example.calorie.data.MealRecordWithFood
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

import android.content.SharedPreferences
import kotlinx.coroutines.flow.combine

class CalorieViewModel(
    private val repository: FoodRepository,
    private val prefs: SharedPreferences
) : ViewModel() {
    private val mealDbApi = MealDbApi()

    private val _currentDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val currentDate: StateFlow<String> = _currentDate

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyMeals: StateFlow<List<MealRecordWithFood>> = _currentDate.flatMapLatest { date ->
        repository.observeMealsByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val height = MutableStateFlow(prefs.getString("height", "") ?: "")
    val weight = MutableStateFlow(prefs.getString("weight", "") ?: "")
    val age = MutableStateFlow(prefs.getString("age", "") ?: "")
    val gender = MutableStateFlow(prefs.getString("gender", "여성") ?: "여성")
    val activityLevel = MutableStateFlow(prefs.getString("activityLevel", "보통") ?: "보통")

    val targetCalories: StateFlow<Int> = combine(height, weight, age, gender, activityLevel) { h, w, a, g, act ->
        val hVal = h.toFloatOrNull() ?: return@combine 2000
        val wVal = w.toFloatOrNull() ?: return@combine 2000
        val aVal = a.toIntOrNull() ?: return@combine 2000
        
        // Mifflin-St Jeor Equation
        val bmr = CalorieCalculator.calculateBmr(hVal, wVal, aVal, g == "남성")

        val multiplier = when (act) {
            "적음" -> 1.2f
            "보통" -> 1.375f
            "많음" -> 1.55f
            "아주 많음" -> 1.725f
            else -> 1.375f
        }
        (bmr * multiplier).toInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2000)

    fun saveProfile(h: String, w: String, a: String, g: String, act: String) {
        height.value = h
        weight.value = w
        age.value = a
        gender.value = g
        activityLevel.value = act
        prefs.edit()
            .putString("height", h)
            .putString("weight", w)
            .putString("age", a)
            .putString("gender", g)
            .putString("activityLevel", act)
            .apply()
        _message.value = "프로필이 저장됐어. 목표 칼로리가 업데이트됐어!"
    }

    fun previousDay() {
        val current = LocalDate.parse(_currentDate.value)
        _currentDate.value = current.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun nextDay() {
        val current = LocalDate.parse(_currentDate.value)
        _currentDate.value = current.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun addMealRecord(foodId: Long, mealType: String, consumedGrams: Int, consumedCalories: Int) {
        viewModelScope.launch {
            repository.saveMealRecord(
                MealRecordEntity(
                    date = _currentDate.value,
                    foodItemId = foodId,
                    mealType = mealType,
                    consumedGrams = consumedGrams,
                    consumedCalories = consumedCalories
                )
            )
            _message.value = "다이어리에 추가됐어."
        }
    }

    fun deleteMealRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteMealRecord(id)
            _message.value = "다이어리에서 삭제됐어."
        }
    }

    val foods: StateFlow<List<FoodItemEntity>> = repository.foods.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _capturedPhotoUri = MutableStateFlow<String?>(null)
    val capturedPhotoUri: StateFlow<String?> = _capturedPhotoUri

    private val _capturedAiName = MutableStateFlow<String?>(null)
    val capturedAiName: StateFlow<String?> = _capturedAiName

    private val _recipeSearchState = MutableStateFlow(RecipeSearchState())
    val recipeSearchState: StateFlow<RecipeSearchState> = _recipeSearchState

    init {

    }

    fun observeFood(id: Long): Flow<FoodWithIngredients?> = repository.observeFood(id)

    fun saveFood(input: FoodInput, onSaved: () -> Unit) {
        val validation = CalorieCalculator.validate(input)
        if (!validation.isValid) {
            _message.value = validation.message
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.saveFood(input)
            }.onSuccess {
                _message.value = "음식 정보가 저장됐어."
                _capturedPhotoUri.value = null
                onSaved()
            }.onFailure {
                _message.value = "저장 실패: 음식 이름이 중복됐거나 입력값을 확인해야 해."
            }
        }
    }

    fun deleteFood(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteFood(id)
            _message.value = "음식 정보가 삭제됐어."
            onDeleted()
        }
    }

    fun updateRecipeQuery(query: String) {
        _recipeSearchState.update { it.copy(query = query) }
    }

    fun searchRecipes() {
        val query = _recipeSearchState.value.query.ifBlank { "밥" }
        _recipeSearchState.update { it.copy(isLoading = true, error = null, recipes = emptyList()) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                mealDbApi.searchRecipes(query)
            }.onSuccess { recipes ->
                _recipeSearchState.update {
                    it.copy(
                        isLoading = false,
                        recipes = recipes,
                        error = if (recipes.isEmpty()) "검색 결과가 없어. 다른 음식명으로 가보자." else null
                    )
                }
            }.onFailure {
                _recipeSearchState.update {
                    it.copy(isLoading = false, error = "레시피 API 연결 실패. 오프라인 음식 사전은 계속 사용 가능해.")
                }
            }
        }
    }

    fun importRecipe(recipe: MealDbRecipe, onImported: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                repository.importRecipe(recipe)
            }.onSuccess {
                _message.value = "레시피를 음식 사전에 저장했어."
                onImported()
            }.onFailure {
                _message.value = "가져오기 실패: 이미 저장된 레시피거나 네트워크 상태를 확인해줘."
            }
        }
    }

    fun setCapturedPhotoUri(uri: String?) {
        _capturedPhotoUri.value = uri
    }

    fun setCapturedAiName(name: String?) {
        _capturedAiName.value = name
    }

    fun clearMessage() {
        _message.value = null
    }

    class Factory(
        private val repository: FoodRepository,
        private val prefs: SharedPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalorieViewModel(repository, prefs) as T
        }
    }
}
