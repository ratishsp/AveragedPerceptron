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
import java.util.Set;

/**
 * @author ratish
 *
 */
public class SimpleGeneTagger {

	private static final String O = "O";
	private static final String I_GENE = "I-GENE";
	private static final String STAR = "*";
	private static final String GENE_DEV_P1_OUT = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_dev.p1.out";
	private static final String GENE_DEV_P2_OUT = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_dev.p2.out";
	private static final String GENE_DEV_P3_OUT = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_dev.p3.out";
	private static final String GENE_DEV = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.dev";
	private static final String GENE_COUNTSJAVA = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.countsjava";
	private static final String GENE_COUNTS = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.counts1";
	private static final String GENE_TRAIN = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train";
	private static final String GENE_TRAIN_1 = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train.1";
	private static final String GENE_TEST = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.test";
	private static final String GENE_TEST_P1_OUT ="/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_test.p1.out";
	private static final String GENE_TEST_P2_OUT ="/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_test.p2.out";
	private static final String RARE = "_RARE_";
	private static final String STOP = "STOP";
	private static final String SPACE = " ";
	private static final Integer OCOUNT = 345128;
	private static final Integer ICOUNT = 41072;

	public static void main(String[] args) {
		SimpleGeneTagger simpleGeneTagger = new SimpleGeneTagger();
		simpleGeneTagger.execute();
	}
	
	private void execute(){
		//load map of x with count
		//replace low frequency words with RARE
		Map<String, Integer> xMap = new HashMap<String, Integer>();
		Map<String, EmissionCounts> emissionMap = new HashMap<String, EmissionCounts>();
		loadXMap(xMap);
		replaceLowFreqWords(xMap);
		loadEmissionCounts(emissionMap);
		tag(emissionMap);
		Map<Trigram, Float> q = new HashMap<Trigram, Float>();
		loadQ(q);
		
		viterbi(q,emissionMap);
		sampleSentence(q,emissionMap);
	}

	private void sampleSentence(Map<Trigram, Float> q,
			Map<String, EmissionCounts> emissionMap) {
		try{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_DEV_P3_OUT)));
		List<String> input = new ArrayList<String>();
		input.add("STAT5A");
		input.add("mutations");
		input.add("in");
		process(input, q, emissionMap, bw);
		bw.newLine();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void viterbi(Map<Trigram, Float> q,
			Map<String, EmissionCounts> emissionMap) {
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_TEST)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_TEST_P2_OUT)));
			String readLine = null;
			List<String> input = new ArrayList<String>();
			while((readLine = br.readLine())!= null){
				if(readLine.length()!=0){
				input.add(readLine);
				}else{
					process(input, q, emissionMap, bw);
					bw.newLine();
					input = new ArrayList<String>();
				}
			}
			process(input, q, emissionMap, bw);
			br.close();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}

	private void process(List<String> input, Map<Trigram, Float> q,
			Map<String, EmissionCounts> emissionMap, BufferedWriter bw) throws IOException {
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
		pi.put(new Kuv(0, STAR, STAR), 1d);
		
		for(int k=0; k<input.size(); k++){
			for(int a = 0; a< t.size(); a++){
				for(int b=0; b<s.size(); b++){
					String u = t.get(a);
					String v = s.get(b);
					Kuv kuv = new Kuv(k+1, u, v);
					
					Double maxValue = 0d;
					Kuv maxKuv = null;
					for(int l=0; l< t.size(); l++){
						String w = t.get(l);
						Kuv kuvTemp = new Kuv(k, w, u);
						double piKMinus1 = 0d;
						if(pi.containsKey(kuvTemp)){
							piKMinus1 = pi.get(kuvTemp);
						}
						Trigram trigram = new Trigram(w, u, v);
						Float qValue = 0f;
						if(q.containsKey(trigram)){
							qValue = q.get(trigram);
						}
						Float eValue = 0f;
						String x = input.get(k);
						EmissionCounts eCounts = null;
						if(emissionMap.containsKey(x)){
							eCounts = emissionMap.get(x);
						}else{
							eCounts = emissionMap.get("_RARE_");
						}
						
//						if((float)eCounts.getGeneCount()/ICOUNT > (float)eCounts.getoCount()/OCOUNT){
//							eValue = (float)eCounts.getGeneCount()/ICOUNT; 
//						}else{
//							eValue = (float)eCounts.getoCount()/OCOUNT;
//						}
//						eValue = 
						if(v.equals(I_GENE)){
							eValue = (float)eCounts.getGeneCount()/ICOUNT;
						}else if(v.equals(O)){
							eValue = (float)eCounts.getoCount()/OCOUNT;
						}
						
						double piValue = piKMinus1*qValue* eValue;
						if(maxValue <= piValue){
							maxValue = piValue;
							maxKuv = kuvTemp;
						}
//						System.out.println("inter piValue "+piValue+" piKMinus1 "+piKMinus1+" "+ trigram +" q "+qValue+ " x "+ x+ " e "+eValue);
						
					}
					pi.put(kuv, maxValue);
//					System.out.println("kuv "+kuv+ "maxValue "+maxValue);
					bp.put(kuv, maxKuv);
//					System.out.println("kuv "+kuv+ "maxKuv "+maxKuv);
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
				Float quvstop = 0f;
				if(q.containsKey(new Trigram(u, v, "STOP"))){
					quvstop = q.get(new Trigram(u, v, "STOP"));
				}
				Double kuvValue = 0d;
				if(pi.containsKey(kuvTemp)){
					kuvValue = pi.get(kuvTemp);
				}
				double value = kuvValue* quvstop;
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

	private void loadQ(Map<Trigram, Float> q ) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_COUNTS)));
			String readLine = null;
			Map<Bigram, Integer> bigramCounts = new HashMap<Bigram, Integer>();
			Map<Trigram, Integer> trigramCounts = new HashMap<Trigram, Integer>();
			while((readLine = br.readLine())!= null){
				if(readLine.indexOf("2-GRAM") != -1){
					String [] args = readLine.split(SPACE);
					String u = args[2];
					String v = args[3];
					Bigram bigram = new Bigram(u, v);
					bigramCounts.put(bigram, Integer.parseInt(args[0]));
				}else if(readLine.indexOf("3-GRAM") != -1){
					String [] args = readLine.split(SPACE);
					String w = args[2];
					String u = args[3];
					String v = args[4];
					Trigram trigram = new Trigram(w, u, v);
					trigramCounts.put(trigram, Integer.parseInt(args[0]));
				}
			}
			
			Set<Map.Entry<Trigram, Integer>>  entrySet = trigramCounts.entrySet();
			for (Map.Entry<Trigram, Integer> entry : entrySet) {
				Trigram key = entry.getKey();
				q.put(key, (float)entry.getValue()/ bigramCounts.get(new Bigram(key.getW(), key.getU())));
			}
			
			System.out.println(q);
			
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
	}

	private void tag(Map<String, EmissionCounts> emissionMap) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_DEV)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_DEV_P1_OUT)));
			String readLine = null;
			String rare = rareType(emissionMap);
			
			while((readLine = br.readLine())!= null){
				String writeLine = "";
				if(readLine.length() >0){
					if(emissionMap.containsKey(readLine)){
						EmissionCounts counts = emissionMap.get(readLine);
						if((float)counts.getGeneCount()/ICOUNT > (float)counts.getoCount()/OCOUNT){
							writeLine = readLine + SPACE + "I-GENE";
						}else{
							writeLine = readLine + SPACE + O;
						}
					}else{
						writeLine = readLine + SPACE + rare;
					}
				}
				bw.write(writeLine);
				bw.newLine();
			}
			br.close();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private String rareType(Map<String, EmissionCounts> emissionMap) {
		EmissionCounts rareCounts = emissionMap.get("_RARE_");
		String rare = null;
		rare = rareType(rareCounts);
		return rare;
	}

	private String rareType(EmissionCounts rareCounts) {
		String rare;
		if((float)rareCounts.getGeneCount()/ICOUNT > (float)rareCounts.getoCount()/OCOUNT){
			rare = "I-GENE";
		}else{
			rare = O;
		}
		return rare;
	}

	private void loadEmissionCounts(Map<String, EmissionCounts> emissionMap) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_COUNTS)));
			String readLine = null;
			
			while((readLine = br.readLine())!= null){
				if(readLine.indexOf("WORDTAG") !=-1){
					String [] split = readLine.split(SPACE);
					String count = split[0];
					String type = split[2];
					EmissionCounts eCounts = new EmissionCounts();
					String input = split[3];
					if(emissionMap.containsKey(input)){
						eCounts = emissionMap.get(input);
					}else{
						emissionMap.put(input, eCounts);
					}
					setCount(eCounts, count, type);
				}
			}
			
			br.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}

	private void setCount(EmissionCounts eCounts, String count, String type) {
		if(type.equals(O)){
			eCounts.setoCount(Integer.parseInt(count));
		}else{
			eCounts.setGeneCount(Integer.parseInt(count));
		}
	}

	private void replaceLowFreqWords(Map<String, Integer> xMap) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_TRAIN)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_TRAIN_1)));
			String readLine = null;
			
			while((readLine = br.readLine())!= null){
				String writeLine = "";
				if(readLine.length() >0){
					String[] split = readLine.split(SPACE);
					if(xMap.get(split[0]) < 5){
						writeLine = RARE + SPACE + split[1];
					}else{
						writeLine = readLine;
					}
				}
				bw.write(writeLine);
				bw.newLine();
			}
			br.close();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
	}

	private void loadXMap(Map<String, Integer> xMap) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_TRAIN)));
			String readLine = null;
			
			while((readLine = br.readLine())!= null){
				if(readLine.length() ==0){
					continue;
				}
				setOrIncrement(xMap, readLine.split(SPACE)[0]);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void setOrIncrement(Map<String, Integer> countMap,
			String key) {
		if(countMap.containsKey(key)){
			countMap.put(key, countMap.get(key)+1);
		}else{
			countMap.put(key, 1);
		}
	}
}

class EmissionCounts{
	private Integer oCount;
	private Integer geneCount;
	
	public EmissionCounts(Integer oCount, Integer geneCount) {
		this.oCount = oCount;
		this.geneCount = geneCount;
	}
	
	public EmissionCounts() {
		this.oCount = 0;
		this.geneCount = 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((geneCount == null) ? 0 : geneCount.hashCode());
		result = prime * result + ((oCount == null) ? 0 : oCount.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmissionCounts other = (EmissionCounts) obj;
		if (geneCount == null) {
			if (other.geneCount != null)
				return false;
		} else if (!geneCount.equals(other.geneCount))
			return false;
		if (oCount == null) {
			if (other.oCount != null)
				return false;
		} else if (!oCount.equals(other.oCount))
			return false;
		return true;
	}

	public void setGeneCount(Integer geneCount) {
		this.geneCount = geneCount;
	}
	
	public void setoCount(Integer oCount) {
		this.oCount = oCount;
	}
	
	
	public Integer getGeneCount() {
		return geneCount;
	}
	
	public Integer getoCount() {
		return oCount;
	}
}

class Bigram{
	private String u;
	private String v;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bigram other = (Bigram) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}

	public Bigram(String u, String v) {
		super();
		this.u = u;
		this.v = v;
	}

	public void setU(String u) {
		this.u = u;
	}
	
	public void setV(String v) {
		this.v = v;
	}
	
	public String getU() {
		return u;
	}
	
	public String getV() {
		return v;
	}
}

class Trigram{
	private String u;
	private String v;
	private String w;
	
	
	public Trigram(String w, String u, String v) {
		super();
		this.u = u;
		this.v = v;
		this.w = w;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		result = prime * result + ((w == null) ? 0 : w.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trigram other = (Trigram) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		if (w == null) {
			if (other.w != null)
				return false;
		} else if (!w.equals(other.w))
			return false;
		return true;
	}


	public void setU(String u) {
		this.u = u;
	}
	public void setV(String v) {
		this.v = v;
	}
	
	public void setW(String w) {
		this.w = w;
	}
	public String getU() {
		return u;
	}
	public String getV() {
		return v;
	}
	public String getW() {
		return w;
	}


	@Override
	public String toString() {
		return "Trigram [w=" + w + ", u=" + u + ", v=" + v + "]";
	}
	
	
}

class Kuv{
	private Integer k;
	private String u;
	private String v;
	public Kuv(Integer k, String u, String v) {
		super();
		this.k = k;
		this.u = u;
		this.v = v;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((k == null) ? 0 : k.hashCode());
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Kuv other = (Kuv) obj;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}


	public Integer getK() {
		return k;
	}
	public void setK(Integer k) {
		this.k = k;
	}
	public String getU() {
		return u;
	}
	public void setU(String u) {
		this.u = u;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}


	@Override
	public String toString() {
		return "Kuv [k=" + k + ", u=" + u + ", v=" + v + "]";
	}
	
	
	
}
