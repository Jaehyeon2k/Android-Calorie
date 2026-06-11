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

class CalorieViewModel(private val repository: FoodRepository) : ViewModel() {
    private val mealDbApi = MealDbApi()

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
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
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
        val query = _recipeSearchState.value.query.ifBlank { "chicken" }
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

    class Factory(private val repository: FoodRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalorieViewModel(repository) as T
        }
    }
}
