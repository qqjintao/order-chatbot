package com.chatbot.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chatbot.dao.entity.KnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDoc> {

    @Select("SELECT * FROM knowledge_doc WHERE status = 1 AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) ORDER BY update_time DESC LIMIT 10")
    List<KnowledgeDoc> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 自定义分页查询（避免覆盖 MyBatis Plus 默认方法）
     */
    @Select("<script>" +
            "SELECT * FROM knowledge_doc WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "<if test='category != null and category != \"\"'> AND category_name = #{category} </if>" +
            "ORDER BY update_time DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<KnowledgeDoc> selectByPage(@Param("offset") int offset, @Param("pageSize") int pageSize, 
                                  @Param("keyword") String keyword, @Param("category") String category);

    @Select("<script>" +
            "SELECT COUNT(*) FROM knowledge_doc WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "<if test='category != null and category != \"\"'> AND category_name = #{category} </if>" +
            "</script>")
    int countByCondition(@Param("keyword") String keyword, @Param("category") String category);
}
