/*
PixData ImageJ PlugIn version 1.0
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



This Plugin works with PixData_Shuffler.class

Installation
Put this file and PixData_Shuffler.java to the plugins folder or subfolder, and compile them with 
the "Compile and Run" command. Restart ImageJ to add the "PixData_Shuffler" command to the 
Plugins menu.

for more detail, see instructoins of PixData_Shuffler.java

*/

public class PixData implements java.lang.Comparable {
	private int value;
	private double randomN;

	//  コンストラクタで初期化を強制
	public PixData (int value, double randomN) {
		this.value = value;
		this.randomN = randomN;
	}
	public int getValue() {
		return this.value;
	}
	public double getRandomN() {
		return this.randomN;
	}
	
	public String message() {
		String str = "OK! OK! OK!";
		return str;
	}
	
	
	public int compareTo(Object object){
		PixData operand = (PixData) object;
		if (this.randomN < operand.randomN){
			return -1;
		}
		else if (this.randomN > operand.randomN){
			return 1;
		}
		else{
			return 0;
		}
	}
	
}
