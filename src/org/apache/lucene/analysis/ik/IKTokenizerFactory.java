package org.apache.lucene.analysis.ik;

import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.wltea.analyzer.lucene.IKTokenizer;

public class IKTokenizerFactory extends TokenizerFactory {
	public IKTokenizerFactory(Map<String, String> args) {
		super(args);
		useSmart = getBoolean(args, "useSmart", false);
	}
	private boolean useSmart;

	@Override
	public Tokenizer create(AttributeFactory attributeFactory) {
		Tokenizer tokenizer = new IKTokenizer(attributeFactory,useSmart);
		return tokenizer;
	}
}
