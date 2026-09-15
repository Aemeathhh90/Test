package com.kakaanime.app.ui.home

data class HomeAnimeUi(val id:String,val title:String,val latestEpisode:Int=0,val rating:String="-",val genre:String="",val posterUrl:String?=null,val status:String="Ongoing",val isNew:Boolean=false)
data class ContinueWatchingUi(val anime:HomeAnimeUi,val episode:Int,val episodeTitle:String?=null,val thumbnailUrl:String?=null,val progressPercent:Int=0)
data class HomeUiState(val anime:List<HomeAnimeUi>=emptyList(),val continueWatching:List<ContinueWatchingUi>=emptyList(),val diamonds:Int=0,val isPremium:Boolean=false,val username:String="Akun Saya",val searchQuery:String="",val selectedFilter:HomeFilter=HomeFilter.ALL)
enum class HomeFilter(val label:String){ALL("All"),ONGOING("Ongoing"),FINISHED("Finished")}
