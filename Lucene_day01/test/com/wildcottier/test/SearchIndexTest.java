package com.wildcottier.test;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

/**
 * 查询索引.
 * @author Tirisfaler
 *2017年10月30日14:58:04
 */
public class SearchIndexTest {
	
	@Test
	public void testSearchIndex() throws IOException{
		//创建Directory对象.指定索引库的目录并返回目录对象.
//		Directory dir = FSDirectory.open(Paths.get("E:/DataSource/WorkSpace/Lucene/index", new String[0]));	//新版本7.1.0的写法.
		Directory dir = FSDirectory.open(new File("E:/DataSource/WorkSpace/Lucene/index")); //换成了老版本4.10.3
		
		//根据目录对象创建出索引读取对象:IndexReader.
		IndexReader reader = DirectoryReader.open(dir);
		
		//根据IndexReader创建索引搜索对象:IndexSearch对象.
		IndexSearcher searcher = new IndexSearcher(reader);
		
		//===============Tips:手动创建查询对象时是没有指定任何分析器的,所以手动创建的查询对象没有分析语句的能力.
		
		//创建一个查询条件对象.
		TermQuery query = new TermQuery(new Term("filename", "apache"));
		//利用IndexSearcher执行查询条件,并返回查询结果对象.TopDocs中包含结果集合查询出结果的总件数.
		TopDocs topDocs = searcher.search(query, 10);
		//打印总件数.
		/**
		 * TopDocs提供了少量属性:totalHits-->匹配搜索条件的总记录数.(匹配索引库中所有记录的数量.)
		 * 					    scoreDocs-->顶部匹配记录.(相关度排名靠前的记录数组,scoreDocs的长度小于等于search()方法指定的参数n.)
		 */
		System.out.println("查询的结果数量:=============" + topDocs.totalHits);	//查询到apache出现的次数.可以在luke中看到.
		
		//从查询结果对象中取得结果集, scoreDocs包含文档对象的主键id和对应文档对象的排名得分.
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		
		//遍历结果集:
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc); //根据文档主键查询出文档对象.
			System.out.println("文件名称:=========>" + doc.get("filename"));	//这里的域是按照生成索引时候的域来查找的.别乱写.
//			System.out.println("文件内容:=========>" + doc.get("content"));
			System.out.println("文件路径:=========>" + doc.get("path"));
			System.out.println("文件大小:=========>" + doc.get("size"));
		}
		reader.close();
	}
	
	
	//查看标准分析器的分词效果:
	@Test
	public void testTokenStream() throws IOException{
		
		//创建一个标准分析器.
		Analyzer analyzer = new StandardAnalyzer();
		
		/**
		 * 获得tokenStream对象:
		 * 		第一个参数:域名.没指定.
		 * 		第二个参数:要分析的文本内容.
		 */
		TokenStream tokenStream = analyzer.tokenStream("test", "The Spring Framework provides a comprehensive programming and configuration model.");
		
		//添加一个引用,可以获得每个关键词:
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		//添加一个偏移量的引用,记录关键词的开始位置以及结束位置.
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		
		//将指针调整到列表的头部:
		tokenStream.reset();
		
		while(tokenStream.incrementToken()){
			//关键词的起始位置:
			System.out.println("start==========>" + offsetAttribute.startOffset());
			
			//取关键词:
			System.out.println(charTermAttribute);
			
			//关键词的结束位置:
			System.out.println("end============>" + offsetAttribute.endOffset());
		}
		tokenStream.close();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
