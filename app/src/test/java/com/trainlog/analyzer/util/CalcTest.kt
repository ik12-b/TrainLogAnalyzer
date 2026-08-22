package com.trainlog.analyzer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.exp

class CalcTest {
    @Test
    fun relativeImprovement_basic() {
        val r = Calc.relativeImprovement(2.0, 1.8)!!
        assertEquals(0.1, r.first, 1e-9)
        assertEquals(10.0, r.second, 1e-9)
    }

    @Test
    fun perplexity_matches_exp() {
        val loss = 2.121
        assertEquals(exp(loss), Calc.perplexityFromLoss(loss), 1e-9)
    }

    @Test
    fun trainingFlops_sixND() {
        assertEquals(6.0 * 1e9 * 1e9, Calc.trainingFlops(1e9, 1e9), 1.0)
    }

    @Test
    fun cosineLr_at_start_and_end() {
        val midish = Calc.cosineLr(0.0, 1000.0, 3e-4, 1e-6, 0.0)
        assertEquals(3e-4, midish, 1e-12)
        val end = Calc.cosineLr(1000.0, 1000.0, 3e-4, 1e-6, 0.0)
        assertEquals(1e-6, end, 1e-12)
    }

    @Test
    fun plateauCheck_detects_flat() {
        val series = listOf(2.12, 2.119, 2.1185, 2.118)
        val r = Calc.plateauCheck(series, 3, 0.5)!!
        assertTrue(r.isPlateau)
    }

    @Test
    fun plateauCheck_detects_drop() {
        val series = listOf(3.0, 2.5, 2.0, 1.5)
        val r = Calc.plateauCheck(series, 3, 0.5)!!
        assertFalse(r.isPlateau)
    }

    @Test
    fun bestCheckpoint_picks_min_loss() {
        val best = Calc.bestCheckpoint("ckpt-100:2.5\nckpt-200:2.1\nckpt-150:2.3")!!
        assertEquals("ckpt-200", best.first)
        assertEquals(2.1, best.second, 1e-9)
    }

    @Test
    fun logImporter_hf_style() {
        val text = "{'loss': 2.5, 'epoch': 0.1}\n{'loss': 2.2, 'epoch': 0.2}\neval_loss: 2.3"
        val imp = LogImporter.import(text)
        assertTrue(imp.trainLosses.size >= 2)
        assertNotNull(imp.lastEvalLoss)
    }

    @Test
    fun scaleLr_linear_and_sqrt() {
        val lin = Calc.scaleLr(1e-4, 128.0, 256.0, false)!!
        assertEquals(2e-4, lin, 1e-12)
        val sq = Calc.scaleLr(1e-4, 100.0, 400.0, true)!!
        assertEquals(2e-4, sq, 1e-12)
    }

    @Test
    fun chinchillaRatio() {
        val r = Calc.chinchillaRatio(20e9, 1e9)!!
        assertEquals(20.0, r, 1e-9)
    }
}
