package com.malliina.boattracker.ui.callouts

import com.malliina.boattracker.AidTypeLang
import com.malliina.boattracker.ConstructionLang
import com.malliina.boattracker.Language
import com.malliina.boattracker.NavMarkLang
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import timber.log.Timber

class MarineAdapter {
    @FromJson fun navMark(d: Double): NavMark {
        return NavMark.navMark(d)
    }
    @FromJson fun aidType(d: Double): AidType {
        return AidType.aidType(d)
    }
    @FromJson fun construction(d: Double): ConstructionInfo? {
        return try { ConstructionInfo.build(d) }
        catch (e: Exception) {
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

@JsonClass(generateAdapter = true)
data class MarineSymbolRaw(val NIMIR: NonEmptyString?,
                           val NIMIS: NonEmptyString?,
                           val SIJAINTIS: NonEmptyString?,
                           val SIJAINTIR: NonEmptyString?,
                           val TY_JNR: AidType,
                           val RAKT_TYYP: ConstructionInfo?,
                           val NAVL_TYYP: NavMark,
                           val OMISTAJA: String) {
    fun toSymbol() = MarineSymbol(NIMIS, NIMIR, SIJAINTIS, SIJAINTIR, TY_JNR, RAKT_TYYP, NAVL_TYYP, OMISTAJA)
}

data class MarineSymbol(val nameFi: NonEmptyString?,
                        val nameSe: NonEmptyString?,
                        val locationFi: NonEmptyString?,
                        val locationSe: NonEmptyString?,
                        val aidType: AidType,
                        val construction: ConstructionInfo?,
                        val navMark: NavMark,
                        val owner: String) {
    fun name(lang: Language) =
        if (lang == Language.Finnish) (nameFi ?: nameSe)
        else (nameSe ?: nameFi)

    fun location(lang: Language): NonEmptyString? =
        if (lang == Language.Finnish) (locationFi ?: locationSe)
        else (locationSe ?: locationFi)
}

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
                else -> throw JsonDataException("Invalid construction value: '$d'.")
            }
        }
    }
}
