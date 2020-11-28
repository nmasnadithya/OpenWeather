package xyz.nmasnadithya.openweather.koin

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.core.scope.Scope
import org.koin.dsl.module

val firebaseModule = module {

  single { getFirebaseAnalytics() }

  single { getFirebaseAuth() }
}

private fun Scope.getFirebaseAnalytics(): FirebaseAnalytics {
  return Firebase.analytics
}

private fun Scope.getFirebaseAuth(): FirebaseAuth {
  return Firebase.auth
}


