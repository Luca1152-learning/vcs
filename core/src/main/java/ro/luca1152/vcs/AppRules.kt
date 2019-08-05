package ro.luca1152.vcs

import com.badlogic.gdx.Preferences
import ktx.inject.Context

class AppRules(context: Context) {
    // Injected objects
    private val preferences: Preferences = context.inject()

    var currentBranch
        get() = preferences.getString("currentBranch", "master")
        set(value) {
            preferences.run {
                putString("currentBranch", value)
                flush()
            }
        }

    var latestCommitOnCurrentBranchHashedName
        get() = preferences.getString("latestCommitOn${currentBranch}tBranchHashedName", "")
        set(value) {
            preferences.run {
                putString("latestCommitOn${currentBranch}tBranchHashedName", value)
                flush()
            }
        }
}