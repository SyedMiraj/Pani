package miraj.biid.com.pani_200;

import android.content.res.Resources;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class SvmComputer {

	private svm_model model;
	private final int bucketsize;
	private String model_file_name = "";
	private boolean valid = false;
	private Double [] histogramMin;
	private Double [] histogramMax;

	public static List<Integer> additionalModels() {
		List<Integer> result = new ArrayList<Integer>();
		File sdcard = Environment.getExternalStorageDirectory();
		for (int bucketsize = 16; bucketsize < 256; bucketsize *= 2)
		{
			String model_file_name = "svm_trained_" + bucketsize + ".model";
			File file = new File(sdcard, BuildConfig.APPLICATION_ID + "/" + model_file_name);
			if (file.exists())
				result.add(bucketsize);
		}
		return result;
	}

	public SvmComputer(int bucketsize, Resources resources) {
		this.bucketsize = bucketsize;
		this.model = null;
		model_file_name = "svm_trained_" + bucketsize + ".model";
		InputStream is = null;
		InputStream isMin = null;
		InputStream isMax = null;
		switch (bucketsize) {
			case 2:
				is = resources.openRawResource(R.raw.svm_trained_2);
				isMin = resources.openRawResource(R.raw.min_2);
				isMax = resources.openRawResource(R.raw.max_2);
				break;
			case 4:
				is = resources.openRawResource(R.raw.svm_trained_4);
				isMin = resources.openRawResource(R.raw.min_4);
				isMax = resources.openRawResource(R.raw.max_4);
				break;
			case 8:
				is = resources.openRawResource(R.raw.svm_trained_8);
				isMin = resources.openRawResource(R.raw.min_8);
				isMax = resources.openRawResource(R.raw.max_8);
				break;
		}
		if (is != null && isMin != null && isMax != null) {
			try {
				model = svm.svm_load_model(new BufferedReader(new InputStreamReader(is)));
				histogramMin = readHistogramMinMax(bucketsize, isMin);
				histogramMax = readHistogramMinMax(bucketsize, isMax);
				valid = true;
			} catch (IOException e) {
			}
		} else {
			File sdcard = Environment.getExternalStorageDirectory();
			File file = new File(sdcard, BuildConfig.APPLICATION_ID + "/" + model_file_name);
			File fileMin = new File(sdcard, BuildConfig.APPLICATION_ID + "/min_" + bucketsize + ".histogram");
			File fileMax = new File(sdcard, BuildConfig.APPLICATION_ID + "/max_" + bucketsize + ".histogram");
			if (file.exists() && fileMin.exists() && fileMax.exists()) {
				try {
					String model_path = String.valueOf(file);
					model = svm.svm_load_model(model_path);
					histogramMin = readHistogramMinMax(bucketsize, new FileInputStream((fileMin)));
					histogramMax = readHistogramMinMax(bucketsize, new FileInputStream(fileMax));
					valid = true;
				} catch (IOException e) {
				}
			}
		}
	}

	private Double [] readHistogramMinMax(final int bucketsize, InputStream is) {
		final int elements = bucketsize * bucketsize * bucketsize;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			String [] sValues = line.split(",");
			Double [] values = new Double[elements];
			for (int i = 0; i < elements; ++i) {
				String sVal = sValues[i];
				values[i] = Double.parseDouble(sVal);
			}
			return values;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void scaleHistogram(List<Double> histogramValues) {
		final int elements = bucketsize * bucketsize * bucketsize;
		for (int i = 0; i < elements; ++i) {
			Double newVal = (histogramValues.get(i) - histogramMin[i]) / (histogramMax[i] - histogramMin[i]);
			if (newVal.isNaN())
				newVal = histogramMin[i];
			else if (newVal.isInfinite())
				newVal = histogramMax[i];
			histogramValues.set(i, newVal);
		}
	}

	public String computeFcover(String fileName, Map<String, Double> results, ProgressMonitor progressMonitor) {
		progressMonitor.initSubJob(3, 30);
		if (valid) {
			progressMonitor.workedSubJob();
			List<Double> histogram = Histogram.getHistogram(fileName, bucketsize);
			progressMonitor.workedSubJob();
			if (histogram != null) {
				scaleHistogram(histogram);
				double fcover = predictSVM(histogram);
				progressMonitor.workedSubJob();
				results.put(fileName, fcover);
				progressMonitor.endSubJob();
				return "";
			} else {
				progressMonitor.endSubJob();
				return "SVM: Failed to compute histogram of '" + fileName + "'";
			}
		} else {
			progressMonitor.endSubJob();
			return "SVM: File '" + model_file_name + "' is missing.";
		}
	}

	private double predictSVM(List<Double> histogram) {
		int m = histogram.size();
		svm_node[] x = new svm_node[m];
		for (int j = 0; j < m; j++) {
			x[j] = new svm_node();
			x[j].index = j;
			x[j].value = histogram.get(j);
		}
		double value = svm.svm_predict(model, x);
		return value;
	}
}
