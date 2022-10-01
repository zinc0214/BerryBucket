dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon
    }
}
rootProject.name = "BerryBucket"
include(":app")
include(":domain")
include(":data")
include(":common")
include(":datastore")
include(":ui-common")
include(":ui-my")
