# TrainLog Analyzer (Android)

Lab notebook for AI researchers training LLMs / CPT runs.

## Researcher sections (per run)

1. **Identity** — git, seed, host, hardware, framework versions, parent run  
2. **Model** — architecture, params, precision, attn, GaLore/LoRA, resume  
3. **Data** — sources, batch, packing, mixture, tokens, epoch equivalent  
4. **Optim** — optimizer, LR schedule, WD, clip, last LR/grad  
5. **Learning curve** — loss series, plateau, PPL, gap, noise  
6. **Compute** — sec/step, tok/s, FLOPs \(6ND\), MFU, wall/GPU-hours  
7. **Downstream** — tasks, forgetting, harness, samples  
8. **Checkpoints** — final/best + notes  
9. **Diagnosis & decision** — hypothesis → change → result → next step  
10. **Failures** — NaN, OOM, missing keys, slow save  

## Tools

- Import HF Trainer / Tunix `[loss-monitor]` logs  
- Loss charts (train / eval / EMA) on screen + PDF  
- Compare runs, Lab calculators (PPL, schedule, mixture, MFU)  
- Export PDF + Markdown  

## Build

```bash
./gradlew assembleDebug
./gradlew test
```

Min SDK 26. Room schema v3 (destructive migration on upgrade).
