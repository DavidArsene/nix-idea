package org.nixos.idea.settings

/**
 * Constants to be used for [Storage.value].
 */
object NixStoragePaths {
    /**
     * Storage location of non-system dependent settings for this plugin.
     * This constant must be used with [RoamingType.DEFAULT].
     */
    const val DEFAULT: String = "nix-idea.xml"

    /**
     * Storage location of settings for external tools.
     * The settings in the file are considered system dependent.
     * This constant must be used with [RoamingType.LOCAL].
     */
    const val TOOLS: String = "nix-idea-tools.xml"
}
