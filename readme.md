# readme





[TOC]



# Colocalization Indices

### Author

Kouichi Nakamura (Kyoto Univ) 

### Date	

2006/10/06

### Source

Colocalization_Indices.java

### Installation

Download Colocalization_Indices.java to the plugins folder or subfolder, and compile it with the "Compile and Run" command. Restart ImageJ to add the "Colocalization_Indices" command to the Plugins menu. 

### Description

ImageJ PlugInFilter "Colocalization_Indices.java" is designed for the quantitative colocalization 
analysis of two immunoreactivities in double immunofluorescence staining.  This PlugInFilter 
calculates correlation coefficients (CC, or Pearson's r), intensity correlation quotient 
(ICQ), and overlap coefficient (OC) from two channels (red and green, in most cases) in 
8bit RGB, 12bit RGB, or 16bit RGB images.  This supports image stacks and ROI (region of
interest), and can make a scatter plot graph.

#### Procedures

1. "Colocalization_Indices.java" requires at least two grayscale (8bit, 8bit color, 12 bit, or 16bit) images as input.  First, you thus need to separate or split channels of single RGB images (or stacks).  This can be done simply by a menu command "RGB split" in "Image -> Color" for standard 8bit RGB images or stacks.  When you handle 12bit or 16bit RGB images, you should choose a menu command "Convert stack into images" in "Image -> Stack". 


2. Before using this PlugInFilter, you need to determine lower threshold (i.e. maximum noise level) of red and green channels, separately.  If you want to determine the threshold simply by visual inspection, a menu command "Threshold..." in "Image -> Adjust" is suitable for this purpose.		
3. Run "Colocalization Indices" by choosing it from "Plugins" menu.
4. Select red image (stack) and green image (green) from the drop-down menus. 
5. Input lower threshold values (maximum noise level) for red and green images (stacks) into boxes.
6. When you use this plug-in for the first time in a session (the first run), you need to check "Initialize the Results Window", to make headers in Results window. Note that this option clears all the data in the current Results window! 
7. Then click "OK". If a dialog ask you bit depth of images, select the appropriate one and click "OK". Results of calculation will appear on Results window. 

#### Options

1. "Green-Red order?" function works on Windows but not on Mac OS X. This option may be helpful when the order of image names listed in the drop-down menus is stable. In default, this Plug-in shows names of images in Red-Green order (suitable for pictures taken with Zeiss microscopes). With "Green-Red order?" option checked, it shows in Green-Red order (maybe suitable for pictures taken with Leica microscopes). This may accelerate your work.


2. If your have to close the image windows each time after calculation, please check "Close Red image window after calculation" and "Close Red image window after calculation" options.


3. "Make Scatter Plot" function enables you to get a scatter plot for the two images of interest (1024x1024 pixels in 16bit grayscale applied a LUT). If you check "... in High Resolution?", the plots will be provided in full resolution. But usually it requires much memories.


4. "Show Time Spent for the Calculation" will display the time in the Results window. 


5. "Show Instructions for the Calculated Data" will provide you instructions for data shown in Results window. This is a help function.

#### Data Format

The data contain 54 values.



"red image" and "green image" show the pair of image file names used for this calculation.

"bit depth" shows image type (8bit, 12bit, or 16bit).

"total pixels N" shows the total number of pixels used for this calculation.

"rMax noise p" and "gMax noise q" show user determined lower threshold (maximum noise) value of red and green, respectively, images (stacks).

"saturated N" shows number of saturated pixels, which has saturated value (=4095) in 'red or green' image.



"M1 (g>q)" and "M2 (r>p)" show Manders's colocalization coefficient (Manders et al., 1993) 



The following is consist of 5 data groups.

+ '-saturated' (or '-s')    ...Pixels for {(red value<4095) AND (green value<4095)}
+ 'p<red<4095' (or 'r>p')    ...Pixels for {p<red value<4095}
+'q<green<4095' (or 'g>q')    ...Pixels for {q<green value<4095}
+ 'p,q<red,green<4095' (or 'rg')    ...Pixels for {(p<red value<4095) OR (q<green value<4095)}
+ 'total' (or 't')    ...Total pixels


Each data group contains 9 statistic numbers as follows ...

+ 'N'    ...Number of Pixels.
+ 'R mean'   ...Mean of red pixel value.
+ 'G mean'   ...Mean of green pixel value.
+ 'R sd'    ...Standard Deviation of red pixel value.
+ 'G sd'    ...Standard Deviation of green pixel value.
+ 'cov'    ...Covariance of red and green pixel values.
+ 'CC'    ...Correlation Coefficient (CC, or Pearson's r; Manders et al., 1992) for red and green pixel values.
+    'overlap C'    ...Manders's Overlap Coefficient (OC; Manders et al., 1993) for red and green pixel values.
+    'ICQ'    ...Intensity Correlation Quotient (ICQ; Li et al., 1994) for red and green pixel values.

#### References

Nakamura K, Watakabe A, Hioki H, Fujiyama F, Tanaka Yasuyo, Yamamori T, Kaneko T. (2007) Transiently increased colocalization of vesicular glutamate transporters 1 and 2 at single axon terminals during postnatal development of mouse neocortex: a quantitative analysis with correlation coefficient. Eur J Neurosci 26(11):3054-67 [pmid:18028110](http://www.ncbi.nlm.nih.gov/pubmed/18028110), doi:[10.1111/j.1460-9568.2007.05868.x](https://doi.org/10.1111/j.1460-9568.2007.05868.x), erratum doi:[10.1111/j.1460-9568.2008.06449.x](https://doi.org/10.1111/j.1460-9568.2008.06449.x)

Li Q, Lau A, Morris TJ, Guo L, Fordyce CB, Stanley EF (2004) J Neurosci 24:4070-4081. 

Manders EM, Stap J, Brakenhoff GJ, van Driel R, Aten JA (1992) J Cell Sci 103 (Pt 3):857-862. 

Manders EMM, Verbeek FJ, Aten JA (1993) J Microsc 169:375-382.





# PixData Shuffler

### Author

Kouichi Nakamura (Kyoto Univ)

### Source

[PixData_Shuffler.java](http://www.mbs.med.kyoto-u.ac.jp/imagej/PixData_Shuffler.java) and [PixData.java](http://www.mbs.med.kyoto-u.ac.jp/imagej/PixData.java)

### Installation

Download [PixData_Shuffler.java](http://www.mbs.med.kyoto-u.ac.jp/imagej/PixData_Shuffler.java) and [PixData.java](http://www.mbs.med.kyoto-u.ac.jp/imagej/PixData.java) to the plugins folder or subfolder, and compile them with the "Compile and Run" command. Restart ImageJ to add the "PixData_Shuffler" command to the Plugins menu.

### Description

ImageJ PlugInFilter "PixData_Shuffler.java" is designed for the quantitative colocalization analysis of two immunoreactivities in double immunofluorescence staining.  This PlugInFilter calculates correlation coefficients (CC, or Pearson's r), intensity correlation quotient (ICQ), and overlap coefficient (OC) from two channels (red and green, in most cases) in 8bit RGB, 12bit RGB, or 16bit RGB images before and after shuffling of pixels. This supports ROI (region of interest) in red image but does not support image stacks.<br />

#### Procedures

1. "PixData_Shuffler.java" requires at least two grayscale (8bit, 8bit color, 12 bit, or 16bit) images as input.  First, you thus need to separate or split channels of single RGB images (or stacks).  This can be done simply by a menu command "RGB split" in "Image -> Color" for standard 8bit RGB images or stacks.  When you handle 12bit or 16bit RGB images, you should choose a menu command "Convert stack into images" in "Image -> Stack". 
2. Before using this PlugInFilter, you need to determine lower threshold (i.e. maximum noise level) of red and green channels, separately.  If you want to determine the threshold simply by visual inspection, a menu command "Threshold..." in "Image -> Adjust" is suitable for this purpose.
3. Run "PixData_Shuffler.java" by choosing it from "Plugins" menu.
4. Select red image (stack) and green image (green) from the drop-down menus. 
5. Input lower threshold values (maximum noise level) for red and green images (stacks) into boxes.
6. When you use this plug-in for the first time in a session (the first run), you need to check "Initialize the Results Window", to make headers in Results window. Note that this option clears all the data in the current Results window! 
7. You must enter the number of shuffles (positive integer) in the box "How many shuffles do you need?".
8. Then click "OK". If a dialog ask you bit depth of images, select the appropriate one and click "OK". Results of calculation will appear on Results window. 

#### Options
1. "Green-Red order?" function works on Windows but not on Mac OS X. This option may be helpful when the order of image names listed in the drop-down menus is stable. In default, this Plug-in shows names of images in Red-Green order (suitable for pictures taken with Zeiss microscopes). With "Green-Red order?" option checked, it shows in Green-Red order (maybe suitable for pictures taken with Leica microscopes). This may accelerate your work.
2. "Show Time Spent for the Calculation" will display the time in the Results window.

 About data formats, see instruction of Pearsons_r.java.
#### References
Nakamura K, Watakabe A, Hioki H, Fujiyama F, Tanaka Yasuyo, Yamamori T, Kaneko T. (2007) Transiently increased colocalization of vesicular glutamate transporters 1 and 2 at single axon terminals during postnatal development of mouse neocortex: a quantitative analysis with correlation coefficient. Eur J Neurosci 26(11):3054-67 [pmid:18028110](http://www.ncbi.nlm.nih.gov/pubmed/18028110), doi:[10.1111/j.1460-9568.2007.05868.x](https://doi.org/10.1111/j.1460-9568.2007.05868.x), erratum doi:[10.1111/j.1460-9568.2008.06449.x](https://doi.org/10.1111/j.1460-9568.2008.06449.x)





# **Quantile 999**

### Author

Kouichi Nakamura

### Date

2006/10/06

### Source

Quantile_999.java

### Installation

Download [Quantile_999.java](http://www.mbs.med.kyoto-u.ac.jp/imagej/Quantile_999.java) to the plugins folder or subfolder, and compile it with the "Compile and Run" command. Restart ImageJ to add the "Quantile 999" command to the Plugins menu.

### Description

This Plugin calcurates 999/1000 quantile and 99/100 quantile (= pecentile) of the fluorescence intensity in an image. This Plugin suppors ROI and stack images.

#### Procedures

1. (Optional) Set ROI in the image of interest
2. Select and check the image name(s) from those listed below. 
3. When you use this plug-in for the first time in a session (the first run), you need to check "Initialize the Results Window", to make headers in Results window. Note that this option clears all the data in the current Results window! 
4. "Show Time Spent for the Calculation" will display the time in the Results window. 
5. If your want to close the image windows each time after calculation, please check "Close image window after calculation" option.
6. Click OK. The calculation may need several minutes.



#### References

Nakamura K, Watakabe A, Hioki H, Fujiyama F, Tanaka Yasuyo, Yamamori T, Kaneko T. (2007) Transiently increased colocalization of vesicular glutamate transporters 1 and 2 at single axon terminals during postnatal development of mouse neocortex: a quantitative analysis with correlation coefficient. Eur J Neurosci 26(11):3054-67 [pmid:18028110](http://www.ncbi.nlm.nih.gov/pubmed/18028110), doi:[10.1111/j.1460-9568.2007.05868.x](https://doi.org/10.1111/j.1460-9568.2007.05868.x), erratum doi:[10.1111/j.1460-9568.2008.06449.x](https://doi.org/10.1111/j.1460-9568.2008.06449.x)

