package com.example.tekuteku_map_google_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tekuteku_map_google_app.ui.theme.TekutekumapgoogleappTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random


const val WEB_CLIENT_ID = "748016273743-4n605dg30asj5d3kj01cdq6deogrhjap.apps.googleusercontent.com"

enum class Screen {
    Login, Home
}

//class UserAccountImpl(
//    override val accountId: String,
//    override val provider: String,
//    override val providerUid: String,
//    override val secretKey: String,
//    override val uuid: String? = null,
//    override val playerId: String? = null,
//    override val credential: Any
//) : UserAccount()
//
//open class UserAccount() {
//    open val accountId: String = ""
//    open val provider: String = ""
//    open val providerUid: String = ""
//    open val  secretKey: String = ""
//    open val uuid: String? = null
//    open val playerId: String? = null
//    open val credential: Any = {}
//}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

//    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "account")

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = Firebase.auth
        setContent {
            TekutekumapgoogleappTheme {
                val navController = rememberNavController()

                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val credentialManager = CredentialManager.create(context)

                val startDestination = if(auth.currentUser != null) Screen.Home.name else Screen.Login.name

                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Screen.Login.name) {
                        LoginScreen (
                            onSigninClick = {
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(true)
                                    .setServerClientId(WEB_CLIENT_ID)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                scope.launch {
                                    try {
                                        val result = credentialManager.getCredential(
                                            context = context,
                                            request = request
                                        )
                                        val credential = result.credential
                                        val googleIdTokenCredential = GoogleIdTokenCredential
                                            .createFrom(credential.data)
                                        val googleIdToken = googleIdTokenCredential.idToken

                                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                                        auth.signInWithCredential((firebaseCredential))
                                            .addOnCompleteListener { task ->
                                                if(task.isSuccessful) {
                                                    navController.popBackStack()
                                                    navController.navigate(Screen.Home.name)
                                                }
                                            }
                                    } catch (e: Exception) {
                                        Toast.makeText( context, "Error: ${e.message}", Toast.LENGTH_SHORT ).show()
                                        e.printStackTrace()
                                    }
                            }
                        })
                    }
                    composable(Screen.Home.name) {
                        HomeScreen ( currentUser = auth.currentUser,
                            onSignOutClick = {
                                auth.signOut()
                                scope.launch {
                                    credentialManager.clearCredentialState(
                                        ClearCredentialStateRequest()
                                    )
                                }
                                navController.popBackStack()
                                navController.navigate(Screen.Login.name)
                            })
                    }
                }
            }
        }
    }
    private val theSalt = "4f24f4ca4107d33a33beeb7001013bda"
    private suspend fun createHashedAccount(accountId: String = UUID.randomUUID().toString()): String {
        val input = "$theSalt$accountId"
        val hashedAccountId = withContext(Dispatchers.IO) { convertHashString(input) }
        return hashedAccountId.uppercase()
    }

    private suspend fun convertHashString(input: String): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        val data = input.toByteArray(StandardCharsets.UTF_8)
        val hashBytes = digest.digest(data)
        hashBytes.joinToString("") { String.format("%02x", it) }
    }

    private fun createSecretKey(length: Int = 32): String {
        val array = ByteArray(length)
        Random.nextBytes(array)
        return array.joinToString("") { byte ->
            String.format("%02X", byte.toInt() and 0xFF)
        }
    }

    private fun createNonce(): String {
        return "${System.currentTimeMillis()}"
    }

//    fun postUsersRegisterAccount(
//        client: Client,
//        accountProviderId: String,
//        hashedAccountId: String,
//        token: String,
//        nonce: String,
//        secretKey: String
//    ): EapiResponse.RegisterAccountResponse {
//        val body = Json.encodeToString(mapOf(
//            "accountProviderId" to accountProviderId,
//            "hashedAccountId" to hashedAccountId,
//            "token" to token,
//            "nonce" to nonce,
//            "secretKey" to secretKey
//        ))
//
//        return client.call("/users/registerAccount", HttpMethod.Post, body)
//    }
//
//   suspend fun login(uid: String, credential: OAuthCredential) {
//        val hashedAccount = createHashedAccount(uid)
//        val secretKey = createSecretKey()
//        val nonce = createNonce()
//        val idToken = credential.idToken
//
//       try {
//            const res = await fetch()
//       } catch (e: Exception) {
//            Toast.makeText( this, "Error: ${e.message}", Toast.LENGTH_SHORT ).show()
//       }
//   }
}
