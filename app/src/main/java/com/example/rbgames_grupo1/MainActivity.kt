package com.example.rbgames_grupo1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.rbgames_grupo1.data.local.database.AppDatabase
import com.example.rbgames_grupo1.data.repository.UserRepository
import com.example.rbgames_grupo1.navigation.AppNavGraph
import com.example.rbgames_grupo1.ui.theme.RBGames_Grupo1Theme
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current.applicationContext

    val db = remember{ AppDatabase.getInstance(context) }
    val userRepository = remember { UserRepository(db.userDao()) }

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userRepository))
    val navController = rememberNavController()


    RBGames_Grupo1Theme {
        Surface {
            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}