package ChatBot;

import java.util.ArrayList;
import java.util.Properties;


import control.TestRun;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

//if the original chatbot doesnt know how to handle a reply, the stanfordAPI is employed to further analyze the user's input, creating a conversation within the conversation that will
//return the point at which it began in the original graph traversal, repeating the question (using the other form, or another if there are more than 2) that raised the exception.
public class InterludeConversation {
	private StateNode sn;
	static StanfordCoreNLP pipeline;
	static String[] lr = {"oh yeah ive been there before its beautiful!", "yeah im familiar with that area ill take you right there", "oh, ive never been to that area but let me know exactly where to turn", "oh, I've never been"};
	static String[] tr = {"That must be interesting", "I always found that difficult", "I'll stick to the transportation industry, but good for you!"};
	static String[] negsentresp = {"im sorry to hear that", "well theres no need to be negative about things"};
	static String[] possentresp = {"yeah I also enjoy it", "I agree, it's great"};
	static String[] neutsentresp = {"well im sure youll grow to enjoy it", "never been much of a fan myself"};
	public int[] sequenceOfResponses;
	public boolean matchedQueue = false;
	public int iterationsToDefault = 3;
	public InterludeConversation(String argS, StateNode sn) {
		this.sn = sn;
		if(pipeline == null) {
			init();
		}
		sequenceOfResponses = new int[3];
		sequenceOfResponses[0] = sn.priorityOfResponses;
		int opt = 0;
		for(int i = 1; i < sequenceOfResponses.length;i++)
			if(opt==sn.priorityOfResponses)sequenceOfResponses[i] = ++opt;
			else sequenceOfResponses[i] = opt;
	    /*if(argS != null && argS != "") {		    
	    	interpretStatement(argS);
	    }*/
	    
	    makeStatement(sn.nlpOpeningStmt);
	    
	}	
	
	//ends the conversation and removes it by setting the only reference to it to null
	public void concludeConversation() {
		makeStatement(sn.nlpClosingStmt);
		sn.movingOn(); 
	}
	
	//this is the entry point for text input to this flow of control (conversation)
	public boolean interpretStatement(String argS) {
		 // make an example document
	    CoreDocument doc = new CoreDocument(argS);
	    // annotate the document
	    pipeline.annotate(doc);
	    ArrayList<CoreSentence> sentences = (ArrayList<CoreSentence>) doc.sentences(); 
	    System.out.println(argS);
	    boolean matchedSomething = false;
	    //run through the array of priorities, matching each in turn if necessary.
	    for(int i = 0; i < this.sequenceOfResponses.length; i++) {
	    	if(matchedSomething)break;
	    	switch(this.sequenceOfResponses[i]) {
		    	case 0:for(CoreSentence sentence:sentences) { //might have to find some way to aggregate the sentences (if there is more than 1) before responding, just to keep the back and forth pattern intact
							String sentiment = sentence.sentiment();
							if(sentiment.equals("Positive")) {
								this.makeStatement(possentresp[(int)(Math.random()*neutsentresp.length)]);
								matchedSomething = true; //the function exits if this is set to true;
							}
							if(sentiment.equals("Neutral")) {
								this.makeStatement(neutsentresp[(int)(Math.random()*neutsentresp.length)]);
								matchedSomething = true; //the function exits if this is set to true;
							}
							if(sentiment.equals("Negative")) {
								this.makeStatement(negsentresp[(int)(Math.random()*neutsentresp.length)]);
								matchedSomething = true; //the function exits if this is set to true;
							}
							System.out.println(sentence + "\t" + sentiment);
						}
		    			break;
		    	case 1:if(doc != null && doc.entityMentions() != null) {
		    			
							
						    for (CoreEntityMention em : doc.entityMentions())
						    	if(em.entityType().equals("LOCATION")) {
						    		this.makeStatement(lr[((int)Math.random())*lr.length]);
						    		System.out.println("\tdetected entity: \t"+em.text()+"\t"+em.entityType());	
						    		if(this.sequenceOfResponses[i] == sn.priorityOfResponses)matchedQueue = true;//the interlude conversation ends if this is set to true
						    		matchedSomething = true; //the function exits if this is set to true;
						    		break;
						    	}
						}
						break;
		    	case 2:if(doc != null && doc.entityMentions() != null) {
							
						    for (CoreEntityMention em : doc.entityMentions())
						    	if(em.entityType().equals("TITLE")) {
						    		this.makeStatement(tr[((int)Math.random())*tr.length]);
						    		System.out.println("\tdetected entity: \t"+em.text()+"\t"+em.entityType());	
						    		if(this.sequenceOfResponses[i] == sn.priorityOfResponses)matchedQueue = true; //the interlude conversation ends if this is set to true
						    		matchedSomething = true; //the function exits if this is set to true;
						    		break;
						    	}
						}
						break;
				
	    	}
	    }
		return matchedSomething; //this currently doesnt signal anything but might be useful information to pass on
	}
	
	public void nextMove(String argS, boolean mQ) {
		if(matchedQueue || this.iterationsToDefault == 0) {
			concludeConversation();		
		}
		else interpretStatement(argS);
	}
	
	//same as in the chatAI class
	public void makeStatement(String str) {
		TestRun.addTextToWindow("Driver: " + str + "\n");
		TestRun.aiOutput.add(str);
	}
	
	public static void init() {
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, entitymentions, parse, dcoref, sentiment");
	    // set up pipeline
	    pipeline = new StanfordCoreNLP(props);
	    
	}
}
