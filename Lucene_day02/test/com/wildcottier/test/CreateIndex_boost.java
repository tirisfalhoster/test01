package com.wildcottier.test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.wildcottier.dao.BookMapper;
import com.wildcottier.pojo.Book;

/**
 * 手动设置boost加权值,来影响查询的时候,索引项的位置(靠前).
 * @author Tirisfaler
 *2017年10月31日14:47:39
 */
public class CreateIndex_boost {
	private SqlSessionFactory sqlSessionFactory = null;
	
	@Before
	public void init() throws Exception {
		InputStream inputStream = Resources.getResourceAsStream("MyBatisConfig.xml");
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
	}
	
	@Test
	public void test() throws Exception {
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionFactory.openSession();
			BookMapper bookDao = sqlSession.getMapper(BookMapper.class);
			// 1. 从数据库采集数据
			List<Book> bookList = bookDao.queryBookList();
			
			// 2. 创建Document文档对象
			List<Document> documents = new ArrayList<Document>();
			for (Book book : bookList) {
				Document document = new Document();
				// Document文档中添加Field域
				// 图书id
				// 不分词、不索引、只存储
				document.add(new StoredField("id", book.getId().toString()));
				// 图书名称
				// 分词、索引、存储
				document.add(new TextField("name", book.getName().toString(), Store.YES));
				// 图书价格
				// 分词、索引、存储
				document.add(new FloatField("price", book.getPrice(), Store.YES));
				// 图书图片地址
				// 不分词、不索引、只存储
				document.add(new StoredField("pic", book.getPic().toString()));
				// 图书描述
				// 分词、索引、不存储
				TextField detailField = new TextField("desc", book.getDescription().toString(), Store.NO);
				// 判断是不是spring的那一条，如果是就增加它的加权值
				if (book.getId() == 4) {
					detailField.setBoost(100f);
				}
				document.add(detailField);
				// 把Document放到list中
				documents.add(document);
			}
			
			// 3. 创建Analyzer分词器(分析文档，对文档进行切分词)
			Analyzer analyzer = new IKAnalyzer();
			// 4. 创建索引
			// 4-1. 创建Directory对象，声明索引库位置
			Directory dir = FSDirectory.open(new File("E:/DataSource/WorkSpace/Lucene/DB_index"));
			// 4-2. 创建IndexWriteConfig对象，写入索引需要的配置
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
			// 4-3. 创建IndexWriter写入对象
			IndexWriter indexWriter = new IndexWriter(dir, config);
			// 4-4. 把Document写入到索引库，通过IndexWriter对象添加文档对象document
			for (Document doc : documents) {
				indexWriter.addDocument(doc);
			}
			// 4-5. 释放资源(释放资源同时还有commit操作)
			indexWriter.close();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			sqlSession.close();
		}
	}

}
