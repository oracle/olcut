/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.util;

public class Getopt {
    
    String argList[]	= null;	    // the argument List to be parsed
    String optStr	= null;	    // the string of arguments in the form "ab:" see getopt man
    public String optArg = null;    // argument of an option
    public int optInd	= 0;	    // index of the option
    int nbArgs		= 0;	    // number of args in the argList
    int optPos		= 1;	    // position of option letter in current argument scanned
    
    static final int EOF = -1; // returned when no option left
    
    
    public Getopt(String[] args, String opts) {
	argList = args;
	nbArgs = argList.length;
	optStr = opts;
	optInd = 0;
	optPos = 1;
	optArg = null;
    }
    
    private void optError(String msg, char optLetter) {
	//System.err.println("Getopt: " + msg + " / " + optLetter);
    }
    
    public int getopt()  {
	optArg = null;
	
	// checking boundaries cases
	if (argList == null || optStr == null) return EOF;
	if (optInd < 0 || optInd >= nbArgs) return EOF;
	
	// get current Arg out of the argList
	String currentArg = argList[optInd];
	int argLength = currentArg.length();
	
	// checking special cases
	// if arg starts with optLetter "a", "xyz"..
	// if arg only 1 char in length "a", "-"
	if (argLength <= 1 || currentArg.charAt(0) != '-') {
	    return EOF;
	} else if (currentArg.equals("--")) { // end of options case
	    optInd++;
	    return EOF;
	}
	
	// get next "letter" from option argument
	char optLetter = currentArg.charAt(optPos);
	
	// find position of option in optStr
	int pos = optStr.indexOf(optLetter);
	if (pos == -1 || optLetter == ':') {
	    optError("illegal option", optLetter);
	    optLetter = '?';
	} else { // check if option requires argument
	    if (pos < optStr.length()-1 && optStr.charAt(pos+1) == ':') {
		// if remain characters after the current option in currentArg
		if (optPos != argLength-1) {
		    // take rest of current arg as the option argument
		    optArg = currentArg.substring(optPos+1);
		    optPos = argLength-1; // go to next arg below in argList
		} else { // take next arg as optArg
		    optInd++;
		    if (optInd < nbArgs &&
		    (argList[optInd].charAt(0) != '-' ||
		    argList[optInd].length() >= 2    &&
		    (optStr.indexOf(argList[optInd].charAt(1)) == -1 ||
		    argList[optInd].charAt(1) == ':')
		    )
		    ) {
			optArg = argList[optInd];
		    } else {
			optError("option '" + optLetter + "' requires an argument", optLetter);
			optArg = null;
			optLetter = '?';
		    }
		}
	    }
	}
	
	// next option argument,
	// either in currentArg or next arg in argList
	optPos++;
	
	// if no more option in currentArg
	if (optPos >= argLength) {
	    optInd++;
	    optPos = 1;  // reset position of opt letter
	}
	return optLetter;
    }
    
    public static void main(String[] args) {  // test the class
	System.out.println("---------- test 1 ------------");
	
	String flags = "ab?c:d:xy";
	String[] argv = { "-ab", "-c", "carg", "-xy",
	"-cCARG", "--", "-a" };
	Getopt getopt = new Getopt(argv, flags);
	
	int c;
	while (getopt.optInd < argv.length) {
	    c = getopt.getopt();
	    System.out.println((char)c + " " +
	    getopt.optArg + "(" + getopt.optInd +
	    ")");
	}
	System.out.println("final optInd (" +
	getopt.optInd + ")");
	
	System.out.println("------------ test 2 ------------");
	
	String[] argv1 = { "-ab", "carg", "kjh" };
	getopt = new Getopt(argv1, flags);
	getopt.optInd = 0;
	while ((c = getopt.getopt()) != -1) {
	    System.out.println((char)c + " " +
	    getopt.optArg + "(" + getopt.optInd +
	    ")");
	}
	System.out.println("final optInd (" +
	getopt.optInd + ")");
	
	System.out.println("------------ test 3 ------------");
	
	String[] argv2 = { "-ab", "-d", "-x", "carg", "kjh" };
	getopt = new Getopt(argv2, flags);
	getopt.optInd = 0;
	while ((c = getopt.getopt()) != -1) {
	    System.out.println((char)c + " " +
	    getopt.optArg + "(" + getopt.optInd +
	    ")");
	}
	System.out.println("final optInd (" +
	getopt.optInd + ")");
	
	System.out.println("------------ test 4 ------------");
	
	String[] argv3 = { "-ab", "-w", "-x", "carg", "kjh" };
	getopt = new Getopt(argv3, flags);
	getopt.optInd = 0;
	while ((c = getopt.getopt()) != -1) {
	    System.out.println((char)c + " " +
	    getopt.optArg + "(" + getopt.optInd +
	    ")");
	}
	System.out.println("final optInd (" +
	getopt.optInd + ")");
	
    }
}


