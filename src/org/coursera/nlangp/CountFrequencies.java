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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ratish
 *
 */
public class CountFrequencies {

	private static final String GENE_DEV_P1_OUT = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_dev.p1.out";
	private static final String GENE_DEV = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.dev";
	private static final String GENE_COUNTSJAVA = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.countsjava";
	private static final String GENE_TRAIN = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train";
	private static final String GENE_TEST = "/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.test";
	private static final String GENE_TEST_P1_OUT ="/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene_test.p1.out";
	private static final String RARE = "_RARE_";
	private static final String STOP = "STOP";
	private static final String SPACE = " ";

	public static void main(String[] args) {
		CountFrequencies countFrequencies = new CountFrequencies();
		countFrequencies.countEmissions();
	}
	
	private void countEmissions(){
		try {
			

			Map<String, Integer> trigramCountMap = new HashMap<String,Integer>();
			Map<String, Integer> bigramCountMap = new HashMap<String,Integer>();
			Map<String, Integer> unigramCountMap = new HashMap<String,Integer>();
			Map<String, Integer> countX = countX();			
			replaceRareWords(countX);
			Map<String, Integer> countWords = countWords();
			countNgrams(trigramCountMap, bigramCountMap, unigramCountMap);
			writeEmissionCounts(unigramCountMap, countWords);
			
			writeNgramCounts(trigramCountMap, bigramCountMap, unigramCountMap);
			Map<String, TagAndWeight> xyMap = getTagAndWeight();	
			unigramTag(xyMap, GENE_DEV, GENE_DEV_P1_OUT);
			unigramTag(xyMap, GENE_TEST, GENE_TEST_P1_OUT);
			
			
						
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Integer> countX() throws IOException {
		String inputLine;
		BufferedReader scanner = new BufferedReader(new FileReader(GENE_TRAIN));
		Map<String, Integer> countWords = new HashMap<String, Integer>();
		
		while((inputLine = scanner.readLine()) != null){
			if(inputLine.trim().length() == 0){
				continue;
			}
			setOrIncrement(countWords, inputLine.split(SPACE)[0]);
		}
		scanner.close();
		return countWords;
	}

	private void unigramTag(Map<String, TagAndWeight> xyMap, String inputFile, String outputFile) throws FileNotFoundException, IOException {
		String readLine;
		BufferedReader genedevReader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter genedevWriter = new BufferedWriter(new FileWriter(outputFile));
		while((readLine = genedevReader.readLine())!=null){
			if(readLine.trim().length() ==0){
				
			}else if(xyMap.containsKey(readLine)){
				genedevWriter.write(readLine + SPACE + xyMap.get(readLine).getTag());
			}else{
				genedevWriter.write(readLine + SPACE + xyMap.get(RARE).getTag());
			}
			genedevWriter.newLine();
		}
			
		genedevReader.close();
		genedevWriter.close();
	}

	private Map<String, TagAndWeight> getTagAndWeight()
			throws FileNotFoundException, IOException {
		Map<String,TagAndWeight> xyMap = new HashMap<String, TagAndWeight>();
		BufferedReader countsReader = new BufferedReader(new FileReader(GENE_COUNTSJAVA));
		String readLine = null;
		while((readLine = countsReader.readLine())!=null){
			if(readLine.indexOf("WORDTAG") == -1){
				continue;
			}
			String[] splitArgs = readLine.split(SPACE);
			String x = splitArgs[3];
			String tag = splitArgs[2];
			BigDecimal weight = new BigDecimal(splitArgs[4]);
			if(xyMap.containsKey(x)){
				if(weight.compareTo(xyMap.get(x).getWeight()) > 0){
					xyMap.put(x, new TagAndWeight(tag, weight));
				}
			}else{
				xyMap.put(x, new TagAndWeight(tag,weight));
			}
		}
		countsReader.close();
		return xyMap;
	}

	private void writeNgramCounts(Map<String, Integer> trigramCountMap,
			Map<String, Integer> bigramCountMap,
			Map<String, Integer> unigramCountMap) throws IOException {
		BufferedWriter ngramWriter = new BufferedWriter(new FileWriter(GENE_COUNTSJAVA, true));			
		writeNgramCount(ngramWriter, trigramCountMap, "3-GRAM");
		writeNgramCount(ngramWriter, bigramCountMap, "2-GRAM");
		writeNgramCount(ngramWriter, unigramCountMap, "1-GRAM");		
		ngramWriter.close();
	}

	private void writeEmissionCounts(Map<String, Integer> unigramCountMap,
			Map<String, Integer> countWords) throws IOException {
		BufferedWriter bufferedwriter = new BufferedWriter(new FileWriter(GENE_COUNTSJAVA));

		Set<Map.Entry<String, Integer>> countWordsSet = countWords.entrySet();
		for(Map.Entry<String, Integer> countWordsEntry : countWordsSet){
			String [] splitWords = countWordsEntry.getKey().split(SPACE);
			bufferedwriter.write(countWordsEntry.getValue() + " WORDTAG "+splitWords[1]+ SPACE+splitWords[0] + SPACE+(float)countWordsEntry.getValue()/unigramCountMap.get(splitWords[1]));
			bufferedwriter.newLine();
		}
		bufferedwriter.close();
	}

	public static void countNgrams(Map<String, Integer> trigramCountMap,
			Map<String, Integer> bigramCountMap,
			Map<String, Integer> unigramCountMap) throws FileNotFoundException,
			IOException {
		String yminus2trigram = "*";
		String yminus1trigram = "*";
		String y0trigram = "";
		String yminus1bigram = "*";
		String y0bigram = "";
		String y0unigram = "";
		String inputLine = null;
		BufferedReader ngramReader = new BufferedReader(new FileReader(GENE_TRAIN));
		int numSentences=0;
		while((inputLine = ngramReader.readLine()) != null){
			if(inputLine.trim().length()==0){
				y0trigram = STOP;
				y0bigram = STOP;
				String trigramKey = yminus2trigram+ SPACE+yminus1trigram+SPACE+y0trigram;
				setOrIncrement(trigramCountMap, trigramKey);
				String bigramKey = yminus1bigram + SPACE+y0bigram;
				setOrIncrement(bigramCountMap, bigramKey);
				
				yminus2trigram = "*";
				yminus1trigram = "*";
				y0trigram = "";
				yminus1bigram = "*";
				y0bigram = "";
				y0unigram="";
				numSentences++;
			}else{
				String[] inputKeywords = inputLine.split(SPACE);
				y0trigram = inputKeywords[1];
				String trigramKey = yminus2trigram+ SPACE+yminus1trigram+SPACE+y0trigram;
				setOrIncrement(trigramCountMap, trigramKey);
				yminus2trigram = yminus1trigram;
				yminus1trigram = y0trigram;
				
				
				y0bigram = inputKeywords[1];
				String bigramKey = yminus1bigram + SPACE+y0bigram;
				setOrIncrement(bigramCountMap, bigramKey);
				yminus1bigram = y0bigram;
				
				y0unigram = inputKeywords[1];
				setOrIncrement(unigramCountMap, y0unigram);
			}
		}
		ngramReader.close();
		
		String trigramKey = yminus1trigram+ SPACE+y0trigram+SPACE+STOP;
		String bigramKey = y0bigram+SPACE+STOP;
		setOrIncrement(trigramCountMap, trigramKey);
		setOrIncrement(bigramCountMap, bigramKey);
		bigramCountMap.put("*"+SPACE+"*", numSentences+1);
	}

	private Map<String, Integer> countWords() throws FileNotFoundException,
			IOException {
		String inputLine;
		BufferedReader scanner = new BufferedReader(new FileReader(GENE_TRAIN));
		Map<String, Integer> countWords = new HashMap<String, Integer>();
		
		while((inputLine = scanner.readLine()) != null){
			if(inputLine.trim().length() == 0){
				continue;
			}
			setOrIncrement(countWords, inputLine);
		}
		scanner.close();
		return countWords;
	}

	private void replaceRareWords(Map<String, Integer> countWords)
			throws FileNotFoundException, IOException {
		BufferedReader trainingSetReader = new BufferedReader(new FileReader(GENE_TRAIN));
		BufferedWriter trainingSetWriter = new BufferedWriter(new FileWriter("/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train_out"));
		String readLine = null;
		while((readLine = trainingSetReader.readLine())!= null){
			if(readLine.trim().length() == 0){
			}				
			else if(countWords.get(readLine.split(SPACE)[0]) >= 5){
				trainingSetWriter.write(readLine);
			}else{
				trainingSetWriter.write(RARE + SPACE+ readLine.split(SPACE)[1]);
			}
			trainingSetWriter.newLine();
		}
		trainingSetWriter.close();
		trainingSetReader.close();
		
		File inputFile = new File(GENE_TRAIN);
//		System.out.println("delete "+inputFile.delete());
		File file = new File("/home/ratish/project/study/nlp/nlp-pa1/h1-p/gene.train_out");
		System.out.println("rename "+file.renameTo(inputFile));
		
	}

	private void writeNgramCount(BufferedWriter ngramWriter,
			Map<String, Integer> trigramCountMap, String ngramType)
			throws IOException {
		Set<Map.Entry<String, Integer>> trigramCountSet = trigramCountMap.entrySet();
		for(Map.Entry<String, Integer> trigramCountentry : trigramCountSet){
			ngramWriter.write(trigramCountentry.getValue()+SPACE+ngramType+SPACE+trigramCountentry.getKey());
			ngramWriter.newLine();
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
	
	class TagAndWeight{
		private String tag;
		private BigDecimal weight;
		
		
		public TagAndWeight(String tag, BigDecimal weight) {
			super();
			this.tag = tag;
			this.weight = weight;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}
		
		public void setWeight(BigDecimal weight) {
			this.weight = weight;
		}
		
		public String getTag() {
			return tag;
		}
		
		public BigDecimal getWeight() {
			return weight;
		}
		
	}
}
