package com.KnappTech.sr.persistence;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.KnappTech.model.IPersistenceManager;
import com.KnappTech.model.Identifiable;
import com.KnappTech.model.KTObject;
import com.KnappTech.sr.model.AbstractSKTSet;
import com.KnappTech.sr.model.Regressable.RecordList;
import com.KnappTech.util.FileReadWrite;
import com.KnappTech.util.FileUtil;
import com.KnappTech.util.ThreadSafeKTOList;

/**
 * Responsible for loading and saving KTObjects, and also 
 * keeps a list to all that are currently loaded.
 * @author Michael Knapp
 */
public abstract class PersistenceManager<E extends KTObject> implements IPersistenceManager<E,String>  {
	/**
	 * A list of all the objects that are loaded, this will be kept in alphabetical
	 * order to make it faster at finding objects.  Every time something is added
	 * to this list, it must be sorted.
	 */
	protected ThreadSafeKTOList<E> loadedObjects = new ThreadSafeKTOList<E>();
	protected final String dataDirectoryPath = PersistProperties.getDataDirectoryPath();
	private final ThreadPoolExecutor threadManager = 
		(ThreadPoolExecutor) Executors.newFixedThreadPool(15);
	
	/**
	 * Searches all of the loaded objects for one with the id provided,
	 * it uses a special algorithm to find them that is faster because
	 * the data is in alphabetical order.  If the object is not already
	 * loaded, this will return null.  This assumes the loaded objects
	 * list is sorted alphabetically or it might fail.
	 * @param id
	 * @return
	 */
	protected E iGetIfLoaded(String id) {
		int index = loadedObjects.getIndex(id);
		if (index>=0) {
			return loadedObjects.get(index);
		}
		return null;
	}

	/**
	 * Determines if the id is already loaded.
	 * @param id
	 * @return
	 */
	protected boolean iIsLoaded(String id) {
		return iGetIfLoaded(id)!=null;
	}
	
	
	/**
	 * gets a list of all the objects with the ids provided if
	 * each is already loaded.
	 * @param ids
	 * @return
	 */
	protected List<E> iGetAllThatAreLoaded(Collection<String> ids) {
		ArrayList<E> objects = new ArrayList<E>();
		for (String id : ids) {
			E obj = iGetIfLoaded(id);
			if (obj!=null) {
				objects.add(obj);
			}
		}
		return objects;
	}
	
	protected List<E> iGetAllThatAreLoaded() {
		return loadedObjects.getList();
	}
	
	/**
	 * Determines if every id in the list is loaded.  You can tell it
	 * to assume that all are stored, or have it reduce the set to just
	 * those that are stored.
	 * @param ids
	 * @param assumeAllStored
	 * @return
	 */
	protected boolean iAreAllLoaded(Collection<String> ids,boolean assumeAllStored) {
		List<String> storedIDs = null;
		if (!assumeAllStored) {
			storedIDs = reduceToStored(ids);
		} else {
			storedIDs = new ArrayList<String>(ids);
		}
		for (String id : storedIDs) {
			if (!iIsLoaded(id)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Tries to get the object by first checking internal data,
	 * then checking the persisted data on hard drive.  If it
	 * cannot be found by either of these two methods, this
	 * will return null.
	 * @param id
	 * @return
	 * @throws InterruptedException 
	 */
	protected E iGetIfStored(String id) throws InterruptedException {
		E obj = iGetIfLoaded(id);
		if (obj!=null) {
			return obj;
		} else {
			// load the object.
			return iLoad(id);
		}
	}
	
	protected List<E> iGetAllThatAreStored(Collection<String> ids) throws InterruptedException {
		return iGetAllThatAreStored(ids,false);
	}
	
	/**
	 * Finds all of the objects with the ids provided by first 
	 * checking if they are already loaded, then by loading them
	 * if they are stored.
	 * @param ids
	 * @return
	 * @throws InterruptedException 
	 */
	protected List<E> iGetAllThatAreStored(Collection<String> ids,boolean verbose) 
	throws InterruptedException {
		ArrayList<E> objects = new ArrayList<E>();
		ArrayList<String> waitingIDs = new ArrayList<String>();
		for (String id : ids) {
			E obj = iGetIfLoaded(id);
			if (obj!=null) {
				objects.add(obj);
			} else if (iIsStored(id)) {
				// try loading as a separate thread.
				if (verbose) {
					System.out.println("   Going to load "+id);
				}
				iLoad(id,true);
				waitingIDs.add(id);
			}
		}
		// unfortunately there is not a better function supported
		// by the thread manager, so I must do this.
		while (threadManager.getActiveCount()>0) {
			Thread.sleep(500);
		}
		
		HashSet<String> lostIDs = new HashSet<String>();
		for (String id : waitingIDs) {
			if (verbose) {
				System.out.println("   Going to wait a few seconds for "+id);
			}
//			E obj = iWaitFor(id, 10);
			E obj = iGetIfLoaded(id);
			if (obj!=null) {
				if (verbose) {
					System.out.println("   Successfully loaded "+id);
				}
				objects.add(obj);
			} else if (verbose) {
				System.out.println("   Could not initially get object with id "+id);
				lostIDs.add(id);
			}
		}
		// this time there is no more waiting.
		for (String id : lostIDs) {
			if (verbose) {
				System.out.println("   Going to wait a few seconds for "+id);
			}
			E obj = iGetIfLoaded(id);
			if (obj!=null) {
				if (verbose) {
					System.out.println("   Successfully loaded "+id);
				}
				objects.add(obj);
			} else if (verbose) {
				System.out.println("   Never found object with id "+id);
			}
		}
		if (objects.size()!=ids.size()) {
			System.err.println("Persistence manager tried loading "+ids.size()+", but actually "+
					"loaded "+objects.size());
			for (String id : ids) {
				boolean found = false;
				for (Identifiable<?> iden : objects) {
					if (iden.getID().equals(id)) {
						found = true;
						break;
					}
				}
				if (!found)
					System.err.println("Lost "+id);
			}
		}
		return objects;
	}
	
	protected HashSet<String> iGetAllStoredIDs(char letter) {
		File dir = getDirectory();
		HashSet<String> ids = new HashSet<String>();
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				String name = file.getName();
				if (name.startsWith(String.valueOf(letter))) {
					int i = name.indexOf(".");
					if (i>=0) {
						name=name.substring(0,i);
					}
					ids.add(name);
				}
			}
		}
		return ids;
	}
	
	protected HashSet<String> iGetAllStoredIDs() {
		File dir = getDirectory();
		if (dir.exists()) {
			File[] files = dir.listFiles();
			HashSet<String> ids = new HashSet<String>();
			for (File file : files) {
				String name = file.getName();
				int i = name.indexOf(".");
				if (i>=0) {
					name=name.substring(0,i);
				}
				ids.add(name);
			}
			return ids;
		} 
		return new HashSet<String>();
	}

	protected List<E> iGetAllThatAreStored(char letter) throws InterruptedException {
		return iGetAllThatAreStored(iGetAllStoredIDs(letter));
	}

	protected List<E> iGetAllThatAreStored(char letter,boolean verbose) throws InterruptedException {
		return iGetAllThatAreStored(iGetAllStoredIDs(letter),verbose);
	}

	protected List<E> iGetAllThatAreStored() throws InterruptedException {
		return iGetAllThatAreStored(iGetAllStoredIDs());
	}
	
	protected boolean iIsStored(String id) {
		if (id!=null && id.length()>0) {
			return getFileForID(id).exists() || getObjectFileForID(id).exists();
		}
		return false;
	}
	
	protected E iWaitFor(String id,int maxSeconds) throws InterruptedException {
		E obj = iGetIfLoaded(id);
		int count = 0;
		if (iIsStored(id)) {
			while (obj==null && (count/5)<maxSeconds) {
				// since this thread does not have a lock on any monitor, 
				// use sleep instead of wait.  The internal data set 
				// is what is synchronized, not this class.
				Thread.sleep(200);
				obj = iGetIfLoaded(id);
				count++;
			}
		}
		return obj;
	}
	
	protected List<E> iWaitForAll(Collection<String> ids,int maxSeconds) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		List<String> storedIDs = reduceToStored(ids);
		for (String id : storedIDs) {
			iWaitFor(id, maxSeconds);
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime-startTime;
			if (((double)elapsedTime/1000)>maxSeconds) {
				break;
			}
		}
		return iGetAllThatAreLoaded(ids);
	}
	
	protected List<E> iWaitForAll(int maxSeconds) throws InterruptedException {
		return iWaitForAll(iGetAllStoredIDs(), maxSeconds);
	}
	
	protected String getFilePathForID(String id) {
		return dataDirectoryPath+File.separator+getFolderPath()+File.separator+id+".txt";
	}
	
	private String getObjectFilePathForID(String id) {
		String s = dataDirectoryPath+File.separator+getFolderPath()+File.separator+id+".sro";
		return s; // sro = stockradamous object.
	}
	
	protected File getFileForID(String id) {
		return new File(getFilePathForID(id));
	}

	private File getObjectFileForID(String id) {
		return new File(getObjectFilePathForID(id));
	}

	protected List<String> reduceToStored(Collection<String> ids) {
		ArrayList<String> attainableIDs = new ArrayList<String>();
		for (String id : ids) {
			if (iIsStored(id)) {
				attainableIDs.add(id);
			}
		}
		return attainableIDs;
	}
	
	/**
	 * Tries to get the object by first checking internal data,
	 * then checking the persisted data on hard drive, and as a last
	 * resort, trying to download the data from the internet.
	 * @param id
	 * @return
	 * @throws InterruptedException 
	 */
	protected E getIfAttainable(String id) throws InterruptedException {
		E obj = iGetIfStored(id);
		if (obj!=null) {
			return obj;
		} else {
			// try to download the object.
			// TODO download.
		}
		return null;
	}

	protected List<E> getAllThatAreAttainable(Collection<String> id) {
		return null;
		// TODO make it download.
	}
	
	protected boolean iSave(E obj,boolean separateThread) throws InterruptedException {
		if (separateThread) {
			Runnable r = new Saver(obj);
			threadManager.execute(r);
		} else {
			String saveString = makeSaveString(obj);
			if (saveString!=null && saveString.length()>0) {
				File file = getFileForID(obj.getID());
				FileUtil.writeStringToFile(file, saveString,true);
			}
			
			Serializable saveObject = makeSaveObject(obj);
			if (saveObject!=null) {
				File file = getObjectFileForID(obj.getID());
				FileReadWrite.writeObjectToFile(saveObject, file);
			}
		}
		return false;
	}

	/**
	 * Runs and saves the object.
	 * @author Michael Knapp
	 */
	private class Saver implements Runnable {
		private E obj = null;
		public Saver(E obj) {
			this.obj = obj;
		}
		@Override
		public void run() {
			try {
				iSave(obj,false);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	protected boolean iSaveAll(AbstractSKTSet<E> objects) throws InterruptedException {
		return iSaveAll(objects,false);
	}
	
	/**
	 * Saves all the objects provided, using multiple threads means
	 * it will not wait for that to finish before the current thread
	 * moves on.
	 * @param objects
	 * @param multiThread
	 * @return
	 * @throws InterruptedException 
	 */
	protected boolean iSaveAll(AbstractSKTSet<E> objects, boolean multiThread) throws InterruptedException {
		boolean worked = true;
		for (E item : objects.getItems()) {
			if (!iSave(item, multiThread))
				worked = false;
		}
		return worked;
	}
	
	protected E iLoad(String id) throws InterruptedException {
		return iLoad(id,false);
	}
	
	/**
	 * loads the object with the id, does not check if we already
	 * have it or not.  Children might use this method, but other
	 * classes should use the get methods. instead.
	 * @param id the id of what you want loaded.
	 * @param separateThread if true this will create a separate
	 * thread to load the object, and then immediately return null.
	 * If false it will use the current thread and only return null
	 * if the object is not on the hard drive.
	 * @return the object you want.
	 * @throws InterruptedException 
	 */
	protected E iLoad(String id,boolean separateThread) throws InterruptedException {
		if (iIsStored(id)) {
			if (separateThread) {
				threadManager.execute(new Loader(id));
			} else {
				File dataFile = getFileForID(id);
				File objectFile = getObjectFileForID(id);
				if (dataFile.exists() || objectFile.exists()) {
					String stringData = null;
					if (dataFile.exists()) {
						stringData = FileUtil.readStringFromFile(dataFile);
					}
					Object objectData = null;
					if (objectFile.exists()) {
						objectData = FileReadWrite.readObjectFromFile(objectFile);
					}
					E object = createInstance(id,stringData,objectData);
					if (object instanceof RecordList<?>) {
						RecordList<?> r = (RecordList<?>)object;
//						r.order();
						r.determineStartDate();
						r.determineEndDate();
					}
					if (object!=null) {
						this.loadedObjects.add(object);
						loadedObjects.sortAlphabetically();
					}
					return object;
				}
			}
		}
		return null;
	}
	
	/**
	 * Loads the id provided.
	 * @author Michael Knapp
	 */
	private class Loader implements Runnable {
		private String id =null;
		public Loader(String id) {
			this.id = id;
		}
		public void run() {
			try {
				iLoad(id,false);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	/**
	 * Loads all of the ids provided.
	 * @param ids
	 * @return
	 * @throws InterruptedException 
	 */
	protected List<E> iLoadAll(Collection<String> ids) throws InterruptedException {
		return iLoadAll(ids,false,false);
	}
	
	
	protected List<E> iLoadAll(Collection<String> ids, boolean multiThread,boolean waitFor) throws InterruptedException {
		List<E> objects = new ArrayList<E>();
		for (String id : ids) {
			E obj = iLoad(id,multiThread);
			if (obj!=null) {
				objects.add(obj);
			}
		}
		if (multiThread && waitFor) {
			objects = iWaitForAll(ids, 10);
		}
		return objects;
	}
	
	protected void iRemove(String id) {
		loadedObjects.remove(id);
	}

	protected void iRemove(LinkedHashSet<String> ids) {
		loadedObjects.remove(ids);
	}
	
	protected String getDirectoryPath() {
		String dir = dataDirectoryPath;
		if (dataDirectoryPath.endsWith(File.separator)) {
			dir+=getFolderPath();
		} else {
			dir+=File.separator+getFolderPath();
		}
		return dir;
	}
	
	protected File getDirectory() {
		return new File(getDirectoryPath());
	}
	
	protected abstract String getFolderPath();
	protected abstract Serializable makeSaveObject(E obj);
	protected abstract String makeSaveString(E obj);
	protected abstract E createInstance(String id, String stringData, Object objectData);
	
	
}