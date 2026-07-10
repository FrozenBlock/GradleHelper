dependencies {
    implementation(libs.forge.srg)
    implementation(libs.forge.gradle)

    implementation(libs.forge.legacy.gradle)
    implementation(libs.neoforge.srg)

    implementation(libs.gson)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.access.AccessWidenerTransformationPlugin"
            description = "converts class tweakers into access transformers and interface injection data"
        }
    }
}
