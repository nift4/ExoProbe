// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	val agpVersion = "8.10.1"
	id("com.android.application") version agpVersion apply false
	val kotlinVersion = "2.0.21"
	kotlin("android") version kotlinVersion apply false
	kotlin("plugin.compose") version kotlinVersion apply false
}