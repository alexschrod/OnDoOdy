package net.alexanderschroeder.OnDoOdy.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/**
 * SLAPI = Saving/Loading API API for Saving and Loading Objects.
 * Switched to BukkitObjectOutputStream/InputStream for version
 * 1.6.2-R1.0 of Bukkit.
 * 
 * @author Tomsik68
 * @author alexschrod
 */
public class SLAPI {
	public static void save(Object obj, String path) throws FileNotFoundException, IOException {
		BukkitObjectOutputStream oos = new BukkitObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}

	public static Object load(String path) throws FileNotFoundException, IOException {
		BukkitObjectInputStream ois = new BukkitObjectInputStream(new FileInputStream(path));
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