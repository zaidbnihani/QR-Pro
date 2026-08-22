package com.example.updater

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("body")
    val body: String?,
    
    @SerializedName("html_url")
    val htmlUrl: String,
    
    @SerializedName("assets")
    val assets: List<ReleaseAsset>?
)

data class ReleaseAsset(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String
)

data class UpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String
)
