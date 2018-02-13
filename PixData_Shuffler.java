/*
PixData_Shuffler ImageJ PlugInFilter version 1.0
Copyright (C) 2006 Kouichi Nakamura 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.



!!!Note!!!
This Plugin requires public class PixData!!!

<< Instructions >>
ImageJ PlugInFilter "PixData_Shuffler.java" is designed for the quantitative colocalization 
analysis of two immunoreactivities in double immunofluorescence staining.  This PlugInFilter 
calculates correlation coefficients (CC, or Pearson's r), intensity correlation quotient 
(ICQ), and overlap coefficient (OC) from two channels (red and green, in most cases) in 
8bit RGB, 12bit RGB, or 16bit RGB images before and after shuffling of pixels. This supports
 ROI (region of interest) in red image but does not support image stacks.

Installation
Put this file and PixData.java to the plugins folder or subfolder, and compile them with 
the "Compile and Run" command. Restart ImageJ to add the "PixData_Shuffler" command to the 
Plugins menu.

Procedures
1:	"PixData_Shuffler.java" requires at least two grayscale (8bit, 8bit color, 12 bit, or 16bit)
	images as input.  First, you thus need to separate or split channels of single RGB 
	images (or stacks).  This can be done simply by a menu command "RGB split" in "Image 
	-> Color" for standard 8bit RGB images or stacks.  When you handle 12bit or 16bit 
	RGB images, you should choose a menu command "Convert stack into images" in "Image 
	-> Stack". 

2:	Before using this PlugInFilter, you need to determine lower threshold (i.e. maximum 
	noise level) of red and green channels, separately.  If you want to determine the 
	threshold simply by visual inspection, a menu command "Threshold..." in "Image -> 
	Adjust" is suitable for this purpose.
		
3:	Run "PixData_Shuffler.java" by choosing it from "Plugins" menu.

4:	Select red image (stack) and green image (green) from the drop-down menus. 

5:	Input lower threshold values (maximum noise level) for red and green images (stacks)
	into boxes.
	
6:	When you use this plug-in for the first time in a session (the first run), you need
	to check "Initialize the Results Window", to make headers in Results window. Note that
	this option clears all the data in the current Results window! 

7:	You must enter the number of shuffles (positive integer) in the box "How many shuffles
	do you need?".

8: 	Then click "OK". If a dialog ask you bit depth of images, select the appropriate one
	and click "OK". Results of calculation will appear on Results window. 

Options
1:	"Green-Red order?" function works on Windows but not on Mac OS X. This option may be 
	helpful when the order of image names listed in the drop-down menus is stable. In 
	default, this Plug-in shows names of images in Red-Green order (suitable for pictures 
	taken with Zeiss microscopes). With "Green-Red order?" option checked, it shows in 
	Green-Red order (maybe suitable for pictures taken with Leica microscopes). This may
	accelerate your work.

2:	 "Show Time Spent for the Calculation" will display the time in the Results window. 

About data formats, see instruction of Pearsons_r.java.
*/

import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.*;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.process.ImageConverter.*;
import ij.process.ImageProcessor.*;
import ij.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.Properties;
import java.text.DecimalFormat;
import java.lang.*;


public class PixData_Shuffler implements PlugInFilter {
	private ImagePlus imp1, imp2;
	int wList[];
	private String titles[];// titles of images
	private String bitdepth1;// bit depth of images
	private String bitdepth2[] = {"12bit", "16bit"};// bit depth of images
	private	int sat=255;
	static String title = "PixData_Shuffler";
	private int p = 0;//lower threshold value for the red image
	private int q = 0;//lower threshold value for the green image
	private ImagePlus imp;
	private boolean /*doplot,*/ head, showtime, orderGR, close1, close2;
	private int howmany = 1; // repeat shuffle
	private int totalN = 0, thresholdN = 0; // number of pix in ROI, number of pixs for calcuration 
	private long time1, time2;
	private byte[] maskArray;
	private ImageProcessor mask;
	private Roi roi;
	private	String isROI;
	private PixData[] redArray, greenArray;
	
	private static String PREFS  = "PixDataShufflerPrefs.txt";



	public int setup(String arg, ImagePlus imp) {
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		wList = WindowManager.getIDList();
		if (wList==null || wList.length<2) {
			IJ.showMessage(title, "There must be at least two windows open.");
			return;
		} //if ... check for open window numbers
		
		titles = new String[wList.length];
		for (int i=0; i<wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);// Numbering open images
			if (imp!=null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		} // for (int i=0; i<wList.length; i++)
			
		if (showDialog()==false) return; 
		
		time1 = System.currentTimeMillis();
		
		roi = imp1.getRoi();
		if (roi==null) isROI = "whole image";
		else isROI = "ROI in red image";
		
		makeArrays(imp1, imp2, roi);
		
		PixData[][] greenArray2 = new PixData[howmany][greenArray.length];

		for (int j = 0; j < howmany; j++) {
			IJ.showProgress(j+1, howmany);
			for (int i = 0; i < greenArray.length ;i++){
				greenArray2[j][i] = new PixData(greenArray[i].getValue(), Math.random());
				// or prepare copy() method in class PixData?
			}
			Arrays.sort(greenArray2[j]);
		}
		if (head) {
			IJ.setColumnHeadings(
				"green data\t" +
				"red image\t" +
				"green image\t" +
				"bit depth\t" +
				"ROI\t" +
				"ROI pixels N\t" +
				"rMax noise p\t" +
				"gMax noise q\t" +
				"threshold N\t" +
				"R mean\t" +
				"G mean\t" +
				"R sd\t" +
				"G sd\t" +
				"cov\t" +
				"Pearson's r\t" +
				"ICQ\t" +
				"OC\t" +
				"mean of r shfl\t" +
				"SD of r shfl\t"
			);
		}
		double originalR = calc(redArray, greenArray, 0);
		
		double[] r = new double[howmany]; 
		double sumR = 0, sumRR = 0;

		for (int j = 0; j < howmany; j++){
			//IJ.showProgress(j+1, howmany);
			r[j] = calc(redArray, greenArray2[j], j+1);
			sumR += r[j];
			sumRR += r[j]*r[j];
		}

		if (howmany > 1) {
			double meanR = sumR/howmany;
			double sdR = Math.sqrt((sumRR - (sumR*sumR/howmany))/howmany);

			DecimalFormat df = new DecimalFormat("##0.000000");
			IJ.write("summary\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + df.format(meanR) + "\t" + df.format(sdR));
		}
		
		if (close1) {
			ImageWindow w1 = imp1.getWindow();
			imp1.changes = false;
			w1.close();
		}
		if (close2) {
			ImageWindow w2 = imp2.getWindow();
			imp2.changes = false;
			w2.close();
		}
		time2 = System.currentTimeMillis();
		if (showtime){
			IJ.write(// Display time
				(time2-time1) + " milli seconds\n"
			);
		}
		IJ.selectWindow("Results");


	}//public void run(ImageProcessor ip) 


	private boolean showDialog() {
		PixDataShufflerPrefs prefs = new PixDataShufflerPrefs(PREFS);
		getPreferences(prefs);
		for (;;) {
			GenericDialog param = new GenericDialog(title, IJ.getInstance());// GenericDialog object

			if (orderGR) {// for Leica
				param.addChoice("Red Image (accept ROI)", titles, titles[1]);
				param.addChoice("Green Image (to be shuffled)", titles, titles[0]);
			} else {// for Zeiss
				param.addChoice("Red Image (accept ROI)", titles, titles[0]);
				param.addChoice("Green Image (to be shuffled)", titles, titles[1]);
			}
			param.addMessage("Lower Threshold (max noise level) for ...\n");
			param.addNumericField("Red Image*", p, 0);
			param.addNumericField("Green Image*", q, 0);
			param.addMessage("   *(0-255) for 8bit, (0-4095) for 12bit, (0-65535) for 16bit");
			param.addCheckbox("Initialize the Results Window**", false);
			param.addMessage("      **\"Initialize the Results Window\" clears all the data in the results\n" +
			"      window!  But the first run requires this option.");
			param.addCheckbox("Green-Red order?*** (for Leica)", orderGR);
			param.addMessage("      ***If image channels are named in Green-Red order, check this option.\n" +
			"      Red-Green order images (ex. Zeiss) don't require this option.\n" +
			"      The change will be reflected when you use this plugin again.");
			param.addNumericField("How many shuffles do you need?", howmany, 0);
			param.addCheckbox("Close Red image window after calculation", close1);
			param.addCheckbox("Close Green image window after calculation", close2);

			//param.addCheckbox("Make Scatter Plot", doplot);
			param.addCheckbox("Show Time Spent for the Calculation", showtime);

			param.showDialog();
			
			if(param.wasCanceled()) // Cancel button
			  return false;

			int imp1Index = param.getNextChoiceIndex();
			int imp2Index = param.getNextChoiceIndex();
			imp1 = WindowManager.getImage(wList[imp1Index]);
			imp2 = WindowManager.getImage(wList[imp2Index]);
			p = (int)param.getNextNumber();
			q = (int)param.getNextNumber();
			head = param.getNextBoolean();
			orderGR = param.getNextBoolean();
			howmany = (int)param.getNextNumber();
			close1 = param.getNextBoolean();
			close2 = param.getNextBoolean();
			//doplot = param.getNextBoolean();
			showtime = param.getNextBoolean();

			int width1 = imp1.getWidth();
			int width2 = imp2.getWidth();
			int height1 = imp1.getHeight();
			int height2 = imp2.getHeight();

			if ((width1 != width2) || (height1 != height2)) {
				IJ.showMessage(title, "Red and Green images (stacks) must be at the same height and width");
				continue;
			}// if
			
			if (!
				(((imp1.getType()==imp1.GRAY8) && (imp2.getType()==imp1.GRAY8))
				||((imp1.getType()==imp1.GRAY16) && (imp2.getType()==imp1.GRAY16)))
			){
				IJ.showMessage(title, "Red and Green images (stacks) must be grayscale of the same bit depth. \n(8 bit, 12 bit, or 16 bit)");
				continue;
			}//if ((imp1.getType()!= 
			
			/*
			if (!(imp1.getStackSize()==imp2.getStackSize())){
				IJ.showMessage(title, "Red and Green stacks must have the same number of slices");
				continue;
			}//if ((imp1.getType()!= 
			*/
			if (!(imp1.getStackSize()==1 && imp2.getStackSize()==1)){
				IJ.showMessage(title, "Stack images are currently not supported.");
				continue;
			}//if ((imp1.getType()!= 
			
			if(howmany > 30){
				IJ.showMessage(title, "Too much shuffleing! (must be <= 30)");
				continue;
			}
			
			if((imp1.getType()==imp1.GRAY8) && (imp2.getType()==imp1.GRAY8)){
				bitdepth1 = "8bit";
			}
			
			if ((imp1.getType()==imp1.GRAY16) && (imp2.getType()==imp1.GRAY16)){
					if(!showDialog2())
						continue;
			}
			
			if (bitdepth1=="8bit") sat = 255;
			else if (bitdepth1=="12bit") sat = 4095;
			else if (bitdepth1=="16bit") sat = 65535;

			if (((p<0) || (p>sat))||((q<0) || (q>sat))) {
				IJ.showMessage(title, "Lower threshold value must be 0-" + sat +" for " + bitdepth1 +" images.");
				continue;
			}//if ((p<=0) .... working
			
			
			if (howmany<0){
				IJ.showMessage(title, "Number of shuffles must be a positive integer.");
				continue;
			}
				  
			savePreferences(prefs);
			break;
		}// for

		return true;
    }//    public boolean showDialog() 

	private boolean showDialog2() {
		ImageProcessor ip12of1 = imp1.getProcessor();
		ImageProcessor ip12of2 = imp2.getProcessor();
		if ((ip12of1.getMax()>4095)||(ip12of2.getMax()>4095)){
			bitdepth1 = "16bit";
			return true;
		} else {
			GenericDialog param2 = new GenericDialog(title, IJ.getInstance());
			param2.addChoice("Both images are ...", bitdepth2, bitdepth2[0]);
			param2.showDialog();
			if(param2.wasCanceled()) return false;
			
			int bit = param2.getNextChoiceIndex();
			if (bit==0) bitdepth1 = "12bit";
			if (bit==1) bitdepth1 = "16bit";
			return true;
		}
	}



	private void getPreferences(PixDataShufflerPrefs prefs) {
		p = prefs.getInt("Red Lower Threshold", 0);
		q = prefs.getInt("Green Lower Threshold", 0);
		head = prefs.getBoolean("Initialize", false);
		orderGR = prefs.getBoolean("Green-Red order", false);
		howmany = prefs.getInt("How many shuffles", 1);
		close1 = prefs.getBoolean("close red window", false);
		close2 = prefs.getBoolean("close green window", false);
		//doplot = prefs.getBoolean("Do Plot", false);
		showtime = prefs.getBoolean("Show Time", false);
	}//  void getPreferences(Preferences prefs)



	private void savePreferences(PixDataShufflerPrefs prefs) {
		prefs.putInt("Red Lower Threshold", p);
		prefs.putInt("Green Lower Threshold", q);
		prefs.putBoolean("Initialize", head);
		prefs.putBoolean("Green-Red order", orderGR);
		prefs.putInt("How many shuffles", howmany);
		prefs.putBoolean("close red window", close1);
		prefs.putBoolean("close green window", close2);
		//prefs.putBoolean("Do Plot", doplot);
		prefs.putBoolean("Show Time", showtime);
		prefs.storePreferences();
	}// void savePreferences(PixDataShufflerPrefs prefs)



	public void makeArrays(ImagePlus imp1, ImagePlus imp2, Roi roi) {
		IJ.showStatus(title + ": calculating...");
		
		ImageProcessor ip1 = imp1.getProcessor();
		ImageProcessor ip2 = imp2.getProcessor();
		//ImageProcessor plot = new FloatProcessor(256, 256);
		//ImageProcessor plot16bit = new ShortProcessor(256, 256);

		//int nslices = imp3.getStackSize();
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		
		int z1, z2;
		int count = 0; // 

		Rectangle rroi = ip1.getRoi();
		if (roi!=null) { 
			ip1.setRoi(roi);// necessary
		}
		ip1.reset(ip1.getMask()); 
		int rx, ry, rh, rw;
		maskArray = ip1.getMaskArray();// 
		
		if (rroi != null) {
			rx = rroi.x;
			ry = rroi.y;
			rw = rroi.width;
			rh = rroi.height;
		} else {
			rx = 0;
			ry = 0;
			rw = width;
			rh = height;
		}
		
	//for (int i=1; i<=nslices; i++) {
				//IJ.showProgress(i, nslices);
		//imp3.setSlice(i);
		//imp4.setSlice(i);
		for (int y=ry, my=0; y<(ry+rh); y++, my++) {
			int mi = my * rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (maskArray==null || maskArray[mi++]!=0) {
					totalN++;
					z1 = (int)ip1.getPixel(x,y); // z-value of pixel (x,y)in red image	
					z2 = (int)ip2.getPixel(x,y); // z-value of pixel (x,y)in green image
					if ( z1!=0 && z1!=sat && z2!=0 && z2!=sat && (z1>p || z2>q )) {
					//( z1 < sat && z2 < sat && z1 > p && z2 > q ){
						thresholdN++;
					}
				}//if (maskArray==
			}// for (int x=0; x<width; x++) 
		}//	for (int y=0; y<height; y++)
	//}//	for (int i=1; i<=nslices; i++) 

		redArray = new PixData[thresholdN];
		greenArray = new PixData[thresholdN];

	//for (int i=1; i<=nslices; i++) {
				//IJ.showProgress(i, nslices);
		//imp3.setSlice(i);
		//imp4.setSlice(i);
		for (int y=ry, my=0; y<(ry+rh); y++, my++) {
			int mi = my * rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (maskArray==null || maskArray[mi++]!=0) {
					z1 = (int)ip1.getPixel(x,y); // z-value of pixel (x,y)in red image	
					z2 = (int)ip2.getPixel(x,y); // z-value of pixel (x,y)in green image
					if ( z1!=0 && z1!=sat && z2!=0 && z2!=sat && (z1>p || z2>q )) {
					//if ( z1 < sat && z2 < sat && z1 > p && z2 > q ){
						redArray[count] = new PixData(z1, 0);
						greenArray[count] = new PixData(z2, 0);
						count++;
					}
				}//if (maskArray==
			}// for (int x=0; x<width; x++) 
		}//	for (int y=0; y<height; y++)
	//}//	for (int i=1; i<=nslices; i++) 		
		return;
	}// public void makeArrays()
	
	
	
	public double calc(PixData[] pd1, PixData[] pd2, int j) {
		String str;
		if (j==0){
			str = "original";
		} else{
			str = "shuffled no." + Integer.toString(j);
		}
	
		int z1, z2;
		double[] sum  = new double[5];
		final int X = 0, XY = 1, XX = 2, YY = 3, Y = 4;
		if (pd1.length != pd2.length) {
			IJ.showMessage("Error: calc()");
			return 0;
		}
		for (int i = 0; i < pd1.length ;i++) {
			z1 = pd1[i].getValue();
			z2 = pd2[i].getValue();
			
			sum[X] += z1;// sum[0][k] = sum[0][k] + z1
			sum[XY] += (z1 * z2);
			sum[XX] += (z1 * z1);
			sum[YY] += (z2 * z2);
			sum[Y] += z2;
		}

		double meanX, meanY, covXY, sdX, sdY, r, overlapC;
		meanX = sum[X]/thresholdN;// mean of red
		meanY = sum[Y]/thresholdN;// mean of green
		covXY = (sum[XY] - (sum[X]*sum[Y]/thresholdN))/thresholdN;// covariance of red and green
		sdX = Math.sqrt((sum[XX] - (sum[X]*sum[X]/thresholdN))/thresholdN);// standard deviation of red
		sdY = Math.sqrt((sum[YY] - (sum[Y]*sum[Y]/thresholdN))/thresholdN);// standard deviation of green
		r = covXY/(sdX*sdY);// Pearson's r for red and green
		
		overlapC = sum[XY]/Math.sqrt(sum[XX]*sum[YY]); //Manders' overlap coefficient
		
		
		double numeratorICQ = 0;
		if (pd1.length != pd2.length) {
			IJ.showMessage("Error: calc()");
			return 0;
		}
		for (int i = 0; i < pd1.length ;i++) {
			z1 = pd1[i].getValue();
			z2 = pd2[i].getValue();
			
			if ((z1-meanX)*(z2-meanY) > 0) numeratorICQ++;
			else if ((z1-meanX)*(z2-meanY) < 0) numeratorICQ--;
		}
		double ICQ = numeratorICQ/(2*thresholdN);
		
		
		DecimalFormat df = new DecimalFormat("##0.000000");

		IJ.write(
			str + "\t" +
			imp1.getTitle() + "\t" + 
			imp2.getTitle() + "\t" + 
			bitdepth1 + "\t" +
			isROI + "\t" +
			totalN + "\t" +
			p + "\t" +
			q + "\t" +
			thresholdN + "\t" +
			df.format(meanX) + "\t" +
			df.format(meanY) + "\t" +
			df.format(sdX) + "\t" +
			df.format(sdY) + "\t" +
			df.format(covXY) + "\t" +
			df.format(r) + "\t" +
			df.format(ICQ) + "\t" +
			df.format(overlapC)
		);

		return r;
	}
	
/*	
	 protected void doplot(PixData[] pd1, PixData[] pd2, int j) {
        StringBuffer sb = new StringBuffer();
        String vheading = stats.binSize==1.0?"value":"bin start";
        if (cal.calibrated() && !cal.isSigned16Bit()) {
            for (int i=0; i<stats.nBins; i++)
                sb.append(i+"\t"+IJ.d2s(cal.getCValue(stats.histMin+i*stats.binSize), digits)+"\t"+histogram[i]+"\n");
            TextWindow tw = new TextWindow(getTitle(), "level\t"+vheading+"\tcount", sb.toString(), 200, 400);
        } else {
            for (int i=0; i<stats.nBins; i++)
                sb.append(IJ.d2s(cal.getCValue(stats.histMin+i*stats.binSize), digits)+"\t"+histogram[i]+"\n");
            TextWindow tw = new TextWindow(getTitle(), vheading+"\tcount", sb.toString(), 200, 400);
        }
    }
*/


/*	
	 protected void showList() {
        StringBuffer sb = new StringBuffer();
        String vheading = stats.binSize==1.0?"value":"bin start";
        if (cal.calibrated() && !cal.isSigned16Bit()) {
            for (int i=0; i<stats.nBins; i++)
                sb.append(i+"\t"+IJ.d2s(cal.getCValue(stats.histMin+i*stats.binSize), digits)+"\t"+histogram[i]+"\n");
            TextWindow tw = new TextWindow(getTitle(), "level\t"+vheading+"\tcount", sb.toString(), 200, 400);
        } else {
            for (int i=0; i<stats.nBins; i++)
                sb.append(IJ.d2s(cal.getCValue(stats.histMin+i*stats.binSize), digits)+"\t"+histogram[i]+"\n");
            TextWindow tw = new TextWindow(getTitle(), vheading+"\tcount", sb.toString(), 200, 400);
        }
    }
*/
}









class PixDataShufflerPrefs {
	String fileName;
	Properties props;

	PixDataShufflerPrefs(String fileName) {//constructor
		this.fileName = fileName;
		props = new Properties();
		if(props != null) loadPrefs();
	}

	void loadPrefs() {
		String prefFile = Prefs.getHomeDir() + "\\Plugins\\" + fileName;
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(prefFile));
			props.load(in);
			in.close();
		}
		catch(Exception e) {
			//IJ.write("PixData_Shuffler" + ": Error loading preferences file");
		}
	}

	void storePreferences() {
		String prefFile = Prefs.getHomeDir() + "\\Plugins\\" + fileName;
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(prefFile));
			props.store(out, "PixData_Shuffler Preferences");
			out.close();
		}
		catch(Exception e) {
			IJ.write("PixData_Shuffler" + ": Error saving preferences file");
		}
	}

	String getString(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

    void putString(String key, String value) {
		props.setProperty(key, value);
    }

	public boolean getBoolean(String key, boolean defaultValue) {
        if (props==null) return defaultValue;
        String s = props.getProperty(key);
        if (s==null) return defaultValue;
        else return s.equals("true");
    }

    void putBoolean(String key, boolean b) {
		String value = "false";
		if(b) value = "true";
		props.setProperty(key, value);
    }

    int getInt(String key, int defaultValue) {
        if (props==null) //workaround for Netscape JIT bug
            return defaultValue;
        String s = props.getProperty(key);
        if (s!=null) {
            try {
                return Integer.decode(s).intValue();
            } catch (NumberFormatException e) {IJ.write(""+e);}
        }
        return defaultValue;
    }

    void putInt(String key, int value) {
		props.setProperty(key, Integer.toString(value));
    }
	
    double getDouble(String key, double defaultValue) {
        if (props==null) return defaultValue;
        String s = props.getProperty(key);
        Double d = null;
        if (s!=null) {
            try {d = new Double(s);}
            catch (NumberFormatException e){d = null;}
            if (d!=null) return(d.doubleValue());
        }
        return defaultValue;
    }

    void putDouble(String key, double value) {
		props.setProperty(key, Double.toString(value));
    }
	

 } // Preferences_
