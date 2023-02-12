package com.malliina.boattracker

import com.malliina.boattracker.ui.callouts.Callouts
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

data class Emo(val v: String)
data class Msg(val msg: String, val emo: String)

class EmoAdapter {
    @FromJson fun emo(s: String): Emo {
        return Emo(s)
    }
}
class ExampleUnitTest {
    val moshi = Moshi.Builder().add(EmoAdapter()).build()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test(expected = JsonDataException::class)
    fun parseJson() {
        val str = """{"IRROTUS_PV":"2018-04-29T00:50:55","LISAKILPI":"","LISATIETOR":"","LISATIETOS":"","LK_TEKSTIR":"","RA_ARVO_T":"","LK_TEKSTIS":"","NIMIR":"","MITTAUSPVM":"19981127","NIMIS":"HKI77A","TUNNISTE":1760.0,"VLM_LAJI":6.0,"OMISTAJA":"Tuntematon","PAATOS":"","PAKO_TYYP":5.0,"PATA_TYYP":53.0,"SIJAINTIR":"","SIJAINTIS":"Lauttasaaren vattuniemerannan aallonmurtajan päässä","TKLNUMERO":54.0,"VAIKUTUSAL":"A","VAYLALAJI":"","VLM_TYYPPI":1.0}"""
        val actual = Callouts.marineSymbolAdapter.fromJson(str)
        // assertEquals("babe", actual?.msg)
    }

    @Test(expected = JsonDataException::class)
    fun jsonTest() {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Foo::class.java)
        adapter.fromJson("{}")
    }
}
