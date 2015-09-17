package org.apache.lucene.analysis.pinyin.lucene5;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pinyin.utils.Constant;
import org.apache.lucene.analysis.pinyin.utils.StringUtils;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * 对转换后的拼音进行EdgeNGram处理的TokenFilter
 * 
 * @author Lanxiaowei
 * 
 */
public class PinyinEdgeNGramTokenFilter extends TokenFilter {
	private final int minGram;
	private final int maxGram;
	/** 是否需要对中文进行NGram[默认为false] */
	private final boolean nGramChinese;
	/** 是否需要对纯数字进行NGram[默认为false] */
	private final boolean nGramNumber;
	private CharTermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	/**位置增量属性*/
	private PositionIncrementAttribute posIncrAtt;
	private TypeAttribute typeAtt;
	private char[] curTermBuffer;
	private int curTermLength;
	private int curGramSize;
	private int tokStart;
	private Side side;
	
	public static final Side DEFAULT_SIDE = Side.FRONT;

	public PinyinEdgeNGramTokenFilter(TokenStream input) {
		this(input, Constant.DEFAULT_SIDE_FRONT,Constant.DEFAULT_MIN_GRAM, Constant.DEFAULT_MAX_GRAM,
				Constant.DEFAULT_NGRAM_CHINESE);
	}
	
	public PinyinEdgeNGramTokenFilter(TokenStream input,String side) {
		this(input, side,Constant.DEFAULT_MIN_GRAM, Constant.DEFAULT_MAX_GRAM,
				Constant.DEFAULT_NGRAM_CHINESE);
	}

	public PinyinEdgeNGramTokenFilter(TokenStream input,String side, int maxGram) {
		this(input,side,Constant.DEFAULT_MIN_GRAM, maxGram, Constant.DEFAULT_NGRAM_CHINESE);
	}

	public PinyinEdgeNGramTokenFilter(TokenStream input,String side,int minGram, int maxGram) {
		this(input,side,minGram, maxGram, Constant.DEFAULT_NGRAM_CHINESE,Constant.DEFAULT_NGRAM_NUMBER);
	}
	
	public PinyinEdgeNGramTokenFilter(TokenStream input, String side,int minGram, int maxGram,boolean nGramChinese) {
		this(input,side, minGram, maxGram, nGramChinese,Constant.DEFAULT_NGRAM_NUMBER);
	}

	public PinyinEdgeNGramTokenFilter(TokenStream input, String side,int minGram, int maxGram,
			boolean nGramChinese,boolean nGramNumber) {
		super(input);
		if (minGram < 1) {
			throw new IllegalArgumentException(
					"minGram must be greater than zero");
		}
		if (minGram > maxGram) {
			throw new IllegalArgumentException(
					"minGram must not be greater than maxGram");
		}
		if(side == null || (!side.equalsIgnoreCase(Constant.DEFAULT_SIDE_FRONT) && 
			!side.equalsIgnoreCase(Constant.DEFAULT_SIDE_BACK))) {
			throw new IllegalArgumentException(
				"side must be either front or back");
		}
		
		this.termAtt = ((CharTermAttribute) addAttribute(CharTermAttribute.class));
		this.offsetAtt = ((OffsetAttribute) addAttribute(OffsetAttribute.class));
		this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
		this.typeAtt = addAttribute(TypeAttribute.class);
		
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.nGramChinese = nGramChinese;
		this.nGramNumber = nGramNumber;
		
		this.side = Side.getSide(side);
	}

	public final boolean incrementToken() throws IOException {
		while (true) {
			if (this.curTermBuffer == null) {
				if (!this.input.incrementToken()) {
					return false;
				}
				String type = this.typeAtt.type();
				if(null != type && "normal_word".equals(type)) {
					return true;
				}
				if(null != type && "numeric_original".equals(type)) {
					return true;
				}
				if(null != type && "chinese_original".equals(type)) {
					return true;
				}
				if ((!this.nGramNumber)
						&& (StringUtils.isNumeric(this.termAtt.toString()))) {
					return true;
				}
				if ((!this.nGramChinese)
						&& (StringUtils.containsChinese(this.termAtt.toString()))) {
					return true;
				}
				this.curTermBuffer = ((char[]) this.termAtt.buffer().clone());

				this.curTermLength = this.termAtt.length();
				this.curGramSize = this.minGram;
				this.tokStart = this.offsetAtt.startOffset();
			}
			
			if (curTermLength >= minGram && this.curGramSize <= this.maxGram) {
				if (this.curGramSize >= this.curTermLength) {
					clearAttributes();
					this.offsetAtt.setOffset(this.tokStart + 0, this.tokStart
							+ this.curTermLength);
					this.termAtt.copyBuffer(this.curTermBuffer, 0,
							this.curTermLength);
					this.posIncrAtt.setPositionIncrement(0);
					this.curTermBuffer = null;
					return true;
				}
				int start = side == Side.FRONT ? 0 : curTermLength
						- curGramSize;
				int end = start + this.curGramSize;
				clearAttributes();
				this.offsetAtt.setOffset(this.tokStart + start, this.tokStart
						+ end);
				this.termAtt.copyBuffer(this.curTermBuffer, start,
						this.curGramSize);
				this.posIncrAtt.setPositionIncrement(0);
				this.curGramSize += 1;
				return true;
			}

			this.curTermBuffer = null;
		}
	}

	public void reset() throws IOException {
		super.reset();
		this.curTermBuffer = null;
	}
	
	
	public static enum Side {
		FRONT {
			@Override
			public String getLabel() {
				return "front";
			}
		},
		BACK {
			@Override
			public String getLabel() {
				return "back";
			}
		};

		public abstract String getLabel();

		public static Side getSide(String sideName) {
			if (FRONT.getLabel().equalsIgnoreCase(sideName)) {
				return FRONT;
			}
			if (BACK.getLabel().equalsIgnoreCase(sideName)) {
				return BACK;
			}
			return null;
		}
	}
}
