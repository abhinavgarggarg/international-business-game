package com.soprasteria.ib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.soprasteria.ib.data.AppDatabase
import com.soprasteria.ib.data.GameRepository
import com.soprasteria.ib.ui.GameViewModel
import com.soprasteria.ib.ui.screens.GameScreen
import com.soprasteria.ib.ui.theme.InternationalBusinessTheme

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GameViewModel(repository) as T
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "international-business.db")
            .fallbackToDestructiveMigration()
            .build()
        val repository = GameRepository(db.gameSaveDao())

        setContent {
            InternationalBusinessTheme {
                val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository))
                GameScreen(viewModel = viewModel)
            }
        }
    }
}
