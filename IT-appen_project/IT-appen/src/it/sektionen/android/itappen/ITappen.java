package it.sektionen.android.itappen;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class ITappen extends Application {

	private static Context mApplicationContext;

	@Override
	public void onCreate() {
		super.onCreate();
		mApplicationContext = getApplicationContext();
		

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true).build();

		// File cacheDir =
		// StorageUtils.getCacheDirectory(getApplicationContext());
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 4;
		// Create global configuration and initialize ImageLoader with this
		// configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).threadPoolSize(5)
				.threadPriority(Thread.NORM_PRIORITY - 1)
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.defaultDisplayImageOptions(defaultOptions)
				.memoryCacheSize(cacheSize).build();
		ImageLoader.getInstance().init(config);
	}

	public static Context getContext() {
		return ITappen.mApplicationContext;
	}

}
