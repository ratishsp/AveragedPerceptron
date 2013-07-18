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
import java.util.HashMap;
import java.util.Map;

/**
 * @author ratish
 *
 */
public class SimpleGeneTagger {

	private static final String GENE_DEV_P1_OUT = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_dev.p1.out";
	private static final String GENE_DEV = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.dev";
	private static final String GENE_COUNTSJAVA = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.countsjava";
	private static final String GENE_COUNTS = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.counts1";
	private static final String GENE_TRAIN = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train";
	private static final String GENE_TRAIN_1 = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train.1";
	private static final String GENE_TEST = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.test";
	private static final String GENE_TEST_P1_OUT ="/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_test.p1.out";
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
	}

	private void tag(Map<String, EmissionCounts> emissionMap) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(GENE_DEV)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(GENE_DEV_P1_OUT)));
			String readLine = null;
			EmissionCounts rareCounts = emissionMap.get("_RARE_");
			String rare = null;
			if((float)rareCounts.getGeneCount()/ICOUNT > (float)rareCounts.getoCount()/OCOUNT){
				rare = "I-GENE";
			}else{
				rare = "O";
			}
			
			while((readLine = br.readLine())!= null){
				String writeLine = "";
				if(readLine.length() >0){
					if(emissionMap.containsKey(readLine)){
						EmissionCounts counts = emissionMap.get(readLine);
						if((float)counts.getGeneCount()/ICOUNT > (float)counts.getoCount()/OCOUNT){
							writeLine = readLine + SPACE + "I-GENE";
						}else{
							writeLine = readLine + SPACE + "O";
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
		if(type.equals("O")){
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
