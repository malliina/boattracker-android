package com.malliina.boattracker.ui.callouts

import com.malliina.boattracker.AidTypeLang
import com.malliina.boattracker.Language
import com.malliina.boattracker.NavMarkLang
import com.squareup.moshi.FromJson

class MarineAdapter {
    @FromJson fun navMark(d: Double): NavMark {
        return when (d.toInt()) {
            0 -> NavMark.Unknown
            1 -> NavMark.Left
            2 -> NavMark.Right
            3 -> NavMark.North
            4 -> NavMark.South
            5 -> NavMark.West
            6 -> NavMark.East
            7 -> NavMark.Rock
            8 -> NavMark.SafeWaters
            9 -> NavMark.Special
            99 -> NavMark.NotApplicable
            else -> NavMark.Unknown
        }
    }
    @FromJson fun aidType(d: Double): AidType {
        return when (d.toInt()) {
            0 -> AidType.Unknown
            1 -> AidType.Lighthouse
            2 -> AidType.SectorLight
            3 -> AidType.LeadingMark
            4 -> AidType.DirectionalLight
            5 -> AidType.MinorLight
            6 -> AidType.OtherMark
            7 -> AidType.EdgeMark
            8 -> AidType.RadarTarget
            9 -> AidType.Buoy
            10 -> AidType.Beacon
            11 -> AidType.SignatureLighthouse
            13 -> AidType.Cairn
            else -> AidType.Unknown
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
}

data class MarineSymbolRaw(val NIMIR: NonEmptyString?,
                           val NIMIS: NonEmptyString?,
                           val SIJAINTIS: NonEmptyString?,
                           val SIJAINTIR: NonEmptyString?,
                           val TY_JNR: AidType,
                           val NAVL_TYYP: NavMark,
                           val OMISTAJA: String) {
    fun toSymbol() = MarineSymbol(NIMIS, NIMIR, SIJAINTIS, SIJAINTIR, TY_JNR, NAVL_TYYP, OMISTAJA)
}

data class MarineSymbol(val nameFi: NonEmptyString?,
                        val nameSe: NonEmptyString?,
                        val locationFi: NonEmptyString?,
                        val locationSe: NonEmptyString?,
                        val aidType: AidType,
                        val navMark: NavMark,
                        val owner: String) {
    fun name(lang: Language) = if (lang == Language.Finnish) (nameFi ?: nameSe) else (nameSe ?: nameFi)
    fun location(lang: Language) = if (lang == Language.Finnish) (locationFi ?: locationSe) else (locationSe ?: locationFi)
}
