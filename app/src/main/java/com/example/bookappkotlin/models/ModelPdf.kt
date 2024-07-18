package com.example.bookappkotlin.models

class ModelPdf {
    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var description:String = ""
    var categoryId:String = ""
    var url:String = ""
    var timestamp:Long = 0
    var viewsCount:Long = 0
    var downloadsCount:Long = 0
    var isFavorite = false
    var author = ""
    var startPage = ""
    var endPage = ""
    var voiceUrl = ""

    constructor()

    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCount: Long,
        isFavorite: Boolean,
        author:String,
        startPage:String,
        endPage:String,
        voiceUrl:String

    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
        this.isFavorite = isFavorite
        this.author = author
        this.startPage = startPage
        this.endPage = endPage
        this.voiceUrl = voiceUrl
    }
}