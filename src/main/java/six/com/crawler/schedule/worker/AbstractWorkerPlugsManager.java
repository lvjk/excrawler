package six.com.crawler.schedule.worker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.entity.Site;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.Worker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月8日 上午9:51:50
 */
public abstract class AbstractWorkerPlugsManager extends ClassLoader implements WorkerPlugsManager {

	final static Logger log = LoggerFactory.getLogger(AbstractWorkerPlugsManager.class);

	@Override
	public final boolean saveClass(Class<?> clz) {
		// 1先保存本地，
		// 2然后写缓存
		return false;
	}

	/**
	 * 保存 worker插件class至缓存
	 * 
	 * @param workerClassName
	 * @param classByte
	 * @return
	 */
	protected abstract boolean savePlugClassToCache(Class<?> clz);

	@Override
	public final Worker<?> newWorker(String workerClassName) {
		Worker<?> worker = null;
		Class<?> workerClass = null;
		try {
			workerClass = loadClass(workerClassName);
		} catch (ClassNotFoundException e) {
			log.error("did not load class:" + workerClass);
		}
		if (null != workerClass) {
			worker = newWokrer(workerClass);
		} else {
			log.error("did not find worker class:" + workerClass);
			throw new RuntimeException("did not find worker class:" + workerClass);
		}
		return worker;
	}

	private Worker<?> newWokrer(Class<?> workerClass) {
		Worker<?> newJobWorker = null;
		Constructor<?> constructor = null;
		String className = workerClass.getName();
		try {
			constructor = workerClass.getConstructor();
		} catch (NoSuchMethodException e) {
			log.error("NoSuchMethodException getConstructor err:" + className, e);
		} catch (SecurityException e) {
			log.error("SecurityException err" + className, e);
		}
		if (null != constructor) {
			try {
				newJobWorker = (Worker<?>) constructor.newInstance();
			} catch (InstantiationException e) {
				log.error("InstantiationException  err:" + workerClass, e);
			} catch (IllegalAccessException e) {
				log.error("IllegalAccessException  err:" + className.concat("|")
						.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()), e);
			} catch (IllegalArgumentException e) {
				log.error("IllegalArgumentException  err:" + className.concat("|")
						.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()), e);
			} catch (InvocationTargetException e) {
				log.error("InvocationTargetException  err:" + className.concat("|")
						.concat(AbstractSchedulerManager.class.getName()).concat("|").concat(Site.class.getName()), e);
			}
		} else {
			log.error("did not find worker's constructor:" + workerClass);
			throw new RuntimeException("did not find worker class:" + workerClass);
		}
		return newJobWorker;

	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> plugClass = findFromCache(name);
		if (null == plugClass) {
			plugClass = findFromLocal(name);
		}
		return plugClass;
	}

	/**
	 * 从本地获取 插件class
	 * 
	 * @param workerClassName
	 * @return
	 */
	private Class<?> findFromLocal(String workerClassName) {
		Class<?> clz = null;
		try {
			clz = Class.forName(workerClassName);
		} catch (ClassNotFoundException e) {
			log.error("did not find class:" + workerClassName, e);
		}
		return clz;
	}

	/**
	 * 从缓存中获取worker插件class
	 * 
	 * @param workerClassName
	 * @return
	 */
	protected abstract Class<?> findFromCache(String workerClassName);
}
