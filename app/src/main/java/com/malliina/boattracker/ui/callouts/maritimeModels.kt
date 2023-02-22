package com.malliina.boattracker.ui.callouts

import com.malliina.boattracker.*
import com.malliina.boattracker.Json.Companion.fail
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import timber.log.Timber

class MarineAdapter {
    @FromJson
    fun navMark(d: Double): NavMark = NavMark.navMark(d)
    @FromJson
    fun aidType(d: Double): AidType = AidType.aidType(d)
    @FromJson
    fun fairwayAreaType(d: Double): FairwayType? = FairwayType.fairwayType(d.toInt())
    @FromJson
    fun markType(d: Double): MarkType = MarkType.markType(d.toInt())
    @FromJson
    fun construction(d: Double): ConstructionInfo? {
        return try {
            ConstructionInfo.build(d)
        } catch (e: Exception) {
            Timber.i(e.message ?: "JSON error.")
            null
        }
    }
    @FromJson
    fun trafficSignInfo(d: Double): TrafficSignInfo? = TrafficSignInfo.trafficSign(d)
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

    fun isKnown(): Boolean {
        return !(this == Unknown || this == NotApplicable)
    }

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

enum class TrafficSignInfo {
    Unknown,
    NoAnchoring,
    NoParking,
    NoAttachment,
    NoOvertaking,
    NoRendezVous,
    NoWaves,
    NoWaterSkiing,
    NoWindSurfing,
    NoMotorPower,
    NoJetSkiing,
    SpeedLimit,
    StopSign,
    GeneralWarning,
    SignalMandatory,
    HeightLimit,
    DepthLimit,
    WidthLimit,
    StrongCurrent,
    FairwaySide,
    SwimmingWarning,
    UseRadio,
    ParkingAllowed,
    AttachmentAllowed,
    AirCable,
    Phone,
    CableFerryCrossing,
    FerryCrossing,
    RadioPossibility,
    DrinkingPoint,
    LimitEnds,
    CableSign,
    WireSign,
    DirectionUpper,
    DirectionLower;

    fun translate(limits: TrafficSignLimitsLang, info: TrafficSignInfoLang): String {
        return when (this) {
            Unknown -> limits.unknown
            NoAnchoring -> limits.noAnchoring
            NoParking -> limits.noParking
            NoAttachment -> limits.noAttachment
            NoOvertaking -> limits.noOvertaking
            NoRendezVous -> limits.noRendezVous
            NoWaves -> limits.noWaves
            NoWaterSkiing -> limits.noWaterSkiing
            NoWindSurfing -> limits.noWindSurfing
            NoMotorPower -> limits.noMotorPower
            NoJetSkiing -> limits.noJetSkiing
            SpeedLimit -> limits.speedLimit
            StopSign -> limits.stopSign
            GeneralWarning -> limits.generalWarning
            SignalMandatory -> limits.signalMandatory
            HeightLimit -> limits.heightLimit
            DepthLimit -> limits.depthLimit
            WidthLimit -> limits.widthLimit
            StrongCurrent -> info.strongCurrent
            FairwaySide -> info.fairwaySide
            SwimmingWarning -> info.swimmingWarning
            UseRadio -> info.useRadio
            ParkingAllowed -> info.parkingAllowed
            AttachmentAllowed -> info.attachmentAllowed
            AirCable -> info.airCable
            Phone -> info.phone
            CableFerryCrossing -> info.cableFerryCrossing
            FerryCrossing -> info.ferryCrossing
            RadioPossibility -> info.radioPossibility
            DrinkingPoint -> info.drinkingPoint
            LimitEnds -> info.limitEnds
            CableSign -> info.cableSign
            WireSign -> info.wireSign
            DirectionUpper -> info.directionUpper
            DirectionLower -> info.directionLower
        }
    }

    companion object {
        fun trafficSign(d: Double): TrafficSignInfo {
            return when (d.toInt()) {
                0 -> Unknown
                1 -> NoAnchoring
                2 -> NoParking
                3 -> NoAttachment
                4 -> NoOvertaking
                5 -> NoRendezVous
                6 -> NoWaves
                7 -> NoWaterSkiing
                8 -> NoWindSurfing
                9 -> NoMotorPower
                10 -> NoJetSkiing
                11 -> SpeedLimit
                12 -> StopSign
                13 -> GeneralWarning
                14 -> SignalMandatory
                15 -> HeightLimit
                16 -> DepthLimit
                17 -> WidthLimit
                18 -> StrongCurrent
                19 -> FairwaySide
                20 -> SwimmingWarning
                21 -> UseRadio
                22 -> ParkingAllowed
                23 -> AttachmentAllowed
                24 -> AirCable
                25 -> Phone
                26 -> CableFerryCrossing
                27 -> FerryCrossing
                28 -> RadioPossibility
                29 -> DrinkingPoint
                30 -> LimitEnds
                31 -> CableSign
                32 -> WireSign
                33 -> DirectionUpper
                34 -> DirectionLower
                else -> fail("Unknown traffic sign type: '$d'.")
            }
        }
    }
}

// {"VAIKUTUSAL":"A","SIJAINTIR":"","SIJAINTIS":"Lauttasaaren vattuniemerannan aallonmurtajan päässä","TKLNUMERO":54.0,"VAYLALAJI":"","VLM_TYYPPI":1.0,"PATA_TYYP":53.0,"PAATOS":"","PAKO_TYYP":5.0,"OMISTAJA":"Tuntematon","NIMIS":"HKI77A","TUNNISTE":1760.0,"VLM_LAJI":6.0,"MITTAUSPVM":"19981127","LK_TEKSTIS":"","NIMIR":"","LK_TEKSTIR":"","RA_ARVO_T":"","LISATIETOS":"","LISATIETOR":"","LISAKILPI":"","IRROTUS_PV":"2018-04-29T00:50:55"}
@JsonClass(generateAdapter = true)
data class TrafficSignRaw(
    val OMISTAJA: String,
    val NIMIR: NonEmptyString?,
    val NIMIS: NonEmptyString?,
    val SIJAINTIS: NonEmptyString?,
    val SIJAINTIR: NonEmptyString?,
//    val VLM_TYYPPI: Double,
    val VLM_LAJI: TrafficSignInfo?,
    val RA_ARVO: Double?
) {
    val speed = if (VLM_LAJI == TrafficSignInfo.SpeedLimit) RA_ARVO?.kmh() else null
    fun toSign() =
        MinimalMarineSymbol(OMISTAJA, NIMIR, NIMIS, SIJAINTIS, SIJAINTIR, VLM_LAJI, speed)
}

data class MinimalMarineSymbol(
    val owner: String,
    val nameFi: NonEmptyString?,
    val nameSe: NonEmptyString?,
    val locationFi: NonEmptyString?,
    val locationSe: NonEmptyString?,
//    val signType: Double,
    val sign: TrafficSignInfo?,
    val limit: Speed?
) {
    fun nameOrEmpty(lang: Language) = name(lang)?.value ?: ""

    fun name(lang: Language) =
        if (lang == Language.Finnish) (nameFi ?: nameSe)
        else (nameSe ?: nameFi)

    fun location(lang: Language): NonEmptyString? =
        if (lang == Language.Finnish) (locationFi ?: locationSe)
        else (locationSe ?: locationFi)
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
    val SUURUUS: Double,
    val PITUUS: Double?,
    val MERK_VAST: NonEmptyString?,
    val NIMI_SIJAI: NonEmptyString?,
    val VAY_NIMISU: NonEmptyString?,
    val IRROTUS_PV: String
) {
    fun toLimitArea() = LimitArea(
        RAJOITUSTY.split(", ").map { LimitType.limitType(it) },
        SUURUUS.kmh(),
        PITUUS?.meters(),
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
    val fairwayName: NonEmptyString?,
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
