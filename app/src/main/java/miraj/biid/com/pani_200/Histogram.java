package miraj.biid.com.pani_200;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.util.Arrays;
import java.util.List;

public class Histogram {

	public static List<Double> getHistogram(String fileName, int buckets) {
		List<Double> feature_vector = null;
		Bitmap image = BitmapFactory.decodeFile(fileName);
		if (image != null) {
			int[][][] ch = new int[buckets][buckets][buckets];
			for (int x = 0; x < image.getWidth(); x++)
				for (int y = 0; y < image.getHeight(); y++) {
					int color = image.getPixel(x, y);
					int red = Color.red(color);
					int green = Color.green(color);
					int blue = Color.blue(color);
					ch[red / (256 / buckets)][green / (256 / buckets)][blue / (256 / buckets)]++;
				}
			feature_vector = Arrays.asList(new Double[buckets * buckets * buckets]);
			double nr_pixels = image.getWidth() * image.getHeight();
			for (int i = 0; i < ch.length; i++)
				for (int j = 0; j < ch[i].length; j++)
					for (int p = 0; p < ch[i][j].length; p++)
						feature_vector.set(i + j * buckets + p * buckets * buckets, new Double(ch[i][j][p] / nr_pixels));
		}
		return feature_vector;
	}
}
