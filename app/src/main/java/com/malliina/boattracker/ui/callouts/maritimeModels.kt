package com.malliina.boattracker.ui.callouts

import com.malliina.boattracker.*
import com.malliina.boattracker.Json.Companion.fail
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import timber.log.Timber
import java.lang.NumberFormatException

class MarineAdapter {
    @FromJson
    fun navMark(d: Double): NavMark {
        return NavMark.navMark(d)
    }

    @FromJson
    fun aidType(d: Double): AidType {
        return AidType.aidType(d)
    }

    @FromJson
    fun fairwayAreaType(d: Double): FairwayType? {
        return FairwayType.fairwayType(d.toInt())
    }

    @FromJson
    fun markType(d: Double): MarkType {
        return MarkType.markType(d.toInt())
    }

    @FromJson
    fun construction(d: Double): ConstructionInfo? {
        return try {
            ConstructionInfo.build(d)
        } catch (e: Exception) {
            Timber.i(e.message ?: "JSON error.")
            null
        }
    }
}

data class NonEmptyString(val value: String)

enum class NavMark {
    Unknown,
    Left,
    Right,
    North,
    South,
    West,
    East,
    Rock,
    SafeWaters,
    Special,
    NotApplicable;

    fun translate(lang: NavMarkLang): String {
        return when (this) {
            Left -> lang.left
            Right -> lang.right
            North -> lang.north
            South -> lang.south
            West -> lang.unknown
            East -> lang.east
            Rock -> lang.rock
            SafeWaters -> lang.safeWaters
            Special -> lang.special
            NotApplicable -> lang.notApplicable
            Unknown -> lang.unknown
        }
    }

    companion object {
        fun navMark(d: Double): NavMark {
            return when (d.toInt()) {
                0 -> Unknown
                1 -> Left
                2 -> Right
                3 -> North
                4 -> South
                5 -> West
                6 -> East
                7 -> Rock
                8 -> SafeWaters
                9 -> Special
                99 -> NotApplicable
                else -> Unknown
            }
        }
    }
}

enum class AidType {
    Unknown,
    Lighthouse,
    SectorLight,
    LeadingMark,
    DirectionalLight,
    MinorLight,
    OtherMark,
    EdgeMark,
    RadarTarget,
    Buoy,
    Beacon,
    SignatureLighthouse,
    Cairn;

    fun translate(lang: AidTypeLang): String {
        return when (this) {
            Lighthouse -> lang.lighthouse
            SectorLight -> lang.sectorLight
            LeadingMark -> lang.leadingMark
            DirectionalLight -> lang.directionalLight
            MinorLight -> lang.minorLight
            OtherMark -> lang.otherMark
            EdgeMark -> lang.edgeMark
            RadarTarget -> lang.radarTarget
            Buoy -> lang.buoy
            Beacon -> lang.beacon
            SignatureLighthouse -> lang.signatureLighthouse
            Cairn -> lang.cairn
            Unknown -> lang.unknown
        }
    }

    companion object {
        fun aidType(d: Double): AidType {
            return when (d.toInt()) {
                0 -> Unknown
                1 -> Lighthouse
                2 -> SectorLight
                3 -> LeadingMark
                4 -> DirectionalLight
                5 -> MinorLight
                6 -> OtherMark
                7 -> EdgeMark
                8 -> RadarTarget
                9 -> Buoy
                10 -> Beacon
                11 -> SignatureLighthouse
                13 -> Cairn
                else -> Unknown
            }
        }
    }
}

enum class FairwayType {
    Navigaton, Anchoring, Meetup, HarborPool, Turn, Channel, CoastTraffic, Core, Special, Lock, ConfirmedExtra, Helcom, Pilot;

    fun translate(lang: FairwayTypesLang): String {
        return when (this) {
            Navigaton -> lang.navigation
            Anchoring -> lang.anchoring
            Meetup -> lang.meetup
            HarborPool -> lang.harborPool
            Turn -> lang.turn
            Channel -> lang.channel
            CoastTraffic -> lang.coastTraffic
            Core -> lang.core
            Special -> lang.special
            Lock -> lang.lock
            ConfirmedExtra -> lang.confirmedExtra
            Helcom -> lang.helcom
            Pilot -> lang.pilot
        }
    }

    companion object {
        fun fairwayType(i: Int): FairwayType? {
            return when (i) {
                1 -> Navigaton
                2 -> Anchoring
                3 -> Meetup
                4 -> HarborPool
                5 -> Turn
                6 -> Channel
                7 -> CoastTraffic
                8 -> Core
                9 -> Special
                10 -> Lock
                11 -> ConfirmedExtra
                12 -> Helcom
                13 -> Pilot
                else -> fail("Invalid fairway type: '$i'.")
            }
        }
    }
}

enum class MarkType {
    Unknown, Lateral, Cardinal;

    fun translate(lang: MarkTypeLang): String {
        return when (this) {
            Unknown -> lang.unknown
            Lateral -> lang.lateral
            Cardinal -> lang.cardinal
        }
    }

    companion object {
        fun markType(i: Int): MarkType {
            return when (i) {
                0 -> Unknown
                1 -> Lateral
                2 -> Cardinal
                else -> fail("Invalid mark type: '$i'.")
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class MarineSymbolRaw(
    val NIMIR: NonEmptyString?,
    val NIMIS: NonEmptyString?,
    val SIJAINTIS: NonEmptyString?,
    val SIJAINTIR: NonEmptyString?,
    val TY_JNR: AidType,
    val RAKT_TYYP: ConstructionInfo?,
    val NAVL_TYYP: NavMark,
    val OMISTAJA: String
) {
    fun toSymbol() =
        MarineSymbol(NIMIS, NIMIR, SIJAINTIS, SIJAINTIR, TY_JNR, RAKT_TYYP, NAVL_TYYP, OMISTAJA)
}

data class MarineSymbol(
    val nameFi: NonEmptyString?,
    val nameSe: NonEmptyString?,
    val locationFi: NonEmptyString?,
    val locationSe: NonEmptyString?,
    val aidType: AidType,
    val construction: ConstructionInfo?,
    val navMark: NavMark,
    val owner: String
) {
    fun name(lang: Language) =
        if (lang == Language.Finnish) (nameFi ?: nameSe)
        else (nameSe ?: nameFi)

    fun location(lang: Language): NonEmptyString? =
        if (lang == Language.Finnish) (locationFi ?: locationSe)
        else (locationSe ?: locationFi)
}

enum class LimitType {
    SpeedLimit, NoWaves, NoWindSurfing, NoJetSkiing, NoMotorPower, NoAnchoring, NoStopping, NoAttachment,
    NoOvertaking, NoRendezVous, SpeedRecommendation;

    fun translate(lang: LimitTypes): String {
        return when (this) {
            SpeedLimit -> lang.speedLimit
            NoWaves -> lang.noWaves
            NoWindSurfing -> lang.noWindSurfing
            NoJetSkiing -> lang.noJetSkiing
            NoMotorPower -> lang.noMotorPower
            NoAnchoring -> lang.noAnchoring
            NoStopping -> lang.noStopping
            NoAttachment -> lang.noAttachment
            NoOvertaking -> lang.noOvertaking
            NoRendezVous -> lang.noRendezVous
            SpeedRecommendation -> lang.speedRecommendation
        }
    }

    companion object {
        fun limitType(s: String): LimitType {
            return when (s) {
                "01" -> SpeedLimit
                "02" -> NoWaves
                "03" -> NoWindSurfing
                "04" -> NoJetSkiing
                "05" -> NoMotorPower
                "06" -> NoAnchoring
                "07" -> NoStopping
                "08" -> NoAttachment
                "09" -> NoOvertaking
                "10" -> NoRendezVous
                "11" -> SpeedRecommendation
                else -> fail("Unknown limit type: '$s'.")
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class LimitAreaJson(
    val RAJOITUSTY: String,
    val SUURUUS: String,
    val PITUUS: String,
    val MERK_VAST: NonEmptyString?,
    val NIMI_SIJAI: NonEmptyString?,
    val VAY_NIMISU: String,
    val IRROTUS_PV: String
) {
    fun toDouble(d: String): Double = try {
        d.toDouble()
    } catch(e: NumberFormatException) {
        fail("Expected double, got: '$d'.")
    }

    fun toLimitArea() = LimitArea(
        RAJOITUSTY.split(", ").map { LimitType.limitType(it) },
        toDouble(SUURUUS).kmh(),
        toDouble(PITUUS).meters(),
        MERK_VAST,
        NIMI_SIJAI,
        VAY_NIMISU,
        IRROTUS_PV
    )
}

data class LimitArea(
    val types: List<LimitType>,
    val limit: Speed?,
    val length: Distance?,
    val responsible: NonEmptyString?,
    val location: NonEmptyString?,
    val fairwayName: String,
    val publishDate: String
)


@JsonClass(generateAdapter = true)
data class FairwayAreaJson(
    val OMISTAJA: NonEmptyString,
    val VAYALUE_TY: FairwayType,
    val VAYALUE_SY: Distance,
    val HARAUS_SYV: Distance,
    val MERK_LAJI: MarkType?
) {
    fun toFairway() = FairwayArea(OMISTAJA, VAYALUE_TY, VAYALUE_SY, HARAUS_SYV, MERK_LAJI)
}

data class FairwayArea(
    val owner: NonEmptyString,
    val fairwayType: FairwayType,
    val fairwayDepth: Distance,
    val harrowDepth: Distance,
    val markType: MarkType?
)

enum class ConstructionInfo {
    BuoyBeacon,
    IceBuoy,
    BeaconBuoy,
    SuperBeacon,
    ExteriorLight,
    DayBoard,
    HelicopterPlatform,
    RadioMast,
    WaterTower,
    SmokePipe,
    RadarTower,
    ChurchTower,
    SuperBuoy,
    EdgeCairn,
    CompassCheck,
    BorderMark,
    BorderLineMark,
    ChannelEdgeLight,
    Tower;

    fun translate(lang: ConstructionLang): String {
        return when (this) {
            BuoyBeacon -> lang.buoyBeacon
            IceBuoy -> lang.iceBuoy
            BeaconBuoy -> lang.beaconBuoy
            SuperBeacon -> lang.superBeacon
            ExteriorLight -> lang.exteriorLight
            DayBoard -> lang.dayBoard
            HelicopterPlatform -> lang.helicopterPlatform
            RadioMast -> lang.radioMast
            WaterTower -> lang.waterTower
            SmokePipe -> lang.smokePipe
            RadarTower -> lang.radarTower
            ChurchTower -> lang.churchTower
            SuperBuoy -> lang.superBuoy
            EdgeCairn -> lang.edgeCairn
            CompassCheck -> lang.compassCheck
            BorderMark -> lang.borderMark
            BorderLineMark -> lang.borderLineMark
            ChannelEdgeLight -> lang.channelEdgeLight
            Tower -> lang.tower
        }
    }

    companion object {
        fun build(d: Double): ConstructionInfo {
            return when (d.toInt()) {
                1 -> BuoyBeacon
                2 -> IceBuoy
                4 -> BeaconBuoy
                5 -> SuperBeacon
                6 -> ExteriorLight
                7 -> DayBoard
                8 -> HelicopterPlatform
                9 -> RadioMast
                10 -> WaterTower
                11 -> SmokePipe
                12 -> RadarTower
                13 -> ChurchTower
                14 -> SuperBuoy
                15 -> EdgeCairn
                16 -> CompassCheck
                17 -> BorderMark
                18 -> BorderLineMark
                19 -> ChannelEdgeLight
                20 -> Tower
                else -> fail("Invalid construction value: '$d'.")
            }
        }
    }
}
