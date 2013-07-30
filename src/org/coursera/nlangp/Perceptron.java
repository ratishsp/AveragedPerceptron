/**
 * 
 */
package org.coursera.nlangp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ratish
 *
 */
public class Perceptron {
	private static final String SEPARATOR = ":";
	private static final String GENE_TRAIN = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train";
	private static final String SPACE = " ";
	private static final String O = "O";
	private static final String I_GENE = "I-GENE";
	private static final String STAR = "*";
	public static void main(String[] args) {
		Perceptron perceptron = new Perceptron();
		perceptron.execute();
//		Map<String,Double> vMap = new HashMap<String,Double>();
//		List<String> input = new ArrayList<String>();
//		input.add("Atherosclerosis");
//		try {
//			perceptron.viterbiImplementation(input, vMap);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private void execute(){
		Map<String,Double> vMap = new HashMap<String,Double>();
		for(int i =0; i<5; i++){
			//iterate through the training set
			//for each sentence
			//compute the most likely tag sequence using Viterbi algorithm
			//best tagging feature vector f(xi,zi)
			//gold tagging feature vector f(xi,yi)
			//v = v + f(xi,yi) - f(xi,zi)
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(GENE_TRAIN)));
				String readLine = null;
				List<String> input = new ArrayList<String>();
				List<String> tags = null;
				List<String> goldStandardTags = new ArrayList<String>();
				while((readLine = br.readLine())!= null){
					if(readLine.length()!=0){
						input.add(readLine.split(SPACE)[0]);
						goldStandardTags.add(readLine.split(SPACE)[1]);
					}else{
						tags = viterbiImplementation(input, vMap);
						Map<String, Double> fxizi = getFValue(input, tags);
						Map<String, Double> fxiyi = getFValue(input, goldStandardTags);
						updateV(vMap, fxiyi, fxizi);
						//f(xi,zi)
						//f(xi,yi)
						input = new ArrayList<String>();
						goldStandardTags = new ArrayList<String>();						
					}
				}
				tags = viterbiImplementation(input, vMap);
				Map<String, Double> fxizi = getFValue(input, tags);
				Map<String, Double> fxiyi = getFValue(input, goldStandardTags);
				updateV(vMap, fxiyi, fxizi);
				
				br.close();
				System.out.println("vMap "+vMap);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}

	
	private Map<String, Double> updateV(Map<String, Double> vMap,
			Map<String, Double> fxiyi, Map<String, Double> fxizi) {

		Set<Map.Entry<String, Double>> entrySet = fxiyi.entrySet();
		for(Map.Entry<String, Double> entry:entrySet){
			String key = entry.getKey();
			Double value = entry.getValue();
			if(vMap.containsKey(key)){
				vMap.put(key, vMap.get(key)+value);
			}else{
				vMap.put(key, value);
			}
		}
		entrySet = fxizi.entrySet();
		for(Map.Entry<String, Double> entry:entrySet){
			String key = entry.getKey();
			Double value = entry.getValue();
			if(vMap.containsKey(key)){
				vMap.put(key, vMap.get(key)-value);
			}else{
				vMap.put(key, -1*value);
			}
		}
		return vMap;
	}

	private Map<String, Double> getFValue(List<String> input, List<String> tags){
		Map<String, Double> f = new HashMap<String, Double>();
		List<String> updatedListTags = new ArrayList<String>();
		updatedListTags.add(STAR);
		updatedListTags.add(STAR);
		updatedListTags.addAll(tags);
		for(int i=2; i< updatedListTags.size(); i++){
			String w = updatedListTags.get(i-2);
			String u = updatedListTags.get(i-1);
			String v = updatedListTags.get(i);
			String key = w+SEPARATOR+u+SEPARATOR+v;
			if(f.containsKey(key)){
				f.put(key, f.get(key)+1);	
			}else{
				f.put(key, 1d);
			}
			
		}
		return f;
	}
	
	private List<String> viterbiImplementation(List<String> input,Map<String, Double> vMap) throws IOException {
		Map<Kuv, Double> pi = new HashMap<Kuv, Double>();
		Map<Kuv, Kuv> bp = new HashMap<Kuv, Kuv>();
		List<String> t = new ArrayList<String>();
		t.add(STAR);
		t.add(I_GENE);
		t.add(O);
		
		List<String> s = new ArrayList<String>();
		s.add(I_GENE);
		s.add(O);
		for(int a = 0; a< t.size(); a++){
			for(int b=0; b<t.size(); b++){
				pi.put(new Kuv(0, t.get(a), t.get(b)), 0d);		
			}
		}
		
		for(int k=0; k<input.size(); k++){
			for(int a = 0; a< t.size(); a++){
				for(int b=0; b<s.size(); b++){
					String u = t.get(a);
					String v = s.get(b);
					Kuv kuv = new Kuv(k+1, u, v);
					
					Double maxValue = Double.NEGATIVE_INFINITY;
					Kuv maxKuv = null;
					for(int l=0; l< t.size(); l++){
						String w = t.get(l);
						if(k==0 && !(u.equals(STAR) && w.equals(STAR))){
							continue;
						}
						
						if(k==1 && u.equals(STAR)){
							continue;
						}
						if(k>1 && (u.equals(STAR) || w.equals(STAR))){
							continue;
						}
						Kuv kuvTemp = new Kuv(k, w, u);
						double piKMinus1 = 0d;
						if(pi.containsKey(kuvTemp)){
							piKMinus1 = pi.get(kuvTemp);
						}
						
						String x = input.get(k);
						String trigramFeature =w+SEPARATOR+u+SEPARATOR+v;
						Double trigram = 0d;
						if(vMap.containsKey(trigramFeature)){
							trigram = vMap.get(trigramFeature);							
						}
						
						double piValue = piKMinus1 + trigram;
						if(maxValue <= piValue){
							maxValue = piValue;
							maxKuv = kuvTemp;
						}
//						System.out.println("inter piValue "+piValue+" piKMinus1 "+piKMinus1+" "+ trigramFeature + " trigram value "+ trigram );
					}
					pi.put(kuv, maxValue);
//					System.out.println("kuv "+kuv+ "maxValue "+maxValue);
					bp.put(kuv, maxKuv);
//					System.out.println("kuv "+kuv+ "maxKuv "+maxKuv);
				}
			}
		}
		
		int n = input.size();
		double maxPi = Double.NEGATIVE_INFINITY;
		Kuv maxKuv = null;
		for(int a = 0; a< t.size(); a++){
			for(int b=0; b<t.size(); b++){
				String u = t.get(a);
				String v = t.get(b);
				Kuv kuvTemp = new Kuv(n, u, v);
				
				Double kuvValue = Double.NEGATIVE_INFINITY;
				if(pi.containsKey(kuvTemp)){
					kuvValue = pi.get(kuvTemp);
				}
				
				String trigramFeatureStop = u+SEPARATOR+v+":STOP";
				Double trigramStop = 0d;
				if(vMap.containsKey(trigramFeatureStop)){
					trigramStop = vMap.get(trigramFeatureStop);
				}
				double value = kuvValue + trigramStop;
				if(maxPi <= value){
					maxPi = value;
					maxKuv = kuvTemp;
				}
			}
		}
		System.out.println("highestKuv "+maxKuv + " maxPi "+maxPi);
		List<Kuv> kuvs = new ArrayList<Kuv>();
		kuvs.add(maxKuv);
		while(bp.get(maxKuv) != null){
			maxKuv = bp.get(maxKuv);
			kuvs.add(maxKuv);
		}
//		System.out.println("bp"+bp);
//		System.out.println("kuvs "+kuvs);

		Collections.reverse(kuvs);
		List<String> tags = new ArrayList<String>();
		for(int k=0; k<input.size(); k++){
			tags.add(kuvs.get(k+1).getV());
		}
		
		return tags;
	}
}
