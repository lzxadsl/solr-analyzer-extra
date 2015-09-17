package org.apache.lucene.analysis.ansj;

import java.util.Map;

import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.nlpcn.commons.lang.tire.domain.Forest;

public class AnsjTokenizerFactory  extends TokenizerFactory {
	/**是否查询分词*/
	private boolean query;
	/**是否分析词干.进行单复数,时态的转换(只针对英文单词)*/
	private boolean pstemming;
	/**自定义停用词词典文件路径*/
	private String stopwordsDir;
		
	public AnsjTokenizerFactory(Map<String, String> args) {
		super(args);
		query = getBoolean(args, "query", false);
		pstemming = getBoolean(args, "pstemming", false);
		stopwordsDir = get(args, "stopwordsDir", "");
	}
	
	@Override
	public Tokenizer create(AttributeFactory factory) {
		if(query) {
			return new AnsjTokenizer(factory,new ToAnalysis(new Forest[0]),stopwordsDir,pstemming);
		}
		return new AnsjTokenizer(factory,new IndexAnalysis(new Forest[0]),stopwordsDir,pstemming);
	}
}
