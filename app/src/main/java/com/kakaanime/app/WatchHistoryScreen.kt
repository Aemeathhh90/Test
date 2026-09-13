package com.kakaanime.app

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakaanime.app.data.AniListPosterService
import com.kakaanime.app.data.KakaAnimePreferences
import com.kakaanime.app.data.WatchHistoryEntry

private enum class Layout { GRID, LIST }
private enum class AnimeFilter { ALL, IN_PROGRESS, COMPLETED, DROPPED }
private enum class EpisodeFilter { ALL, TODAY, WEEK, MONTH }

@Composable
fun AnimeWatchedScreen(p: KakaAnimePreferences, animeList: List<Anime>, onBack: () -> Unit, onAnimeClick: (Anime) -> Unit) {
    var layout by remember { mutableStateOf(Layout.GRID) }; var filter by remember { mutableStateOf(AnimeFilter.ALL) }
    var query by remember { mutableStateOf("") }; var sort by remember { mutableStateOf("Last Watched") }; var sortOpen by remember { mutableStateOf(false) }
    var posters by remember { mutableStateOf(emptyMap<String,String>()) }
    val legacy = p.loadWatchedEpisodes(); val history = p.loadWatchHistory()
    val records = history.groupBy { it.title }.values.mapNotNull { e -> e.maxByOrNull { it.watchedAt }?.let { h -> animeList.firstOrNull { it.title.equals(h.title,true) }?.let { a -> a to h } } }.ifEmpty { legacy.mapNotNull { (t,e) -> animeList.firstOrNull { it.title.equals(t,true) }?.let { it to WatchHistoryEntry(t,e,null,0,0) } } }
    val shown = records.filter { (a,h) -> (filter==AnimeFilter.ALL || animeStatus(a,h.episode)==filter) && (query.isBlank() || a.title.contains(query,true) || a.genre.contains(query,true)) }.let { if(sort=="Last Watched") it.sortedByDescending { x->x.second.watchedAt } else it.sortedBy { x->x.first.title.lowercase() } }
    LaunchedEffect(records.map { it.first.title }) { posters = AniListPosterService().getPosterUrls(records.map { it.first.title }) }
    WatchShell("Anime Watched", onBack, query, {query=it}, layout, {layout=it}, sort, sortOpen, {sortOpen=true}, {sortOpen=false}, {sort=it;sortOpen=false}, listOf("Last Watched","Title"), shown.size) {
        listOf(AnimeFilter.ALL to "All",AnimeFilter.IN_PROGRESS to "In Progress",AnimeFilter.COMPLETED to "Completed",AnimeFilter.DROPPED to "Dropped").forEach { (v,l) -> FilterChip(selected=filter==v,onClick={filter=v},label={Text(l,fontSize=11.sp)}) }
    } {
        if(layout==Layout.GRID) LazyVerticalGrid(GridCells.Fixed(4),Modifier.fillMaxSize(),contentPadding=PaddingValues(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(shown,key={it.first.title}){(a,h)->AnimeGrid(a,h,posters[a.title]){onAnimeClick(a)}}}
        else LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){items(shown,key={it.first.title}){(a,h)->AnimeList(a,h,posters[a.title]){onAnimeClick(a)}}}
    }
}

@Composable
fun EpisodeWatchedScreen(p: KakaAnimePreferences, animeList: List<Anime>, onBack: () -> Unit, onEpisodeClick: (Anime,Int)->Unit) {
    var layout by remember { mutableStateOf(Layout.GRID) }; var filter by remember { mutableStateOf(EpisodeFilter.ALL) }
    var query by remember { mutableStateOf("") }; var sort by remember { mutableStateOf("Newest") }; var sortOpen by remember { mutableStateOf(false) }
    var posters by remember { mutableStateOf(emptyMap<String,String>()) }; val now=System.currentTimeMillis(); val history=p.loadWatchHistory()
    val shown=history.filter { h -> val age=if(h.watchedAt>0) now-h.watchedAt else Long.MAX_VALUE; val ok=when(filter){EpisodeFilter.ALL->true;EpisodeFilter.TODAY->age<=86400000L;EpisodeFilter.WEEK->age<=604800000L;EpisodeFilter.MONTH->age<=2678400000L}; ok&&(query.isBlank()||h.title.contains(query,true)||h.episodeTitle?.contains(query,true)==true)}.let{if(sort=="Newest")it.sortedByDescending{h->h.watchedAt}else it.sortedBy{h->h.title.lowercase()}}
    LaunchedEffect(shown.map { it.title }){posters=AniListPosterService().getPosterUrls(shown.map{it.title})}
    WatchShell("Episode Watched",onBack,query,{query=it},layout,{layout=it},sort,sortOpen,{sortOpen=true},{sortOpen=false},{sort=it;sortOpen=false},listOf("Newest","Title"),shown.size){listOf(EpisodeFilter.ALL to "All",EpisodeFilter.TODAY to "Today",EpisodeFilter.WEEK to "This Week",EpisodeFilter.MONTH to "This Month").forEach{(v,l)->FilterChip(selected=filter==v,onClick={filter=v},label={Text(l,fontSize=11.sp)})}}{
        if(layout==Layout.GRID) LazyVerticalGrid(GridCells.Fixed(4),Modifier.fillMaxSize(),contentPadding=PaddingValues(12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(shown,key={"${it.title}-${it.episode}"}){h->EpisodeGrid(h,posters[h.title]){animeList.firstOrNull{it.title.equals(h.title,true)}?.let{a->onEpisodeClick(a,h.episode)}}}}
        else LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){items(shown,key={"${it.title}-${it.episode}"}){h->EpisodeList(h,posters[h.title]){animeList.firstOrNull{it.title.equals(h.title,true)}?.let{a->onEpisodeClick(a,h.episode)}}}}
    }
}

@Composable private fun WatchShell(title:String,onBack:()->Unit,query:String,onQuery:(String)->Unit,layout:Layout,onLayout:(Layout)->Unit,sort:String,sortOpen:Boolean,onSort:()->Unit,onDismiss:()->Unit,onSelect:(String)->Unit,sorts:List<String>,total:Int,filters:@Composable RowScope.()->Unit,content:@Composable()->Unit){Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding()){Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Outlined.ArrowBack,"Back")};Text(title,modifier=Modifier.weight(1f),fontSize=22.sp,fontWeight=FontWeight.Bold);IconButton(onClick={}){Icon(Icons.Outlined.MoreVert,"More")}};OutlinedTextField(value=query,onValueChange=onQuery,modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp).height(52.dp),singleLine=true,leadingIcon={Icon(Icons.Outlined.Search,null)},placeholder={Text("Search",fontSize=12.sp)});Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal=12.dp,vertical=8.dp),horizontalArrangement=Arrangement.spacedBy(7.dp),content=filters);Row(Modifier.fillMaxWidth().padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Sort,null,Modifier.size(18.dp));Spacer(Modifier.width(6.dp));Text("Sort: $sort",fontSize=12.sp,modifier=Modifier.clickable(onClick=onSort));Spacer(Modifier.weight(1f));Text("Total: $total",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);IconButton(onClick={onLayout(if(layout==Layout.GRID)Layout.LIST else Layout.GRID)}){Icon(if(layout==Layout.GRID)Icons.Outlined.ViewList else Icons.Outlined.GridView,"Toggle layout")};DropdownMenu(sortOpen,onDismiss){sorts.forEach{v->DropdownMenuItem(text={Text(if(v==sort)"✓ $v" else v)},onClick={onSelect.bind(v)})}}};Box(Modifier.weight(1f)){content()}}}

private fun <T> ((T)->Unit).bind(value:T):()->Unit = { this(value) }

@Composable private fun AnimeGrid(a:Anime,h:WatchHistoryEntry,url:String?,click:()->Unit){val progress=if(a.latestEpisode>0)(h.episode.toFloat()/a.latestEpisode).coerceIn(0f,1f)else 0f;Surface(Modifier.fillMaxWidth().clickable(onClick=click),RoundedCornerShape(11.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.48f)){Column(Modifier.padding(4.dp)){Poster(url,Modifier.fillMaxWidth().aspectRatio(2f/3f));Text(a.title,modifier=Modifier.padding(3.dp),fontSize=10.sp,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis);Text("${h.episode} / ${a.latestEpisode}",modifier=Modifier.padding(horizontal=3.dp),fontSize=9.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Progress(progress)}}}
@Composable private fun AnimeList(a:Anime,h:WatchHistoryEntry,url:String?,click:()->Unit){val progress=if(a.latestEpisode>0)(h.episode.toFloat()/a.latestEpisode).coerceIn(0f,1f)else 0f;Surface(Modifier.fillMaxWidth().clickable(onClick=click),RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.45f)){Row(Modifier.padding(9.dp),verticalAlignment=Alignment.CenterVertically){Poster(url,Modifier.size(82.dp,112.dp));Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(a.title,fontSize=15.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(a.genre,fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis);Text("Episode ${h.episode} / ${a.latestEpisode}",fontSize=12.sp);Progress(progress);Text("${if(animeStatus(a,h.episode)==AnimeFilter.COMPLETED)"Completed" else "In Progress"} • ${ago(h.watchedAt)}",fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Icon(Icons.Outlined.MoreVert,"More")}}}
@Composable private fun EpisodeGrid(h:WatchHistoryEntry,url:String?,click:()->Unit){Surface(Modifier.fillMaxWidth().clickable(onClick=click),RoundedCornerShape(11.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.48f)){Column(Modifier.padding(4.dp)){Box(Modifier.fillMaxWidth().aspectRatio(16f/10f)){Poster(url,Modifier.fillMaxSize());Duration(h.durationMs,Modifier.align(Alignment.BottomEnd).padding(5.dp))};Text(h.title,modifier=Modifier.padding(3.dp),fontSize=10.sp,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis);Text("Episode ${h.episode}",modifier=Modifier.padding(horizontal=3.dp),fontSize=9.sp);Text(h.episodeTitle?:"Judul episode belum tersedia",modifier=Modifier.padding(horizontal=3.dp),fontSize=8.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis);Text(ago(h.watchedAt),modifier=Modifier.padding(3.dp),fontSize=8.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable private fun EpisodeList(h:WatchHistoryEntry,url:String?,click:()->Unit){Surface(Modifier.fillMaxWidth().clickable(onClick=click),RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.45f)){Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(118.dp,76.dp)){Poster(url,Modifier.fillMaxSize());Duration(h.durationMs,Modifier.align(Alignment.BottomEnd).padding(5.dp))};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(h.title,fontSize=14.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text("Episode ${h.episode}",fontSize=12.sp);Text(h.episodeTitle?:"Judul episode belum tersedia",fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis);Text(ago(h.watchedAt),fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}
@Composable private fun Poster(url:String?,m:Modifier){Surface(m,RoundedCornerShape(8.dp),color=MaterialTheme.colorScheme.surfaceVariant){if(url==null)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("K",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)}else AsyncImage(model=url,contentDescription=null,modifier=Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),contentScale=ContentScale.Crop)}}
@Composable private fun Progress(v:Float){Box(Modifier.fillMaxWidth().padding(3.dp).height(4.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.12f))){Box(Modifier.fillMaxWidth(v).fillMaxHeight().background(MaterialTheme.colorScheme.primary))}}
@Composable private fun Duration(ms:Long,m:Modifier){Surface(m,RoundedCornerShape(5.dp),color=MaterialTheme.colorScheme.scrim.copy(alpha=.8f)){Text(if(ms>0)"%d:%02d".format(ms/60000,(ms/1000)%60)else"—:—",modifier=Modifier.padding(5.dp),fontSize=8.sp,color=MaterialTheme.colorScheme.onPrimary)}}
private fun ago(t:Long)=if(t<=0)"Riwayat lama"else DateUtils.getRelativeTimeSpanString(t,System.currentTimeMillis(),DateUtils.MINUTE_IN_MILLIS).toString()
private fun animeStatus(a:Anime,e:Int)=if(a.latestEpisode>0&&e>=a.latestEpisode)AnimeFilter.COMPLETED else AnimeFilter.IN_PROGRESS
