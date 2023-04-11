/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.messanginfirebase.presentation

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.text.CaseMap.Title
import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiObjects
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Water
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.messanginfirebase.R
import com.example.messanginfirebase.presentation.NavRoute.principal
import com.example.messanginfirebase.presentation.theme.MessanginfirebaseTheme
import com.example.messanginfirebase.presentation.theme.amaticsc
import com.google.android.gms.tasks.OnCompleteListener


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers



class MainActivity : ComponentActivity() {
    val title = "Notificacion Title"
    var body = "Notificacion Body"

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getFirebaseToken()
            Toast.makeText(
                this, "Permission granted", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this, "Permission denied", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                requestPermissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            }
        }
    }

    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "Token: $token")

        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreference = getSharedPreferences("USERNAME_PREF0",0)
        setContent {
            WearApp(title, body, sharedPreference)
        }
        askNotificationPermission()
        getFirebaseToken()
    }
}

@Composable
fun WearApp(title: String, body: String, sharedPreference: SharedPreferences){
    MessanginfirebaseTheme {

        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        val listState = rememberScalingLazyListState()
        Scaffold(timeText = {
            if (!listState.isScrollInProgress) {
                TimeText()
            }
        },
            vignette = {
                Vignette(vignettePosition = VignettePosition.Top)
            },
            positionIndicator = {
                PositionIndicator(scalingLazyListState = listState)
            }
        ) {
            principal(sharedPreferences = sharedPreference)
        }
    }

    }


@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

//////////////////////////////////////////////////////////////////
object NavRoute {
    const val HomeScreen = "home"
    const val tabDatos= "tabDatos"
    const val tabFoco ="tabFoco"

    @Composable
    fun principal(sharedPreferences: SharedPreferences ) {
        val navController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(navController = navController, startDestination = NavRoute.HomeScreen)
        {

            composable(NavRoute.HomeScreen) {
                homeScreen(sharedPreferences,navController)
            }
            composable(NavRoute.tabFoco) {
                tabFoco(sharedPreferences, navController, viewModel=FirebaseView())
            }
            composable(NavRoute.tabDatos) {
                tabDatos(navController, sharedPreferences, viewModel=FirebaseView())
            }

        }

    }
}

@Composable
fun homeScreen(sharedPreferences: SharedPreferences, navigation: NavController) {
    val state= rememberScrollState()
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
        .verticalScroll(state),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,) {

        Spacer(modifier = Modifier.height(45.dp))
        Chip(
            label = { Text(
                fontFamily = amaticsc,
                fontWeight = FontWeight.Bold,
                text = "LED",
                textAlign = TextAlign.Center,
            ) },
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.tabFoco) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.ejercicio)
            ),
        )
        Spacer(modifier = Modifier.height(45.dp))
        Chip(
            label = { Text(
                fontFamily = amaticsc,
                fontWeight = FontWeight.Bold,
                text = "Temperatura",
                textAlign = TextAlign.Center,
            ) },
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.tabDatos) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.ejercicio)
            ),
        )
    }
}

@Composable
fun tabDatos( navController: NavController, sharedPreferences: SharedPreferences, viewModel: FirebaseView){
    viewModel.getData()
    val index = viewModel.Datos_firebase.value.size
    val state = rememberScrollState()
    LaunchedEffect(Unit) { state.animateScrollTo(100) }
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(viewModel.Datos_firebase.value) { workout ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colors.background
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    modifier = Modifier.padding(top = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White
                    ),
                    onClick = { /**/ }, // Ação
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Thermostat,
                        contentDescription = "airplane",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(ButtonDefaults.DefaultButtonSize)
                            .wrapContentSize(align = Alignment.Center),
                    )
                }
                Text(
                    text = "Temperatura: ${workout.temperatura}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                )
                Button(
                    modifier = Modifier.padding(top = 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White
                    ),
                    onClick = { /**/ }, // Ação
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Water,
                        contentDescription = "airplane",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(ButtonDefaults.DefaultButtonSize)
                            .wrapContentSize(align = Alignment.Center),
                    )
                }
                Text(
                    text = "Humedad: ${workout.humedad}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                )
            }
        }
    }
        }






@Composable
fun tabFoco( sharedPreferences: SharedPreferences, navController: NavController, viewModel: FirebaseView){
    viewModel.getData()
    val index = viewModel.Datos_firebase.value.size
    val state = rememberScrollState()
    LaunchedEffect(Unit) { state.animateScrollTo(100) }
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(viewModel.Datos_firebase.value) { workout ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colors.background
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    modifier = Modifier.padding(top = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (workout.relay==1) {
                            Color.Yellow
                        } else {
                            Color.Blue
                        }),
                    onClick = {
                        viewModel.writeToDB(
                            datos_firebase(
                                relay = if (workout.relay==1) 0 else 1
                            ),0)
                              }, // Ação
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiObjects,
                        contentDescription = "airplane",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(ButtonDefaults.DefaultButtonSize)
                            .wrapContentSize(align = Alignment.Center),
                    )
                }
                Text(
                    text = if (workout.relay==1) "Encendido" else "Apagado",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                )
            }
        }
}
}


/////////////////////////////////////Firebase///////////////////////
data class datos_firebase(
    val humedad: Int = 0,
    val relay: Int=0,
    var temperatura: Int = 0
)

class FirebaseView : ViewModel() {
    private val database = Firebase.database("https://rugged-cooler-303905-default-rtdb.firebaseio.com")
    private var _datos = mutableStateOf<List<datos_firebase>>(emptyList())
    val Datos_firebase: State<List<datos_firebase>> = _datos

    fun getData() {
        database.getReference("test").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _datos.value = snapshot.getValue<List<datos_firebase>>()!!
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error getting data", error.toException())
                }
            }
        )
    }
    fun writeToDB(data: datos_firebase, index: Int) {
        val database = Firebase.database("https://rugged-cooler-303905-default-rtdb.firebaseio.com")
        val myRef = database.getReference("test")
        listOf(data).forEach {
            myRef.child(index.toString()).setValue(it)
            Log.i("FIRE","PASO")
        }
    }
}
