package es.iessaladillo.rafamartinez.supermanzanares.utils

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object GoogleIdentityHelper {

    fun buildRequest(webClientId: String): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    fun launchSignIn(
        activity: Activity,
        webClientId: String,
        onResult: (success: Boolean, isNewUser: Boolean, error: String?) -> Unit
    ) {
        val credentialManager = CredentialManager.create(activity)
        val request = buildRequest(webClientId)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result: GetCredentialResponse =
                    credentialManager.getCredential(
                        context = activity,
                        request = request
                    )


                val credential = result.credential
                val googleIdTokenCredential = try {
                    GoogleIdTokenCredential.createFrom(credential.data)
                } catch (e: GoogleIdTokenParsingException) {
                    onResult(false, false, e.message)
                    return@launch
                }

                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                FirebaseAuth.getInstance()
                    .signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { authTask ->
                        if (!authTask.isSuccessful) {
                            onResult(false, false, authTask.exception?.message)
                            return@addOnCompleteListener
                        }

                        val isNew =
                            authTask.result?.additionalUserInfo?.isNewUser ?: false
                        onResult(true, isNew, null)
                    }

            } catch (e: GetCredentialException) {
                onResult(false, false, e.message)
            } catch (e: Exception) {
                onResult(false, false, e.message)
            }
        }
    }
}
