package com.xiaou.codepen.constant;

/**
 * 代码共享器常量
 * 
 * @author xiaou
 */
public class CodePenConstants {
    
    /**
     * 创建作品奖励积分
     */
    public static final int CREATE_REWARD_POINTS = 10;
    
    /**
     * 作品状态
     */
    public static class Status {
        /** 草稿 */
        public static final int DRAFT = 0;
        /** 已发布 */
        public static final int PUBLISHED = 1;
        /** 已下架 */
        public static final int OFFLINE = 2;
        /** 已删除 */
        public static final int DELETED = 3;
    }
    
    /**
     * 可见性
     */
    public static class Visibility {
        /** 私密 */
        public static final int PRIVATE = 0;
        /** 公开 */
        public static final int PUBLIC = 1;
    }
    
    /**
     * 是否免费
     */
    public static class FreeStatus {
        /** 付费 */
        public static final int PAID = 0;
        /** 免费 */
        public static final int FREE = 1;
    }
    
    /**
     * 交易类型
     */
    public static class TransactionType {
        /** 免费Fork */
        public static final int FREE_FORK = 0;
        /** 付费Fork */
        public static final int PAID_FORK = 1;
    }
    
    /**
     * 评论状态
     */
    public static class CommentStatus {
        /** 正常 */
        public static final int NORMAL = 1;
        /** 已隐藏 */
        public static final int HIDDEN = 2;
        /** 已删除 */
        public static final int DELETED = 3;
    }
    
    /**
     * Fork价格限制
     */
    public static final int MIN_FORK_PRICE = 0;
    public static final int MAX_FORK_PRICE = 1000;
    
    /**
     * 标题长度限制
     */
    public static final int MIN_TITLE_LENGTH = 1;
    public static final int MAX_TITLE_LENGTH = 100;
    
    /**
     * 描述长度限制
     */
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    
    /**
     * 评论长度限制
     */
    public static final int MIN_COMMENT_LENGTH = 1;
    public static final int MAX_COMMENT_LENGTH = 500;
    
    /**
     * 标签数量限制
     */
    public static final int MAX_TAGS_COUNT = 5;
}

