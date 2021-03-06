package com.KnappTech.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

public class FileReadWrite {

	public static Collection<?> readCollectionFromFile(String path) {
        Collection<?> myObject = (Collection<?>)readObjectFromFile(path);
		return myObject;
	}

	public static Object[] readArrayFromFile(String path) {
		Object[] myObject = (Object[])readObjectFromFile(path);
		return myObject;
	}

	public static Object readObjectFromFile(String path) {
		File file = new File(path);
		return readObjectFromFile(file);
	}

	public static Object readObjectFromFile(File file) {
		ObjectInputStream ois = null;
        FileInputStream fis = null;
        Object myObject = null;
        try {
	        try {
	            fis = new FileInputStream(file);
	        	ois = new ObjectInputStream(fis);
	            myObject = ois.readObject();
	            ois.close();
	            fis.close();
	        } catch (InvalidClassException e) {
	        	String msg = "The class found was invalid when trying to load the object from: "+'\n' + 
	        			file.toString() + '\n' + 
	        			"Processing will resume with a null object instead." + 
	        			"\nThe error message is:\n"+e.getMessage()+"\nThrown from FileReadWrite " +
	        					"when trying to read an object from a file.";
	        	// Runner.stockTrendLog.log(Level.SEVERE, msg, e);
	        	System.err.println(msg);
	        } catch (IOException e) {
	        	String msg = "There was an IO Exception when trying to load the object from: " + '\n' +
	        	file.toString()+'\n' +
	        			"Processing will resume with a null object instead." + 
	        			"\nThe error message is:\n"+e.getMessage()+"\nThrown from DataStorage loadObject method.";
	        	// Runner.stockTrendLog.log(Level.SEVERE, msg, e);
	        	System.err.println(msg);
	        	e.printStackTrace();
	        } catch (ClassNotFoundException e) {
	        	String msg = "The class was not found when trying to load the object from: " + '\n' +
	        	file.toString() + "\nThe error message is:\n"+e.getMessage()+
	        			"\nThrown from DataStorage loadObject method.";
	        	// Runner.stockTrendLog.log(Level.SEVERE, msg, e);
	        	System.err.println(msg);
	        } catch (ClassCastException e) {
	        	String msg = "There was a mismatch between a stored data type and its specified type (ClassCastException)\n" +
	        			"when trying to load the object from: " + '\n' +
	        			file.toString() + "\nThe error message is:\n"+e.getMessage()+
		    			"\nThrown from DataStorage loadObject method.";
		    	// Runner.stockTrendLog.log(Level.SEVERE, msg, e);
	        	System.err.println(msg);
		    	myObject = null;
	        } finally {
	            if (fis != null) {
	            	fis.close();
	            }
	            if (ois!= null) {
	            	ois.close();
	            }
	        }
        } catch (IOException e) {
            // does nothing here.
        }
        return myObject;
	}
	
	public static void writeArrayToFile(Object[] items, String path) {
		writeObjectToFile(items,path);
	}
	
//	public static void writeCollectionToFile(Collection<? extends Serializable> items, String path) {
//		writeObjectToFile(items,path);
//	}
	
	public static void writeCollectionToFileAsArray(Collection<?> items, String path) {
		Object[] arr = items.toArray();
		writeObjectToFile(arr,path);
	}
	
	public static void writeObjectToFile(Serializable items, String path) {
		File file = new File(path);
		writeObjectToFile(items, file);
	}
	
	public static void writeObjectToFile(Serializable items, File file) {
		ObjectOutputStream oos = null;
        try {
	        try {
	        	String path =  file.toString();
	        	int ind = path.lastIndexOf(File.separator);
	        	String dir = path.substring(0,ind);
	        	File myDir = new File(dir);
	        	if (!myDir.exists()) {
	        		myDir.mkdirs();
	        	}
	        	File myFile = new File(path);
	        	if (!myFile.exists()) {
	        		myFile.createNewFile();
	        	}
	            oos = new ObjectOutputStream(new FileOutputStream(myFile));
	            oos.writeObject(items);
	            oos.close();
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } finally {
	            if (oos!=null){
	                oos.close();
	            }
	        }
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}

	public static void deleteFile(String path) {
		File file = null;
        file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}
}
