package com.example.calorie

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calorie.data.CalorieDatabase
import com.example.calorie.data.FoodItemEntity
import com.example.calorie.data.FoodRepository
import com.example.calorie.data.FoodWithIngredients
import com.example.calorie.domain.CalorieCalculator
import com.example.calorie.domain.FoodInput
import com.example.calorie.domain.IngredientInput
import com.example.calorie.network.MealDbRecipe
import com.example.calorie.ui.CalorieViewModel
import com.example.calorie.ui.theme.CalorieTheme
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalorieTheme {
                val database = remember { CalorieDatabase.getInstance(applicationContext) }
                val repository = remember { FoodRepository(database.foodDao()) }
                val prefs = remember { applicationContext.getSharedPreferences("calorie_prefs", Context.MODE_PRIVATE) }
                val viewModel: CalorieViewModel = viewModel(
                    factory = CalorieViewModel.Factory(repository, prefs)
                )
                CalorieApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CalorieApp(viewModel: CalorieViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsStateWithLifecycle()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "diary",
                    onClick = { navController.navigate("diary") { popUpTo(navController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true ; restoreState = true } },
                    label = { Text("다이어리") },
                    icon = { Text("🗓️") }
                )
                NavigationBarItem(
                    selected = currentRoute == "list",
                    onClick = { navController.navigate("list") { popUpTo(navController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true ; restoreState = true } },
                    label = { Text("음식사전") },
                    icon = { Text("📖") }
                )
                NavigationBarItem(
                    selected = currentRoute == "profile",
                    onClick = { navController.navigate("profile") { popUpTo(navController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true ; restoreState = true } },
                    label = { Text("내 정보") },
                    icon = { Text("👤") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "diary",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("diary") {
                DiaryScreen(viewModel = viewModel)
            }
            composable("list") {
                FoodListScreen(
                    viewModel = viewModel,
                    onAdd = { navController.navigate("edit") },
                    onRecipes = { navController.navigate("recipes") },
                    onOpen = { navController.navigate("detail/$it") }
                )
            }
            composable("profile") {
                ProfileScreen(viewModel = viewModel)
            }
            composable("recipes") {
                RecipeExplorerScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onImported = { navController.popBackStack("list", inclusive = false) }
                )
            }
            composable(
                route = "detail/{foodId}",
                arguments = listOf(navArgument("foodId") { type = NavType.LongType })
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getLong("foodId") ?: return@composable
                FoodDetailScreen(
                    viewModel = viewModel,
                    foodId = foodId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate("edit?foodId=$foodId") },
                    onDeleted = { navController.popBackStack("list", inclusive = false) }
                )
            }
            composable(
                route = "edit?foodId={foodId}",
                arguments = listOf(
                    navArgument("foodId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                FoodEditScreen(
                    viewModel = viewModel,
                    foodId = backStackEntry.arguments?.getLong("foodId") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack("list", inclusive = false) }
                )
            }
        }
    }
}

@Composable
private fun FoodListScreen(
    viewModel: CalorieViewModel,
    onAdd: () -> Unit,
    onRecipes: () -> Unit,
    onOpen: (Long) -> Unit
) {
    val foods by viewModel.foods.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FloatingActionButton(onClick = onRecipes) {
                    Text("📚")
                }
                FloatingActionButton(onClick = onAdd) {
                    Text("+")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeroHeader(
                    title = "Calorie Atlas",
                    subtitle = "음식 사전 + 레시피 백과 + 오프라인 칼로리 기록",
                    modifier = Modifier.testTag("food_list_title")
                )
                Text("오프라인 음식 사전 · ${foods.size}개 저장됨", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRecipes) { Text("요리백과 API") }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(foods, key = { it.id }) { food ->
                FoodCard(food = food, onClick = { onOpen(food.id) })
            }
        }
    }
}

@Composable
private fun HeroHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B5E20), Color(0xFF00A86B), Color(0xFFFFB300))
                )
            )
            .padding(22.dp)
    ) {
        Text("✦", color = Color.White, modifier = Modifier.align(Alignment.TopEnd).alpha(.45f))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White)
            Text(subtitle, color = Color.White.copy(alpha = .92f))
        }
    }
}

@Composable
private fun FoodCard(food: FoodItemEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PhotoPlaceholder(uri = food.photoUri)
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${food.category} · ${food.servingSizeGram}g", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (food.isCustom) Text("직접 만든 음식", color = MaterialTheme.colorScheme.primary)
            }
            Text("${food.calories} kcal", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RecipeExplorerScreen(
    viewModel: CalorieViewModel,
    onBack: () -> Unit,
    onImported: () -> Unit
) {
    val state by viewModel.recipeSearchState.collectAsStateWithLifecycle()

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (state.recipes.isEmpty() && state.error == null) viewModel.searchRecipes()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedButton(onClick = onBack) { Text("뒤로") }

        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::updateRecipeQuery,
                    label = { Text("검색어: 김치찌개, 불고기...") },
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { 
                        focusManager.clearFocus()
                        viewModel.searchRecipes() 
                    }),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = viewModel::searchRecipes,
                    enabled = !state.isLoading
                ) { Text(if (state.isLoading) "검색중" else "검색") }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        items(state.recipes, key = { it.externalId }) { recipe ->
            RecipeCard(recipe = recipe, onImport = { viewModel.importRecipe(recipe, onImported) })
        }
    }
}

@Composable
private fun RecipeCard(recipe: MealDbRecipe, onImport: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(recipe.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${recipe.area} · ${recipe.category}", color = MaterialTheme.colorScheme.primary)
                Text(
                    recipe.ingredients.take(5).joinToString(" · "),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    recipe.instructions,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                    Text("음식 사전에 저장")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodDetailScreen(
    viewModel: CalorieViewModel,
    foodId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit
) {
    val foodWithIngredients by viewModel.observeFood(foodId).collectAsStateWithLifecycle(initialValue = null)
    val food = foodWithIngredients?.food

    if (food == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("음식 정보를 불러오는 중이야.")
        }
        return
    }

    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var consumedGrams by remember { mutableStateOf(food.servingSizeGram.toString()) }
        var mealType by remember { mutableStateOf("점심") }
        val parsedGrams = consumedGrams.toDoubleOrNull() ?: 0.0
        val calculatedCalories = if (food.servingSizeGram > 0) {
            ((food.calories * parsedGrams) / food.servingSizeGram).toInt()
        } else food.calories

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("다이어리에 기록하기") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${food.name}을(를) 언제 드셨나요?")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("아침", "점심", "저녁", "간식").forEach { type ->
                            FilterChip(
                                selected = mealType == type,
                                onClick = { mealType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                    OutlinedTextField(
                        value = consumedGrams,
                        onValueChange = { consumedGrams = it },
                        label = { Text("먹은 양(g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    Text("계산된 섭취 칼로리: $calculatedCalories kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addMealRecord(food.id, mealType, parsedGrams.toInt(), calculatedCalories)
                    showAddDialog = false
                    onBack()
                }) {
                    Text("기록 저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("취소") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack) { Text("뒤로") }
                Button(onClick = onEdit) { Text("수정") }
                TextButton(onClick = { viewModel.deleteFood(food.id, onDeleted) }) { Text("삭제") }
            }
            Spacer(modifier = Modifier.height(10.dp))
            PhotoPlaceholder(uri = food.photoUri, modifier = Modifier.fillMaxWidth().height(160.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(food.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${food.category} · ${food.servingSizeGram}g 기준")
            Text("${food.calories} kcal", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            if (food.note.isNotBlank()) Text("메모: ${food.note}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("🗓️ 오늘 다이어리에 기록하기")
            }
        }
        if (foodWithIngredients?.ingredients?.isNotEmpty() == true) {
            item {
                Text("재료", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            items(foodWithIngredients!!.ingredients) { ingredient ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${ingredient.name} ${ingredient.weightGram}g")
                        Text("${ingredient.calories} kcal")
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryScreen(viewModel: CalorieViewModel) {
    val currentDate by viewModel.currentDate.collectAsStateWithLifecycle()
    val dailyMeals by viewModel.dailyMeals.collectAsStateWithLifecycle()
    val targetCalories by viewModel.targetCalories.collectAsStateWithLifecycle()
    val totalCalories = dailyMeals.sumOf { it.record.consumedCalories }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::previousDay) { Text("◀") }
            Text(currentDate, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = viewModel::nextDay) { Text("▶") }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("오늘 섭취한 총 칼로리", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$totalCalories / $targetCalories kcal", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(dailyMeals, key = { it.record.id }) { meal ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.record.mealType, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(meal.food.name, style = MaterialTheme.typography.titleMedium)
                            Text("${meal.record.consumedGrams}g 섭취", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${meal.record.consumedCalories} kcal", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.deleteMealRecord(meal.record.id) }) {
                            Text("❌")
                        }
                    }
                }
            }
            if (dailyMeals.isEmpty()) {
                item {
                    Text("기록된 식단이 없습니다.\n사전에서 음식을 찾아 다이어리에 기록해보세요!", modifier = Modifier.fillMaxWidth().padding(top = 40.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FoodEditScreen(
    viewModel: CalorieViewModel,
    foodId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val capturedPhotoUri by viewModel.capturedPhotoUri.collectAsStateWithLifecycle()
    val existing by if (foodId > 0) {
        viewModel.observeFood(foodId).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { mutableStateOf<FoodWithIngredients?>(null) }
    }
    var initialized by remember(foodId) { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("기타") }
    var servingSizeGram by remember { mutableStateOf("") }
    var manualCalories by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf(capturedPhotoUri) }
    val ingredients = remember { mutableStateListOf<IngredientInput>() }
    val ingredientTotal = CalorieCalculator.totalIngredientCalories(ingredients)
    var apiKcalPer100 by remember { mutableStateOf<Int?>(null) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    LaunchedEffect(ingredientTotal, isCustom) {
        if (isCustom) {
            manualCalories = ingredientTotal.toString()
        } else if (ingredients.isNotEmpty()) {
            isCustom = true
            manualCalories = ingredientTotal.toString()
        }
    }

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val savedUriStr = copyUriToInternalStorage(context, uri)
            photoUri = savedUriStr
            viewModel.setCapturedPhotoUri(savedUriStr)
        }
    }

    LaunchedEffect(existing?.food?.id, capturedPhotoUri) {
        if (!initialized && existing != null) {
            val food = existing!!.food
            name = food.name
            category = food.category
            servingSizeGram = food.servingSizeGram.toString()
            manualCalories = food.calories.toString()
            note = food.note
            isCustom = food.isCustom
            photoUri = food.photoUri
            ingredients.clear()
            ingredients.addAll(
                existing!!.ingredients.map {
                    IngredientInput(it.name, it.weightGram.toString(), it.kcalPer100Gram.toString())
                }
            )
            initialized = true
        } else if (!initialized && foodId == 0L) {
            photoUri = capturedPhotoUri
            initialized = true
        }
    }

    LaunchedEffect(name) {
        if (name.length >= 2) {
            delay(500)
            val info = kotlinx.coroutines.Dispatchers.IO.let {
                kotlinx.coroutines.withContext(it) {
                    com.example.calorie.network.MealDbApi().searchIngredientInfo(name)
                }
            }
            if (info != null && !isCustom) {
                val (kcal, serving) = info
                val kcal100 = if (serving > 0) (kcal * 100) / serving else kcal
                apiKcalPer100 = kcal100
                
                if (servingSizeGram.isEmpty() || manualCalories.isEmpty() || manualCalories == "0") {
                    servingSizeGram = serving.toString()
                    manualCalories = kcal.toString()
                } else {
                    val currentGrams = servingSizeGram.toDoubleOrNull() ?: 0.0
                    manualCalories = ((kcal100 * currentGrams) / 100).toInt().toString()
                }
            } else if (info == null && !isCustom) {
                apiKcalPer100 = 0
                if (manualCalories != "0") {
                    manualCalories = "0"
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack) { Text("뒤로") }
                Button(
                    onClick = {
                        viewModel.saveFood(
                            FoodInput(
                                id = foodId,
                                name = name,
                                category = category,
                                servingSizeGram = servingSizeGram,
                                manualCalories = manualCalories,
                                photoUri = photoUri,
                                note = note,
                                isCustom = isCustom,
                                ingredients = ingredients
                            ),
                            onSaved = onSaved
                        )
                    }
                ) { Text("저장") }
            }
            Text(
                text = if (foodId > 0) "음식 수정" else "음식 등록",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("음식 이름") },
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_name_field")
                )
        }
        item {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("카테고리") },
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = servingSizeGram,
                    onValueChange = { 
                        servingSizeGram = it
                        if (apiKcalPer100 != null && !isCustom) {
                            val newGrams = it.toDoubleOrNull() ?: 0.0
                            manualCalories = ((apiKcalPer100!! * newGrams) / 100).toInt().toString()
                        }
                    },
                    label = { Text("제공량(g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = manualCalories,
                    onValueChange = { 
                        manualCalories = it
                        apiKcalPer100 = null
                    },
                    label = { Text("최종 kcal") },
                    readOnly = isCustom,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("직접 만든 음식", modifier = Modifier.weight(1f))
                Switch(checked = isCustom, onCheckedChange = { isCustom = it })
            }
            OutlinedButton(onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Text("갤러리에서 사진 첨부")
            }
            if (photoUri != null) {
                PhotoPlaceholder(uri = photoUri, modifier = Modifier.fillMaxWidth().height(160.dp))
            }
        }
        item {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("메모") },
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("재료 합산: $ingredientTotal kcal", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }
            Button(onClick = { ingredients.add(IngredientInput("", "", "")) }) {
                Text("재료 추가")
            }
        }
        itemsIndexed(ingredients) { index, ingredient ->
            IngredientRow(
                ingredient = ingredient,
                onChange = { ingredients[index] = it },
                onRemove = { ingredients.removeAt(index) }
            )
        }
    }
}

@Composable
private fun IngredientRow(
    ingredient: IngredientInput,
    onChange: (IngredientInput) -> Unit,
    onRemove: () -> Unit
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    // 0.5초 디바운스 딜레이 적용 후 식약처 실제 API 연동
    LaunchedEffect(ingredient.name) {
        if (ingredient.name.length >= 2) {
            delay(500)
            val info = kotlinx.coroutines.Dispatchers.IO.let {
                kotlinx.coroutines.withContext(it) {
                    com.example.calorie.network.MealDbApi().searchIngredientInfo(ingredient.name)
                }
            }
            if (info != null) {
                val (kcal, serving) = info
                // 100g 당 칼로리 환산: (총칼로리 * 100) / 1회제공량
                val kcalPer100 = if (serving > 0) (kcal * 100) / serving else kcal
                if (kcalPer100.toString() != ingredient.kcalPer100Gram) {
                    onChange(ingredient.copy(kcalPer100Gram = kcalPer100.toString()))
                }
            } else {
                if (ingredient.kcalPer100Gram != "0") {
                    onChange(ingredient.copy(kcalPer100Gram = "0"))
                }
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = { onChange(ingredient.copy(name = it)) },
                label = { Text("재료명") },
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ingredient.weightGram,
                    onValueChange = { onChange(ingredient.copy(weightGram = it)) },
                    label = { Text("중량(g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ingredient.kcalPer100Gram,
                    onValueChange = { onChange(ingredient.copy(kcalPer100Gram = it)) },
                    label = { Text("100g kcal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "계산: ${CalorieCalculator.totalIngredientCalories(listOf(ingredient))} kcal",
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRemove) { Text("삭제") }
            }
        }
    }
}


@Composable
private fun PhotoPlaceholder(uri: String?, modifier: Modifier = Modifier.size(64.dp)) {
    if (uri != null) {
        AsyncImage(
            model = uri,
            contentDescription = "food photo",
            modifier = modifier.clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFECEFF1)),
            contentAlignment = Alignment.Center
        ) {
            Text("🍽️")
        }
    }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    val directory = File(context.filesDir, "food_photos")
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "food_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    }
    return file.toURI().toString()
}

private fun copyUriToInternalStorage(context: Context, uri: android.net.Uri): String {
    val directory = File(context.filesDir, "food_photos")
    if (!directory.exists()) directory.mkdirs()
    val file = File(directory, "food_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file.toURI().toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: CalorieViewModel) {
    val heightFlow by viewModel.height.collectAsStateWithLifecycle()
    val weightFlow by viewModel.weight.collectAsStateWithLifecycle()
    val ageFlow by viewModel.age.collectAsStateWithLifecycle()
    val genderFlow by viewModel.gender.collectAsStateWithLifecycle()
    val activityLevelFlow by viewModel.activityLevel.collectAsStateWithLifecycle()
    val targetCalories by viewModel.targetCalories.collectAsStateWithLifecycle()

    var height by remember(heightFlow) { mutableStateOf(heightFlow) }
    var weight by remember(weightFlow) { mutableStateOf(weightFlow) }
    var age by remember(ageFlow) { mutableStateOf(ageFlow) }
    var gender by remember(genderFlow) { mutableStateOf(genderFlow) }
    var activityLevel by remember(activityLevelFlow) { mutableStateOf(activityLevelFlow) }

    val hVal = height.toFloatOrNull()
    val wVal = weight.toFloatOrNull()
    val bmi = if (hVal != null && wVal != null && hVal > 0) {
        val hMeter = hVal / 100f
        wVal / (hMeter * hMeter)
    } else 0f

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("내 정보 및 BMI / 목표 칼로리 계산기", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("나의 기초 프로필", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("키 (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done), keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("몸무게 (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done), keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("만 나이") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = androidx.compose.ui.text.input.ImeAction.Done), keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth())
                    
                    Text("성별", modifier = Modifier.padding(top = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("남성", "여성").forEach { g ->
                            FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) })
                        }
                    }

                    Text("활동량", modifier = Modifier.padding(top = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("적음", "보통", "많음", "아주 많음").forEach { act ->
                            FilterChip(selected = activityLevel == act, onClick = { activityLevel = act }, label = { Text(act) })
                        }
                    }

                    Button(
                        onClick = { viewModel.saveProfile(height, weight, age, gender, activityLevel) },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Text("저장 및 계산하기")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("계산 결과", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (bmi > 0) {
                        Text("BMI 지수: ${String.format("%.1f", bmi)}", style = MaterialTheme.typography.titleLarge)
                    }
                    Text("일일 권장 목표 칼로리: $targetCalories kcal", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
