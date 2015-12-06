package org.islamic.civil.util.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SearchUtils implements RTLCharacters{
	
	static HashSet<Character> charSet = new HashSet<Character>();
	static HashMap<Character , Character> charMap = new HashMap<Character , Character>();
	static {
		char[] arr = new char[] { SUKUN, SHADDA, KASRA, DAMMA, FATHA, KASRATAN, DAMMATAN, FATHATAN, SUPERSCRIPT_ALEF , TATWEEL ,SMALL_LOW_SEEN, SMALL_HIGH_MEEM, SMALL_WAW, SMALL_YEH, MADDAH_ABOVE, SMALL_ROUNDED_ZERO};
		
		for (int i = 0; i < arr.length; i++) {
			charSet.add(arr[i]);
		}
		
		charMap.put(ALEF_HAMZA_ABOVE, ALEF);
		charMap.put(ALEF_HAMZA_BELOW, ALEF);
		charMap.put(ALEF_WASLA, ALEF);
		charMap.put(YEH_HAMZA_ABOVE, ARABIC_YEH);
		charMap.put(ALEF_MAKSURA, ARABIC_YEH);
		charMap.put(FARSI_YEH, ARABIC_YEH);
		charMap.put(TEH_MARBUTA, TEH);
		charMap.put(WAW_HAMZA_ABOVE, WAW);
		charMap.put(SWASH_KEHEH, ARABIC_KAF);
		charMap.put(FARSI_KEHEH, ARABIC_KAF);
		
	}
	
	static ArrayList<Integer> getIndex(String str,String[] words){
		ArrayList<Integer> indexArray = null;
		return indexArray;
	}
	
	static boolean containWord(String str,String[] words){
		boolean result = true;
		for(int i=0;i<words.length;i++)
			result &= str.contains(words[i]);
		return result;
	}
	
	static boolean containWordSequence(String str,String[] words){
		int index = 0;
		int i;
		for(i=0;i<words.length;i++){
			int t = str.indexOf(words[i], index);
			if(t < index)
				break;
			index = t;
		}
		return i==words.length;
	}
	
	public static String arabicSimplify4AdvancedSearch(String str) {
		StringBuilder sbuilder = new StringBuilder();
		for(int i = 0 ;i<str.length();i++){
			char c = str.charAt(i);
			if(!charSet.contains(c)){
				if(charMap.containsKey(c))
					sbuilder.append(charMap.get(c));
				else
					sbuilder.append(c);
			}
		}

		return sbuilder.toString();
	}

	static String[] getWords(String sentence){
		return sentence.split("\\s+");
	}
	
	static final int Simple = 0 , Simple2Sentece = 1, Similar = 2 , Similar2Sentece = 3;
	
	
	static public class SearchThread implements Runnable {
		String[] data , words;
		int type;
		boolean matchWholeWord , matchSequence ;
		Thread thread;
		SearchResultListener listener;
		
		public SearchThread(String data[],String word ,boolean mww , boolean ms , int st ,SearchResultListener srl){
			this.data = data;
			type = st;
			if(isSimplify())
				word = arabicSimplify4AdvancedSearch(word);
			words = getWords(word);
			matchWholeWord = mww;
			matchSequence = ms;
			thread = new Thread(this);
			listener = srl;
		}

		public void run() {
			String[] words ;
			boolean isSimple = isSimplify();
			if(matchWholeWord){
				words = new String[this.words.length];
				for(int i = 0;i<this.words.length;i++)
					words[i] = ' ' + this.words[i] + ' ';
			}
			else
				words = this.words;
			for(int i=0;i<data.length;i++){
				String line = data[i];
				if(isSimple)
					line = arabicSimplify4AdvancedSearch(line);
				if(matchSequence){
					if(containWordSequence(line, words))
						sendResult(i);
				}
				else{
					if(containWord(line, words))
						sendResult(i);
				}
			}
		}
		
		public void start(){
			thread.start();
		}
		
		public void stop(){
			thread.stop();
		}
		
		public boolean isSimplify(){
			return (type&2) > 0;
		}
		
		private void sendResult(int i){
			if(listener != null)
				listener.newSearchData(i,-1,0);
		}
	}
	
	static public interface SearchResultListener {
		void newSearchData(int index, int start, int end);
	}
	
	public void search(String data[],String word ,boolean mww , boolean ms , int st ,SearchResultListener srl){
		SearchThread sthread = new SearchThread(data, word, mww, ms, st, srl);
		sthread.start();
	}


	public static boolean isRTL(char c)
	{
		return (( ((c >= 0x05d0) && (c <= 0x07b1)) ||
				((c >= 0xFE80) && (c <= 0xFEFC)) ||
				((c >= 0xfb1d) && (c <= 0xfefc))) && 	!isDigit(c));
	}

	public static boolean isLTR(char c)
	{
		return !isRTL(c);
	}

	public static boolean isDigit(char c)
	{
		return (c>=0x30&&c<=0x39)||(c>=0x6F0&&c<=0x6F9)||(c>=0x660&&c<=0x669);
	}

	public static boolean isSymbol(char c)
	{/////////////////////////////////////////////////////////////////////////////
		return (c>=0x21&&c<=0x2f)||(c>=0x3a&&c<=0x3f)||(c>=0x5e&&c<=0x60)||(c>=0x7e&&c<=0x7f)||
				(c==0x06E9)||(c==061F)||(c==0x06DE)||(c==0x06DD)||
				(c>=0x066A&&c<=0x066D)||(c>=0x606&&c<=0x60E)||
				(c==0xa9)||(c==0xae);
	}

	public static boolean isAlamat(char c)
	{/////////////////////////////////////////////////////////////
		return (c>=0x64b&&c<=0x65f)||(c>=0x610&&c<=0x61A);
	}

	public static boolean isSmall(char c)
	{
		//0x65a 0x 65b///////////////////////////////////////////
		return (c>=0x615&&c<=0x61a)||(c>=0x6d6&&c<=0x6DC)||(c>=0x6DF&&c<=0x6E8)
				||(c>=0x610&&c<=0x61A)||(c>=0x610&&c<=0x61A)||c==0x670||c==0x674||c==0x6ED;
	}

	public static boolean isWhiteSpce(char c)
	{
		return (c==' ')||(c=='\n')||(c=='\t')||(c=='\r')||(c=='\f');//////////////////////////
	}

	public static boolean isSpace(char c)
	{
		return c==' '||c=='\t';
	}

	public static boolean isArabic(char c)
	{
		return (c>=0x600&&c<0x750)||(c>=0xFB50&&c<0xFD40)||(c>=0xFE70&&c<0xFF00);
	}

	public static boolean isArabicLetter(char c)
	{
		return c!=0x0;//////////////////////////////////////////////////////////////
	}

	public static boolean isRTLString(String s)
	{
		int i=0;
		for(;i<s.length() && i<10 ;i++)
		{
			if(isArabic(s.charAt(i)) || isArabic(s.charAt(s.length()-1-i)))
				return true;
		}
		return false;
	}
}
