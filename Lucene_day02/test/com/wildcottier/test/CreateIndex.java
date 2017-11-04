package com.wildcottier.test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.wildcottier.dao.BookMapper;
import com.wildcottier.pojo.Book;

/**
 * 创建索引.
 * @author Tirisfaler
 *2017年10月30日22:46:38
 */
public class CreateIndex {

	//利用IKAnalyzer分析器创建IndexWriter.
	private IndexWriter createIndexWriterIK(String indexRepositoryPath) throws Exception {
		// 指定索引库存放的目录,并返回目录对象
		Directory directory = FSDirectory.open(new File(indexRepositoryPath));
		
		// 创建一个分析器
		// 分析器可以是Lucene官方提供的,也可以使用第三方的
		IKAnalyzer analyzer = new IKAnalyzer();
		
		// 创建一个索引写入配置对象IndexWriterConfig
		// 需要指定Lucene的版本和分析器对象
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		
		// 根据目录对象和配置对象创建出写入对象
		return new IndexWriter(directory, config);
	}
	
	//从数据库采集数据:
	private List<Book> getBookInfoFromDB() throws Exception {
		SqlSession sqlSession = null;
		try {
			InputStream inputStream = Resources.getResourceAsStream("MyBatisConfig.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
			sqlSession = sqlSessionFactory.openSession();
			BookMapper bookDao = sqlSession.getMapper(BookMapper.class);
			List<Book> bookList = bookDao.queryBookList();
			return bookList;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			sqlSession.close();
		}
	}
	
	//创建索引:
	@Test
	public void createIndex() throws Exception {
		
		// 创建IndexWriter对象
		IndexWriter indexWriter = this.createIndexWriterIK("E:/DataSource/WorkSpace/Lucene/DB_index");
		
		// 采集数据
		List<Book> bookList = this.getBookInfoFromDB();
		
		
		// 遍历采集到的文件列表,并对文件创建Document对象,最终创建出文件索引
		for (Book book : bookList) {
			// 取得文件的信息
			
			// 对文件信息创建一个Document对象
			TextField bookIdField = new TextField("id", String.valueOf(book.getId()), Store.YES);
			TextField bookNameField = new TextField("name", book.getName(), Store.YES);
			TextField bookPriceField = new TextField("price", String.valueOf(book.getPrice()), Store.YES);
			TextField bookPicField = new TextField("pic", book.getPic(), Store.YES);
			TextField bookDescField = new TextField("desc", book.getDescription(), Store.YES);
			Document document = new Document();
			document.add(bookIdField);
			document.add(bookNameField);
			document.add(bookPriceField);
			document.add(bookPicField);
			document.add(bookDescField);
			
			// 把Document对象写入索引库(分析,整理,归纳)
			indexWriter.addDocument(document);
		}
		
		// 关闭IndexWriter对象
		indexWriter.close();
	}
}
