/**
 * 
 */
package org.coursera.nlangp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
	private static final String SUFF_FEATURE = "SUFF:";
	private static final String TAG_FEATURE = "TAG:";
	private static final String TRIGRAM_FEATURE = "TRIGRAM:";
	private static final String STOP = "STOP";
	private static final String SEPARATOR = ":";
	private static final String GENE_TRAIN = "/home/ratish/project/study/nlp/h4-assignment/gene.train";
	private static final String GENE_MODEL_GEN = "/home/ratish/project/study/nlp/h4-assignment/tag.modelgen";
	private static final String SPACE = " ";
	private static final String GENE_DEV_P3_OUT = "/home/ratish/project/study/nlp/h4-assignment/gene_dev.p3.out";
	private static final String SAMPLE_SENTENCE = "/home/ratish/project/study/nlp/h4-assignment/sampleSentence";
	private static final String GENE_DEV = "/home/ratish/project/study/nlp/h4-assignment/gene.dev";
	private static final String GENE_DEV1 = "/home/ratish/project/study/nlp/h4-assignment/gene.dev1";
	private static final String O = "O";
	private static final String I_GENE = "I-GENE";
	private static final String STAR = "*";
	private static final String GENE_MODEL_GEN_AVG = "/home/ratish/project/study/nlp/h4-assignment/tag.modelgenavg";
	private static final String GENE_MODEL_GEN_AVG_OPTIM = "/home/ratish/project/study/nlp/h4-assignment/tag.modelgenavgoptim";
	private Map<String, String> wordClusters;
	public static void main(String[] args) {
		Perceptron perceptron = new Perceptron();
		perceptron.loadWordClusters();
		perceptron.train();
		Map<String,Double> vMap = new HashMap<String,Double>();
		perceptron.loadV(vMap);
		perceptron.decode(vMap);
//		perceptron.sampleSentence();
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
	
	private Map<String, String> loadWordClusters() {
		String filepath = "/home/ratish/Downloads/word-clusters";
		wordClusters = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String readLine = null;
			while((readLine = br.readLine())!= null){
				String [] args = readLine.split("\t");
				wordClusters.put(args[1], args[0]);
			}
			br.close();
			return wordClusters;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

/*	private void sampleSentence(){
		Map<String,Double> vMap = new HashMap<String,Double>();
		List<String> input = new ArrayList<String>();
		input.add("Comparison");
		input.add("with");
		input.add("alkaline");
		input.add("phosphatases");
		input.add("and");
		input.add("5");
		input.add("-");
		input.add("nucleotidase");
//		input.add("Characteristics");
//		input.add("of");
//		input.add("lipase");
//		input.add("activity");
//		input.add(".");
		
		input.add("Pharmacologic");
		input.add("aspects"); 
		input.add("of"); 
		input.add("neonatal"); 
		input.add("hyperbilirubinemia"); 
		input.add("."); 
		
		List<String> goldStandardTags = new ArrayList<String>();
		goldStandardTags.add(O);
		goldStandardTags.add(O);
		goldStandardTags.add(I_GENE);
		goldStandardTags.add(I_GENE);
		goldStandardTags.add(O);
		goldStandardTags.add(I_GENE);
		goldStandardTags.add(I_GENE);
		goldStandardTags.add(I_GENE);

		goldStandardTags.add(O);
		goldStandardTags.add(O);
		goldStandardTags.add(O);
		goldStandardTags.add(O);
		goldStandardTags.add(O);
		goldStandardTags.add(O);
		
//		goldStandardTags.add(O);
//		goldStandardTags.add(O);
//		goldStandardTags.add(I_GENE);
//		goldStandardTags.add(O);
//		goldStandardTags.add(O);
		try {
			for(int i=0; i<5;i++){
				System.out.println("iteration "+(i+1));
				List<String> outputTags = viterbiImplementation(input, vMap);
				Map<String, Double> fxizitags = getFValue(input, outputTags);
				Map<String, Double> fxiyitags = getFValue(input, goldStandardTags);
				System.out.println(i);
				System.out.println("v[trigram:o:o:o] "+vMap.get("TRIGRAM:O:O:O"));
				System.out.println("b[trigram:o:o:o] "+fxizitags.get("TRIGRAM:O:O:O"));
				System.out.println("g[trigram:o:o:o] "+fxiyitags.get("TRIGRAM:O:O:O"));
				updateV(vMap, fxiyitags, fxizitags);
				System.out.println("v[trigram:o:o:o] "+vMap.get("TRIGRAM:O:O:O"));
//				System.out.println("fxiyitags "+fxiyitags);
//				System.out.println("fxizitags "+fxizitags);
//				System.out.println("vmap "+vMap);		
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	
	private void train(){
		try{
			Map<String,Double> vMap = new HashMap<String,Double>();
//			Map<String,Double> vMapAvg = new HashMap<String,Double>();
			Map<String,AvgValue> vMapAvg2 = new HashMap<String,AvgValue>();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_MODEL_GEN)));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(GENE_MODEL_GEN_AVG_OPTIM)));
			int l = 0;
			for(int i =0; i<6; i++){
				//iterate through the training set
				//for each sentence
				//compute the most likely tag sequence using Viterbi algorithm
				//best tagging feature vector f(xi,zi)
				//gold tagging feature vector f(xi,yi)
				//v = v + f(xi,yi) - f(xi,zi)
				
					BufferedReader br = new BufferedReader(new FileReader(new File(GENE_TRAIN)));
					String readLine = null;
					List<String> input = new ArrayList<String>();
					List<String> tags = null;
//					List<String> outputTags = null;
	//				List<String> tags = null;
					List<String> goldStandardTags = new ArrayList<String>();
					while((readLine = br.readLine())!= null){
						l++;
//						System.out.println("readline "+readLine);
						if(readLine.length()!=0){
							input.add(readLine.split(SPACE)[0]);
							goldStandardTags.add(readLine.split(SPACE)[1]);
						}else{
//							l++;
							tags = viterbiImplementation(input, vMap);
//							outputTags = viterbiImplementation(input, vMap, "tag");
							Map<String, Double> fxizi = new HashMap<String, Double>();
							getFValue(input, tags, fxizi);
							getFValue(input, tags, fxizi,"tag");
							getFValue(input, tags, fxizi, "", "suff");
							getFValue(input, tags, fxizi, "", "", "pref");
							Map<String, Double> fxiyi = new HashMap<String,Double>();
							getFValue(input, goldStandardTags, fxiyi);
							getFValue(input, goldStandardTags, fxiyi, "tag");
							getFValue(input, goldStandardTags, fxiyi, "", "suff");
							getFValue(input, goldStandardTags, fxiyi, "", "", "pref");
//							System.out.println(l-1);
//							System.out.println("v[trigram:o:o:o] "+vMap.get("TRIGRAM:O:O:O"));
//							System.out.println("b[trigram:o:o:o] "+fxizitrigram.get("TRIGRAM:O:O:O"));
//							System.out.println("g[trigram:o:o:o] "+fxiyitrigram.get("TRIGRAM:O:O:O"));
							updateV(vMap, fxiyi, fxizi);
//							updateVmapAvg(vMap,vMapAvg);
							updateVmapAvg(vMap,vMapAvg2, fxiyi,fxizi);
//							System.out.println("v[trigram:o:o:o] "+vMap.get("TRIGRAM:O:O:O"));
//							Map<String, Double> fxizitags = getFValue(input, outputTags, "tag");
//							Map<String, Double> fxiyitags = getFValue(input, goldStandardTags, "tag");
//							updateV(vMap, fxiyitags, fxizitags);
							//f(xi,zi)
							//f(xi,yi)
							input = new ArrayList<String>();
							goldStandardTags = new ArrayList<String>();						
						}
					}
					tags = viterbiImplementation(input, vMap);
//					outputTags = viterbiImplementation(input, vMap, "tag");
					Map<String, Double> fxizi = new HashMap<String, Double>();
					
					getFValue(input, tags, fxizi);
					getFValue(input, tags, fxizi, "tag");
					getFValue(input, tags, fxizi, "", "suff");
					getFValue(input, tags, fxizi, "", "", "pref");
					Map<String, Double> fxiyi = new HashMap<String,Double>();
					getFValue(input, goldStandardTags, fxiyi);
					getFValue(input, goldStandardTags, fxiyi, "tag");
					getFValue(input, goldStandardTags, fxiyi, "", "suff");
					getFValue(input, goldStandardTags, fxiyi, "", "","pref");
//					System.out.println("v[trigram:o:o:o] "+vMap.get("TRIGRAM:O:O:O"));
//					System.out.println("b[trigram:o:o:o] "+fxizitrigram.get("TRIGRAM:O:O:O"));
//					System.out.println("g[trigram:o:o:o] "+fxiyitrigram.get("TRIGRAM:O:O:O"));					
					updateV(vMap, fxiyi, fxizi);
//					System.out.println("v[trigram:o:o:o] "+vMap.get("TRIGRAM:O:O:O"));
//					Map<String, Double> fxizitags = getFValue(input, outputTags, "tag");
//					Map<String, Double> fxiyitags = getFValue(input, goldStandardTags, "tag");
//					updateV(vMap, fxiyitags, fxizitags);
//					updateVmapAvg(vMap,vMapAvg);
					updateVmapAvg(vMap,vMapAvg2, fxiyi,fxizi);
					br.close();
//					System.out.println("fxiyi "+fxiyi);
//					System.out.println("fxizi "+fxizi);
//					System.out.println("vMap "+vMap);
					
					System.out.println("i "+i);
			
			}
			Set<Map.Entry<String, Double>> entrySet = vMap.entrySet();
			for (Map.Entry<String, Double> entry : entrySet) {
				bw.write(entry.getKey()+SPACE+entry.getValue());
				bw.newLine();
			}
			
			Set<Map.Entry<String, AvgValue>>entrySet2 = vMapAvg2.entrySet();
			for (Map.Entry<String, AvgValue> entry : entrySet2) {
				AvgValue value = entry.getValue();
				bw2.write(entry.getKey()+SPACE+(value.getD()/value.getCount()));
				bw2.newLine();
			}
			
			bw.close();
			bw2.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void updateVmapAvg(Map<String, Double> vMap,
			Map<String, AvgValue> vMapAvg, Map<String, Double> fxiyi,
			Map<String, Double> fxizi) {
		Set<Map.Entry<String, Double>> entrySet = fxiyi.entrySet();
		Map<String,Object> changed = new HashMap<String, Object>();
		for(Map.Entry<String, Double> entry:entrySet){
			String key = entry.getKey();
			changed.put(key, new Object());
		}
		
		entrySet = fxizi.entrySet();
		for(Map.Entry<String, Double> entry:entrySet){
			String key = entry.getKey();
			changed.put(key, new Object());
		}
		
		Set<Map.Entry<String, Object>> changedSet =  changed.entrySet();
		for(Map.Entry<String, Object> changedSetEntry: changedSet){
			String key = changedSetEntry.getKey();
			if(vMapAvg.containsKey(key)){
				AvgValue avg = vMapAvg.get(key);
				Double sum = avg.getD() + vMap.get(key);
				int count = avg.getCount() +1;
				vMapAvg.put(key, new AvgValue(sum,count));
			}else{
				vMapAvg.put(key, new AvgValue(vMap.get(key), 1));
			}
		}
		
	}

	private void updateVmapAvg(Map<String, Double> vMap, Map<String, Double> vMapAvg) {
		Set<Map.Entry<String, Double>> entrySet = vMap.entrySet();
		for(Map.Entry<String, Double> entry:entrySet){
			String key = entry.getKey();
			Double value = entry.getValue();
			if(vMapAvg.containsKey(key)){
				vMapAvg.put(key, value+vMapAvg.get(key));
			}else{
				vMapAvg.put(key, value);
			}
		}
	}

	private void decode(Map<String, Double> vMap){
		try {
			BufferedReader br1 = new BufferedReader(new FileReader(new File(GENE_DEV)));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(GENE_DEV_P3_OUT)));
			String readLine = null;
			List<String> input = new ArrayList<String>();
			List<String>  tags = null;
			while((readLine = br1.readLine())!= null){
				if(readLine.length()!=0){
				input.add(readLine);
				}else{
					tags = viterbiImplementation(input,vMap);
					writeDevTags(bw1, input, tags);
					input = new ArrayList<String>();
				}
			}
			tags = viterbiImplementation(input, vMap);
			writeDevTags(bw1, input, tags);
			br1.close();
			bw1.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadV(Map<String, Double> vMap) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_MODEL_GEN_AVG_OPTIM)));
			String readLine = null;
			while((readLine = br.readLine())!= null){
				String [] args = readLine.split(SPACE);
				vMap.put(args[0], Double.parseDouble(args[1]));
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeDevTags(BufferedWriter bw1, List<String> input,
			List<String> tags) throws IOException {
		for(int i = 0; i< input.size(); i++){
			bw1.write(input.get(i)+SPACE+ tags.get(i));
			bw1.newLine();
		}
		bw1.newLine();
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

	private Map<String, Double> getFValue(List<String> input, List<String> tags, Map<String, Double> f){
//		Map<String, Double> f = new HashMap<String, Double>();
		List<String> updatedListTags = new ArrayList<String>();
		updatedListTags.add(STAR);
		updatedListTags.add(STAR);
		updatedListTags.addAll(tags);
		updatedListTags.add(STOP);
		for(int i=2; i< updatedListTags.size(); i++){
			String w = updatedListTags.get(i-2);
			String u = updatedListTags.get(i-1);
			String v = updatedListTags.get(i);
			String key = TRIGRAM_FEATURE+w+SEPARATOR+u+SEPARATOR+v;
			setOrIncrement(f, key);
			
		}
		return f;
	}
	
	private Map<String, Double> getFValue(List<String> input, List<String> tags, Map<String, Double> f,String feature){
//		Map<String, Double> f = new HashMap<String, Double>();
		for(int i=0; i< input.size(); i++){
			String key = TAG_FEATURE+input.get(i)+SEPARATOR+tags.get(i);
			setOrIncrement(f, key);
		}
		return f;
	}
	
	private Map<String, Double> getFValue(List<String> input, List<String> tags, Map<String, Double> f,String feature, String feature2){
//		Map<String, Double> f = new HashMap<String, Double>();
		for(int i=0; i< input.size(); i++){
			String x = input.get(i);
			int len = x.length();
			String tag = tags.get(i);
			if(len>0){
				String key = suffix1(x, len, tag);
				setOrIncrement(f, key);
			}
			if(len >1){
				String key = suffix2(tag, x, len);
				setOrIncrement(f, key);
			}
			if(len >2){
				String key = suffix3(tag, x, len);
				setOrIncrement(f, key);
			}
		}
		return f;
	}
	
	private Map<String, Double> getFValue(List<String> input, List<String> tags, Map<String, Double> f,String feature, String feature2, String prefbitstring){
		for(int i=0; i< input.size(); i++){
			String x = input.get(i);
			String bitString = wordClusters.get(x);
			String tag = tags.get(i);
			setOrIncrement(f, bitString, tag,8, "CURR");
			setOrIncrement(f, bitString, tag,12, "CURR");
			setOrIncrement(f, bitString, tag,16, "CURR");
			setOrIncrement(f, bitString, tag,20, "CURR");
		}
		
		for(int i=0; i< input.size()-1; i++){
			String prev = input.get(i);
			String bitString = wordClusters.get(prev);
			String tag = tags.get(i+1);
			
			setOrIncrement(f, bitString, tag,8, "PREV");
			setOrIncrement(f, bitString, tag,12, "PREV");
			setOrIncrement(f, bitString, tag,16, "PREV");
			setOrIncrement(f, bitString, tag,20, "PREV");	
		}
		
		for(int i=0; i< input.size()-1; i++){
			String next = input.get(i+1);
			String bitString = wordClusters.get(next);
			String tag = tags.get(i);

			setOrIncrement(f, bitString, tag,8, "NEXT");
			setOrIncrement(f, bitString, tag,12, "NEXT");
			setOrIncrement(f, bitString, tag,16, "NEXT");
			setOrIncrement(f, bitString, tag,20, "NEXT");
		}
		
		return f;
	
	}

	private void setOrIncrement(Map<String, Double> f, String bitString,
			String tag, int prefLength, String prefType) {
		if(bitString!= null && bitString.length()>prefLength){
			String key=getPrefixFeature(tag, bitString, prefLength, prefType);
			setOrIncrement(f, key);
		}
	}

	private void setOrIncrement(Map<String, Double> f, String key) {
		if(f.containsKey(key)){
			f.put(key, f.get(key)+1);	
		}else{
			f.put(key, 1d);
		}
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
						double piKMinus1 = Double.NEGATIVE_INFINITY;
						if(pi.containsKey(kuvTemp)){
							piKMinus1 = pi.get(kuvTemp);
						}
						
						String trigramFeature =TRIGRAM_FEATURE+w+SEPARATOR+u+SEPARATOR+v;
						Double trigram = 0d;
						if(vMap.containsKey(trigramFeature)){
							trigram = vMap.get(trigramFeature);							
						}
						String x = input.get(k);
						String tagFeature = TAG_FEATURE+x+SEPARATOR+v;
						Double tag = 0d;
						if(vMap.containsKey(tagFeature)){
							tag = vMap.get(tagFeature);							
						}
						
						//suffix feature is of form u:j:v
						//u is suffix
						//j is length of suffix
						//v is the tag
						Double suffix1 = 0d;
						Double suffix2 = 0d;
						Double suffix3 = 0d;
						int len = x.length();
						String suffixFeature1 = null;
						String suffixFeature2 = null;
						String suffixFeature3 = null;
						if(len>0){
							suffixFeature1 = suffix1(x, len, v);
							if(vMap.containsKey(suffixFeature1)){
								suffix1 = vMap.get(suffixFeature1);
							}
						}
						
						if(len >1){
							suffixFeature2 = suffix2(v, x, len);
							if(vMap.containsKey(suffixFeature2)){
								suffix2 = vMap.get(suffixFeature2);
							}
						}
						if(len >2){
							suffixFeature3 = suffix3(v, x, len);
							if(vMap.containsKey(suffixFeature3)){
								suffix3 = vMap.get(suffixFeature3);
							}
						}
						
						Double tagPref8CurrWordValue = 0d;
						Double tagPref12CurrWordValue = 0d;
						Double tagPref16CurrWordValue = 0d;
						Double tagPref20CurrWordValue = 0d;
						Double tagPref8PrevWordValue = 0d;
						Double tagPref12PrevWordValue = 0d;
						Double tagPref16PrevWordValue = 0d;
						Double tagPref20PrevWordValue = 0d;
						Double tagPref8NextWordValue = 0d;
						Double tagPref12NextWordValue = 0d;
						Double tagPref16NextWordValue = 0d;
						Double tagPref20NextWordValue = 0d;
						if(wordClusters.containsKey(x)){
							String bitString = wordClusters.get(x);
							tagPref8CurrWordValue =  tagPrefixWord(vMap, w, bitString,8,"CURR");
							tagPref12CurrWordValue = tagPrefixWord(vMap, w, bitString,12,"CURR");
							tagPref16CurrWordValue =  tagPrefixWord(vMap, w, bitString,16,"CURR");
							tagPref20CurrWordValue = tagPrefixWord(vMap, w, bitString,20,"CURR");
							
//							System.out.println("x "+x+" bitstring "+bitString+ " 8 "+tagPref8CurrWordValue+ " 12 "+tagPref12CurrWordValue+ " 16 "+tagPref16CurrWordValue+ " 20 "+tagPref20CurrWordValue);
						}
						
						if(k>0){
							String xMinus1 = input.get(k-1);
							if(wordClusters.containsKey(xMinus1)){
								String bitString = wordClusters.get(xMinus1);
								tagPref8PrevWordValue =  tagPrefixWord(vMap, w, bitString,8,"PREV");
								tagPref12PrevWordValue = tagPrefixWord(vMap, w, bitString,12,"PREV");
								tagPref16PrevWordValue =  tagPrefixWord(vMap, w, bitString,16,"PREV");
								tagPref20PrevWordValue = tagPrefixWord(vMap, w, bitString,20,"PREV");
//								System.out.println("prev "+xMinus1+" bitstring "+bitString+ " 8 "+tagPref8PrevWordValue+ " 12 "+tagPref12PrevWordValue+ " 16 "+tagPref16PrevWordValue+ " 20 "+tagPref20PrevWordValue);
							}
							
						}
						
						if(k<input.size()-1){
							String xPlus1 = input.get(k+1);
							if(wordClusters.containsKey(xPlus1)){
								String bitString = wordClusters.get(xPlus1);
								tagPref8NextWordValue =  tagPrefixWord(vMap, w, bitString,8,"NEXT");
								tagPref12NextWordValue = tagPrefixWord(vMap, w, bitString,12,"NEXT");
								tagPref16NextWordValue =  tagPrefixWord(vMap, w, bitString,16,"NEXT");
								tagPref20NextWordValue = tagPrefixWord(vMap, w, bitString,20,"NEXT");
//								System.out.println("next "+xPlus1+" bitstring "+bitString+ " 8 "+tagPref8NextWordValue+ " 12 "+tagPref12NextWordValue+ " 16 "+tagPref16NextWordValue+ " 20 "+tagPref20NextWordValue);
							}
						}
						
						double piValue = piKMinus1 + trigram + tag + suffix1 + suffix2 + suffix3
								+ tagPref8CurrWordValue+tagPref12CurrWordValue+tagPref16CurrWordValue+tagPref20CurrWordValue
								+ tagPref8PrevWordValue+tagPref12PrevWordValue+tagPref16PrevWordValue+tagPref20PrevWordValue
								+ tagPref8NextWordValue+tagPref12NextWordValue+tagPref16NextWordValue+tagPref20NextWordValue;
						if(maxValue <= piValue){
							maxValue = piValue;
							maxKuv = kuvTemp;
						}
//						System.out.println("inter piValue "+piValue+" piKMinus1 "+piKMinus1+" "+ trigramFeature + " trigram value "+ trigram +" "+ tagFeature+" "+tag+ " "+suffixFeature1 + " "+suffix1 +" "+suffixFeature2 + " "+ suffix2 +" "+suffixFeature3 + " "+ suffix3);
//						System.out.println("");
					}
					pi.put(kuv, maxValue);
					bp.put(kuv, maxKuv);
//					System.out.println("kuv "+kuv+ "maxValue "+maxValue);
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
				
				String trigramFeatureStop =TRIGRAM_FEATURE+ u+SEPARATOR+v+":STOP";
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
//		System.out.println("highestKuv "+maxKuv + " maxPi "+maxPi);
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

	private Double tagPrefixWord(Map<String, Double> vMap, String w,
			String bitString, int prefixLength, String wordType) {
		Double tagPrefWordValue=0d;
		if(bitString.length()>=prefixLength){
			String tagPrefCurrWord = getPrefixFeature(w, bitString, prefixLength, wordType);
			if(vMap.containsKey(tagPrefCurrWord)){
				tagPrefWordValue = vMap.get(tagPrefCurrWord);
			}
		}
		return tagPrefWordValue;
	}

	private String getPrefixFeature(String w, String bitString,
			int prefixLength, String wordType) {
		return "TAGPREF"+prefixLength+wordType+SEPARATOR+w+SEPARATOR+bitString.substring(0,prefixLength);
	}

	private String suffix3(String v, String x, int len) {
		String suffixFeature = SUFF_FEATURE+x.substring(len-3)+SEPARATOR+3+SEPARATOR+v;
		return suffixFeature;
	}

	private String suffix2(String v, String x, int len) {
		String suffixFeature = SUFF_FEATURE+x.substring(len-2)+SEPARATOR+2+SEPARATOR+v;
		return suffixFeature;
	}

	private String suffix1(String x, int len, String v) {
		String key = SUFF_FEATURE+x.substring(len-1)+SEPARATOR+1+SEPARATOR+v;
		return key;
	}
	
	private List<String> viterbiImplementation(List<String> input,Map<String, Double> vMap, String feature) throws IOException {
		Map<Kuv, Double> pi = new HashMap<Kuv, Double>();
		Map<Kuv, Kuv> bp = new HashMap<Kuv, Kuv>();
		List<String> t = new ArrayList<String>();
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
						
						Kuv kuvTemp = new Kuv(k, w, u);
						double piKMinus1 = Double.NEGATIVE_INFINITY;
						if(pi.containsKey(kuvTemp)){
							piKMinus1 = pi.get(kuvTemp);
						}
						
						String x = input.get(k);
//						String trigramFeature ="TAG:"+w+SEPARATOR+u+SEPARATOR+v;
						String tagFeature = TAG_FEATURE+x+SEPARATOR+v;
						Double tag = 0d;
						if(vMap.containsKey(tagFeature)){
							tag = vMap.get(tagFeature);							
						}
						
						double piValue = piKMinus1 + tag;
						if(maxValue <= piValue){
							maxValue = piValue;
							maxKuv = kuvTemp;
						}
//						System.out.println("inter piValue "+piValue+" piKMinus1 "+piKMinus1+" "+ tagFeature + " tag value "+ tag );
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
				double value = kuvValue ;
				if(maxPi <= value){
					maxPi = value;
					maxKuv = kuvTemp;
				}
			}
		}
//		System.out.println("highestKuv "+maxKuv + " maxPi "+maxPi);
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

	
	class AvgValue{
		private Double d;
		private int count;
		
		public AvgValue() {
			// TODO Auto-generated constructor stub
		}
		
		
		public AvgValue(Double d, int count) {
			super();
			this.d = d;
			this.count = count;
		}


		public Double getD() {
			return d;
		}
		public void setD(Double d) {
			this.d = d;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		
		
	}
}
