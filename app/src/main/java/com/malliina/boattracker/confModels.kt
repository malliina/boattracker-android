package com.malliina.boattracker

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
data class Foo(val bar: String)

@Parcelize
@JsonClass(generateAdapter = true)
data class ProfileInfo(val email: Email,
                       val token: IdToken,
                       val trackName: TrackName?): Parcelable {
    companion object {
        const val key = "profile"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class Link(val text: String, val url: FullUrl): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class AppAttribution(val title: String,
                          val text: String?,
                          val links: List<Link>): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class AttributionInfo(val title: String,
                           val attributions: List<AppAttribution>): Parcelable {
    companion object {
        const val key = "attributions"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class FairwayStateLang(val confirmed: String,
                            val aihio: String,
                            val mayChange: String,
                            val changeAihio: String,
                            val mayBeRemoved: String,
                            val removed: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ZonesLang(val area: String, val fairway: String, val areaAndFairway: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class FairwayTypesLang(val navigation: String,
                            val anchoring: String,
                            val meetup: String,
                            val harborPool: String,
                            val turn: String,
                            val channel: String,
                            val coastTraffic: String,
                            val core: String,
                            val special: String,
                            val lock: String,
                            val confirmedExtra: String,
                            val helcom: String,
                            val pilot: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class FairwayLang(val fairwayType: String,
                       val fairwayDepth: String,
                       val harrowDepth: String,
                       val minDepth: String,
                       val maxDepth: String,
                       val state: String,
                       val states: FairwayStateLang,
                       val zones: ZonesLang,
                       val types: FairwayTypesLang): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class AisLang(val draft: String, val destination: String, val shipType: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class TrackLang(val track: String,
                     val boats: String,
                     val tracks: String,
                     val speed: String,
                     val water: String,
                     val depth: String,
                     val top: String,
                     val duration: String,
                     val distance: String,
                     val topSpeed: String,
                     val avgSpeed: String,
                     val waterTemp: String,
                     val date: String,
                     val trackHistory: String,
                     val graph: String): Parcelable {
    companion object {
        val key = "track.lang"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class MarkTypeLang(val lateral: String, val cardinal: String, val unknown: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class AidTypeLang(val unknown: String,
                       val lighthouse: String,
                       val sectorLight: String,
                       val leadingMark: String,
                       val directionalLight: String,
                       val minorLight: String,
                       val otherMark: String,
                       val edgeMark: String,
                       val radarTarget: String,
                       val buoy: String,
                       val beacon: String,
                       val signatureLighthouse: String,
                       val cairn: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class NavMarkLang(val left: String,
                       val right: String,
                       val north: String,
                       val south: String,
                       val west: String,
                       val east: String,
                       val rock: String,
                       val safeWaters: String,
                       val special: String,
                       val notApplicable: String,
                       val unknown: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ConstructionLang(val buoyBeacon: String,
                            val iceBuoy: String,
                            val beaconBuoy: String,
                            val superBeacon: String,
                            val exteriorLight: String,
                            val dayBoard: String,
                            val helicopterPlatform: String,
                            val radioMast: String,
                            val waterTower: String,
                            val smokePipe: String,
                            val radarTower: String,
                            val churchTower: String,
                            val superBuoy: String,
                            val edgeCairn: String,
                            val compassCheck: String,
                            val borderMark: String,
                            val borderLineMark: String,
                            val channelEdgeLight: String,
                            val tower: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class MarkLang(val markType: String,
                    val aidType: String,
                    val navigation: String,
                    val construction: String,
                    val influence: String,
                    val location: String,
                    val owner: String,
                    val types: MarkTypeLang,
                    val navTypes: NavMarkLang,
                    val structures: ConstructionLang,
                    val aidTypes: AidTypeLang): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class SpecialWords(val transportAgency: String,
                        val defenceForces: String,
                        val portOfHelsinki: String,
                        val cityOfHelsinki: String,
                        val cityOfEspoo: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ProfileLang(val username: String,
                       val signedInAs: String,
                       val logout: String,
                       val chooseIdentityProvider: String,
                       val language: String,
                       val finnish: String,
                       val swedish: String,
                       val english: String): Parcelable {
    companion object {
        val key = "lang.profile"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class MessagesLang(val loading: String,
                        val failedToLoadProfile: String,
                        val noSavedTracks: String,
                        val notAvailable: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class FormatsLang(val date: String,
                       val time: String,
                       val timeShort: String,
                       val dateTime: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class SettingsLang(val welcome: String,
                        val welcomeText: String,
                        val laterText: String,
                        val notifications: String,
                        val notificationsText: String,
                        val howItWorks: String,
                        val signIn: String,
                        val signInText: String,
                        val boat: String,
                        val token: String,
                        val tokenText: String,
                        val tokenTextLong: String,
                        val rename: String,
                        val renameBoat: String,
                        val newName: String,
                        val edit: String,
                        val cancel: String,
                        val done: String,
                        val back: String,
                        val noTracksHelp: String,
                        val formats: FormatsLang): Parcelable {
    companion object {
        val key = "lang.settings"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class LimitTypes(val speedLimit: String,
                      val noWaves: String,
                      val noWindSurfing: String,
                      val noJetSkiing: String,
                      val noMotorPower: String,
                      val noAnchoring: String,
                      val noStopping: String,
                      val noAttachment: String,
                      val noOvertaking: String,
                      val noRendezVous: String,
                      val speedRecommendation: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class LimitLang(val limit: String,
                     val magnitude: String,
                     val length: String,
                     val location: String,
                     val fairwayName: String,
                     val responsible: String,
                     val types: LimitTypes): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class SpecialCategory(val fishing: String,
                           val tug: String,
                           val dredger: String,
                           val diveVessel: String,
                           val militaryOps: String,
                           val sailing: String,
                           val pleasureCraft: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ShipTypesLang(val wingInGround: String,
                         val highSpeedCraft: String,
                         val pilotVessel: String,
                         val searchAndRescue: String,
                         val searchAndRescueAircraft: String,
                         val portTender: String,
                         val antiPollution: String,
                         val lawEnforce: String,
                         val localVessel: String,
                         val medicalTransport: String,
                         val specialCraft: String,
                         val passenger: String,
                         val cargo: String,
                         val tanker: String,
                         val other: String,
                         val unknown: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class AisLayers(val vessel: String, val trail: String, val vesselIcon: String): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Layers(val marks: List<String>,
                  val fairways: List<String>,
                  val fairwayAreas: List<String>,
                  val limits: List<String>,
                  val ais: AisLayers): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Lang(val language: Language,
                val appName: String,
                val map: String,
                val name: String,
                val qualityClass: String,
                val time: String,
                val comparisonLevel: String,
                val specialWords: SpecialWords,
                val fairway: FairwayLang,
                val track: TrackLang,
                val mark: MarkLang,
                val ais: AisLang,
                val shipTypes: ShipTypesLang,
                val attributions: AttributionInfo,
                val profile: ProfileLang,
                val messages: MessagesLang,
                val settings: SettingsLang,
                val limits: LimitLang): Parcelable {
    companion object {
        val key = "lang"
    }
}

@Parcelize
@JsonClass(generateAdapter = true)
data class Languages(val finnish: Lang, val swedish: Lang, val english: Lang): Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ClientConf(val languages: Languages, val layers: Layers): Parcelable
