package com.fzshuai.job.service;

import cn.hutool.core.bean.BeanUtil;
import com.fzshuai.blog.domain.Article;
import com.fzshuai.blog.domain.vo.ArticleVO;
import com.fzshuai.blog.mapper.ArticleMapper;
import com.fzshuai.common.utils.redis.RedisUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.fzshuai.common.constant.RedisConstant.ARTICLE_VIEWS_COUNT;

/**
 * 博客浏览量
 *
 * @author fzshuai
 * @date 2023/10/12 21:19
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class BlogService {

    private final ArticleMapper baseMapper;

    /**
     * 更新文章浏览量
     */
    @XxlJob("updateBlogViewJobHandler")
    public void updateBlogViewJobHandler() throws Exception {
        log.info("开始将缓存中数据更新到数据库...");
        Collection<ScoredEntry<Object>> zSetEntryRange = RedisUtils.getZSetEntryRange(ARTICLE_VIEWS_COUNT, 0, -1);
        zSetEntryRange.forEach(item -> {
            ArticleVO articleVO = baseMapper.selectVoById((Long) item.getValue());
            articleVO.setViewCount(item.getScore().intValue());
            Article article = BeanUtil.toBean(articleVO, Article.class);
            baseMapper.updateById(article);
        });
        log.info("更新完毕");
    }
}
