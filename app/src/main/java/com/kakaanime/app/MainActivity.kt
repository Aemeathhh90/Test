package com.kakaanime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.monetization.AdMobRewardedAdGateway
import com.kakaanime.app.monetization.DiamondRules
import com.kakaanime.app.monetization.MonetizationState
import com.kakaanime.app.player.VideoPlayerScreen
import com.kakaanime.app.premium.PremiumScreen
import com.kakaanime.app.provider.ProviderEpisode
import com.kakaanime.app.provider.ProviderPlaybackResolver
import com.kakaanime.app.ui.theme.KakaAccent
import com.kakaanime.app.ui.theme.KakaAnimeTheme
import com.kakaanime.app.ui.theme.KakaThemeState

data class Anime(val title:String,val latestEpisode:Int,val genre:String,val description:String,val studio:String,val season:String,val year:String,val type:String,val status:String,val rating:String,val introStart:Long=0L,val introEnd:Long=0L,val outroStart:Long=0L,val outroEnd:Long=0L)
val localAnime=listOf(Anime("One Piece",1140,"Action, Adventure, Fantasy","Monkey D. Luffy dan kru Topi Jerami melanjutkan perjalanan mereka menuju One Piece.","Toei Animation","Ongoing","1999","TV","Ongoing","9.0",90L,180L,1380L,1440L),Anime("Solo Leveling",25,"Action, Fantasy","Sung Jin-woo berkembang dari hunter terlemah menjadi hunter yang sangat kuat.","A-1 Pictures","Season 2","2025","TV","Finished","8.8",75L,165L,1380L,1440L))
private enum class AnimeScreen{HOME,DETAIL,PLAYER,PREMIUM}
enum class BottomTab{HOME,CALENDAR,SOCIAL,LIBRARY,PROFILE}
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{KakaAnimeApp()}}}
@Composable fun KakaAnimeApp(){
 val context=LocalContext.current; val preferences=remember(context){KakaAnimePreferences(context)}; val themeState=remember(preferences){KakaThemeState(accent=runCatching{KakaAccent.valueOf(preferences.loadAccentName())}.getOrDefault(KakaAccent.Blue),darkMode=preferences.loadDarkMode())}; val rewardedAds=remember(context){AdMobRewardedAdGateway(context)}
 var selectedAnime by remember{mutableStateOf<Anime?>(null)}; var selectedEpisode by remember{mutableStateOf<Int?>(null)}; var selectedTab by remember{mutableStateOf(BottomTab.HOME)}; var showPremium by remember{mutableStateOf(false)}; var resolvedStreamUrl by remember{mutableStateOf<String?>(null)}; var streamLoading by remember{mutableStateOf(false)}; var streamRetry by remember{mutableIntStateOf(0)}; var providerEpisodes by remember{mutableStateOf<List<ProviderEpisode>>(emptyList())}; var episodeListLoading by remember{mutableStateOf(false)}; var homeRefreshKey by remember{mutableIntStateOf(0)}
 var favoriteTitles by remember(preferences){mutableStateOf(preferences.loadFavoriteTitles())}; var watchedEpisodes by remember(preferences){mutableStateOf(preferences.loadWatchedEpisodes())}; var watchedEpisodeNumbers by remember(preferences){mutableStateOf(preferences.loadWatchHistory().groupBy { it.title }.mapValues { (_, entries) -> entries.map { it.episode }.toSet() })}; var monetizationState by remember(preferences){mutableStateOf(MonetizationState(preferences.loadDiamonds(),preferences.loadPremium()))}
 fun recordWatched(anime:Anime,episode:Int){val providerEpisode=providerEpisodes.firstOrNull{it.number==episode}; preferences.recordWatchedEpisode(anime.title,episode,providerEpisode?.title,providerEpisode?.thumbnailUrl); watchedEpisodes=watchedEpisodes+(anime.title to episode); preferences.saveWatchedEpisodes(watchedEpisodes); watchedEpisodeNumbers=watchedEpisodeNumbers.toMutableMap().apply{put(anime.title,(get(anime.title).orEmpty()+episode).toSet())}; homeRefreshKey++}
 fun openEpisode(anime:Anime,episode:Int){ fun grantAndOpen(){selectedAnime=anime;selectedEpisode=episode}; if(monetizationState.isPremium){grantAndOpen();return}; val consumed=DiamondRules.consumeForEpisode(monetizationState); if(consumed!=null){monetizationState=consumed;preferences.saveDiamonds(consumed.diamonds);grantAndOpen();return}; rewardedAds.show(onReward={diamonds->val rewardedState=monetizationState.copy(diamonds=monetizationState.diamonds+diamonds);monetizationState=rewardedState;val afterReward=DiamondRules.consumeForEpisode(rewardedState);if(afterReward!=null){monetizationState=afterReward;preferences.saveDiamonds(afterReward.diamonds);grantAndOpen()}},onUnavailable={}) }
 LaunchedEffect(selectedAnime?.title,selectedEpisode,monetizationState.isPremium,streamRetry){val anime=selectedAnime;val episode=selectedEpisode;if(anime==null||episode==null){resolvedStreamUrl=null;streamLoading=false;return@LaunchedEffect};resolvedStreamUrl=null;streamLoading=true;resolvedStreamUrl=runCatching{ProviderPlaybackResolver.resolve(anime.title,episode,monetizationState.isPremium)?.url}.getOrNull();streamLoading=false;if(resolvedStreamUrl!=null){recordWatched(anime,episode)}}
 LaunchedEffect(selectedAnime?.title){val anime=selectedAnime;if(anime==null){providerEpisodes=emptyList();episodeListLoading=false;return@LaunchedEffect};episodeListLoading=true;providerEpisodes=runCatching{ProviderPlaybackResolver.episodes(anime.title)}.getOrDefault(emptyList());episodeListLoading=false;while(true){kotlinx.coroutines.delay(10*60*1000L);if(selectedAnime?.title!=anime.title)return@LaunchedEffect;val refreshed=runCatching{ProviderPlaybackResolver.episodes(anime.title)}.getOrDefault(emptyList());if(refreshed.isNotEmpty())providerEpisodes=refreshed}}
 val screen=when{showPremium->AnimeScreen.PREMIUM;selectedAnime!=null&&selectedEpisode!=null->AnimeScreen.PLAYER;selectedAnime!=null->AnimeScreen.DETAIL;else->AnimeScreen.HOME}
 KakaAnimeTheme(themeState=themeState){AnimatedContent(targetState=screen,transitionSpec={(fadeIn()+slideInHorizontally{it/8}) togetherWith (fadeOut()+slideOutHorizontally{-it/10})},label="screen_transition"){target->when(target){
  AnimeScreen.HOME->Box(Modifier.fillMaxSize()){AnimatedContent(targetState=selectedTab,transitionSpec={fadeIn() togetherWith fadeOut()},label="tab_transition",modifier=Modifier.fillMaxSize().padding(bottom=84.dp)){tab->when(tab){BottomTab.HOME->ReDantotsuHomeScreen(localAnime,{selectedAnime=it},{anime,episode->openEpisode(anime,episode)},homeRefreshKey);BottomTab.CALENDAR->CalendarScreen(localAnime,{selectedAnime=it},favoriteTitles);BottomTab.SOCIAL->SocialScreen();BottomTab.LIBRARY->LibraryScreen("Library",localAnime,{selectedAnime=it},"Belum ada data library");BottomTab.PROFILE->ProfileScreen(themeState,monetizationState){showPremium=true}}};KakaBottomNavigation(selectedTab){selectedTab=it as BottomTab}}
  AnimeScreen.DETAIL->AnimeDetailScreen(selectedAnime!!,selectedAnime!!.title in favoriteTitles,watchedEpisodes[selectedAnime!!.title],watchedEpisodeNumbers[selectedAnime!!.title].orEmpty(),providerEpisodes,episodeListLoading,{selectedAnime=null;selectedEpisode=null},{favoriteTitles=if(selectedAnime!!.title in favoriteTitles)favoriteTitles-selectedAnime!!.title else favoriteTitles+selectedAnime!!.title;preferences.saveFavoriteTitles(favoriteTitles)},{openEpisode(selectedAnime!!,it)})
  AnimeScreen.PLAYER->{val anime=selectedAnime!!;val episode=selectedEpisode!!;val previousProviderEpisode=providerEpisodes.map{it.number}.filter{it<episode}.maxOrNull()?:((episode-1).coerceAtLeast(1));val nextProviderEpisode=providerEpisodes.map{it.number}.filter{it>episode}.minOrNull()?:episode+1;val latestEpisode=providerEpisodes.maxOfOrNull{it.number} ?: anime.latestEpisode;if(streamLoading)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){CircularProgressIndicator();Spacer(Modifier.height(12.dp));Text("Mencari stream Episode $episode...",color=MaterialTheme.colorScheme.onBackground)}}else if(resolvedStreamUrl!=null)VideoPlayerScreen(videoUrl=resolvedStreamUrl!!,title=anime.title,episodeNumber=episode,description=anime.description,introStart=anime.introStart,introEnd=anime.introEnd,outroStart=anime.outroStart,outroEnd=anime.outroEnd,isPremium=monetizationState.isPremium,episodes=providerEpisodes,watchedEpisodes=watchedEpisodeNumbers[anime.title].orEmpty(),modifier=Modifier.fillMaxSize(),onBack={selectedEpisode=null},onPreviousEpisode={if(previousProviderEpisode<episode)openEpisode(anime,previousProviderEpisode)},onNextEpisode={if(nextProviderEpisode>episode&&nextProviderEpisode<=latestEpisode)openEpisode(anime,nextProviderEpisode)},onEpisodeClick={target->if(target!=episode)openEpisode(anime,target)})else Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("Stream tidak ditemukan",fontSize=20.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text("Provider belum menemukan sumber untuk ${anime.title} Episode $episode.",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(16.dp));Button(onClick={streamRetry++}){Text("Coba lagi")}}}}
  AnimeScreen.PREMIUM->PremiumScreen(state=monetizationState,onBack={showPremium=false},onSubscribe={monetizationState=monetizationState.copy(isPremium=true);preferences.savePremium(true)})
 }}}
}