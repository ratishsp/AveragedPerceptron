AveragedPerceptron
==================

AveragedPerceptron implementation
It is implementation of Averaged Perceptron algorithm in paper "Discriminative Training Methods for Hidden Markov Models: Theory and Experiments with Perceptron Algorithms"
by Michael Collins (2002)
http://acl.ldc.upenn.edu/W/W02/W02-1001.pdf

For a simple tagger implementation with features such as suffix of lengths 1,2,3, current word, and previous tag, use the tagger file PerceptronBigramNoWordVecNoPOS.java  
You need to configure the training data file path TRAINING_FILE, test file path DEV_FILE, test output file DEV_OUT_FILE, model output path MODEL_GEN_FILE (perceptron model), MODEL_GEN_AVG_OPTIM (average perceptron model)  
Format of the input:   
It follows CONLL 2003 format for NER  
Each word has been put on a separate line and there is an empty line after each sentence. The first item on each line is a word, the second the named entity tag

eg: training data format  
ताजा	JJ  
साँसें	N  
और	CC  
चमचमाते	JJ  
दाँत	N  
आपके	PR  
व्यक्तित्व	N  
को	PSP  
निखारते	V  
हैं	V  
।	RD  
  
दाँतों	N  
से	PSP  
आपका	PR  
आत्मविश्वास	N  
भी	RP  
बढ़ता	V  
है	V  
।	RD  

test data format:  
साल  
और  
बाँस  
के  
इस  
जंगल  
के  
बीच  
जंगली  
घास  
के  
कई  
मैदान  
हैं  
  
जो  
इस  
अदम् य  
दृश्य  
को  
एक  
परिचित  
भाव  
देते  
हैं  
।  
  

The NE classes need to be configured in the java file itself. Presently the following NE have been hardcoded:    
ARTIFACT, COUNT, DATE,
DAY,
DISEASE,
DISTANCE,
ENTERTAINMENT,
FACILITIES,
LIVTHINGS,
LOCATION,
LOCOMOTIVE,
MATERIALS,
MONEY,
MONTH,
O,
ORGANIZATION,
PERIOD,
PERSON,
PLANTS,
QUANTITY,
SDAY,
TIME,
YEAR
  
