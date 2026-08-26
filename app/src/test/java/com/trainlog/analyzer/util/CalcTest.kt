package com.trainlog.analyzer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
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
        assertEquals(3e-4, Calc.cosineLr(0.0, 1000.0, 3e-4, 1e-6, 0.0), 1e-12)
        assertEquals(1e-6, Calc.cosineLr(1000.0, 1000.0, 3e-4, 1e-6, 0.0), 1e-12)
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
        val text = "{'loss': 2.5, 'learning_rate': 1e-4, 'grad_norm': 0.5, 'epoch': 0.1}\n{'loss': 2.2, 'epoch': 0.2}\neval_loss: 2.3"
        val imp = LogImporter.import(text)
        assertTrue(imp.trainLosses.size >= 2)
        assertNotNull(imp.lastEvalLoss)
        assertTrue(imp.learningRates.isNotEmpty())
    }

    @Test
    fun logImporter_tunix_monitor() {
        val text = """
            [loss-monitor] train/loss: step=470275 value=1.8081
            [loss-monitor] train/loss: step=553102 value=1.9185
        """.trimIndent()
        val imp = LogImporter.import(text)
        assertEquals(2, imp.trainLosses.size)
        assertEquals(553102, imp.lastStep)
        assertEquals("Tunix / loss-monitor", imp.sourceHint)
    }

    @Test
    fun logImporter_qwen_banner() {
        val text = """
            Seq len              : 1024
            Global batch (2 GPU) : 128
            Total params : 385,277,184 (385.3M)
            GaLore rank       : 128
            95%|█████████▍| 8600/9066 [11:10:21<4:14:27, 32.76s/it]
            {'loss': '2.115', 'grad_norm': '1.049', 'learning_rate': '2.067e-06', 'epoch': '1.897'}
        """.trimIndent()
        val imp = LogImporter.import(text)
        assertEquals(128, imp.globalBatch)
        assertEquals(1024, imp.seqLen)
        assertEquals(385277184L, imp.params)
        assertEquals(8600, imp.lastStep)
        assertEquals(9066, imp.totalSteps)
        assertEquals(2.115, imp.lastTrainLoss!!, 1e-6)
        assertEquals(32.76, imp.secPerStepEstimate!!, 1e-3)
    }

    @Test
    fun scaleLr_linear_and_sqrt() {
        assertEquals(2e-4, Calc.scaleLr(1e-4, 128.0, 256.0, false)!!, 1e-12)
        assertEquals(2e-4, Calc.scaleLr(1e-4, 100.0, 400.0, true)!!, 1e-12)
    }

    @Test
    fun chinchillaRatio() {
        assertEquals(20.0, Calc.chinchillaRatio(20e9, 1e9)!!, 1e-9)
    }
}
