package com.wildcottier.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
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
public class CreateIndex_modifyField {

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
			StringField bookIdField = new StringField("id", String.valueOf(book.getId()), Store.YES);	//不分词,索引,存储.
			TextField bookNameField = new TextField("name", book.getName(), Store.YES);	//分词,索引,存储.
			FloatField bookPriceField = new FloatField("price", book.getPrice(), Store.YES);	//分词,索引,存储.
			StoredField bookPicField = new StoredField("pic", book.getPic());	//不分词,不索引,要存储
			TextField bookDescField = new TextField("desc", book.getDescription(), Store.NO); //要分词,要索引,不存储.
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
	
	//增加索引:
	@Test
	public void testAddIndex() throws Exception {
		// 第一步：创建IndexWriter
		IndexWriter indexWriter = this.createIndexWriterIK("E:/DataSource/WorkSpace/Lucene/DB_index");
		// 创建两个文档对象
		Document doc1 = new Document();
		Document doc2 = new Document();
		// 给第一个文档对象添加域
		// id
		doc1.add(new StringField("id", "6", Store.YES));
		// 图书名称
		doc1.add(new TextField("name", "传智播客", Store.YES));
		// 图书描述
		doc1.add(new TextField("desc", "新增document2", Store.NO));
		// 给第二个文档对象添加域
		// id
		doc2.add(new StringField("id", "7", Store.YES));
		// 图书名称
		doc2.add(new TextField("name", "itcast", Store.YES));
		// 图书描述
		doc2.add(new TextField("desc", "新增document3", Store.NO));
		// 创建索引
		indexWriter.addDocument(doc1);
		indexWriter.addDocument(doc2);
		
		// 关闭IndexWriter对象
		indexWriter.close();
	}
	
	//删除索引:根据Term删除指定的文档对象.
	@Test
	public void testDeleteIndex() throws Exception {
		// 创建目录对象，指定索引路径
		Directory dir = FSDirectory.open(new File("E:/DataSource/WorkSpace/Lucene/DB_index"));
		// 创建分析器
		Analyzer analyzer = new IKAnalyzer();
		// 创建写入配置
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		// 创建写入对象
		IndexWriter indexWriter = new IndexWriter(dir, config);
		// 根据一个term对象删除索引
		indexWriter.deleteDocuments(new Term("name", "itcast"));
		
		//强制清空回收站.
		indexWriter.forceMergeDeletes();
		
		// 释放资源
		indexWriter.close();
	}
	
	//删除全部索引.(慎用)
	@Test
	public void testDeleteAll() throws Exception {
		// 创建目录对象，指定索引路径
		Directory dir = FSDirectory.open(new File("E:/DataSource/WorkSpace/Lucene/DB_index"));
		// 创建分析器
		Analyzer analyzer = new IKAnalyzer();
		// 创建写入配置
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		// 创建写入对象
		IndexWriter indexWriter = new IndexWriter(dir, config);
		
		indexWriter.deleteAll();	//删除全部索引.(慎用).
		// 释放资源
		indexWriter.close();
	}

	//更新索引:
	@Test
	public void testUpdateIndex() throws Exception {
		// 创建分析器对象
		Analyzer analyzer = new IKAnalyzer();
		// 创建目录对象,指定索引库目录
		Directory dir = FSDirectory.open(new File("E:/DataSource/WorkSpace/Lucene/DB_index"));
		// 创建写入的配置信息
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		// 创建写入对象
		IndexWriter indexWriter = new IndexWriter(dir, config);
		// 创建一个新的document对象
		Document doc = new Document();
		doc.add(new StringField("id", "8", Store.YES));
		doc.add(new TextField("name", "luceneUpdateTest", Store.YES));
		// 根据term词项更新
		indexWriter.updateDocument(new Term("name", "java"), doc);
		// 释放资源
		indexWriter.close();
	}
	
	/**
	 * 以下是查询索引:
	 * @throws IOException 
	 */
	//抽取共用的查询处理逻辑:
	private void doSearch(Query query) throws IOException{
		System.out.println("实际的查询条件: " + query);
		
		// 指定索引库的目录并返回目录对象
		Directory directory = FSDirectory.open(new File("E:/DataSource/WorkSpace/Lucene/DB_index"));
		// 根据目录对象创建出索引读取对象IndexReader
		IndexReader indexReader = DirectoryReader.open(directory);
		// 根据IndexReader创建索引搜索对象IndexSearcher
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		// 利用IndexSearcher执行查询条件,并返回查询结果对象
		// TopDocs中包含结果集和查询出结果的总件数
		TopDocs topDocs = indexSearcher.search(query, 10);
		// 打印总件数
		System.out.println("查询结果的总件数:" + topDocs.totalHits);
		
		// 从查询结果对象中取得结果集
		// scoreDocs包含文档对象的主键ID和对应文档对象的排名得分
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		// 遍历结果集打印输出结果
		for (ScoreDoc scoreDoc : scoreDocs) {
			// 根据文档主键查询出文档对象
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println("======================================");
			System.out.println("docID:" + scoreDoc.doc);
			System.out.println("bookId:" + doc.get("id"));
			System.out.println("name:" + doc.get("name"));
			System.out.println("price:" + doc.get("price"));
			System.out.println("pic:" + doc.get("pic"));
		}
		// 关闭IndexReader
		indexReader.close();
	}
	
	//全部索引查询---MatchAllDocsQuery.
	//查询索引目录下的所有文档的全部内容.
	@Test
	public void testMatchAllDocsQuery() throws IOException{
		Query query = new MatchAllDocsQuery();
		doSearch(query);
	}
	
	//精确指定索引词项查询---TermQuery
	/**
	 * TermQuery:通过Term项查询.不使用分析器,所以不进行分析,是精确匹配.大小写敏感.
	 * 所以建议匹配不分词的Field域查询,比如订单号,分类ID等.而且只接受一个Term项.
	 * @throws IOException
	 */
	@Test
	public void testByTermQuery() throws IOException{
		Query query = new TermQuery(new Term("name", "mybatis"));	//查询name域中term项为mybatis的文档.
		doSearch(query);
	}

	//数值范围查询---NumericRangeQuery
	//NumericRangeQuery指定数字范围的查询,5个参数:域名,最小值,最大值, 是否包含最小值,是否包含最大值.
	@Test
	public void testByNumericRangeQuery() throws IOException{
		Query query = NumericRangeQuery.newFloatRange("price", 55f, 66f, true, false);
		doSearch(query);
		System.out.println("   ");
		
		query = NumericRangeQuery.newFloatRange("price", 55f, 66f, false, true);
		doSearch(query);
		System.out.println("   ");
		
		query = NumericRangeQuery.newFloatRange("price", 55f, 66f, true, true);
		doSearch(query);
	}
	
	//组合条件查询:BooleanQuery
	@Test
	public void testByBooleanQuery1() throws IOException{
		Query query1 = new TermQuery(new Term("name", "apache"));
		Query query2 = NumericRangeQuery.newFloatRange("price", 55f, 66f, false, true);
		
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(query1, Occur.MUST);
		booleanQuery.add(query2, Occur.MUST);
		System.out.println("实际的查询条件: " + booleanQuery);
		doSearch(booleanQuery);
	}
	
	//组合查询2:SHOULD与SHOULD:并集
	@Test
	public void testByBooleanQuery2() throws Exception {
		Query query1 = new TermQuery(new Term("name", "lucene"));
		Query query2 = NumericRangeQuery.newFloatRange("price", 55f, 66f, true, false);
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(query1, Occur.SHOULD);  // 书名带有mybatis的应该包含进来
		booleanQuery.add(query2, Occur.SHOULD);  // 价格大于等于55，小于66范围内的书籍应该包含
		System.out.println("实际的查询条件：" + booleanQuery);

		doSearch(booleanQuery);
	}
	
	//组合查询:MUST与MUST_NOT:前者包含后者不包含.
	@Test
	public void testByBooleanQuery3() throws Exception {
		Query query1 = new TermQuery(new Term("name", "mybatis"));
		Query query2 = NumericRangeQuery.newFloatRange("price", 55f, 66f, true, false);
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(query1, Occur.MUST_NOT);  // 书名带有mybatis的不能包含进来
		booleanQuery.add(query2, Occur.MUST);  // 价格大于等于55，小于66范围内的书籍
		System.out.println("实际的查询条件：" + booleanQuery);

		doSearch(booleanQuery);
	}

	//组合查询:MUST_NOT和MUST_NOT,没有任何意义,什么都查询不到.
	@Test
	public void testByBooleanQuery4() throws Exception {
		Query query1 = new TermQuery(new Term("name", "apache"));
		Query query2 = NumericRangeQuery.newFloatRange("price", 55f, 56f, true, true);
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(query1, Occur.MUST_NOT);  // 书名带有apache的必须不包含进来
		booleanQuery.add(query2, Occur.MUST_NOT);  // 价格大于等于55，小于等于56范围内的书籍必须不包含进来
		System.out.println("实际的查询条件：" + booleanQuery);

		doSearch(booleanQuery);
	}

	//组合查询:SHOULD和MUST:SHOULD控制的条件失效不被查询考虑,只查询MUST控制的条件.
	@Test
	public void testByBooleanQuery5() throws Exception {
		Query query1 = new TermQuery(new Term("name", "lucene"));
		Query query2 = NumericRangeQuery.newFloatRange("price", 55f, 56f, true, true);
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(query1, Occur.SHOULD);  // 书名带有lucene的应该包含进来（SHOULD控制的条件失效）
		booleanQuery.add(query2, Occur.MUST);  // 价格大于等于55，小于等于56范围内的书籍必须包含进来
		System.out.println("实际的查询条件：" + booleanQuery);

		doSearch(booleanQuery);
	}

	//组合查询:SHOULD和MUST_NOT:SHOULD条件有效,在SHOULD查询结果范围内用MUST_NOT过滤.
	//因为MUST_NOT需要一个结果范围,所以SHOULD条件必须被执行,并固定了范围.然后再用MUST_NOT排除.
	@Test
	public void testByBooleanQuery6() throws Exception {
		Query query1 = new TermQuery(new Term("name", "mybatis"));
		Query query2 = NumericRangeQuery.newFloatRange("price", 55f, 66f, true, false);
		BooleanQuery booleanQuery = new BooleanQuery();
		booleanQuery.add(query1, Occur.MUST_NOT);  // 书名带有mybatis的不能包含进来
		booleanQuery.add(query2, Occur.SHOULD);  // 价格大于等于55，小于66范围内的书籍应该包含
		System.out.println("实际的查询条件：" + booleanQuery);

		doSearch(booleanQuery);
	}
	
	/**
	 * 通过QueryParser搜索查询.
	 * @throws ParseException 
	 * @throws IOException 
	 */
	//基础查询:
	@Test
	public void testByQueryParser1() throws ParseException, IOException{
		Analyzer analyzer = new StandardAnalyzer();	//创建分析器对象.
		QueryParser queryParser = new QueryParser("desc", analyzer);	//创建查询解析器.
		Query query = queryParser.parse("name:mybatis");
		
		doSearch(query); 	//进行查询.
		System.out.println("   ");
		
		query = queryParser.parse("name:Mybatis");
		doSearch(query);
	}
	
	//通过MultiFieldQueryParser多域查询解析器对多个域查询,两个域之间相当于用 '或' 连接.
	@Test
	public void testBymultiFieldQueryParser() throws ParseException, IOException{
		Analyzer analyzer = new StandardAnalyzer();
		String[] fields = {"name", "desc"};
		
		//创建多域查询解析器:
		MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
		Query query = queryParser.parse("lucene");	//解析:name:lucene OR desc:lucene
		System.out.println("query内容:============ " + query);
		doSearch(query);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
