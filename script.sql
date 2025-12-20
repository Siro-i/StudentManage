/*
 Navicat MySQL Data Transfer
 Source Database       : school_db
 Target Server Type    : MySQL
 Target Server Version : 8.0+
 File Encoding         : 65001

 Date: 2025-12-17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 创建数据库 (如果不存在)
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `school_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `school_db`;

-- ----------------------------
-- 2. 表结构：user_table (主用户表)
-- ----------------------------
DROP TABLE IF EXISTS `user_table`;
CREATE TABLE `user_table`  (
                               `user_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号',
                               `user_pwd` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'MD5加密密码',
                               `user_real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
                               `user_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
                               `user_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
                               `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色: admin/teacher/student',
                               `user_createtime` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                               `user_updatetime` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
                               PRIMARY KEY (`user_id`) USING BTREE,
                               UNIQUE INDEX `uk_username`(`user_name`) USING BTREE COMMENT '账号唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 表结构：admin_table (管理员扩展表)
-- ----------------------------
DROP TABLE IF EXISTS `admin_table`;
CREATE TABLE `admin_table`  (
                                `admin_id` bigint(0) NOT NULL AUTO_INCREMENT,
                                `user_id` bigint(0) NOT NULL COMMENT '关联user_table主键',
                                `admin_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职务',
                                `admin_createtime` datetime(0) NULL DEFAULT NULL,
                                `admin_updatetime` datetime(0) NULL DEFAULT NULL,
                                PRIMARY KEY (`admin_id`) USING BTREE,
                                INDEX `idx_admin_userid`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4. 表结构：teacher_table (教师扩展表)
-- ----------------------------
DROP TABLE IF EXISTS `teacher_table`;
CREATE TABLE `teacher_table`  (
                                  `teacher_id` bigint(0) NOT NULL AUTO_INCREMENT,
                                  `user_id` bigint(0) NOT NULL COMMENT '关联user_table主键',
                                  `teacher_college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属学院',
                                  `teacher_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职称',
                                  `teacher_createtime` datetime(0) NULL DEFAULT NULL,
                                  PRIMARY KEY (`teacher_id`) USING BTREE,
                                  INDEX `idx_teacher_userid`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 表结构：student_table (学生扩展表)
-- ----------------------------
DROP TABLE IF EXISTS `student_table`;
CREATE TABLE `student_table`  (
                                  `student_id` bigint(0) NOT NULL AUTO_INCREMENT,
                                  `user_id` bigint(0) NOT NULL COMMENT '关联user_table主键',
                                  `student_college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属学院',
                                  `student_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '年级',
                                  `student_class` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班级',
                                  `student_createtime` datetime(0) NULL DEFAULT NULL,
                                  PRIMARY KEY (`student_id`) USING BTREE,
                                  INDEX `idx_student_userid`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 表结构：course_table (课程表)
-- ----------------------------
DROP TABLE IF EXISTS `course_table`;
CREATE TABLE `course_table`  (
                                 `course_id` bigint(0) NOT NULL AUTO_INCREMENT,
                                 `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
                                 `teacher_id` bigint(0) NOT NULL COMMENT '关联user_table的user_id (教师)',
                                 `course_credit` int(0) NOT NULL DEFAULT 0 COMMENT '学分',
                                 `course_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上课时间',
                                 `course_room` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上课地点',
                                 `max_num` int(0) NOT NULL DEFAULT 0 COMMENT '最大人数',
                                 `selected_num` int(0) NOT NULL DEFAULT 0 COMMENT '已选人数',
                                 `course_status` int(0) NOT NULL DEFAULT 1 COMMENT '状态: 0未发布 1已发布',
                                 `course_createtime` datetime(0) NULL DEFAULT NULL,
                                 PRIMARY KEY (`course_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7. 表结构：student_course_table (选课/成绩关联表)
-- ----------------------------
DROP TABLE IF EXISTS `student_course_table`;
CREATE TABLE `student_course_table`  (
                                         `sc_id` bigint(0) NOT NULL AUTO_INCREMENT,
                                         `student_id` bigint(0) NOT NULL COMMENT '关联student_table的student_id (注意不是user_id)',
                                         `course_id` bigint(0) NOT NULL COMMENT '关联course_table主键',
                                         `sc_score` int(0) NULL DEFAULT NULL COMMENT '成绩',
                                         `sc_selecttime` datetime(0) NULL DEFAULT NULL COMMENT '选课时间',
                                         `sc_updatetime` datetime(0) NULL DEFAULT NULL COMMENT '成绩录入时间',
                                         PRIMARY KEY (`sc_id`) USING BTREE,
                                         UNIQUE INDEX `uk_student_course`(`student_id`, `course_id`) USING BTREE COMMENT '防止重复选课'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 8. 初始化数据
-- 密码均为 123456 的MD5值: e10adc3949ba59abbe56e057f20f883e
-- ----------------------------

-- 8.1 插入用户 (Admin, Teacher, Student)
INSERT INTO `user_table` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000000', 'admin@school.com', 'admin', NOW(), NOW());
INSERT INTO `user_table` VALUES (2, 'teacher1', 'e10adc3949ba59abbe56e057f20f883e', '张教授', '13911111111', 'zhang@school.com', 'teacher', NOW(), NOW());
INSERT INTO `user_table` VALUES (3, 'student1', 'e10adc3949ba59abbe56e057f20f883e', '李同学', '13622222222', 'li@school.com', 'student', NOW(), NOW());

-- 8.2 插入扩展信息
INSERT INTO `admin_table` VALUES (1, 1, '超级管理员', NOW(), NOW());
INSERT INTO `teacher_table` VALUES (1, 2, '计算机学院', '教授', NOW());
INSERT INTO `student_table` VALUES (1, 3, '计算机学院', '2021级', '软件工程1班', NOW());

-- 8.3 插入测试课程 (由 teacher1 发布)
INSERT INTO `course_table` VALUES (1, '高等数学', 2, 4, '周一 1-2节', '一教101', 50, 0, 1, NOW());
INSERT INTO `course_table` VALUES (2, 'Java程序设计', 2, 3, '周三 3-4节', '实验楼A101', 40, 0, 1, NOW());
INSERT INTO `course_table` VALUES (3, '数据库原理', 2, 3, '周五 5-6节', '二教303', 45, 0, 1, NOW());

-- 8.4 插入选课记录 (student1 选了 Java)
INSERT INTO `student_course_table` VALUES (1, 1, 2, NULL, NOW(), NULL);

-- 更新课程已选人数
UPDATE `course_table` SET `selected_num` = 1 WHERE `course_id` = 2;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 完成
-- 默认管理员账号: admin / 123456
-- ----------------------------