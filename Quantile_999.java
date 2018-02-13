/*
Quantile_999 ImageJ PlugInFilter version 1.0
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
This Plugin calcurates 999/1000 quantile and 99/100 quantile (= pecentile) of the 
fluorescence intensity in an image. This Plugin suppors ROI and stack images.

Installation
Put this file to the plugins folder or subfolder, and compile it with the "Compile and Run" 
command. Restart ImageJ to add the "Quantile_999" command to the Plugins menu.

Procedures
1:	 (Optional) Set ROI in the image of interest

2:	Select and check the image name(s) from those listed below. 

3:	When you use this plug-in for the first time in a session (the first run), you need
	to check "Initialize the Results Window", to make headers in Results window. Note that
	this option clears all the data in the current Results window! 

4:	 "Show Time Spent for the Calculation" will display the time in the Results window. 

5:	If your want to close the image windows each time after calculation, please check
	"Close image window after calculation" option.
	
6:	Click OK. The calculation may need several minutes.



*/
import java.awt.*;
import java.io.*;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.text.*;
import ij.plugin.PlugIn;
import java.text.DecimalFormat; 
import ij.plugin.filter.PlugInFilter;
import ij.plugin.*;
import ij.io.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;
import ij.process.ImageConverter.*;
import ij.process.ImageProcessor.*;


public class Quantile_999 implements PlugInFilter {

private ImagePlus imp1, imp2;
int wList[];
private String titles[];// titles of images
private String bitdepth;// bit depth of images
private String bitdepth1[] = {"12bit", "16bit"};// bit depth of images
static String pluginname = "Quantile_999";//version information
private	int maxp999, maxp995, maxp99, maxp95, maxp90;

private ImagePlus imp;
private boolean head, showtime, close;
long time1, time2;


public int setup(String arg, ImagePlus imp){
	return DOES_ALL;
}


public void run(ImageProcessor ip) {
	wList = WindowManager.getIDList();
	if (wList==null) {
		IJ.showMessage(pluginname, "There must be at least one window open.");
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
	
		
	GenericDialog param = new GenericDialog(pluginname, IJ.getInstance());// GenericDialog object


	param.addCheckbox("Initialize the Results Window*", false);
	param.addMessage("      *\"Initialize the Results Window\" clears all the data in the results\n" +
	"      window!  But the first run requires this option.");
	param.addCheckbox("Show Time Spent for the Calculation", false);
	param.addCheckbox("Close Windoe after the Calculation", true);
	param.addMessage("Check images for calculation");
	for (int i=0; i<wList.length; i++){ 
		param.addCheckbox(titles[i],true);
	}
	param.showDialog();
	if(param.wasCanceled()) // Cancel button
	  return;
	  
	time1 = System.currentTimeMillis();

	head = param.getNextBoolean();
	showtime = param.getNextBoolean();
	close = param.getNextBoolean();

	if (head) {
		IJ.setColumnHeadings(
			"label\t" +
			//"bit depth\t" +
			"total pixels N\t" +
			"mean\t" +
			"stdev\t" +
			"min\t" +
			"max\t" +
			"N 99.9%\t" +
			"N 99.0%\t" +
			"value 99.9%\t" +
			"value 99.0%\t"
		);
		IJ.write(pluginname + " ... Calculate signal value of pixels at top X % in ranking.\n");
	}
	
	for (int i=0; i<wList.length; i++){ 
		//if (wList.length>1) IJ.showProgress(i, (wList.length-1));
		if (param.getNextBoolean()){
			imp = WindowManager.getImage(wList[i]);
			
			Roi roi = imp.getRoi();
			if (roi==null) 	{
				calculate(imp);
			}
			else {
				/*
				String title1 = imp1.getTitle();
				IJ.selectWindow(title1);
				imp1.unlock();
				IJ.run("Make Inverse");
				IJ.run("Make Inverse");
				IJ.run("Restore Selection");
				calculate(imp1);		
				*/
				
				Rectangle r;
				r = roi.getBounds();
				String title = imp.getTitle();
				IJ.selectWindow(title);
				imp.unlock();
				IJ.run("Make Inverse");
				IJ.run("Make Inverse");
				IJ.run("Duplicate...", "title=[New], duplicate");// The left "duplicate" means "duplicate entire stack" option.
				//IJ.selectWindow(title1);
				ip = imp.getProcessor();
				ip.setRoi(roi);
				//IJ.run("Restore Selection");
				IJ.selectWindow("New");
				imp1 = WindowManager.getCurrentImage();
				imp1.setTitle(title+ ";ROI");
				//ImageProcessor ip2 = imp2.getProcessor();
				//ip2.setRoi(roi);
				IJ.selectWindow(title+ ";ROI");
				IJ.run("Restore Selection");
				IJ.setBackgroundColor(0,0,0);
				IJ.run("Clear Outside", "stack");
				IJ.run("Crop");
				//IJ.run("Select None");
				
				calculate(imp1);
			}  // else
		}		
	}
	
	time2 = System.currentTimeMillis();	
	DecimalFormat df = new DecimalFormat("##0.00");
	int minutes = (int)((time2-time1)/(1000*60));
	double seconds = ((double)(time2-time1))/1000-minutes*60;

	if (showtime){
		IJ.write(// Display time
		minutes + " min " + df.format(seconds) + " sec\n");
	}
	IJ.selectWindow("Results");
	
  }// public void run(



public void calculate(ImagePlus imp0) {
	IJ.showStatus(pluginname + ": calculating...");
	ImageProcessor ip0 = imp0.getProcessor();

	ip0.snapshot();
	ip0.resetMinAndMax();//necesarry!
	
	String title0 = imp0.getTitle();
	/*
	//imp0.unlock();
	IJ.selectWindow(title0);
	IJ.run("Make Inverse");
	IJ.run("Make Inverse");
	*/
	Rectangle roi = ip0.getRoi();
		if (roi ==null) IJ.showMessage(pluginname, "roi null");
	//if (roi != null) title0 = imp0.getTitle() + "; ROI";
	

	int nslices = imp0.getStackSize();
	int width = imp0.getWidth();
	int height = imp0.getHeight();
	int value;

	int rx, ry, rh, rw;
	ImageStatistics stats = imp0.getStatistics();
	
	byte[] mask = ip0.getMaskArray();// This command must be written after "ImageStatistics stats = imp0.getStatistics();".
	//	if (mask !=null) IJ.showMessage(title, "Mask on");

	if (roi != null) {
            rx = roi.x;
            ry = roi.y;
            rw = roi.width;
            rh = roi.height;
	}
        else {
            rx = 0;
            ry = 0;
            rw = width;
            rh = height;
	}
	
	int fullcount = (stats.pixelCount)*nslices;
	double mean;
	double stdev;
	int min = (int)stats.min;
	int max = (int)stats.max;
	
	if (nslices==1){
		mean = stats.mean;
		stdev = stats.stdDev;
	}
	else {
	//if (mask != null) IJ.showMessage(title, "Mask is not null.");
	//else if (mask == null) IJ.showMessage(title, "Mask is null.");
	
		double sumZ=0, sumZZ=0;
		int n=0, Z;
		
		for (int k=1; k<= nslices; k++) {
			imp0.setSlice(k);
			for (int y=ry, my=0; y<(ry+rh); y++, my++) {
				int mi = my * rw;
				for (int x=rx; x<(rx+rw); x++) {
					if (mask==null||mask[mi++]!=0) {
						Z = (int)ip0.getPixel(x,y);	
						n++;
						sumZ += Z;
						sumZZ += (Z*Z);
						if(min > Z) min = Z;
						if(max < Z) max = Z;
					 }
				}
			}
		}
		
		mean = sumZ/n;
		stdev = Math.sqrt((sumZZ - sumZ*sumZ/n)/n);
	}// else

	int p999 = (int)(fullcount*(100-99.9)/100), 
	p995 = (int)(fullcount*(100-99.5)/100),
	p99 = (int)(fullcount*(100-99)/100),
	p95 = (int)(fullcount*(100-95)/100),
	p90 = (int)(fullcount*(100-90)/100);
	
	int j = 0;

	/*
	// for check ROI function, display i in the results window.
	int i = 0;
	for (int k=1; k<= nslices; k++) {
		imp0.setSlice(k);
		for (int y=ry, my=0; y<(ry+rh); y++, my++) {
			int mi = my * rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (mask==null||mask[mi++]!=0) {
					i++;
				 }
			}
		}
	}
	*/
	
	//if (mask != null) IJ.showMessage("Mask is not null.");
	//else if (mask == null) IJ.showMessage("Mask is null.");
	
	XXXX: for(;;) {
		//IJ.showProgress(j, p90);
		//IJ.showStatus(j + " / " + count);// slower
		for (int k=1; k<= nslices; k++) {
			imp0.setSlice(k);
			for (int y=ry, my=0; y<(ry+rh); y++, my++) {
				int mi = my * rw;
				for (int x=rx; x<(rx+rw); x++) {
					if (mask==null||mask[mi++]!=0) {
						value = ip0.getPixel(x,y);//necesarry!
						//IJ.showStatus((int)ip0.getMax() + " in " + j + "/" + count);// for valdating
						if (value==(int)ip0.getMax()){
							ip0.putPixel(x, y, 0);
							j++;
							ip0.resetMinAndMax();//necesarry!
							//if(ip0.getPixel(x,y)==0) IJ.showMessage("OK");// working
							if (j==p999) {
								maxp999=(int)ip0.getMax();
								//IJ.showStatus(pluginname + ": 10% done");
							}
							else if (j==p995) {
								maxp995=(int)ip0.getMax();
								//IJ.showStatus(pluginname + ": 50% done");
							}
							else if (j==p99) {
								maxp99=(int)ip0.getMax();
								//IJ.showStatus(pluginname + ": 100% done");
								break XXXX;
							}
							/*
							else if (j==p95) {
								maxp95=(int)ip0.getMax();
								IJ.showStatus(title + ": 50% done");
							}
							else if (j==p90) {
								maxp90=(int)ip0.getMax();
								IJ.showStatus(title + ": 100% done");
								break XXXX;
							}
							*/
						}
					}
				}
			}
		}
	}

	
	DecimalFormat df = new DecimalFormat("##0.00");
	IJ.write(// Display results		
		title0 + "\t" + 
		//bitdepth + "\t" +
		fullcount + "\t" +
		//n + "\t" +
		df.format(mean) + "\t" +
		df.format(stdev) + "\t" +
		min + "\t" +
		max + "\t" +
		(fullcount-p999) + "\t" +
		(fullcount-p99) + "\t" +
		maxp999 + "\t" +
		maxp99
		//i
	);// IJ.write
	
	IJ.beep();

	ip0.reset();
	ip0.resetMinAndMax();
	
	if (close){
		imp0.changes = false;
		imp0.getWindow().close();
	}
	
	
 }//public void calculaate
}//public class