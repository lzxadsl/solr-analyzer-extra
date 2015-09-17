package org.apache.lucene.analysis.pinyin.solr5;

import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pinyin.lucene5.PinyinEdgeNGramTokenFilter;
import org.apache.lucene.analysis.pinyin.utils.Constant;
import org.apache.lucene.analysis.util.TokenFilterFactory;
/**
 * PinyinEdgeNGramTokenFilter工厂类
 * @author Lanxiaowei
 *
 */
public class PinyinEdgeNGramTokenFilterFactory extends TokenFilterFactory {
	private int minGram;
	private int maxGram;
	private String side;
	/** 是否需要对中文进行NGram[默认为false] */
	private boolean nGramChinese;
	/** 是否需要对纯数字进行NGram[默认为false] */
	private boolean nGramNumber;

	public PinyinEdgeNGramTokenFilterFactory(Map<String, String> args) {
		super(args);

		this.minGram = getInt(args, "minGram", Constant.DEFAULT_MIN_GRAM);
		this.maxGram = getInt(args, "maxGram", Constant.DEFAULT_MAX_GRAM);
		this.side = get(args, "side", Constant.DEFAULT_SIDE_FRONT);
		this.nGramChinese = getBoolean(args, "nGramChinese", Constant.DEFAULT_NGRAM_CHINESE);
		this.nGramNumber = getBoolean(args, "nGramNumber", Constant.DEFAULT_NGRAM_NUMBER);
	}

	public TokenFilter create(TokenStream input) {
		return new PinyinEdgeNGramTokenFilter(input,side, this.minGram, this.maxGram,
				this.nGramChinese,this.nGramNumber);
	}
}