/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.imagereaders.bioformats;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.media.jai.widget.ScrollingImagePanel;

import org.apache.log4j.BasicConfigurator;

import ch.systemsx.cisd.base.io.RandomAccessFileImpl;

/**
 * An image viewer that uses BioFormats for reading and ImageJ for intensity rescaling.
 * 
 * @author Bernd Rinn
 */
@SuppressWarnings("deprecation")
public class BioFormatsImageJImageViewer
{

    private static void showImage(final RenderedImage image)
    {
        final Frame frame = new Frame("Image");
        final ScrollingImagePanel panel =
                new ScrollingImagePanel(image, image.getWidth() + 10, image.getHeight() + 10);
        frame.add(panel);
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.err.println("Syntax: showBioFormatsImage <imagefile>");
            System.exit(1);
        }
        BasicConfigurator.configure();
        final BufferedImage image =
                BioFormatsImageJUtils.readAndTransformImage(args[0], new RandomAccessFileImpl(
                        args[0], "r"), 0, 0);
        showImage(image);
    }
}
