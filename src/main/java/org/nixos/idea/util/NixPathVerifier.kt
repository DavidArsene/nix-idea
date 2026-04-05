package org.nixos.idea.util

import java.io.File

class NixPathVerifier(private val nixpath: String) {
    // private val searchPaths: MutableMap<String?, File?> = HashMap()

    init {
        this.verify()
    }

    fun verify(): Boolean {
        val sps = nixpath.split(":").dropLastWhile { it.isEmpty() }
        for (i in sps.indices) {
            val ns = sps[i].split("=").dropLastWhile { it.isEmpty() }

            val name = if (ns.size == 2) ns[1] else ""
            val path = if (ns.size == 2) ns[1] else ns[0]

            val file = if (path.endsWith(".nix")) File(path)
            else File(path + File.separator + "default.nix")

            val fret = file.exists() && file.canRead()
            // if (fret) searchPaths[name] = file
            if (!fret) return false
        }
        return true
    }
}
