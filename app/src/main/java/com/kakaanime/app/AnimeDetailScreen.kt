package com.kakaanime.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakaanime.app.provider.ProviderEpisode

@Composable
fun AnimeDetailScreen(anime: Anime,isFavorite:Boolean,watchedEpisode:Int?,episodes:List<ProviderEpisode>,loading:Boolean,onBack:()->Unit,onToggleFavorite:()->Unit,onEpisodeClick:(Int)->Unit){
 LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),contentPadding=PaddingValues(bottom=24.dp)){
  item{Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Outlined.ArrowBack,"Back")};Text(anime.title,Modifier.weight(1f),fontWeight=FontWeight.Bold);IconButton(onClick=onToggleFavorite){Icon(if(isFavorite)Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,"Favorite")}}}
  item{Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(anime.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("★ ${anime.rating}  •  ${anime.year}  •  ${anime.status}",color=MaterialTheme.colorScheme.onSurfaceVariant);Text(anime.genre,fontSize=12.sp);Text(anime.description,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(if(watchedEpisode!=null)"Terakhir ditonton: Episode $watchedEpisode" else "Belum ada episode ditonton",fontSize=12.sp,color=MaterialTheme.colorScheme.primary)}}
  item{Text("Episodes",style=MaterialTheme.typography.titleLarge,modifier=Modifier.padding(horizontal=18.dp,vertical=8.dp))}
  if(loading)item{Box(Modifier.fillMaxWidth().padding(30.dp),contentAlignment=Alignment.Center){CircularProgressIndicator()}}
  else if(episodes.isEmpty())item{Text("Episode belum tersedia dari provider.",modifier=Modifier.padding(18.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)}
  else items(episodes,key={it.number}){ep->val watched=watchedEpisode!=null&&ep.number<=watchedEpisode;Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=4.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.45f)).clickable{onEpisodeClick(ep.number)}.padding(13.dp),verticalAlignment=Alignment.CenterVertically){if(!watched)Icon(Icons.Outlined.Lock,"Belum ditonton",tint=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.size(18.dp))else Spacer(Modifier.size(18.dp));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("Episode ${ep.number}",fontWeight=FontWeight.SemiBold);Text(ep.title?:"Episode ${ep.number}",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)};if(ep.isNew)Text("NEW",fontSize=9.sp,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)}}
 }
}
