#!/usr/bin/python

from Quartz import *
from Cocoa import *
from LaunchServices import * # for kUTTypePNG
import math
import os
import shutil

size = 512
well = "A1"
tileNum = 6

def createContext():
  cs = CGColorSpaceCreateDeviceRGB ()
  pixelsWide = size
  pixelsHigh = size
  bits_per_component = 8
  number_of_components = 4 # r,g,b,a
  # Convert bits per component to bytes per pixel, rounding up to the next int if necessary
  bytes_per_pixel = (bits_per_component * number_of_components + 7)/8
  bitmapBytesPerRow   = (pixelsWide * bytes_per_pixel);
  bitmapByteCount     = (bitmapBytesPerRow * pixelsHigh);  
  # Create an RGB bitmap, let CoreGraphics deal with allocating and managing memory
  ctx = CGBitmapContextCreate(None, pixelsWide, pixelsHigh, bits_per_component, bitmapBytesPerRow, cs, kCGImageAlphaPremultipliedLast)
  return ctx
  
def write_to_png_file(ctx, filename):
  image = CGBitmapContextCreateImage(ctx)
  fileUrl = NSURL.fileURLWithPath_(filename)
  dest = CGImageDestinationCreateWithURL(fileUrl, kUTTypePNG, 1, None);
  CGImageDestinationAddImage(dest, image, None);
  CGImageDestinationFinalize(dest);

def drawRect(ctx, r, g, b, start, isUnfilled = 0):
  with CGSavedGState(ctx):
    CGContextSetRGBStrokeColor(ctx, r, g, b, 1)
    if isUnfilled:
      CGContextSetRGBFillColor(ctx, 0, 0, 0, 0) # transparent
    else:
      CGContextSetRGBFillColor(ctx, r, g, b, 1)
    CGContextSetLineWidth(ctx, 40)
    CGContextAddRect(ctx, CGRectMake(start, start, size - 2 * start, size - 2 * start))
    CGContextDrawPath(ctx, kCGPathFillStroke)
  
def drawText(ctx, x, y, text):
  with CGSavedGState(ctx):
    CGContextSetRGBStrokeColor(ctx, 0, 0, 0, 1) # black
    CGContextSetRGBFillColor(ctx, 1, 1, 1, 1) # white
    CGContextSetTextMatrix(ctx, CGAffineTransformIdentity)
    CGContextSelectFont(ctx, "Helvetica Neue", 70, kCGEncodingMacRoman)
    CGContextSetTextDrawingMode(ctx, kCGTextFillStroke)
    CGContextShowTextAtPoint(ctx, x, y, text, len(text))

def drawMatrix(coordsList, dir, channel, isOverlay):
  nonemptyTiles = set([ calcTile(coords) for coords in coordsList ])
  for tile in range(1, 10):
    c = createContext()
    if tile in nonemptyTiles:
      if not isOverlay:
        drawRect(c, 0, 0, 0, 0)
        drawRect(c, 0.5, 0.5, 0.5, 70, isUnfilled = 0)
      elif isOverlay == 1:
        drawRect(c, 1, 1, 1, 30, isUnfilled = 1)
      else:
        drawText(c, size/2, size/2, "X")
    destFile = dir + "/c"+channel + "_s" + str(tile) +".png"
    write_to_png_file(c, destFile)

def calcTile(coords):
  x,y = coords
  return (y-1)*3+x


def overlayTests(sampleCode):
  rootDir = "targets/generated-images"
  recreateDir(rootDir)
  
  dir = rootDir + "/" + sampleCode+".basic"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",  0);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL",   0);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",  0);
  
  dir = rootDir + "/" + sampleCode+".overlay-surround"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS",  1);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL",     1);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION",  1);

  dir = rootDir + "/" + sampleCode+".overlay-text"
  recreateDir(dir)
  drawMatrix(((1,1), (1,2), (1,3), (3,1)), dir, "NUCLEUS-TEXT",   2);
  drawMatrix(((1,2), (2,2), (2,3), (3,1)), dir, "CELL-TEXT",    2);
  drawMatrix(((1,3), (2,3), (3,3), (3,1)), dir, "MITOCHONDRION-TEXT",  2);

  
def save(dir, filename, text, r, g, b, merged = 0):
  c = createContext()
  drawRect(c, 0, 0, 0, 0) # fill with black
  
  zero = 0
  if merged:
    zero = 1
  if r:
    drawRect(c, 1, zero, zero, 20) # red
  if g:
    drawRect(c, zero, 1, zero, 70) # green
  if b:
    drawRect(c, zero, zero, 1, 120) # blue

  # text annotation
  drawText(c, 5, size / 2, text)
    
  # Write the bitmap to disk in PNG format
  write_to_png_file(c, dir + "/" + filename)

def recreateDir(dir):
  if os.path.exists(dir):
    shutil.rmtree(dir)
  os.mkdir(dir)

# -------------------------------

def generateMicroscopy3D(dir, channelName, r, g, b, merged = 0):  
  timePoints = [ 5, 10, 15 ]
  depthPoints = [ 3, 6, 9 ]
  series = [1,2,3]
  for seriesNum in series:
    for tile in range(1, tileNum+1):
      for time in timePoints:
        for depth in depthPoints:
          desc = "s"+str(tile)+"_z"+str(depth)+"_t"+str(time)+"_n"+str(seriesNum)
          file = desc+"_c"+channelName+".png"
          save(dir, file, desc, r, g, b, merged)

def generateMicroscopySeries(dir, channelName, r, g, b, merged = 0):  
  for seriesNum in range(1,11):
    for tile in range(1, tileNum+1):
      desc = "s"+str(tile)+"_n"+str(seriesNum)
      file = desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b, merged)

def generateMicroscopySeriesBrokenTiles(dir, channelName, r, g, b, merged = 0): 
  tiles = [1,3,5]
  for seriesNum in range(1,11):
    for tile in tiles:
      desc = "s"+str(tile)+"_n"+str(seriesNum)
      file = desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b, merged)
          
def generateMicroscopyChannel(dir, channelName, r, g, b, merged = 0):
  #timePoints = [ 1 ] 
  #tileNum = 1
  #depthPoints = [ 1 ]
  
  timePoints = [ 5, 10, 15 ]
  depthPoints = [ 3, 6, 9 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      for depth in depthPoints:
        desc = "s"+str(tile)+"_z"+str(depth)+"_t"+str(time)
        file = desc+"_c"+channelName+".png"
        save(dir, file, desc, r, g, b, merged)
        
def generateMicroscopyChannelNoDepth(dir, channelName, r, g, b):
  timePoints = [ 5, 10, 15 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      desc = "s"+str(tile)+"_t"+str(time)
      file = desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b)

def generateMicroscopyChannelNoTimeNoDepth(dir, channelName, r, g, b):
  for tile in range(1, tileNum+1):
    desc = "s"+str(tile)
    file = desc+"_c"+channelName+".png"
    save(dir, file, desc, r, g, b)
      
# -------------------------------

def generatePlateChannel(dir, channelName, r, g, b, merged = 0):
  timePoints = [ 5, 10, 15 ]
  depthPoints = [ 3, 6, 9 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      for depth in depthPoints:
        desc = "s"+str(tile)+"_z"+str(depth)+"_t"+str(time)
        file = "bPLATE_w"+well+"_"+desc+"_c"+channelName+".png"
        save(dir, file, desc, r, g, b, merged)

def generatePlateChannelNoDepth(dir, channelName, r, g, b):
  timePoints = [ 5, 10, 15 ]
  for tile in range(1, tileNum+1):
    for time in timePoints:
      desc = "s"+str(tile)+"_t"+str(time)
      file = "bPLATE_w"+well+"_"+desc+"_c"+channelName+".png"
      save(dir, file, desc, r, g, b)

def generatePlateChannelNoTimeNoDepth(dir, channelName, r, g, b):
  for tile in range(1, tileNum+1):
    desc = "s"+str(tile)
    file = "bPLATE_w"+well+"_"+desc+"_c"+channelName+".png"
    save(dir, file, desc, r, g, b)


def generatePlateSplitChannels(dir):
  recreateDir(dir)
  generatePlateChannel(dir, "RED", 1, 0, 0,   1)
  generatePlateChannel(dir, "GREEN", 0, 1, 0, 1)
  generatePlateChannel(dir, "BLUE", 0, 0, 1,  1)

def generatePlateMergedChannels(dir):
  recreateDir(dir)
  generatePlateChannel(dir, "RGB", 1, 1, 1)

# -------------------------------

def generateMicroscopySplitChannels(dir):
  recreateDir(dir)
  generateMicroscopyChannel(dir, "RED", 1, 0, 0,    1)
  generateMicroscopyChannel(dir, "GREEN", 0, 1, 0,  1)
  generateMicroscopyChannel(dir, "BLUE", 0, 0, 1,   1)

def generateMicroscopyMergedChannels(dir):
  recreateDir(dir)
  generateMicroscopyChannel(dir, "RGB", 1, 1, 1)

def generateMicroscopyMergedChannelsNoTimeNoDepth():
  dir = "TEST.microscopy.mergedChannels_tile_noTime_noDepth"
  recreateDir(dir)
  generateMicroscopyChannelNoTimeNoDepth(dir, "RGB", 1, 1, 1)

def generateMicroscopyMergedChannelsNoDepth():
  dir = "TEST.microscopy.mergedChannels_tile_time_noDepth"
  recreateDir(dir)
  generateMicroscopyChannelNoDepth(dir, "RGB", 1, 1, 1)

def generateMicroscopySplitChannelsSeriesBrokenTiles():
  dir = "TEST.microscopy.splitChannels_someTiles_series"
  recreateDir(dir)
  generateMicroscopySeriesBrokenTiles(dir, "AAA", 1, 0, 0,  1)
  generateMicroscopySeriesBrokenTiles(dir, "BBB", 0, 1, 0,  1)
  generateMicroscopySeriesBrokenTiles(dir, "CCC", 0, 0, 1,  1)

def generateMicroscopySplitChannels3D():
  dir = "TEST.microscopy.splitChannels_3D"
  recreateDir(dir)
  generateMicroscopy3D(dir, "AAA", 1, 0, 0, 1)
  generateMicroscopy3D(dir, "DDD", 0, 1, 0,   1)
  generateMicroscopy3D(dir, "EEE", 0, 0, 1,   1)
  
def generateMicroscopySplitChannelsSeries():
  dir = "TEST.microscopy.splitChannels_series"
  recreateDir(dir)
  generateMicroscopySeries(dir, "AAA", 1, 0, 0, 1)
  generateMicroscopySeries(dir, "CCC", 0, 1, 0,   1)
  generateMicroscopySeries(dir, "FFF", 0, 0, 1,   1)

def generateMicroscopySplitChannelsSeries():
  dir = "TEST.microscopy.splitChannels_series"
  recreateDir(dir)
  generateMicroscopySeries(dir, "AAA", 1, 0, 0, 1)
  generateMicroscopySeries(dir, "BBB", 0, 1, 0,   1)
  generateMicroscopySeries(dir, "CCC", 0, 0, 1,   1)
  generateMicroscopySeries(dir, "DDD", 1, 1, 0, 1)
  generateMicroscopySeries(dir, "EEE", 0, 1, 1,   1)
  generateMicroscopySeries(dir, "FFF", 1, 0, 1,   1)

#generatePlateSplitChannels("PLATE.splitChannels_tile_time_depth") 
#generatePlateMergedChannels("PLATE.mergedChannels_tile_time_depth") 
#generateMicroscopySplitChannels("TEST.microscopy.splitChannels_tile_time_depth")
#generateMicroscopyMergedChannels("TEST.microscopy.mergedChannels_tile_time_depth")
#generateMicroscopyMergedChannels3D("TEST.microscopy.mergedChannels_tile_time_depth_series")
#generateMicroscopyMergedChannelsNoTimeNoDepth()
#generateMicroscopyMergedChannelsNoDepth()
#generateMicroscopySplitChannelsSeriesBrokenTiles()
#generateMicroscopySplitChannelsSeries()
#generateMicroscopySplitChannels3D()
overlayTests("OVERLAY-TEST")