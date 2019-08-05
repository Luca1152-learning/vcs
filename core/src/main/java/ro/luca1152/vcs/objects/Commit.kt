package ro.luca1152.vcs.objects

class Commit : VcsObject("commit") {
    var tree: Tree? = null
    var message = ""
}