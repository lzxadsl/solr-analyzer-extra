package org.apache.lucene.analysis.pinyin.lucene5;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.CodepointCountFilter;
import org.apache.lucene.analysis.pinyin.utils.Constant;
import org.apache.lucene.analysis.pinyin.utils.StringUtils;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;

/**
 * 对转换后的拼音进行NGram处理的TokenFilter
 * 
 * @author Lanxiaowei
 * 
 */
@SuppressWarnings("unused")
public class PinyinNGramTokenFilter extends TokenFilter {
	private char[] curTermBuffer;
	private int curTermLength;
	private int curCodePointCount;
	private int curGramSize;
	private int curPos;
	private int curPosInc, curPosLen;
	private int tokStart;
	private int tokEnd;
	private boolean hasIllegalOffsets;

	private int minGram;
	private int maxGram;
	/** 是否需要对中文进行NGram[默认为false] */
	private final boolean nGramChinese;
	/** 是否需要对纯数字进行NGram[默认为false] */
	private final boolean nGramNumber;

	private final CharacterUtils charUtils;
	private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private PositionIncrementAttribute posIncAtt;
	private PositionLengthAttribute posLenAtt;
	private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private TypeAttribute typeAtt;

	public PinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram,
			boolean nGramChinese,boolean nGramNumber) {
		super(new CodepointCountFilter(input, minGram, Integer.MAX_VALUE));
		this.charUtils = CharacterUtils.getInstance();
		if (minGram < 1) {
			throw new IllegalArgumentException(
					"minGram must be greater than zero");
		}
		if (minGram > maxGram) {
			throw new IllegalArgumentException(
					"minGram must not be greater than maxGram");
		}
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.nGramChinese = nGramChinese;
		this.nGramNumber = nGramNumber;
		
		this.termAtt = addAttribute(CharTermAttribute.class);
		this.offsetAtt = addAttribute(OffsetAttribute.class);
		this.typeAtt = addAttribute(TypeAttribute.class);
		this.posIncAtt = addAttribute(PositionIncrementAttribute.class);
		this.posLenAtt = addAttribute(PositionLengthAttribute.class);
	}

	public PinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram,
			boolean nGramChinese) {
		this(input, minGram, maxGram, nGramChinese, Constant.DEFAULT_NGRAM_NUMBER);
	}
	
	public PinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram) {
		this(input, minGram, maxGram, Constant.DEFAULT_NGRAM_CHINESE);
	}
	
	public PinyinNGramTokenFilter(TokenStream input, int minGram) {
		this(input, minGram, Constant.DEFAULT_MAX_GRAM);
	}
	
	public PinyinNGramTokenFilter(TokenStream input) {
		this(input, Constant.DEFAULT_MIN_GRAM);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		while (true) {
			if (curTermBuffer == null) {
				if (!input.incrementToken()) {
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
				curTermBuffer = termAtt.buffer().clone();
				curTermLength = termAtt.length();
				curCodePointCount = charUtils.codePointCount(termAtt);
				curGramSize = minGram;
				curPos = 0;
				curPosInc = posIncAtt.getPositionIncrement();
				curPosLen = posLenAtt.getPositionLength();
				tokStart = offsetAtt.startOffset();
				tokEnd = offsetAtt.endOffset();

				hasIllegalOffsets = (tokStart + curTermLength) != tokEnd;
			}

			if (curGramSize > maxGram
					|| (curPos + curGramSize) > curCodePointCount) {
				++curPos;
				curGramSize = minGram;
			}
			if ((curPos + curGramSize) <= curCodePointCount) {
				clearAttributes();
				final int start = charUtils.offsetByCodePoints(curTermBuffer,
						0, curTermLength, 0, curPos);
				final int end = charUtils.offsetByCodePoints(curTermBuffer, 0,
						curTermLength, start, curGramSize);
				termAtt.copyBuffer(curTermBuffer, start, end - start);
				posIncAtt.setPositionIncrement(curPosInc);
				curPosInc = 0;
				posLenAtt.setPositionLength(curPosLen);
				offsetAtt.setOffset(tokStart, tokEnd);
				curGramSize++;
				return true;
			}
			curTermBuffer = null;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		curTermBuffer = null;
	}
}
