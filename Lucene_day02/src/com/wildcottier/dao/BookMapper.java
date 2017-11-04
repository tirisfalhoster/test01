package com.wildcottier.dao;

import java.util.List;

import com.wildcottier.pojo.Book;

public interface BookMapper {
    
    public List<Book> queryBookList() throws Exception;
}