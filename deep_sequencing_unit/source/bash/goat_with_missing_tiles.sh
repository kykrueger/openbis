#!/bin/bash


RUN_DIR=/dsf/Analysis/Runs/090701_426LKAAXX/ 
MISSING_TILES=--tiles=s_1_000[1-9],s_1_00[1-5][0-9],s_1_006[0-1],s_1_006[3-9],s_1_00[7-9][0-9],s_1_0100,s_2,s_3_000[1-9],s_3_00[1-4][0-9],s_3_0050,s_3_005[3-9],s_3_00[6-7][0-9],s_3_008[0-8],s_3_009[0-9],s_3_0100,s_4,s_5_000[1-9],s_5_00[1-3][0-9],s_5_0040,s_5_004[2-9],s_5_005[0-9],s_5_006[0-2],s_5_0064,s_5_006[6-7],s_5_0069,s_5_00[7-9][0-9],s_5_0100,s_6_000[1-9],s_6_00[1-2][0-9],s_6_003[0-8],s_6_00[4-9][0-9],s_6_0100,s_7_000[1-9],s_7_00[1-4][0-9],s_7_005[0-2],s_7_005[4-5],s_7_005[7-9],s_7_00[6-9][0-9],s_7_0100,s_8_000[1-9],s_8_00[1-6][0-9],s_8_007[0-8],s_8_0081,s_8_008[4-9],s_8_009[0-9],s_8_0100
GOAT=/dsf/GAPipeline-1.4.0/bin/goat_pipeline.py
#CONTROL_LANE=--control-lane=5

cd $RUN_DIR
echo Started: `date`
$GOAT --control-lane=5 $MISSING_TILES ./ --make

echo Finished: `date`
