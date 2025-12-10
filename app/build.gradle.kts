plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ramkrsmama"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ramkrsmama"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase Auth + Firestore
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.google.firebase:firebase-firestore:24.7.0")
    implementation("com.google.firebase:firebase-analytics:21.4.0")

    // 🔥 WAJIB karena kamu upload bukti gambar
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-base:18.2.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // Okio
    implementation("com.squareup.okio:okio:3.4.0")

    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")

}
