package com.wildcottier.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 创建索引.
 * @author Tirisfaler
 *2017年10月30日13:09:55
 */
public class CreateIndexTest {
	
	//创建IndexWriter(创建索引准备工作).
	
	
	//利用IK中文分析器创建IndexWriter.
	private IndexWriter createIndexWriter_IK(String indexRepositoryPath) throws IOException{
		//创建Directory对象.指定索引库存放的目录.并返回目录对象.
//		Directory dir = FSDirectory.open(Paths.get(indexRepositoryPath, new String[0]));	//创建方式跟老版本的不同.
		FSDirectory dir = FSDirectory.open(new File(indexRepositoryPath)); //换成了老版本4.10.3
		
		//创建一个标准分析器.分析器可以是Lucene官方提供的,也可以使用第三方的.
		//这里使用IKAnalyzer中文分析器.
		IKAnalyzer analyzer = new IKAnalyzer();
		
		//创建IndexWriterConfig对象:参数为分析器对象. (新版本的Lucene跟老版本的lucene构造参数是不同的.)
//		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer); //老版本4.10.3
		
		//创建IndexWriter对象.(根据目录对象和配置对象创建出写入对象IndexWriter)
		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}
	
	//创建索引1:在创建IndexWriter中使用的是StandardAnalyzer标准分析器.
	@Test
	public void testCreateIndex() throws IOException{
		
		//创建IndexWriter(创建索引的准备工作).指定索引存放的位置.
		IndexWriter indexWriter = createIndexWriter("E:/DataSource/WorkSpace/Lucene/index");
		
		//开始创建索引:
		//采集原始数据.(从指定的目录下取得文件对象列表集合.)
		//遍历采集到的文件列表,并对文件创建Document对象,最终创建出文件索引.
		File dirSource = new File("E:/SomeOthers/testTempDoc/Lucene_searchsource");
		for(File f : dirSource.listFiles()){
			
			//文件名.
			String fileName = f.getName();
			//文件内容.
			String fileContent = FileUtils.readFileToString(f, "utf-8");
			//文件路径:
			String filePath = f.getPath();
			//文件大小.
			long fileSize = FileUtils.sizeOf(f);
			
			//创建文件名域:参数1:域名称,参数2:域内容,参数3:是否存储.
			TextField fileNameField = new TextField("filename", fileName, Store.YES);
			//创建内容域:
			TextField fileContentField = new TextField("content", fileContent, Store.YES);
			//创建文件路径域:
			TextField filePathField = new TextField("path", filePath, Store.YES);
			//创建文件大小域:
			TextField fileSizeField = new TextField("size", String.valueOf(fileSize), Store.YES);
			
			//创建document对象:
			Document document = new Document();
			document.add(fileNameField);
			document.add(fileContentField);
			document.add(filePathField);
			document.add(fileSizeField);
			
			//创建索引:(用indexWriter对象.)
			indexWriter.addDocument(document);
		}
		
		//关闭indexWriter对象.
		indexWriter.close();
		
	}
	
	//创建索引2:在创建IndexWriter中使用的是IKAnalyzer分析器.
	@Test
	public void testCreateIndex_IK() throws IOException{
		
		//创建IndexWriter(创建索引的准备工作).指定索引存放的位置.
		IndexWriter indexWriter = createIndexWriter_IK("E:/DataSource/WorkSpace/Lucene/IK_index");
		
		//开始创建索引:
		//采集原始数据.(从指定的目录下取得文件对象列表集合.)
		//遍历采集到的文件列表,并对文件创建Document对象,最终创建出文件索引.
		File dirSource = new File("E:/SomeOthers/testTempDoc/Lucene_searchsource");
		for(File f : dirSource.listFiles()){
			
			//文件名.
			String fileName = f.getName();
			//文件内容.
			String fileContent = FileUtils.readFileToString(f, "utf-8");
			//文件路径:
			String filePath = f.getPath();
			//文件大小.
			long fileSize = FileUtils.sizeOf(f);
			
			//创建文件名域:参数1:域名称,参数2:域内容,参数3:是否存储.
			TextField fileNameField = new TextField("filename", fileName, Store.YES);
			//创建内容域:
			TextField fileContentField = new TextField("content", fileContent, Store.YES);
			//创建文件路径域:
			TextField filePathField = new TextField("path", filePath, Store.YES);
			//创建文件大小域:
			TextField fileSizeField = new TextField("size", String.valueOf(fileSize), Store.YES);
			
			//创建document对象:
			Document document = new Document();
			document.add(fileNameField);
			document.add(fileContentField);
			document.add(filePathField);
			document.add(fileSizeField);
			
			//创建索引:(用indexWriter对象.)
			indexWriter.addDocument(document);
		}
		
		//关闭indexWriter对象.
		indexWriter.close();
		
	}

}
