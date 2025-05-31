plugins {
	id("com.android.application")
	kotlin("android")
	kotlin("plugin.compose")
}

android {
	namespace = "org.nift4.exoprobe"
	compileSdk = 35

	defaultConfig {
		applicationId = "org.nift4.exoprobe"
		minSdk = 24
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {
	implementation("androidx.activity:activity-compose:1.10.1")
	val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
	implementation(composeBom)
	implementation("androidx.compose.material3:material3")
	implementation("androidx.media3:media3-exoplayer:1.8.0-alpha01")
}