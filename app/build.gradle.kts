plugins { id("com.android.application") }

val officialCertificate = "13e311e9a9583813c876a3cae3496f73e81177e70840edc590c01a10d7432cd7"
val releaseKey = providers.environmentVariable("JINGYU_KEYSTORE").orNull
val certificate = providers.environmentVariable("JINGYU_SIGNING_SHA256")
    .orElse(providers.gradleProperty("jingyuSigningSha256"))
    .getOrElse(officialCertificate).lowercase()
require(certificate.matches(Regex("[0-9a-f]{64}"))) { "Signing certificate must be a SHA-256 hex digest" }
if (releaseKey != null) {
    require(providers.environmentVariable("JINGYU_SIGNING_SHA256").isPresent ||
        providers.gradleProperty("jingyuSigningSha256").isPresent) {
        "Set JINGYU_SIGNING_SHA256 to the certificate of your release keystore"
    }
}

android {
    namespace = "cn.jingyu.deepseek"
    compileSdk = 35
    defaultConfig {
        applicationId = "cn.jingyu.deepseek"
        minSdk = 26
        targetSdk = 35
        versionCode = providers.gradleProperty("jingyuVersionCode").get().toInt()
        versionName = providers.gradleProperty("jingyuVersionName").get()
        buildConfigField("String", "SIGNING_CERT_SHA256", "\"$certificate\"")
    }
    buildFeatures { buildConfig = true }
    if (releaseKey != null) {
        signingConfigs.create("ownerRelease") {
            storeFile = file(releaseKey)
            storePassword = providers.environmentVariable("JINGYU_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("JINGYU_KEY_ALIAS").getOrElse("jingyu")
            keyPassword = providers.environmentVariable("JINGYU_KEY_PASSWORD").get()
        }
    }
    buildTypes {
        debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-dev" }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            if (releaseKey != null) signingConfig = signingConfigs.getByName("ownerRelease")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets.getByName("test") {
        java.srcDirs("src/test/java", "../test")
        java.exclude("android/**")
    }
    testOptions.unitTests.all {
        it.systemProperty("jingyu.repoRoot", rootProject.projectDir.absolutePath)
    }
    packaging.resources.excludes += setOf("META-INF/LICENSE*", "META-INF/NOTICE*", "META-INF/DEPENDENCIES")
    androidResources { noCompress += "ttf" }
    lint { abortOnError = true }
}

dependencies {
    implementation(files(rootProject.file("vendor/annotation-1.1.0.jar")))
    implementation(files(rootProject.file("vendor/commonmark-0.13.0.jar")))
    implementation(files(rootProject.file("vendor/commonmark-ext-gfm-tables-0.13.0.jar")))
    implementation(fileTree(rootProject.file("vendor")) { include("*.aar") })
    testImplementation("junit:junit:4.13.2")
}
