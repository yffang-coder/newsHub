package com.newshub.backend.application.service;

import com.newshub.backend.domain.model.Article;

public interface NewsProducer {
    void sendNews(Article article);
}

