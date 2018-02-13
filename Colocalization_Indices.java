/*
Colocalization_Indices ImageJ PlugInFilter version 1.0
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


<< Instructions >>
ImageJ PlugInFilter "Colocalization_Indices.java" is designed for the quantitative colocalization 
analysis of two immunoreactivities in double immunofluorescence staining.  This PlugInFilter 
calculates correlation coefficients (CC, or Pearson's r), intensity correlation quotient 
(ICQ), and overlap coefficient (OC) from two channels (red and green, in most cases) in 
8bit RGB, 12bit RGB, or 16bit RGB images.  This supports image stacks and ROI (region of
interest), and can make a scatter plot graph.

Installation
Put this file to the plugins folder or subfolder, and compile it with the "Compile and Run" 
command. Restart ImageJ to add the "Colocalization_Indices" command to the Plugins menu.

Procedures
1:	"Colocalization_Indices.java" requires at least two grayscale (8bit, 8bit color, 12 bit, or 16bit)
	images as input.  First, you thus need to separate or split channels of single RGB 
	images (or stacks).  This can be done simply by a menu command "RGB split" in "Image 
	-> Color" for standard 8bit RGB images or stacks.  When you handle 12bit or 16bit 
	RGB images, you should choose a menu command "Convert stack into images" in "Image 
	-> Stack". 

2:	Before using this PlugInFilter, you need to determine lower threshold (i.e. maximum 
	noise level) of red and green channels, separately.  If you want to determine the 
	threshold simply by visual inspection, a menu command "Threshold..." in "Image -> 
	Adjust" is suitable for this purpose.
		
3:	Run "Colocalization_Indices.java" by choosing it from "Plugins" menu.

4:	Select red image (stack) and green image (green) from the drop-down menus. 

5:	Input lower threshold values (maximum noise level) for red and green images (stacks)
	into boxes.
	
6:	When you use this plug-in for the first time in a session (the first run), you need
	to check "Initialize the Results Window", to make headers in Results window. Note that
	this option clears all the data in the current Results window! 

7: 	Then click "OK". If a dialog ask you bit depth of images, select the appropriate one
	and click "OK". Results of calculation will appear on Results window. 

Options
1:	"Green-Red order?" function works on Windows but not on Mac OS X. This option may be 
	helpful when the order of image names listed in the drop-down menus is stable. In 
	default, this Plug-in shows names of images in Red-Green order (suitable for pictures 
	taken with Zeiss microscopes). With "Green-Red order?" option checked, it shows in 
	Green-Red order (maybe suitable for pictures taken with Leica microscopes). This may
	accelerate your work.

2:	If your have to close the image windows each time after calculation, please check
	"Close Red image window after calculation" and "Close Red image window after 
	calculation" options.
  
3:	"Make Scatter Plot" function enables you to get a scatter plot for the two images of 
	interest (1024x1024 pixels in 16bit grayscale applied a LUT). If you check "... in High
	Resolution?", the plots will be provided in full resolution. But usually it requires 
	much memories.

4:	 "Show Time Spent for the Calculation" will display the time in the Results window. 

5:	"Show Instructions for the Calculated Data" will provide you instructions for data 
	shown in Results window. This is a help function.
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




public class Colocalization_Indices implements PlugInFilter {

private static boolean displayCounts;
private ImagePlus imp1, imp2;
int wList[];
private String titles[];// titles of images
private String bitdepth1;// bit depth of images
private String bitdepth2[] = {"12bit", "16bit"};// bit depth of images
private	int sat = 255;
static String title = "Colocalization Indices";
private int p = 0;//lower threshold value for the red image
private int q = 0;//lower threshold value for the green image
private ImagePlus imp;
private boolean doplot, highRes, head, showtime, instruction, orderGR, close1, close2;
long time1, time2;
byte[] maskArray;
private ImageProcessor mask;
private static String PREFS  = "PearsonsrPrefs.txt";
//private String order[] = {"R-G","G-R"};// order if channels



public int setup(String arg, ImagePlus imp){
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
	
	Roi roi = imp1.getRoi();
	correlate(imp1, imp2, roi);
	
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
	
	
  }// public void run(


public boolean showDialog() {
    PearsonPrefs prefs = new PearsonPrefs(PREFS);
    getPreferences(prefs);
	for (;;) {
		GenericDialog param = new GenericDialog(title, IJ.getInstance());// GenericDialog object

		if (orderGR) {// for Leica
			param.addChoice("Red Image (stack)", titles, titles[1]);
			param.addChoice("Green Image (stack)", titles, titles[0]);
		}
		else {// for Zeiss
			param.addChoice("Red Image (stack)", titles, titles[0]);
			param.addChoice("Green Image (stack)", titles, titles[1]);
		}
		param.addMessage("Lower Threshold (max noise level) for ...\n");
		param.addNumericField("Red Image (stack)*", p, 0);
		param.addNumericField("Green Image (stack)*", q, 0);
		param.addMessage("   *(0-255) for 8bit, (0-4095) for 12bit, (0-65535) for 16bit");
		param.addCheckbox("Initialize the Results Window*", false);
		param.addMessage("      *\"Initialize the Results Window\" clears all the data in the results\n" +
		"      window!  But the first run requires this option.");
		param.addCheckbox("Green-Red order?** (for Leica)", orderGR);
		param.addMessage("      **If image channels are named in Green-Red order, check this option.\n" +
		"      Red-Green order images (ex. Zeiss) don't require this option.\n" +
		"      The change will be reflected when you use this plugin again.");
		param.addCheckbox("Close Red image window after calculation", close1);
		param.addCheckbox("Close Green image window after calculation", close2);

		param.addCheckbox("Make Scatter Plot", doplot);
		param.addCheckbox("... in High Resolution?", highRes);
		
		param.addCheckbox("Show Time Spent for the Calculation", showtime);
		param.addCheckbox("Show Instructions for the Calculated Data", instruction);

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
		close1 = param.getNextBoolean();
		close2 = param.getNextBoolean();
		doplot = param.getNextBoolean();
		highRes = param.getNextBoolean();

		showtime = param.getNextBoolean();
		instruction = param.getNextBoolean();

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
		
		if (!(imp1.getStackSize()==imp2.getStackSize())){
			IJ.showMessage(title, "Red and Green stacks must have the same number of slices");
			continue;
		}//if ((imp1.getType()!= 
		
		
		if((imp1.getType()==imp1.GRAY8) && (imp2.getType()==imp1.GRAY8)){
			bitdepth1 = "8bit";
		}
		
		if ((imp1.getType()==imp1.GRAY16) && (imp2.getType()==imp1.GRAY16)){
				if(!showDialog2())
					continue;
		}
		
		if (bitdepth1.equals("8bit")) sat = 255;
		else if (bitdepth1.equals("12bit")) sat = 4095;
		else if (bitdepth1.equals("16bit")) sat = 65535;

		if (((p<0) || (p>sat))||((q<0) || (q>sat))) {
			IJ.showMessage(title, "Lower threshold value must be 0-" + sat +" for " + bitdepth1 +" images.");
			continue;
		}//if ((p<=0) .... working
		      
		savePreferences(prefs);
		break;
	}// for

	return true;
    }//    public boolean showDialog() 

public boolean showDialog2() {
	ImageProcessor ip12of1 = imp1.getProcessor();
	ImageProcessor ip12of2 = imp2.getProcessor();
	if ((ip12of1.getMax()>4095)||(ip12of2.getMax()>4095)){
		bitdepth1 = "16bit";
		return true;
	}
	else {
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

void getPreferences(PearsonPrefs prefs) {
    p = prefs.getInt("Red Lower Threshold", 0);
    q = prefs.getInt("Green Lower Threshold", 0);
    head = prefs.getBoolean("Initialize", false);
	orderGR = prefs.getBoolean("Green-Red order", false);
	close1 = prefs.getBoolean("close red window", false);
	close2 = prefs.getBoolean("close green window", false);
	
    doplot = prefs.getBoolean("Do Plot", false);
    highRes = prefs.getBoolean("High Resolution Plot?", false);
	
    showtime = prefs.getBoolean("Show Time", false);
    instruction = prefs.getBoolean("Instruction", false);
}//  void getPreferences(Preferences prefs)



void savePreferences(PearsonPrefs prefs) {
    prefs.putInt("Red Lower Threshold", p);
    prefs.putInt("Green Lower Threshold", q);
	prefs.putBoolean("Initialize", head);
	prefs.putBoolean("Green-Red order", orderGR);
    prefs.putBoolean("close red window", close1);
    prefs.putBoolean("close green window", close2);
		
    prefs.putBoolean("Do Plot", doplot);
    prefs.putBoolean("High Resolution Plot?", highRes);
	
    prefs.putBoolean("Show Time", showtime);
    prefs.putBoolean("Instruction", instruction);
    prefs.storePreferences();
}// void savePreferences(PearsonPrefs prefs)


public void correlate(ImagePlus imp3, ImagePlus imp4, Roi roi) {
	IJ.showStatus(title + ": calculating...");

	time1 = System.currentTimeMillis();
	
	ImageProcessor ip3 = imp3.getProcessor();
	ImageProcessor ip4 = imp4.getProcessor();
	
	int plotSizeX, plotSizeY;
	
	if (highRes) {
		ImageStatistics stats3 = imp3.getStatistics();
		ImageStatistics stats4 = imp4.getStatistics();
		plotSizeX = (int)stats3.max;
		plotSizeY = (int)stats4.max;	
	}
	else if(bitdepth1.equals("12bit") || bitdepth1.equals("16bit") ) {
		plotSizeX = 1024;
		plotSizeY = 1024;
	}
	else {
		plotSizeX = 256;
		plotSizeY = 256;
	}
	
	ImageProcessor plot = new FloatProcessor(plotSizeX, plotSizeY);// used for 32 bit floating point images
	ImageProcessor plot16bit = new ShortProcessor(plotSizeX, plotSizeY);// used for 16 bit gray scale images

	int nslices = imp3.getStackSize();
	int width = imp3.getWidth();
	int height = imp3.getHeight();

	int z1, z2, z3, z4, count;

	int k=0;// k = {0, 1, 2, 3, 4}
	
	double sum[][] = new double[5][5];
	for (int i =0; i<=4; i++) {
		for (int j=0; j<=4; j++) {
			sum[i][j] = 0;
		}
	}// sum[][] default is 0
	
	/*	
	sum[0][]... sumX
	sum[1][]...	sumXY
	sum[2][]...	sumXX
	sum[3][]...	sumYY
	sum[4][]...	sumY 
	*/
	
	int n[]= {0, 0, 0, 0, 0};// n[k]
	
	/*	
	n[0]... ( R==sat || R==0 || G==sat || G==0)
	n[1]...	( 0<R<p && 0<G<q)
	n[2]...	( R>p &&  0<G<q )
	n[3]...	( 0<R<p && G>q ) 
	n[4]...	( R>p && G>q ) 
	*/
	

	Rectangle rroi = ip3.getRoi();
	if (roi!=null) { 
		ip3.setRoi(roi);// necessary
	}
	ip3.reset(ip3.getMask()); 
	int rx, ry, rh, rw;
	maskArray = ip3.getMaskArray();// 
	
	/* // check 
	if (maskArray == null) IJ.showMessage(title, "3 Mask null");
	else IJ.showMessage(title, "3 Mask is on. " + maskArray[1]);
	*/
	if (rroi != null) {
		rx = rroi.x;
		ry = rroi.y;
		rw = rroi.width;
		rh = rroi.height;
	}
	else {
		rx = 0;
		ry = 0;
		rw = width;
		rh = height;
	}

	for (int i=1; i<=nslices; i++) {
		IJ.showStatus(title+ "; calculating...");
		IJ.showProgress(i, nslices);
		imp3.setSlice(i);
		imp4.setSlice(i);
		for (int y=ry, my=0; y<(ry+rh); y++, my++) {
			int mi = my * rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (maskArray==null || maskArray[mi++]!=0) {
					
					z1 = (int)ip3.getPixel(x,y); // z-value of pixel (x,y)in red image	
					z2 = (int)sat-ip4.getPixel(x,y); // green value is plotted as y axis in plot, but (0,0) is (0,255) in plot.  
					z3 = (int)ip4.getPixel(x,y); // z-value of pixel (x,y)in green image
					z4 = (int)plotSizeY-ip4.getPixel(x,y); 
						
					if ( z1==0 || z3==0 || z1==sat || z3==sat )		k=0;
					else if ((z1>0 && z1<=p) && (z3>0 && z3<=q))	k=1;
					else if ((z1>p && z1<sat) && (z3>0 && z3<=q))	k=2;
					else if ((z1>0 && z1<=p) && (z3>q && z3<sat))	k=3;
					else if ((z1>p && z1<sat) && (z3>q && z3<sat))	k=4;

					n[k]++;// n[k]=n[k]+1
					
					sum[0][k] += z1;// sum[0][k] = sum[0][k] + z1
					sum[1][k] += (z1 * z3);
					sum[2][k] += (z1 * z1);
					sum[3][k] += (z3 * z3);
					sum[4][k] += z3;

					if (highRes){
						count = plot.getPixel(z1, z4);
						if (count<65535) count++;/// What is this?
						plot.putPixel(z1, z4, count);//void putPixel(int x, int y, int value)
						plot16bit.putPixel(z1, z4, count);
					}
					else {
						count = plot.getPixel((int)z1/((sat+1)/plotSizeX), (int)z2/((sat+1)/plotSizeY));
						if (count<65535) count++;/// What is this?
						plot.putPixel((int)z1/((sat+1)/plotSizeX), (int)z2/((sat+1)/plotSizeY), count);//void putPixel(int x, int y, int value)
						plot16bit.putPixel((int)z1/((sat+1)/plotSizeX), (int)z2/((sat+1)/plotSizeY), count);
					}
				}//if (maskArray==
			}// for (int x=0; x<width; x++) 
		}//	for (int y=0; y<height; y++)
	}//	for (int i=1; i<=nslices; i++)  

	/*
	SUM[][0], N[0], r[0]... total
	SUM[][1], N[1], r[1]... ( 0<R<sat ) && ( 0<G<sat )
	SUM[][2], N[2], r[2]... ( p<R<sat && 0<G<sat )
	SUM[][3], N[3], r[3]... ( 0<R<sat && q<G<sat )
	SUM[][4], N[4], r[4]... ( p<R<sat || q<G<sat )
	*/
	
	double SUM[][] = new double[5][5];
	for (int i =0; i < SUM.length; i++) {
		for (int j=0; j<SUM[i].length; j++) {
			SUM[i][j] = 0;
		}
	}// SUM[][] default is 0
	
	int N[]= new int[5];
	//N[]={0, 0, 0, 0, 0};
	
	N[0] = n[0] + n[1] + n[2] + n[3] + n[4];
	N[1] = n[1] + n[2] + n[3] + n[4];
	N[2] = n[2] + n[4];
	N[3] = n[3] + n[4]; 
	N[4] = n[2] + n[3] + n[4];
	
	for (int i=0; i<=4; i++) SUM[i][0] = sum[i][0] + sum[i][1] + sum[i][2] + sum[i][3] + sum[i][4];
	for (int i=0; i<=4; i++) SUM[i][1] = sum[i][1] + sum[i][2] + sum[i][3] + sum[i][4]; 
	for (int i=0; i<=4; i++) SUM[i][2] = sum[i][2] + sum[i][4];
	for (int i=0; i<=4; i++) SUM[i][3] = sum[i][3] + sum[i][4];
	for (int i=0; i<=4; i++) SUM[i][4] = sum[i][2] + sum[i][3] + sum[i][4];

	double pearsons1[] = new double[5];
	double pearsons2[] = new double[5];
	double pearsons3[] = new double[5];
		
	double meanX[] = new double[5];
	double meanY[] = new double[5];
	double covXY[] = new double[5];
	double sdX[] = new double[5];
	double sdY[] = new double[5];
	double r[] = new double[5];
	
	double overlapC[] = new double[5];
	double M1p, M2q;
	

	
	for (int i=0; i<=4; i++){	
		meanX[i] = SUM[0][i]/N[i];// mean of red
		meanY[i] = SUM[4][i]/N[i];// mean of green
		covXY[i] = (SUM[1][i] - (SUM[0][i]*SUM[4][i]/N[i]))/N[i];// covariance of red and green
		sdX[i] = Math.sqrt((SUM[2][i] - (SUM[0][i]*SUM[0][i]/N[i]))/N[i]);// standard deviation of red
		sdY[i] = Math.sqrt((SUM[3][i] - (SUM[4][i]*SUM[4][i]/N[i]))/N[i]);// standard deviation of green
		r[i] = covXY[i]/(sdX[i]*sdY[i]);// Pearson's r for red and green
		
		overlapC[i] = SUM[1][i]/Math.sqrt(SUM[2][i]*SUM[3][i]); //Manders' overlap coefficient
	}
	
	M1p = (sum[0][3]+sum[0][4])/(sum[0][1]+sum[0][2]+sum[0][3]+sum[0][4]);
	M2q = (sum[4][2]+sum[4][4])/(sum[4][1]+sum[4][2]+sum[4][3]+sum[4][4]);// Manders' colocalization coefficients
	// Following points are modified from the original calculation (Manders et al., 1992).
	// This modification is reasonable to apply the coefficients to our analysis.
	// (i)  This calculation exclude pixels that have 0 or saturated intensity (since they have no linear information).
	// (ii) Thershold value p and q is used, instead of 0 intensity, to determin positive pixels.
	// Thus, if p and q are both 0, M1p and M2q are always both 1.0.
	
	
	double numeratorICQ[] = new double[5];
	
	for (int i=1; i<=nslices; i++) { // ICQ calcuration for each fraction
		IJ.showStatus(title+ "; calculating...");
		IJ.showProgress(i, nslices);
		imp3.setSlice(i);
		imp4.setSlice(i);
		for (int y=ry, my=0; y<(ry+rh); y++, my++) {
			int mi = my * rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (maskArray==null || maskArray[mi++]!=0) {
					
					z1 = (int)ip3.getPixel(x,y); // z-value of pixel (x,y)in red image	
					z3 = (int)ip4.getPixel(x,y); // z-value of pixel (x,y)in green image
						
					if ( z1==0 || z3==0 || z1==sat || z3==sat )	{
						//k=0;
						if ((z1-meanX[0])*(z3-meanY[0]) > 0) numeratorICQ[0]++;
						else if ((z1-meanX[0])*(z3-meanY[0]) < 0) numeratorICQ[0]--;
					}	
					else if ((z1>0 && z1<=p) && (z3>0 && z3<=q)) {
						//k=1;
						if ((z1-meanX[0])*(z3-meanY[0]) > 0) numeratorICQ[0]++;
						else if ((z1-meanX[0])*(z3-meanY[0]) < 0) numeratorICQ[0]--;
						if ((z1-meanX[1])*(z3-meanY[1]) > 0) numeratorICQ[1]++;
						else if ((z1-meanX[1])*(z3-meanY[1]) < 0) numeratorICQ[1]--;
					}
					else if ((z1>p && z1<sat) && (z3>0 && z3<=q)) {
						//k=2;
						if ((z1-meanX[0])*(z3-meanY[0]) > 0) numeratorICQ[0]++;
						else if ((z1-meanX[0])*(z3-meanY[0]) < 0) numeratorICQ[0]--;
						if ((z1-meanX[1])*(z3-meanY[1]) > 0) numeratorICQ[1]++;
						else if ((z1-meanX[1])*(z3-meanY[1]) < 0) numeratorICQ[1]--;
						if ((z1-meanX[2])*(z3-meanY[2]) > 0) numeratorICQ[2]++;
						else if ((z1-meanX[2])*(z3-meanY[2]) < 0) numeratorICQ[2]--;
						if ((z1-meanX[4])*(z3-meanY[4]) > 0) numeratorICQ[4]++;
						else if ((z1-meanX[4])*(z3-meanY[4]) < 0) numeratorICQ[4]--;
					}
					else if ((z1>0 && z1<=p) && (z3>q && z3<sat)) {
						//k=3;
						if ((z1-meanX[0])*(z3-meanY[0]) > 0) numeratorICQ[0]++;
						else if ((z1-meanX[0])*(z3-meanY[0]) < 0) numeratorICQ[0]--;
						if ((z1-meanX[1])*(z3-meanY[1]) > 0) numeratorICQ[1]++;
						else if ((z1-meanX[1])*(z3-meanY[1]) < 0) numeratorICQ[1]--;
						if ((z1-meanX[3])*(z3-meanY[3]) > 0) numeratorICQ[3]++;
						else if ((z1-meanX[3])*(z3-meanY[3]) < 0) numeratorICQ[3]--;
						if ((z1-meanX[4])*(z3-meanY[4]) > 0) numeratorICQ[4]++;
						else if ((z1-meanX[4])*(z3-meanY[4]) < 0) numeratorICQ[4]--;						
					}
					else if ((z1>p && z1<sat) && (z3>q && z3<sat)){
						//k=4;
						if ((z1-meanX[0])*(z3-meanY[0]) > 0) numeratorICQ[0]++;
						else if ((z1-meanX[0])*(z3-meanY[0]) < 0) numeratorICQ[0]--;
						if ((z1-meanX[1])*(z3-meanY[1]) > 0) numeratorICQ[1]++;
						else if ((z1-meanX[1])*(z3-meanY[1]) < 0) numeratorICQ[1]--;
						if ((z1-meanX[2])*(z3-meanY[2]) > 0) numeratorICQ[2]++;
						else if ((z1-meanX[2])*(z3-meanY[2]) < 0) numeratorICQ[2]--;
						if ((z1-meanX[3])*(z3-meanY[3]) > 0) numeratorICQ[3]++;
						else if ((z1-meanX[3])*(z3-meanY[3]) < 0) numeratorICQ[3]--;
						if ((z1-meanX[4])*(z3-meanY[4]) > 0) numeratorICQ[4]++;
						else if ((z1-meanX[4])*(z3-meanY[4]) < 0) numeratorICQ[4]--;
					}
					/*
					n[k]++;// n[k]=n[k]+1
					
					sum[0][k] += z1;// sum[0][k] = sum[0][k] + z1
					sum[1][k] += (z1 * z3);
					sum[2][k] += (z1 * z1);
					sum[3][k] += (z3 * z3);
					sum[4][k] += z3;
					*/

				}//if (maskArray==
			}// for (int x=0; x<width; x++) 
		}//	for (int y=0; y<height; y++)
	}//	for (int i=1; i<=nslices; i++) 

	double ICQ[] = new double[5];
	
	ICQ[0] = numeratorICQ[0]/(2*N[0]);
	ICQ[1] = numeratorICQ[1]/(2*N[1]);
	ICQ[2] = numeratorICQ[2]/(2*N[2]);
	ICQ[3] = numeratorICQ[3]/(2*N[3]);
	ICQ[4] = numeratorICQ[4]/(2*N[4]);
	
	time2 = System.currentTimeMillis();	

	/*	
	sum[0][]... sumX
	sum[1][]...	sumXY
	sum[2][]...	sumXX
	sum[3][]...	sumYY
	sum[4][]...	sumY 
	*/

	DecimalFormat df = new DecimalFormat("##0.000000");
	

	if (head) {
		IJ.setColumnHeadings(
		"red image\t" +
		"green image\t" +
		"bit depth\t" +
		"total pixels N\t" +
		"rMax noise p\t" +
		"gMax noise q\t" +
		"saturated N\t" +
		"M1 (g>q)\t" +
		"M2 (r>p)\t" +		
		
		"-saturated N\t" +
		"-s R mean\t" +
		"-s G mean\t" +
		"-s R sd\t" +
		"-s G sd\t" +
		"-s cov\t" +
		"-s CC\t" +
		"-s overlap C\t" +
		"-s ICQ\t" +
		
		"p<red<"+sat+" N\t" +
		"r>p R mean\t" +
		"r>p G mean\t" +
		"r>p R sd\t" +
		"r>p G sd\t" +
		"r>p cov\t" +
		"r>p CC\t" +
		"r>p overlap C\t" +
		"r>p ICQ\t" +		
		
		"q<green<"+sat+" N\t" +
		"g>q R mean\t" +
		"g>q G mean\t" +
		"g>q R sd\t" +
		"g>q G sd\t" +
		"g>q cov\t" +
		"g>q CC\t" +
		"g>q overlap C\t" +
		"g>q ICQ\t" +		
		
		"p,q<red,green<"+sat+" N\t" +
		"rg R mean\t" +
		"rg G mean\t" +
		"rg R sd\t" +
		"rg G sd\t" +
		"rg cov\t" +	
		"rg CC\t" +
		"rg overlap C\t" +
		"rg ICQ\t" +
		
		"total N\t" +
		"t R mean\t" +
		"t G mean\t" +
		"t R sd\t" +
		"t G sd\t" +
		"t cov\t" +	
		"t CC\t" +
		"t overlap C\t" +
		"t ICQ\t"
		);
	}
	if (showtime){
	IJ.write(// Display time
		(time2-time1) + " milli seconds\n");
	}
	
	String title3, title4;
	if (roi==null) {
		title3 = imp3.getTitle();
		title4 = imp4.getTitle();
	}
	else {
		title3 = imp3.getTitle() + " -ROI";
		title4 = imp4.getTitle() + " -ROI";
	}
	
	IJ.write(// Display results		
		imp3.getTitle() + "\t" + 
		imp4.getTitle() + "\t" + 
		bitdepth1 + "\t" +
		N[0] + "\t" +
		p + "\t" + // red max noise level
		q + "\t" + // greern max noise level
		n[0] + "\t" + // saturated pixels
		df.format(M1p) + "\t" + 
		df.format(M2q) + "\t" + 
		
		N[1] + "\t" + // total -saturated pixels
		df.format(meanX[1]) + "\t" +
		df.format(meanY[1]) + "\t" +
		df.format(sdX[1]) + "\t" +
		df.format(sdY[1]) + "\t" +
		df.format(covXY[1]) + "\t" +
		df.format(r[1]) + "\t" +
		df.format(overlapC[1]) + "\t" +
		df.format(ICQ[1]) + "\t" +
		
		N[2] + "\t" + // red >p
		df.format(meanX[2]) + "\t" +
		df.format(meanY[2]) + "\t" +
		df.format(sdX[2]) + "\t" +
		df.format(sdY[2]) + "\t" +
		df.format(covXY[2]) + "\t" +
		df.format(r[2]) + "\t" +
		df.format(overlapC[2]) + "\t" +
		df.format(ICQ[2]) + "\t" +
		
		N[3] + "\t" + // green >q
		df.format(meanX[3]) + "\t" +
		df.format(meanY[3]) + "\t" +
		df.format(sdX[3]) + "\t" +
		df.format(sdY[3]) + "\t" +
		df.format(covXY[3]) + "\t" +
		df.format(r[3]) + "\t" +
		df.format(overlapC[3]) + "\t" +
		df.format(ICQ[3]) + "\t" +		
		
		N[4] + "\t" + // (red >p)||(green >q)
		df.format(meanX[4]) + "\t" +
		df.format(meanY[4]) + "\t" +
		df.format(sdX[4]) + "\t" +
		df.format(sdY[4]) + "\t" +
		df.format(covXY[4]) + "\t" +
		df.format(r[4]) + "\t" +
		df.format(overlapC[4]) + "\t" +
		df.format(ICQ[4]) + "\t" +			
		
		N[0] + "\t" + // total pixels
		df.format(meanX[0]) + "\t" +
		df.format(meanY[0]) + "\t" +
		df.format(sdX[0]) + "\t" +
		df.format(sdY[0]) + "\t" +
		df.format(covXY[0]) + "\t" +
		df.format(r[0]) + "\t" +
		df.format(overlapC[0]) + "\t" +
		df.format(ICQ[0])
		);// IJ.write
	
	if (doplot == true){
		plot.invertLut();
		plot.resetMinAndMax();
		new ImagePlus("Correlation Plot", plot16bit).show();
		ImagePlus imp5 = WindowManager.getCurrentImage();
		plot16bit.resetMinAndMax();
	
		IJ.run("Enhance Contrast", "saturated=0.5 equalize");
		
		IJ.run("Fire");// applying LUT "Fire"
		imp5.setTitle(imp5.getTitle() + " plot");
	}
	
	if (instruction == true){
		IJ.write ( 
			"\n<<Instructions>>\n" +

			"The data contain 54 values.\n \n" +
			
			"\"red image\" and \"green image\" show the pair of image file names used for this calculation.\n" +
			"\"bit depth\" shows image type (8bit, 12bit, or 16bit).\n" +
			"\"total pixels N\" shows the total number of pixels used for this calculation.\n" +
			"\"rMax noise p\" and \"gMax noise q\" show user determined lower threshold\n" +
			"    (maximum noise) value of red and green, respectively, images (stacks).\n" +
			"\"saturated N\" shows number of saturated pixels, which has saturated value\n" +
			"    (="+sat+") in \'red or green\' image.\n \n" +
			"\"M1 (g>q)\" and \"M2 (r>p)\" show Manders\'s colocalization coefficient (Manders et al., 1993) \n \n" +
	
			"The following is consist of 5 data groups.\n" +
			"The 5 data groups are ...\n" +
			"    \'-saturated\' (or \'-s\')    ...Pixels for {(red value<" +sat+ ") AND (green value<" +sat+ ")}\n" +
			"    \'p<red<"+sat+"\' (or \'r>p\')    ...Pixels for {p<red value<" +sat+ "}\n" +
			"    \'q<green<"+sat+"\' (or \'g>q\')    ...Pixels for {q<green value<" +sat+ "}\n" +
			"    \'p,q<red,green<"+sat+"\' (or \'rg\')    ...Pixels for {(p<red value<" +sat+ ") OR (q<green value<" +sat+ ")}\n" +
			"    \'total\' (or \'t\')    ...Total pixels\n\n" +
			
			"Each data group contains 9 statistic numbers as follows ...\n" +
			"    \'N\'    ...Number of Pixels.\n" +
			"    \'R mean\'   ...Mean of red pixel value.\n" +
			"    \'G mean\'   ...Mean of green pixel value.\n" +			
			"    \'R sd\'    ...Standard Deviation of red pixel value.\n" +
			"    \'G sd\'    ...Standard Deviation of green pixel value.\n" +
			"    \'cov\'    ...Covariance of red and green pixel values.\n" +
			"    \'CC\'    ...Correlation Coefficient (CC, or Pearson\'s r; Manders et al., 1992) for red and green pixel values.\n" +
			"    \'overlap C\'    ...Manders\'s Overlap Coefficient (OC; Manders et al., 1993) for red and green pixel values.\n" +
			"    \'ICQ\'    ...Intensity Correlation Quotient (ICQ; Li et al., 1994) for red and green pixel values.\n\n" +
			"Li Q, Lau A, Morris TJ, Guo L, Fordyce CB, Stanley EF (2004) J Neurosci 24:4070-4081. \n" +
			"Manders EM, Stap J, Brakenhoff GJ, van Driel R, Aten JA (1992) J Cell Sci 103 (Pt 3):857-862. \n" +
			"Manders EMM, Verbeek FJ, Aten JA (1993) J Microsc 169:375-382. \n"
			
		);
	}
	
	IJ.selectWindow("Results");
 }//public void correlate
}//public class 








class PearsonPrefs {
	String fileName;
	Properties props;

	PearsonPrefs(String fileName) {
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
			//IJ.write("Pearson\'s r" + ": Error loading preferences file");
		}
	}

	void storePreferences() {
		String prefFile = Prefs.getHomeDir() + "\\Plugins\\" + fileName;
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(prefFile));
			props.store(out, "Pearsons r Preferences");
			out.close();
		}
		catch(Exception e) {
			IJ.write("Pearson\'s r" + ": Error saving preferences file");
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

