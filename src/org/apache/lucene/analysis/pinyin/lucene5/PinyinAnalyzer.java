package org.apache.lucene.analysis.pinyin.lucene5;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.pinyin.utils.Constant;
import org.wltea.analyzer.lucene.IKTokenizer;
/**
 * 自定义拼音分词器
 * @author Lanxiaowei
 *
 */
@SuppressWarnings("unused")
public class PinyinAnalyzer extends Analyzer {
	private int minGram;
	private int maxGram;
	private boolean useSmart;
	/** 是否需要对中文进行NGram[默认为false] */
	private boolean nGramChinese;
	/** 是否需要对纯数字进行NGram[默认为false] */
	private boolean nGramNumber;
	/**N-Gram的方向：front/back*/
	private String side;
	
	public PinyinAnalyzer() {
		super();
		this.maxGram = Constant.DEFAULT_MAX_GRAM;
		this.minGram = Constant.DEFAULT_MIN_GRAM;
		this.useSmart = Constant.DEFAULT_IK_USE_SMART;
	}
	
	public PinyinAnalyzer(boolean useSmart) {
		this(Constant.DEFAULT_MIN_GRAM, Constant.DEFAULT_MAX_GRAM, Constant.DEFAULT_SIDE_FRONT, Constant.DEFAULT_IK_USE_SMART, Constant.DEFAULT_NGRAM_CHINESE,Constant.DEFAULT_NGRAM_NUMBER);
	}
	
	public PinyinAnalyzer(int maxGram) {
		this(Constant.DEFAULT_MIN_GRAM, maxGram, Constant.DEFAULT_SIDE_FRONT, Constant.DEFAULT_IK_USE_SMART, Constant.DEFAULT_NGRAM_CHINESE,Constant.DEFAULT_NGRAM_NUMBER);
	}

	public PinyinAnalyzer(int maxGram,boolean useSmart) {
		this(Constant.DEFAULT_MIN_GRAM, maxGram, Constant.DEFAULT_SIDE_FRONT, useSmart, Constant.DEFAULT_NGRAM_CHINESE,Constant.DEFAULT_NGRAM_NUMBER);
	}
	
	public PinyinAnalyzer(int minGram, int maxGram,boolean useSmart) {
		this(minGram, maxGram, Constant.DEFAULT_SIDE_FRONT, useSmart, Constant.DEFAULT_NGRAM_CHINESE,Constant.DEFAULT_NGRAM_NUMBER);
	}
	
	public PinyinAnalyzer(int minGram, int maxGram,String side,boolean useSmart) {
		this(minGram, maxGram, side, useSmart, Constant.DEFAULT_NGRAM_CHINESE,Constant.DEFAULT_NGRAM_NUMBER);
	}

	public PinyinAnalyzer(int minGram, int maxGram,String side,boolean useSmart,
			boolean nGramChinese) {
		this(minGram, maxGram, side, useSmart,nGramChinese,Constant.DEFAULT_NGRAM_NUMBER);
	}
	
	public PinyinAnalyzer(int minGram, int maxGram,String side,boolean useSmart,
			boolean nGramChinese,boolean nGramNumber) {
		super();
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.side = side;
		this.useSmart = useSmart;
		this.nGramChinese = nGramChinese;
		this.nGramNumber = nGramNumber;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Reader reader = new BufferedReader(new StringReader(fieldName));
		Tokenizer tokenizer = new IKTokenizer(reader, useSmart);
		//转拼音
		TokenStream tokenStream = new PinyinTokenFilter(tokenizer,
			Constant.DEFAULT_SHORT_PINYIN,Constant.DEFAULT_PINYIN_ALL, Constant.DEFAULT_MIN_TERM_LRNGTH);
		//对拼音进行NGram处理
		/*tokenStream = new PinyinEdgeNGramTokenFilter(tokenStream,Constant.DEFAULT_SIDE_FRONT, 
			this.minGram, this.maxGram,true,true);*/
		tokenStream = new PinyinNGramTokenFilter(tokenStream, 
			minGram, maxGram, nGramChinese, nGramNumber);
		
	    //tokenStream = new LowerCaseFilter(tokenStream);
		//tokenStream = new StopFilter(tokenStream,StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	    return new Analyzer.TokenStreamComponents(tokenizer, tokenStream);
	}
}
