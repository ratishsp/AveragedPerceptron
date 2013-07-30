/**
 * 
 */
package org.coursera.nlangp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ratish
 *
 */
public class GLM {

	private static final String FEATURE_WEIGHTS = "/home/ratish/project/study/nlp/h4-assignment/tag.model";
	private static final String SPACE = " ";
	private static final String O = "O";
	private static final String I_GENE = "I-GENE";
	private static final String STAR = "*";
	private static final String GENE_DEV_P1_OUT = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_dev.p1.out";
	private static final String GENE_DEV_P2_OUT = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_dev.p2.out";
	private static final String GENE_DEV_P3_OUT = "/home/ratish/project/study/nlp/h4-assignment/gene_dev.p3.out";
	private static final String SAMPLE_SENTENCE = "/home/ratish/project/study/nlp/h4-assignment/sampleSentence";
	private static final String GENE_DEV = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.dev";
	public static void main(String[] args) {

		GLM glm = new GLM();
		glm.execute();
	}

	private void execute() {
		Map<String, Double> vMap = new HashMap<String, Double>();
		loadV(vMap);
//		viterbi(vMap);
		sampleSentence(vMap);
	}

	private void loadV(Map<String, Double> vMap) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(FEATURE_WEIGHTS)));
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
	
	public void viterbi(Map<String, Double> vMap) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_DEV)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_DEV_P3_OUT)));
			String readLine = null;
			List<String> input = new ArrayList<String>();
			while((readLine = br.readLine())!= null){
				if(readLine.length()!=0){
				input.add(readLine);
				}else{
					viterbiImplementation(input, bw, vMap);
					bw.newLine();
					input = new ArrayList<String>();
				}
			}
			viterbiImplementation(input, bw, vMap);
			br.close();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	private void sampleSentence( Map<String, Double> vMap) {
		try{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_DEV_P3_OUT)));
		List<String> input = new ArrayList<String>();
//		input.add("Characteristics");
//		input.add("of");
//		input.add("lipase");
//		input.add("activity");
//		input.add(".");
		
//		BufferedReader br = new BufferedReader(new FileReader(new File(SAMPLE_SENTENCE)));
//		String readLine = null;
//		while((readLine = br.readLine())!= null){
//			input.add(readLine);
//		}
		input.add("Atherosclerosis");
		
		
		viterbiImplementation(input, bw, vMap);
		bw.newLine();
//		br.close();
		bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void viterbiImplementation(List<String> input,BufferedWriter bw, Map<String, Double> vMap) throws IOException {
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
						String trigramFeature = "TRIGRAM:"+w+":"+u+":"+v;
						Double trigram = 0d;
						if(vMap.containsKey(trigramFeature)){
							trigram = vMap.get(trigramFeature);							
						}
						String tagFeature = "TAG:"+x+":"+v;
						Double tag = 0d;
						if(vMap.containsKey(tagFeature)){
							tag = vMap.get(tagFeature);
						}
						double piValue = piKMinus1 + trigram+ tag;
						if(maxValue <= piValue){
							maxValue = piValue;
							maxKuv = kuvTemp;
						}
						System.out.println("inter piValue "+piValue+" piKMinus1 "+piKMinus1+" "+ trigramFeature + " trigram value "+ trigram + " tag feature "+tagFeature+ " value "+ tag);
					}
					pi.put(kuv, maxValue);
					System.out.println("kuv "+kuv+ "maxValue "+maxValue);
					bp.put(kuv, maxKuv);
					System.out.println("kuv "+kuv+ "maxKuv "+maxKuv);
				}
			}
		}
		
		int n = input.size();
		double maxPi = 0d;
		Kuv maxKuv = null;
		for(int a = 0; a< t.size(); a++){
			for(int b=0; b<t.size(); b++){
				String u = t.get(a);
				String v = t.get(b);
				Kuv kuvTemp = new Kuv(n, u, v);
				
				Double kuvValue = 0d;
				if(pi.containsKey(kuvTemp)){
					kuvValue = pi.get(kuvTemp);
				}
				
				String trigramFeatureStop = "TRIGRAM:"+u+":"+v+":STOP";
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
		
		System.out.println("kuvs "+kuvs);
	
		Collections.reverse(kuvs);
		for(int k=0; k<input.size(); k++){
			bw.write(input.get(k)+SPACE+ kuvs.get(k+1).getV());
			bw.newLine();
		}
		
	}
}
