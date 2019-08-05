package ro.luca1152.vcs.objects

class Tree : VcsObject("tree") {
    // String = file name; Blob = hashed content
    var blobs = mutableMapOf<String, Blob>()
}