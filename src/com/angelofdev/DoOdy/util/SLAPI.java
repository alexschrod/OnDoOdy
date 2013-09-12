package com.angelofdev.DoOdy.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * SLAPI = Saving/Loading API API for Saving and Loading Objects.
 * 
 * @author Tomsik68
 */
public class SLAPI {
	public static void save(Object obj, String path) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}

	public static Object load(String path) throws FileNotFoundException, IOException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		Object result;
		try {
			result = ois.readObject();
		} catch (ClassNotFoundException e) {
			return null;
		} finally {
			ois.close();
		}
		return result;
	}
}