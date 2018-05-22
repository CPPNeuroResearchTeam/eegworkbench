/*
*   This file is part of EEG Workbench.
*
*   EEG Workbench is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   EEG Workbench is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with EEG Workbench.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.io.StringWriter;
import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import jwave.Transform;
import jwave.transforms.DiscreteFourierTransform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.haar.Haar1;
import jwave.transforms.wavelets.daubechies.Daubechies10;
import jwave.transforms.wavelets.daubechies.Daubechies2;
import jwave.transforms.wavelets.daubechies.Daubechies4;

import libsvm.*;

import org.neuroph.core.*;
import org.neuroph.core.learning.*;
import org.neuroph.nnet.*;
import org.neuroph.util.norm.*;

import java.util.Arrays;

public class Workbench {

	private static String eeg_datadir		= "eeg_data";
	private static String app_staticdir		= "htdocs"; 
	private static String static_servletroot	= "/workbench";
	private static String json_servletroot		= "/json";
	private static int app_port			= 9999;

	private static final String RAW			= "raw";
	private static final String FREQ		= "frequency";
	private static final String DELTA		= "delta";
	private static final String ALPHA		= "alpha";
	private static final String BETA		= "beta";
	private static final String GAMMA		= "gamma";
	private static final String THETA		= "theta";

	private static final String ACTIVITY		= "activity";
	private static final String MOBILITY 		= "mobility";
	private static final String COMPLEXITY		= "complexity";
	private static final String STD_DEVIATION	= "std_deviation";
	private static final String MEAN		= "mean";
	private static final String MSV			= "msv";
	private static final String SKEWNESS		= "skewness";
	private static final String KURTOSIS		= "kurtosis";
	private static final String LENGTH 		= "length";

	private class Classify {
		JSONObject test_v;
		ArrayList<ArrayList<Double>> true_v;
		ArrayList<ArrayList<Double>> false_v;
		ArrayList<String> feature_bands;
		ArrayList<String> features;

		// organize training set and test/src vector into neuroph DataSet to utilize MaxMinNormalizer
		// MinMaxNormalizer normalizes Vector[i] = (Vector[i] - min[i]) / (max[i] - min[i])
		DataSet nn_training_set;
		double[] test_vector;
		
		double[] training_means;
		double[] training_stddevs;

		JSONObject classify_data;
	
		public Classify(JSONObject src_eeg,
				JSONArray class_true,
				JSONArray class_false,	
				ArrayList<String> feat_bands,
				ArrayList<String> feats) 
		{
			//System.out.println(String.format("Classify(%d,%d)", class_true.size(), class_false.size()));
			this.feature_bands	= feat_bands;
			this.features		= feats;
			this.test_v		= src_eeg;

			// construct feature vectors
			true_v = new ArrayList<ArrayList<Double>>();
			false_v = new ArrayList<ArrayList<Double>>();
	
			for ( int idx = 0; idx < class_true.size(); idx += 1 ) {	
				ArrayList<Double> t = new ArrayList<Double>();

				for ( String fb : feature_bands ) {
					for  ( String f : features ) {
						//System.out.println(String.format("%s:%s", fb, f));
						t.add( Double.valueOf( 
							(String) 
							( (JSONObject) ( (JSONObject) ((JSONObject) class_true.get(idx)).get(fb)).get("hjorth")).get(f) ));
					}

				}
				
				true_v.add(t);
			}

			for ( int idx = 0; idx < class_false.size(); idx += 1 ) {	
				ArrayList<Double> t = new ArrayList<Double>();

				for ( String fb : feature_bands ) {	
					for  ( String f : features ) {
						//System.out.println(String.format("%s:%s", fb, f));
						t.add( Double.valueOf( 
							(String) 
							( (JSONObject) ( (JSONObject) ((JSONObject) class_false.get(idx)).get(fb)).get("hjorth")).get(f) ));
					}

				}
				
				false_v.add(t);
			}

			buildDataSet();

		}

		private void buildDataSet() {

			// standardize/normalize using column mean and std deviation
			this.training_means = new double[true_v.get(0).size()];

			// calculate column (feature) means
			// method: sum(x) / n
			for ( int idx = 0; idx < true_v.size(); idx += 1 ) {
				for ( int jdx = 0; jdx < true_v.get(idx).size(); jdx += 1 ) {
					if ( idx == 0 ) {
						this.training_means[jdx] = 
							(double) true_v.get(idx).get(jdx);
					}
					else {
						this.training_means[jdx] += 
							(double) true_v.get(idx).get(jdx);

					}
				}
			}

			for ( int idx = 0; idx < false_v.size(); idx += 1 ) {
				for ( int jdx = 0; jdx < false_v.get(idx).size(); jdx += 1 ) {
					this.training_means[jdx] += 
						false_v.get(idx).get(jdx);
				}
			}

			for ( int idx = 0; idx < this.training_means.length; idx += 1 ) {
				this.training_means[idx] = 
					this.training_means[idx] / 
					(true_v.size() + false_v.size());	
				
			}

			// calculate column (feature) std deviations
			// method: sum((x - mean)^2) / n
			this.training_stddevs = new double[true_v.get(0).size()];

			for ( int idx = 0; idx < true_v.size(); idx += 1 ) {
				for ( int jdx = 0; jdx < true_v.get(idx).size(); jdx += 1 ) {
					if ( idx == 0 ) {
						this.training_stddevs[jdx] = 
							Math.pow( 
							true_v.get(idx).get(jdx) -
							training_means[jdx], 2.0);
					}
					else {
						this.training_stddevs[jdx] += 
							Math.pow( 
							true_v.get(idx).get(jdx) -
							training_means[jdx], 2.0);

					}
				}
			}

			for ( int idx = 0; idx < false_v.size(); idx += 1 ) {
				for ( int jdx = 0; jdx < false_v.get(idx).size(); jdx += 1 ) {
					this.training_stddevs[jdx] += 
						Math.pow( 
						(double) false_v.get(idx).get(jdx) - 
						training_means[jdx], 2.0);

				}
			}

			for ( int idx = 0; idx < this.training_stddevs.length; idx += 1 ) {
				this.training_stddevs[idx] = 
					Math.sqrt( this.training_stddevs[idx] / 
						(true_v.size() + false_v.size()) );	
			}

			// Construct training set (#features, 1 output)
			// Top row of nn_training_set are Z scores
			nn_training_set = new DataSet(true_v.get(0).size(), 1);

			// neuroph wants double[] data types
			// This is converting each value to a Z score: nn_training_set[idx][jdx] = (x - mean) / std
			for ( int idx = 0; idx < true_v.size(); idx += 1 ) {
				double[] t = new double[true_v.get(idx).size()];
				for ( int jdx = 0; jdx < true_v.get(idx).size(); jdx += 1 ) {
					t[jdx] = ( (double) true_v.get(idx).get(jdx) -
							training_means[jdx] ) / 
						 training_stddevs[jdx];	
				}

				nn_training_set.addRow( new DataSetRow (t, new double[] {1.0}) );

			}
			
			for ( int idx = 0; idx < false_v.size(); idx += 1 ) {
				double[] t = new double[false_v.get(idx).size()];
				for ( int jdx = 0; jdx < false_v.get(idx).size(); jdx += 1 ) {
					t[jdx] = ( (double) false_v.get(idx).get(jdx) -
							training_means[jdx]) / 
						training_stddevs[jdx];	
				}

				// TODO: find out what the desired output refers to
				nn_training_set.addRow( new DataSetRow (t, new double[] {0.0}) );

			}

			// scale/normalize test vector (i.e. source cross vector)
			// using training data means,std deviations
			test_vector = new double[true_v.get(0).size()];
			int idx = 0;

			for ( String fb : feature_bands ) {	
				for  ( String f : features ) {
					//System.out.println(String.format("%d %s:%s", idx, fb, f));
					test_vector[idx] = ((Double.valueOf(
						(String) 
						( (JSONObject) ( (JSONObject) test_v.get(fb)).get("hjorth")).get(f) )) - training_means[idx]) / training_stddevs[idx];
					idx += 1;
				}

			}

		}

		// TODO: review Neuroph docs to verify correct usage
		// TODO: ensure NeuralNetwork is an MLP
		public double classifyMLP() {
			//System.out.println( String.format("classifyMLP(): %d, %d", true_v.size(), true_v.get(0).size()) );

			// create perceptron neural network (x inputs/features, 1 output/category/label/prediction)
			NeuralNetwork nn_perceptron = new Perceptron(true_v.get(0).size(),1);

			nn_perceptron.learn(nn_training_set);
			
			nn_perceptron.setInput(test_vector);
			nn_perceptron.calculate();

			double[] out1 = nn_perceptron.getOutput();
	
				
			for ( int i = 0; i < out1.length; i += 1 ) {
				System.out.println( String.format("NN %d: %f", i, out1[i]) );
			}
			

			return out1[0];

		}

		public double classifySVM() {

			//override default libsvm print string
			svm.svm_set_print_string_function( new libsvm.svm_print_interface() {
				@Override public void print( String s ) { }
			});

			svm_node[][]	svm_training_set	= new svm_node [nn_training_set.size()][];
			double[]	svm_training_labels	= new double[nn_training_set.size()];

			// libsvm wants svm_node[] data types in training set
			int svm_idx = 0;
			for ( DataSetRow dsr : nn_training_set.getRows() ) {
				double[] dsr_vector = dsr.getInput();
				double[] dsr_label = dsr.getDesiredOutput();

				svm_node[] f_vect = new svm_node[dsr_vector.length + 1];

				// each feature needs an index value
				for ( int jdx = 0; jdx < dsr_vector.length; jdx += 1 ) {
					svm_node t = new svm_node();
						t.index = jdx + 1;
						t.value = dsr_vector[jdx];

					f_vect[jdx] = t;
				}

				// libsvm requires an "ending" svm node per feature vector
				svm_node t = new svm_node();
					t.index = -1;
					t.value = 0.0;

				f_vect[true_v.get(0).size()] = t;

				svm_training_set[svm_idx] = f_vect;
				svm_training_labels[svm_idx] = dsr_label[0];

				svm_idx += 1;

			}

			/*
			for ( int idx = 0; idx < svm_training_set.length; idx += 1 ) {
				System.out.print(String.format("%f: [", svm_training_labels[idx]));
				for ( int jdx = 0; jdx < svm_training_set[idx].length; jdx += 1 ) {
					System.out.print(String.format("%f,", svm_training_set[idx][jdx].value));
				}
				System.out.println("]");

			}
			*/

			// define test vector
			svm_node[] test_a = new svm_node[true_v.get(0).size() + 1];
			int feat_idx = 1;

			for ( int idx = 0; idx < test_vector.length; idx += 1 ) {
				svm_node svm_temp = new svm_node();
					svm_temp.index = feat_idx;
					svm_temp.value = test_vector[idx];

				test_a[idx] = svm_temp;
				
				feat_idx += 1;
			}

                        svm_node svm_temp = new svm_node();
                                svm_temp.index = -1;
                                svm_temp.value = 0.0;

                        test_a[true_v.get(0).size()] = svm_temp;

			// define problem using training set and svm parameters
			svm_problem problem = new svm_problem(); 
				problem.l = svm_training_set.length;
				problem.y = svm_training_labels;
				problem.x = svm_training_set;

			// use C-SVC with LINEAR kernel
			svm_parameter parameters = new svm_parameter();
			parameters.svm_type     = svm_parameter.C_SVC;
			parameters.kernel_type  = svm_parameter.LINEAR;
			parameters.C            = 1.0;
			parameters.cache_size   = 10000;
			parameters.probability  = 1;
			//parameters.nr_weight  = 0;
			//parameters.weight_label       = ;
			//parameters.weight     = ;
			//parameters.shrinking  = ;

			// with RBF kernel
			parameters.gamma        = 0.5;
			
			// build model and classify
			svm_model model = svm.svm_train(problem, parameters);

			return svm.svm_predict( model, test_a );

		}

		public JSONObject getData() {
			this.classify_data = new JSONObject();			

			JSONArray train	= new JSONArray();	
			JSONArray test	= new JSONArray();	

			for ( DataSetRow dsr : nn_training_set.getRows() ) {
				double[] dsr_vector = dsr.getInput();
				double[] dsr_label = dsr.getDesiredOutput();
			
				JSONArray dsr_json = new JSONArray();
				dsr_json.add(dsr_label[0]);

				for (int i = 0; i < dsr_vector.length; i += 1 ) {
					dsr_json.add(dsr_vector[i]);
				}	

				train.add(dsr_json);	

			}
	
			for (int i = 0; i < this.test_vector.length; i += 1 ) {
				test.add(this.test_vector[i]);
			}
		
	
			this.classify_data.put("train", train);
			this.classify_data.put("test", test);
	
			return this.classify_data;
		}

	}

	private class CosineSim {
		private JSONArray u;
		private JSONArray v;
		private double cosine_sim;

		public CosineSim( JSONObject u1, 
				JSONObject v1,
				ArrayList<String> feature_bands,
				ArrayList<String> features) 
		{

			// construct feature vector (using hjorth params)
			this.u = new JSONArray();
			this.v = new JSONArray();
		
			for ( String fb : feature_bands ) {	
				for  ( String f : features ) {
					//System.out.println(String.format("%s:%s", fb, f));
					this.u.add( Double.valueOf( 
						(String) 
						( (JSONObject) ((JSONObject) u1.get(fb)).get("hjorth")).get(f) ));

					this.v.add( Double.valueOf( 
						(String) 
						( (JSONObject) ((JSONObject) v1.get(fb)).get("hjorth")).get(f) ));
				}

			}

			//this.u.add( (double) ((JSONArray) ((JSONObject) u1.get("alpha")).get("x")).size());
			//this.v.add( (double) ((JSONArray) ((JSONObject) v1.get("alpha")).get("x")).size());

			double dot_product      = 0;
			for (int idx = 0; idx < this.v.size(); idx += 1 ) {
				dot_product += ( (double) this.u.get(idx) * 
					(double) this.v.get(idx) );
			}

			// calculate euclidean norms
			
			double u_cv_en    = 0;
			double v_en       = 0;

			for ( Object ud : this.u ) {
				u_cv_en += Math.pow( (double) ud, 2);
			}
			u_cv_en = Math.sqrt(u_cv_en);
			//System.out.println(String.format("euclidean u: %f", u_cv_en));

			for ( Object vd : this.v ) {
				v_en += Math.pow( (double) vd, 2);
			}
			v_en = Math.sqrt(v_en);
			//System.out.println(String.format("euclidean v: %f", v_en));

			// calculate cosine sim
			this.cosine_sim = (double) (dot_product / ( u_cv_en * v_en ));
		}

		private double getMedianMagnitude(JSONArray a, int start, int end) {
			//System.out.println(String.format("getMedianMagnitude(%d, %d, %d)", 
				//a.size(), start, end ));
			// first sort (ascending ), then get the middle (median) element
			JSONArray sorted			= new JSONArray();
			HashMap<Integer,Boolean> skip_idx	= new HashMap<>();

			if (end < 0 ) {
				end = a.size();
			}

			if ( end > a.size() ) {
				return 0.0;
			}

			// loop through find min, then populate sorted
			int min		= Math.abs((int) a.get(start)); // min is the closest value to x axis
			int min_idx	= start;
			skip_idx.put(start,true);
			for (int j = start; j < a.size() && j < end; j += 1) {
				
				for (int i = start; i < a.size() && i < end; i += 1) {
					if (min > (int)a.get(i) && !skip_idx.containsKey(i) ) {
						min = (int) a.get(i);	
						min_idx = i;
					}
				}	

				// add 25 to keep values 0 zero out of calculation
				sorted.add(min + 25);
				skip_idx.put(min_idx, true);	
				
				for (	min_idx = start; min_idx < a.size() && min_idx < end; min_idx += 1 ) { 
					if (!skip_idx.containsKey(min_idx)) {
						min = (int) a.get(min_idx);
						break;
					}
				}


			}		

			/*System.out.println( String.format("sorted() ----- %d==%d",a.size(), sorted.size() ) );
			for (int l = 0; l < sorted.size(); l += 1) {
				System.out.println((int) sorted.get(l));
			}*/
	
			return (int) sorted.get( (int) Math.ceil(sorted.size() / 2.0) );
		
				
			/*int max_a = (int) a.get(0);

			for ( int i = 1; i < a.size(); i += 1) {
				if (max_a < (int) a.get(i) ) {
					max_a = (int) a.get(i);
				}
			}

			return max_a;
			*/

		}

		public double getCosineSim() {
			return this.cosine_sim;
		}

	}

	private class Hjorth {
		double activity;
		double mobility;
		double complexity;
		double std_deviation;
		double mean;
		double skewness;
		double kurtosis;
		double msv;
		int flength;

		public Hjorth(JSONArray y) {

			// calculate mean
			this.mean = 0;
			for ( int idx = 0; idx < y.size(); idx += 1 ) {
				this.mean += (double) ((int) y.get(idx));

			}

			this.mean = this.mean / y.size();

			// calculate mean sample value (aka root mean square)
			// msv = sqrt(sum(y[i]^2 / n))
			this.msv = 0.0;

			for (int i = 0; i < y.size(); i += 1) {
				this.msv += Math.pow((int) y.get(i), 2);	
			}

			this.msv = Math.sqrt(this.msv / y.size());

			// activity = var(y) = variance
			// var(y) = sum((x - mean)^2) / n
			// TODO: this  is calculated as a parameter.  Since this is a sample, shouldn't you use n - 1?
			this.activity = 0;
			for ( int idx = 0; idx < y.size(); idx += 1 ) {
				this.activity += Math.pow( (double)((int) y.get(idx)) - this.mean, 2);
			}
			this.activity = this.activity / y.size();

			// calculate standard deviation	(squareroot of the variance)
			this.std_deviation = Math.sqrt(this.activity / y.size());

			// mobility: Math.sqrt(var(y')/var(y))
			
			
			// calculate first difference/derivative of y (
			//TODO: verify that this is a correct method of finding derivative
			ArrayList<Double> y1 = new ArrayList<Double>();
			for ( int idx = 1, idx1 = 0; idx < y.size(); idx += 1, idx1 += 1 ) {
				y1.add(Double.valueOf( (int) y.get(idx) - (int) y.get(idx1) ));
			}

			//mean of first difference/derivative
			double y1_mean = 0;
			for ( int idx = 0; idx < y1.size(); idx += 1 ) {
				y1_mean += y1.get(idx);				

			}
			y1_mean = y1_mean / y1.size();

			//activity of first difference/derivative
			//TODO: variance should be calculated with n instead of n - 1
			double y1_activity = 0;
			for ( int idx = 0; idx < y1.size(); idx += 1 ) {
				y1_activity += Math.pow( (y1.get(idx) - y1_mean), 2);
			}
			y1_activity = y1_activity / y1.size();

			this.mobility = Math.sqrt(y1_activity / this.activity);

			// complexity = mobility(y')/mobility(y) 

			// calculate second difference/derivative of y (first diff/deriv of y')
			ArrayList<Double> y2 = new ArrayList<Double>();
			for ( int idx = 1, idx1 = 0; idx < y1.size(); idx += 1, idx1 += 1 ) {
				y2.add( Double.valueOf( y1.get(idx) - y1.get(idx1) ) );
			}

			//mean of second difference/derivative
			double y2_mean = 0;
			for ( int idx = 0; idx < y2.size(); idx += 1 ) {
				y2_mean += y2.get(idx);
			}
			y2_mean = y2_mean / y2.size();

			//activity of first difference/derivative
			double y2_activity = 0;
			for ( int idx = 0; idx < y2.size(); idx += 1 ) {
				y2_activity += Math.pow( (y2.get(idx) - y2_mean), 2);
			}
			y2_activity = y2_activity / y2.size();

			double y1_mobility = Math.sqrt( y2_activity / y1_activity );

			this.complexity = y1_mobility / this.mobility;

			//skewness
			//Fisher skewness is the expected value of the cubed Z scores
			//skewness = sum((x - mean)^3) / ( n * std^3 )
			this.skewness = 0;
			// sum(Z^3)
			for ( int idx = 0; idx < y.size(); idx += 1 ) {
				this.skewness += Math.pow(( Double.valueOf((int) y.get(idx) ) - this.mean) / std_deviation, 3);
			}
			// divide by n
			this.skewness = this.skewness / (y.size());

			//kurtosis
			//Pearson's skewness is the expected value of the z scores raised to the 4th power
			//kurtosis = sum(Z^4) / n
			this.kurtosis= 0;
			//sum(Z^4)
			for ( int idx = 0; idx < y.size(); idx += 1 ) {
				this.kurtosis+= Math.pow( ( Double.valueOf((int) y.get(idx) ) - this.mean) / std_deviation, 4);
			}
			//divide by n
			this.kurtosis = this.kurtosis / y.size();

			// size / length
			this.flength = y.size();


		}

		public JSONObject getParameters() {
			JSONObject j = new JSONObject();

			j.put(Workbench.MEAN, String.format("%.2f", this.mean) );
			j.put(Workbench.MSV, String.format("%.2f", this.msv) );
			j.put(Workbench.STD_DEVIATION, String.format("%.2f", this.std_deviation) );
			j.put(Workbench.ACTIVITY, String.format("%.2f", this.activity) );
			j.put(Workbench.MOBILITY, String.format("%.2f", this.mobility) );
			j.put(Workbench.COMPLEXITY, String.format("%.2f", this.complexity) );	
			j.put(Workbench.SKEWNESS, String.format("%.2f", this.skewness) );
			j.put(Workbench.KURTOSIS, String.format("%.2f", this.kurtosis) );	
			j.put(Workbench.LENGTH, String.format("%d", this.flength) );	

			return j;
			
		}	
		

	};

	private class SignalTransformer {
		JSONArray 	eeg;
		JSONArray 	freq_space;
		JSONArray 	freq_magnitudes;
		int		sample_rate;
		int		zero_padding;
		double[] 	raw_transform_data;

		public SignalTransformer(JSONArray raw_eeg) {
			this.eeg		= raw_eeg;
			this.freq_space		= new JSONArray();
			this.freq_magnitudes 	= new JSONArray();
			this.sample_rate	= 100;
			this.zero_padding	= 0;
		}

	};

	private class JwaveTransform extends SignalTransformer {
		private Transform	t;
	
		public JwaveTransform( JSONArray raw_eeg, String wlet) {
			super(raw_eeg);

			if ( wlet.indexOf("fft_jwave") == 0) {
				this.t 	= new Transform( new DiscreteFourierTransform() );
			}
			else if ( wlet.indexOf("haar_jwave") == 0 ) {
				this.t	= new Transform( new FastWaveletTransform( new Haar1() ) );
			}
			else if (wlet.indexOf("db2_jwave") == 0 ) {
				this.t = new Transform( new FastWaveletTransform( new Daubechies2() ) );
			}
			else {
				this.t = new Transform( new FastWaveletTransform( new Daubechies4() ) );
			}

			this._transform();			
		}

		private void _transform() {
			ArrayList<Double> og_raw = new ArrayList<>();

			for( int idx = 0; idx < this.eeg.size(); idx += 1) {
				long i = (long) this.eeg.get(idx);
				og_raw.add( (double) i );
			}

			// pad with zeros to make og_raw.size() a power of 2 (if necessary)
			int p2 = 1;
			int p = 1;
			while ( p < og_raw.size() ) {
				p2 += 1;
				p = (int) Math.pow(2, p2);
			}

			this.zero_padding = p - og_raw.size();
			for ( int j = this.zero_padding; j > 0; j -= 1) {
				og_raw.add(0.0);
			}

			double[] og_raw_arr = new double[og_raw.size()];
			for (int xj = 0; xj < og_raw.size(); xj += 1) {
				og_raw_arr[xj] = og_raw.get(xj);
			}

			this.raw_transform_data = t.forward(og_raw_arr);

			double real;
			double im;
			for(int i = 0; i < this.raw_transform_data.length; i++){
				//real = this.raw_transform_data[i].getReal();
				//im = this.raw_transform_data[i].getImaginary();
				//this.freq_magnitudes.add( (int) Math.sqrt((real * real) + (im*im)) );
				this.freq_magnitudes.add( (int) this.raw_transform_data[i] );

			}

			// calculate frequency space (i.e. x vals)
			for (int idx = 0; idx < raw_transform_data.length; idx += 1) {
				this.freq_space.add( (int) Math.ceil(
				idx * ((double) this.sample_rate / (this.raw_transform_data.length - 1))));
			}		

		}	

		public JSONObject extract( String w ) {

			// return all bands,freq info in json object
			JSONArray _x = new JSONArray();
			JSONArray _y = new JSONArray();
			
			JSONObject result_json = new JSONObject();

			if (w == Workbench.FREQ) {
				// create "represtative" bins, i.e. consolodate multiple 
				// y vals for x  (where the y val chosen is the max for 
				// all values corresponding to x)
				JSONObject freq_json = new JSONObject();

				// only go up to 50hz (eeg signals stop there)
				for ( int current_freq = 0, i = 0; current_freq <= 50; current_freq += 1) {
					int max_freq_val = 0;	
					
					while(current_freq == (int) this.freq_space.get(i)) {
						if (max_freq_val < (int) this.freq_magnitudes.get(i)) {
							max_freq_val = (int) this.freq_magnitudes.get(i);	
						}
						
						i += 1;	
					} 

					_x.add(current_freq);
					_y.add(max_freq_val);

				}
				
				//entire freq space
				/*for ( int i = 0; i < this.freq_space.size(); i += 1) {
					_x.add(i);
					_y.add((int) this.freq_magnitudes.get(i));

				}*/

				freq_json.put("x", _x);
				freq_json.put("y", _y);

				result_json.put(w, freq_json);

			}
			else {
				// get eeg rhythm/band through "windowing" 
				// this is a subset of raw transformed data; go through 
				// freq_space to find which indexes correspond to,
				// e.g. alpha,  8-13 freqs
			
				// default band is delta	
				int win_low = 0;
				int win_high = 4;

				if ( w == Workbench.ALPHA ) {
					win_low	= 8;
					win_high = 13;	
				}
				else if ( w == Workbench.BETA ) {
					win_low = 13;
					win_high = 30;
				}
				else if ( w == Workbench.GAMMA ) {
					win_low = 30;
					win_high = 40;
				}
				else if ( w == Workbench.THETA ) {
					win_low = 4;
					win_high = 8;
				}

				double[] band_raw_transform_data = new double[this.freq_space.size()];
				for (int jdx = 0; jdx < this.freq_space.size(); jdx += 1) {
					if ( (int) this.freq_space.get(jdx) >= win_low && 
						(int) this.freq_space.get(jdx) <= win_high )
					{
						band_raw_transform_data[jdx] = raw_transform_data[jdx];
					}
					else {
						band_raw_transform_data[jdx] = 0.0;
					}
				}
				
				double[] it_band = this.t.reverse(band_raw_transform_data);

				JSONObject band_json = new JSONObject();
				for (int i = 0; i < (it_band.length - this.zero_padding); i += 1) {
					_y.add( (int) it_band[i]);	
					_x.add( i );	
				}

				band_json.put("x", _x);
				band_json.put("y", _y);

				result_json.put(w, band_json);

			}

			return result_json;
		}

	
	};

	//TODO: thorough review of fft functions and implementation
	private class ApacheFFT extends SignalTransformer {
		FastFourierTransformer	fft;
		Complex[] 		fft_og_raw;
	
		public ApacheFFT( JSONArray raw_eeg ) {
			super(raw_eeg);

			this.fft = new FastFourierTransformer(DftNormalization.STANDARD);
			this._fft();			

		}

		public JSONObject extract( String w ) {
			// return all bands,freq info in json object
			JSONArray _x = new JSONArray();
			JSONArray _y = new JSONArray();
			
			JSONObject result_json = new JSONObject();

			if (w == Workbench.FREQ) {
				// create "represtative" bins, i.e. consolodate multiple 
				// y vals for x  (where the y val chosen is the max for 
				// all values corresponding to x)
				JSONObject freq_json = new JSONObject();

				// only go up to 50hz (eeg signals stop there)
				for ( int current_freq = 0, i = 0; current_freq <= 50; current_freq += 1) {
					int max_freq_val = 0;	

					//TODO: find the index out of bounds error
					while(current_freq == (int) this.freq_space.get(i)) {
						if (max_freq_val < (int) this.freq_magnitudes.get(i)) {
							max_freq_val = (int) this.freq_magnitudes.get(i);	
						}
						
						i += 1;	
					} 

					_x.add(current_freq);
					_y.add(max_freq_val);

				}

				freq_json.put("x", _x);
				freq_json.put("y", _y);

				result_json.put(w, freq_json);

			}
			else {

				// get eeg rhythm/band through fft "windowing" 
				// this is a subset of raw fft; go through 
				// freq_space to find which indexes correspond to,
				// e.g. alpha,  8-13 freqs
			
				// default band is delta	
				int win_low = 0;
				int win_high = 4;

				if ( w == Workbench.ALPHA ) {
					win_low	= 8;
					win_high = 13;	
				}
				else if ( w == Workbench.BETA ) {
					win_low = 13;
					win_high = 30;
				}
				else if ( w == Workbench.GAMMA ) {
					win_low = 30;
					win_high = 40;
				}
				else if ( w == Workbench.THETA ) {
					win_low = 4;
					win_high = 8;
				}

				ArrayList<Complex> band_fft_og_raw = new ArrayList<Complex>();
				for (int jdx = 0; jdx < this.freq_space.size(); jdx += 1) {
					if ( (int) this.freq_space.get(jdx) >= win_low && 
						(int) this.freq_space.get(jdx) <= win_high )
					{
						band_fft_og_raw.add( fft_og_raw[jdx] );
					}
					else {
						band_fft_og_raw.add(new Complex(0));
					}
				}
				

				Complex[] band_fft_og_raw_arr = band_fft_og_raw.toArray(
					new Complex[band_fft_og_raw.size()]);

				Complex[] ifft_band = fft.transform(band_fft_og_raw_arr, 
					TransformType.INVERSE);

				JSONObject band_json = new JSONObject();
				for (int i = 0; i < (ifft_band.length - this.zero_padding); i += 1) {
					_y.add( (int) ifft_band[i].getReal() );	
					_x.add( i );	
				}

				band_json.put("x", _x);
				band_json.put("y", _y);

				result_json.put(w, band_json);

			}

			return result_json;
			
		}

		private void _fft() {
			ArrayList<Complex> og_raw = new ArrayList<Complex>();

			for( int idx = 0; idx < this.eeg.size(); idx += 1) {
				long i = (long) this.eeg.get(idx);
				og_raw.add( new Complex( (double) i  ) );
			}

			// pad with zeros to make og_raw.size() a power of 2 (if necessary)
			int p2 = 1;
			int p = 1;
			while ( p < og_raw.size() ) {
				p2 += 1;
				p = (int) Math.pow(2, p2);

			}

			this.zero_padding = p - og_raw.size();
			for ( int j = this.zero_padding; j > 0; j -= 1) {
				og_raw.add(new Complex(0));
			}

			Complex[] og_raw_arr = og_raw.toArray(new Complex[og_raw.size()]);

			this.fft_og_raw = fft.transform(og_raw_arr, TransformType.FORWARD);

			double real;
			double im;
			for(int i = 0; i < this.fft_og_raw.length; i++){
				real = this.fft_og_raw[i].getReal();
				im = this.fft_og_raw[i].getImaginary();
				this.freq_magnitudes.add( (int) Math.sqrt((real * real) + (im*im)) );
			}

			// calculate frequency space (i.e. x vals)
			for (int idx = 0; idx < fft_og_raw.length; idx += 1) {
				this.freq_space.add( (int) Math.ceil(
				idx * ((double) this.sample_rate / (this.fft_og_raw.length - 1))));
			}		

		}	
	};

	private class EEGProcessor {
		private JSONArray eeg_data;
		private JSONArray eeg_false_data; //used as "false" data for training set(s) in classification

		public EEGProcessor(String filename, 
			HashMap<Integer, ArrayList<String>> data_indexes,
			String transform_type ) 
		{
			//System.out.println(String.format("EEGProcessor(%s)", filename));
			this.eeg_data		= new JSONArray();
			this.eeg_false_data	= new JSONArray();
			
			try {
				JSONParser jp = new JSONParser();
				Object fobj = jp.parse(new FileReader(String.format("eeg_data/%s", filename ))); 
				JSONObject jo = (JSONObject) fobj;
			
				JSONArray ja = (JSONArray) jo.get("data");
				for ( int d_idx = 0; d_idx < ja.size(); d_idx += 1 ) {
					JSONObject tja = (JSONObject) ja.get(d_idx);

					// currently only gets single sensor (e.g. FP1)
					// this.eeg_data will need to change to accommodate 
					// multiple sensors (e.g. object has entry for each sensor
					JSONArray tja_sensor_data = (JSONArray) tja.get("FP1");
				
					JSONObject _result = new JSONObject();

					// include raw eeg recording
					JSONObject _raw	= new JSONObject();

					JSONArray _raw_x = new JSONArray();
					for ( int i = 0; i < tja_sensor_data.size(); i += 1 ) {
						_raw_x.add(i);
					}
					
					_raw.put("x", _raw_x);
					_raw.put("y", tja_sensor_data);
					_result.put(Workbench.RAW, _raw);
	
					// default transform will be jwave 
					if ( transform_type.indexOf("_jwave") < 0 ) {
						ApacheFFT eegutil = new ApacheFFT(tja_sensor_data);

						// frequency	
						_result.put(Workbench.FREQ, 
							eegutil.extract(Workbench.FREQ).get(Workbench.FREQ) );	

						// delta 
						JSONObject delta_ = (JSONObject) eegutil.extract(Workbench.DELTA).get(
							Workbench.DELTA);

						Hjorth delta_hjorth = new Hjorth((JSONArray) delta_.get("y"));
						delta_.put("hjorth", delta_hjorth.getParameters());

						_result.put(Workbench.DELTA, delta_);	

						// alpha
						JSONObject alpha_ = (JSONObject) eegutil.extract(Workbench.ALPHA).get(
							Workbench.ALPHA);

						Hjorth alpha_hjorth = new Hjorth((JSONArray) alpha_.get("y"));
						alpha_.put("hjorth", alpha_hjorth.getParameters());

						_result.put(Workbench.ALPHA, alpha_);	
					
						// beta	
						JSONObject beta_ = (JSONObject) eegutil.extract(Workbench.BETA).get(
							Workbench.BETA);

						Hjorth beta_hjorth = new Hjorth((JSONArray) beta_.get("y"));
						beta_.put("hjorth", beta_hjorth.getParameters());

						_result.put(Workbench.BETA, beta_);	

						// gamma
						JSONObject gamma_ = (JSONObject) eegutil.extract(Workbench.GAMMA).get(
							Workbench.GAMMA);

						Hjorth gamma_hjorth = new Hjorth((JSONArray) gamma_.get("y"));
						gamma_.put("hjorth", gamma_hjorth.getParameters());

						_result.put(Workbench.GAMMA, gamma_);	

						// theta
						JSONObject theta_ = (JSONObject) eegutil.extract(Workbench.THETA).get(
							Workbench.THETA);

						Hjorth theta_hjorth = new Hjorth((JSONArray) theta_.get("y"));
						theta_.put("hjorth", theta_hjorth.getParameters());

						_result.put(Workbench.THETA, theta_);	


					}
					else {
						JwaveTransform eegutil = new JwaveTransform(tja_sensor_data,
							transform_type);

						// frequency	
						_result.put(Workbench.FREQ, 
							eegutil.extract(Workbench.FREQ).get(Workbench.FREQ) );	

						// delta 
						JSONObject delta_ = (JSONObject) eegutil.extract(Workbench.DELTA).get(
							Workbench.DELTA);

						Hjorth delta_hjorth = new Hjorth((JSONArray) delta_.get("y"));
						delta_.put("hjorth", delta_hjorth.getParameters());

						_result.put(Workbench.DELTA, delta_);	

						// alpha
						JSONObject alpha_ = (JSONObject) eegutil.extract(Workbench.ALPHA).get(
							Workbench.ALPHA);

						Hjorth alpha_hjorth = new Hjorth((JSONArray) alpha_.get("y"));
						alpha_.put("hjorth", alpha_hjorth.getParameters());

						_result.put(Workbench.ALPHA, alpha_);	
					
						// beta	
						JSONObject beta_ = (JSONObject) eegutil.extract(Workbench.BETA).get(
							Workbench.BETA);

						Hjorth beta_hjorth = new Hjorth((JSONArray) beta_.get("y"));
						beta_.put("hjorth", beta_hjorth.getParameters());

						_result.put(Workbench.BETA, beta_);	

						// gamma
						JSONObject gamma_ = (JSONObject) eegutil.extract(Workbench.GAMMA).get(
							Workbench.GAMMA);

						Hjorth gamma_hjorth = new Hjorth((JSONArray) gamma_.get("y"));
						gamma_.put("hjorth", gamma_hjorth.getParameters());

						_result.put(Workbench.GAMMA, gamma_);	

						// theta
						JSONObject theta_ = (JSONObject) eegutil.extract(Workbench.THETA).get(
							Workbench.THETA);

						Hjorth theta_hjorth = new Hjorth((JSONArray) theta_.get("y"));
						theta_.put("hjorth", theta_hjorth.getParameters());

						_result.put(Workbench.THETA, theta_);	

					}
		
					boolean is_false_data = true;	
					for ( int f_idx : data_indexes.keySet() ) {
						if ( f_idx == d_idx ) {
							is_false_data = false;
						}
					}

					if (is_false_data) {
						//System.out.println(String.format("False data: %d", d_idx));
						this.eeg_false_data.add(_result);

					}
					else {
						//System.out.println(String.format("True data: %d", d_idx));
						this.eeg_data.add(_result);

					}
				}

			} catch(Exception e) {
				e.printStackTrace();
			}



		}

		public JSONArray getEEGData() {
			return this.eeg_data;
		}

		public JSONArray getEEGFalseData() {
			return this.eeg_false_data;
		}

	}

	@SuppressWarnings("serial")
	private class RootServlet extends HttpServlet {
		@Override
		protected void doGet( HttpServletRequest request,
			HttpServletResponse response ) 
		throws ServletException, IOException
		{
			response.sendRedirect( String.format("%s/index.html", static_servletroot) );
		}
	};

	@SuppressWarnings("serial")
	private class JsonServlet extends HttpServlet {

		@Override
		protected void doPost( HttpServletRequest request,
			HttpServletResponse response ) 
		throws ServletException, IOException
		{

			// open source/target json files and return the requested indexes from each
			try {
				JSONParser jp = new JSONParser();
				JSONObject result_json		= new JSONObject();

				JSONObject result_cross		= new JSONObject();
				StringWriter j_swrite		= new StringWriter();

				// source 
				HashMap<Integer,ArrayList<String>> src_datamap = 
					new HashMap<Integer,ArrayList<String>>();

				for ( String idx : request.getParameterValues("src_eegs[]") ) {
					// [0] = sensor(s) i.e. FP1, [1] = dataset index
					String[] data_parts = idx.split("-");  
					
					if ( !src_datamap.containsKey(Integer.valueOf(data_parts[1])) ) {
						src_datamap.put( Integer.valueOf(data_parts[1]), 
							new ArrayList<String>() );
					}

					src_datamap.get(Integer.valueOf(data_parts[1])).add(data_parts[0]);
				}

				EEGProcessor source_ep = new EEGProcessor(request.getParameter("src"),
					src_datamap,
					request.getParameter("prefreq") );

				// source sensor used for cross simularity (only use one sensor/data index)
				String src_cross = request.getParameter("src_cross");	
				if (!src_cross.isEmpty()) {
					String[] cross_parts = src_cross.split("-");
					
					HashMap<Integer,ArrayList<String>> cross_datamap = 
						new HashMap<Integer, ArrayList<String>>();

					cross_datamap.put(Integer.valueOf(cross_parts[1]),
						new ArrayList<String>());

					cross_datamap.get(Integer.valueOf(cross_parts[1])).add(cross_parts[0]);

					EEGProcessor source_cross = new EEGProcessor(request.getParameter("src"),
						cross_datamap,
						request.getParameter("prefreq") );

					result_cross.put("src", 
						( (JSONArray) source_cross.getEEGData()).get(0) );
					
				}


				// target (contains "true" trial(s) when using authentication) 
				HashMap<Integer,ArrayList<String>> tgt_datamap = 
					new HashMap<Integer,ArrayList<String>>();


				for ( String idx : request.getParameterValues("tgt_eegs[]") ) {
					// [0] = sensor(s) i.e. FP1, [1] = dataset index
					String[] data_parts = idx.split("-");  
					
					if ( !tgt_datamap.containsKey(Integer.valueOf(data_parts[1])) ) {
						tgt_datamap.put( Integer.valueOf(data_parts[1]), 
							new ArrayList<String>() );
					}

					tgt_datamap.get(Integer.valueOf(data_parts[1])).add(data_parts[0]);
				}

				EEGProcessor target_ep= new EEGProcessor(request.getParameter("tgt"),
					tgt_datamap,
					request.getParameter("prefreq") );

				// source sensor used for cross simularity (only use one sensor/data index)
				String tgt_cross = request.getParameter("tgt_cross");	
				if (!tgt_cross.isEmpty()) {
					String[] cross_parts = tgt_cross.split("-");
					
					HashMap<Integer,ArrayList<String>> cross_datamap = 
						new HashMap<Integer, ArrayList<String>>();

					cross_datamap.put(Integer.valueOf(cross_parts[1]),
						new ArrayList<String>());

					cross_datamap.get(Integer.valueOf(cross_parts[1])).add(cross_parts[0]);

					EEGProcessor target_cross= new EEGProcessor(request.getParameter("tgt"),
						cross_datamap,
						request.getParameter("prefreq") );

					result_cross.put("tgt", 
						( (JSONArray) target_cross.getEEGData()).get(0) );
					
				}

				// compute cosine simularity between cross src/tgt
				ArrayList<String> feature_bands = new ArrayList<String>();
				for ( String fb : request.getParameterValues("feature_band[]") ) {
					feature_bands.add(fb);
				}
			
				ArrayList<String> features = new ArrayList<String>();
				for ( String f : request.getParameterValues("features[]") ) {
					features.add(f);
				}

				CosineSim cs = new CosineSim( (JSONObject) result_cross.get("src"),
					(JSONObject) result_cross.get("tgt"),
					feature_bands,
					features );

				result_cross.put( "cosine_sim", cs.getCosineSim() );

				// compute classifications
				Classify classifier = new Classify( (JSONObject) result_cross.get("src"),
					(JSONArray) source_ep.getEEGData(),
					(JSONArray) target_ep.getEEGData(),
					feature_bands,
					features );

				result_cross.put( "mlp_class", classifier.classifyMLP() );
				result_cross.put( "svm_class", classifier.classifySVM() );
				result_cross.put( "classify_data", classifier.getData() );
				//result_cross.put( "mlp_class", 0.0 );
				//result_cross.put( "svm_class", 0.0 );
		
				String paction = request.getParameter("action");
				//System.out.println(paction);

				if ( paction.indexOf("export_authjson") == 0 ) {
					result_json.put("auth", "filename.json" );					
				}
				else if ( paction.indexOf("export_matlab") == 0 ) {
					result_json.put("matlab", "filename.matlab" );					
				}
				else {	
					result_json.put("src", (JSONArray) source_ep.getEEGData() );	
					result_json.put("tgt", (JSONArray) target_ep.getEEGData() );	
					result_json.put("cross", (JSONObject) result_cross);


				}

				result_json.writeJSONString(j_swrite);
				
				response.setContentType("application/json");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(j_swrite.toString());
				
				
			} catch(Exception e) {
				e.printStackTrace();
			}

		}


		@Override
		protected void doGet( HttpServletRequest request,
			HttpServletResponse response ) 
		throws ServletException, IOException
		{
			/*System.out.println( "doGet(): " + request.getServletPath() );
			System.out.println( "doGet(): " + request.getQueryString() );
			for( String p : request.getParameterMap().keySet() ) {
				System.out.println("doGet(): " + p + " " + request.getParameter(p) );
			}
			*/
			
			try {
				JSONObject response_json	= new JSONObject();
				JSONObject response_data	= new JSONObject();
				boolean response_status		= false;
				String response_msg		= "Error";
				StringWriter j_swrite		= new StringWriter();

				Set<String> query_keys	= request.getParameterMap().keySet();

				// eeg json data file listing
				if ( query_keys.isEmpty() ) {
					ArrayList<String> json_filenames = new ArrayList<String>();
					Path eeg_dir = FileSystems.getDefault().getPath(eeg_datadir);
					DirectoryStream<Path> eeg_dirstream = Files.newDirectoryStream(
						eeg_dir, "*.{json}");

					for (Path p: eeg_dirstream) {
						json_filenames.add(p.getFileName().toString());
					}

					response_data.put("eeg", json_filenames);
					response_status = true;
					response_msg = "ok";
				} 

				// read specific file
				else if ( query_keys.contains("file") ) {
					String fname = request.getParameter("file");
					String full_fname = String.format("%s/%s", 
						eeg_datadir, fname);
					JSONParser j_parse = new JSONParser();

					if ( Files.exists(Paths.get(full_fname)) ) {
						FileReader j_fread =  new FileReader(full_fname);

						response_data = (JSONObject) j_parse.parse(j_fread);
						response_status = true;
						response_msg = "ok";
					}
				}	

				response_json.put("status", response_status);	
				response_json.put("msg", response_msg);	
				response_json.put("data", response_data);	
				response_json.writeJSONString(j_swrite);				

				response.setContentType("application/json");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(j_swrite.toString());

			} catch ( Exception e ) {
				e.printStackTrace();
			}	


		}
	};

	public static void main(String a[]) { 
		try {
			Workbench w = new Workbench(); 
			w.run(a);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void run (String a[]) throws Exception {
		//System.setProperty("org.eclipse.jetty.LEVEL","INFO");

		// main servlet context
		ServletContextHandler scontext = new ServletContextHandler(
			ServletContextHandler.SESSIONS);

		scontext.setContextPath("/");
		scontext.addServlet(new ServletHolder(new Workbench.RootServlet()), "/*" );
		scontext.addServlet(new ServletHolder(new Workbench.JsonServlet()), 
			String.format("%s/*", json_servletroot) );

		// static html/css/js
		ServletHolder holderStatic = new ServletHolder("app-static", DefaultServlet.class);
		holderStatic.setInitParameter("resourceBase", app_staticdir);
		holderStatic.setInitParameter("dirAllowed","true");
		holderStatic.setInitParameter("pathInfoOnly","true");
		scontext.addServlet(holderStatic, String.format("%s/*", static_servletroot) );

		// initialize server
		Server server = new Server(app_port);

		server.setHandler(scontext);
		server.start();
		server.join();
	}

}
